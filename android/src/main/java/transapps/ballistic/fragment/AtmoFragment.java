package transapps.ballistic.fragment;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import transapps.ballistic.BallisticDisplayActivity;
import transapps.ballistic.R;
import transapps.ballistic.db.dvo.AtmosphereDVO;
import transapps.ballistic.lib.data.Atmosphere;
import transapps.ballistic.lib.util.Conversions;
import transapps.ballistic.sensor.AtmoSensors;

import java.text.DecimalFormat;

public class AtmoFragment extends BaseFragment implements FragmentUpdate,
		AtmoSensors.Update {
	private static final String[] alt_unit = {"Ft", "M"};
	private static final String[] temp_unit = {"F", "C"};
	private static final String[] barometer_unit = {"in/hg", "mm/hg"};
	private static final String[] density_unit = {"lb/ft3", "kg/m3"};
	public final DecimalFormat df2 = new DecimalFormat("0.00");
	public final DecimalFormat df5 = new DecimalFormat("0.00000");
	private Spinner altUnitSpinner;
	private Spinner tempUnitSpinner;
	private Spinner barometerUnitSpinner;
	private Spinner densityUnitSpinner;
	private Spinner machUnitSpinner;
	private CheckBox densityAltCheck;
	private CheckBox atmoCheck;
	private CheckBox sensors;
	private int altSel = 0;
	private int tempSel = 0;
	private int barSel = 0;
	private int denSel = 0;
	private int machSel = 0;
	private View v;
	private boolean enabled = true;
	private boolean sensorsOn = false;
	private final AtmoSensors atmoSensors = AtmoSensors.i();

	public interface Callback {
		public void setAtmo(boolean atmoOn, Atmosphere atmo);
	}

	public static AtmoFragment newInstance(boolean atmoOn, AtmosphereDVO atmo) {
		AtmoFragment f = new AtmoFragment();
		Bundle args = new Bundle();
		args.putBoolean("atmo_on", atmoOn);
		args.putParcelable("atmo", atmo.toContentValues());
		f.setArguments(args);
		return f;
	}

	protected int getLayout() {
		return R.layout.atmo_dialog;
	}

	private void setForAlt() {
		double temp = Atmosphere.DEFAULT_TEMPERATURE;
		double humidity = Atmosphere.DEFAULT_HUMIDITY;
		double alt = getVal(R.id.altitude_value, "Density Altitude");
		if (!isAltImperial()) {
			alt = (float)Conversions.metersToFeet(alt);
		}
		double barometer = Atmosphere.standardPressureForAlt(alt);
		if (!isInHg()) {
			barometer = Conversions.inhgTommhg(barometer);
		}
		EditText t = (EditText)v.findViewById(R.id.barometer_value);
		t.setText(df2.format(barometer));
		t = (EditText)v.findViewById(R.id.temp_value);
		if (!isFahrenheit()) {
			temp = Conversions.fahrenheitToCelsius(temp);
		}
		t.setText(df2.format(temp));
		t = (EditText)v.findViewById(R.id.humidity_value);
		t.setText(df2.format(humidity));
	}

	private TextWatcher watch = new TextWatcher() {
		boolean running = false;
		@Override
		public void afterTextChanged(Editable s) { }

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) { }

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			if (running) return;
			running = true;
			if (densityAltCheck.isChecked()) {
				setForAlt();
			}
			Atmosphere a = getAtmosphere();
			double density = a.density;
			if (denSel == 1){// if 1 convert to kg/m3
				density = Conversions.lbft3Tokgm3(a.density);
			}
			((TextView)v.findViewById(R.id.density_value)).setText(df5.format(density));
			double mach = a.mach;
			if (machSel == 1){// if 1 convert to Meters
				mach = Conversions.feetToMeters(a.mach);
			}
			((TextView)v.findViewById(R.id.mach_value)).setText(df2.format(mach));
			running = false;
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		SharedPreferences pref = getActivity().getSharedPreferences(
				"transapps.ballistic.Atmo.Settings",
				Context.MODE_PRIVATE);
		altSel = pref.getInt("alt_sel", altSel);
		tempSel = pref.getInt("temp_sel", tempSel);
		barSel = pref.getInt("bar_sel", barSel);
		denSel = pref.getInt("density_sel", denSel);
		machSel = pref.getInt("mach_sel", machSel);

		v = inflater.inflate(getLayout(), container, false);
		getActivity().setTitle("Set Atmospheric Conditions");

		altUnitSpinner = (Spinner)v.findViewById(R.id.altitude_unit_value);
		ArrayAdapter<String> aadp = new ArrayAdapter<String>(getActivity(), 
				android.R.layout.simple_list_item_1, alt_unit);
		altUnitSpinner.setAdapter(aadp);
		aadp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		tempUnitSpinner = (Spinner)v.findViewById(R.id.temp_unit_value);
		aadp = new ArrayAdapter<String>(getActivity(), 
				android.R.layout.simple_list_item_1, temp_unit);
		tempUnitSpinner.setAdapter(aadp);
		aadp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		barometerUnitSpinner = (Spinner)v.findViewById(R.id.barometer_unit_value);
		aadp = new ArrayAdapter<String>(getActivity(), 
				android.R.layout.simple_list_item_1, barometer_unit);
		barometerUnitSpinner.setAdapter(aadp);
		aadp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		densityUnitSpinner = (Spinner)v.findViewById(R.id.density_unit_value);
		aadp = new ArrayAdapter<String>(getActivity(), 
				android.R.layout.simple_list_item_1, density_unit);
		densityUnitSpinner.setAdapter(aadp);
		aadp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		machUnitSpinner = (Spinner)v.findViewById(R.id.mach_unit_value);
		aadp = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, alt_unit);
		machUnitSpinner.setAdapter(aadp);
		aadp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		final Button atmoBut = (Button)v.findViewById(R.id.atmo_on_button);
		atmoBut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String text = atmoBut.getText().toString();
				if (text.equals("Off")) {
					atmoBut.setText("On");
					enableInput();
				} else {
					atmoBut.setText("Off");
					disabledInput();
				}
			}
		});
		Button reset = (Button)v.findViewById(R.id.standard_atmo_button);
		reset.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setValues(true, new Atmosphere("",
						Atmosphere.DEFAULT_BAROMETER, Atmosphere.DEFAULT_TEMPERATURE,
						Atmosphere.DEFAULT_HUMIDITY));
				sensors.setChecked(false);
				disableSensors();
				atmoCheck.setChecked(true);
				enableInput();
			}
		});  

		altUnitSpinner.setSelection(altSel);
		tempUnitSpinner.setSelection(tempSel);
		barometerUnitSpinner.setSelection(barSel);
		densityUnitSpinner.setSelection(denSel);
		machUnitSpinner.setSelection(machSel);

		tempUnitSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){ 
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if (tempUnitSpinner.getSelectedItemPosition() == tempSel) return;
				tempSel = tempUnitSpinner.getSelectedItemPosition();
				EditText t = (EditText)v.findViewById(R.id.temp_value);
				double pro = Double.parseDouble(t.getText().toString());

				if (!isFahrenheit()){// if 1 convert to celsius
					pro = Conversions.fahrenheitToCelsius(pro);
					t.setText(""+df2.format(pro));
				}else{
					pro = Conversions.celsiusToFahrenheit(pro);
					t.setText(""+df2.format(pro));
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		altUnitSpinner.setSelected(false);
		altUnitSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if (altUnitSpinner.getSelectedItemPosition() == altSel) return;
				altSel = altUnitSpinner.getSelectedItemPosition();
				EditText t = (EditText)v.findViewById(R.id.altitude_value);
				double pro = Double.parseDouble(t.getText().toString());

				if (!isAltImperial()){// if 1 convert to Meters
					pro = Conversions.feetToMeters(pro);
					t.setText(""+df2.format(pro));
				}else{
					pro = Conversions.metersToFeet(pro);
					t.setText(""+df2.format(pro));
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		barometerUnitSpinner.setSelected(false);
		barometerUnitSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){ 
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if (barometerUnitSpinner.getSelectedItemPosition() == barSel) return;
				barSel = barometerUnitSpinner.getSelectedItemPosition();
				EditText t = (EditText)v.findViewById(R.id.barometer_value);
				double pro = Double.parseDouble(t.getText().toString());

				if (!isInHg()){// if 1 convert to mmhg
					pro = Conversions.inhgTommhg(pro);
					t.setText(""+df2.format(pro));

				}else{
					pro = Conversions.mmhgToinhg(pro);
					t.setText(""+df2.format(pro));
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		densityUnitSpinner.setSelected(false);
		densityUnitSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){ 
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if (densityUnitSpinner.getSelectedItemPosition() == denSel) return;
				denSel = densityUnitSpinner.getSelectedItemPosition();
				TextView t = (TextView)v.findViewById(R.id.density_value);
				if (denSel == 1){// if 1 convert to kg/m3
					double pro = Conversions.lbft3Tokgm3(getAtmosphere().density);
					t.setText(""+df5.format(pro));
				}else{
					t.setText(""+df5.format(getAtmosphere().density));
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		machUnitSpinner.setSelected(false);
		machUnitSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
									   int arg2, long arg3) {
				if (machUnitSpinner.getSelectedItemPosition() == machSel) return;
				machSel = machUnitSpinner.getSelectedItemPosition();
				TextView t = (TextView)v.findViewById(R.id.mach_value);
				if (machSel == 1){// if 1 convert to Meters
					double pro = Conversions.feetToMeters(getAtmosphere().mach);
					t.setText(""+df2.format(pro));
				}else{
					t.setText(""+df2.format(getAtmosphere().mach));
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		densityAltCheck = (CheckBox)v.findViewById(R.id.density_altitude_check);
		atmoCheck = (CheckBox)v.findViewById(R.id.atmospherics_check);
		densityAltCheck.setChecked(false);
		atmoCheck.setChecked(true);
		boolean atmoOn = getArguments().getBoolean("atmo_on");
		ContentValues cv = getArguments().getParcelable("atmo");
		Atmosphere atmo = cv==null?null:AtmosphereDVO.create(cv);
		setValues(atmoOn, atmo);
		if (savedInstanceState != null) {
			String atmoStr = savedInstanceState.getString("ATMO_ON");
			if (atmoStr == null) atmoStr = "Off";
			((Button)v.findViewById(R.id.atmo_on_button)).setText(atmoStr);
		}
		densityAltCheck.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (densityAltCheck.isChecked()) {
					atmoCheck.setChecked(false);
					setForAlt();
					enableInput();
				}
			}
		});
		atmoCheck.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (atmoCheck.isChecked()) {
					densityAltCheck.setChecked(false);
					EditText t = (EditText)v.findViewById(R.id.altitude_value);
					t.setText("0");
					enableInput();
				}
			}
		});
		sensors = (CheckBox)v.findViewById(R.id.sensors_button);
		sensors.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (sensors.isChecked()) {
					enableSensors();
				} else {
					disableSensors();
				}
			}
		});
		disableSensors();
		if (atmoSensors.hasHumidity() || atmoSensors.hasTemp() || atmoSensors.hasPressure()) {
			v.findViewById(R.id.sensors_button).setVisibility(enabled?View.VISIBLE:View.GONE);
			sensors.setText("Sensors ("+(atmoSensors.hasTemp()?" Temp":"")+
					(atmoSensors.hasPressure()?" Barometer":"")+
					(atmoSensors.hasHumidity()?" Humidity":"")+" )");
		} else {
			v.findViewById(R.id.sensors_button).setVisibility(View.GONE);
		}

		String atmoStr = ((Button)v.findViewById(R.id.atmo_on_button)).getText().toString();
		if (atmoStr.equals("Off")) {
			disabledInput();
		} else {
			enableInput();
		}

		((EditText)v.findViewById(R.id.altitude_value)).addTextChangedListener(watch);
		((EditText)v.findViewById(R.id.barometer_value)).addTextChangedListener(watch);
		((EditText)v.findViewById(R.id.temp_value)).addTextChangedListener(watch);
		((EditText)v.findViewById(R.id.humidity_value)).addTextChangedListener(watch);

		return v;
	}

	@Override
	public void onSaveInstanceState (Bundle outState) {
		super.onSaveInstanceState(outState);
		String atmoStr = ((Button)v.findViewById(R.id.atmo_on_button)).getText().toString();
		outState.putString("ATMO_ON", atmoStr);
	}

	@Override
	public void onPause() {
		super.onPause();
		atmoSensors.pause();
		SharedPreferences pref = getActivity().getSharedPreferences(
				"transapps.ballistic.Atmo.Settings",
				Context.MODE_PRIVATE);
		SharedPreferences.Editor e = pref.edit();
		e.putInt("alt_sel", altSel);
		e.putInt("temp_sel", tempSel);
		e.putInt("bar_sel", barSel);
		e.putInt("density_sel", denSel);
		e.putInt("mach_sel", machSel);
		e.commit();
	}

	@Override
	public void onResume() {
		super.onResume();
		atmoSensors.resume(this);
	}

	private float getVal(int field, String name) {
		float ret = 0;
		try {
			String val = ((EditText)v.findViewById(field)).getText().toString();
			if (val.trim().length() > 0)  // If String is empty then go with 0...
				ret = Float.parseFloat(val);
		} catch (NumberFormatException ex) {
			Toast.makeText(getActivity(), "Invalid "+name+"!", Toast.LENGTH_LONG).show();
		}
		return ret;
	}

	private void disableSensors() {
		sensorsOn = false;
		if (atmoSensors.hasHumidity()) {
			v.findViewById(R.id.humidity_value).setEnabled(enabled);
		}
		if (atmoSensors.hasTemp()) {
			v.findViewById(R.id.temp_value).setEnabled(enabled);
		}
		if (atmoSensors.hasPressure()) {
			v.findViewById(R.id.barometer_value).setEnabled(enabled);
		}
	}

	private void enableSensors() {
		sensorsOn = true;
		if (atmoSensors.hasHumidity()) {
			v.findViewById(R.id.humidity_value).setEnabled(false);
		}
		if (atmoSensors.hasTemp()) {
			v.findViewById(R.id.temp_value).setEnabled(false);
		}
		if (atmoSensors.hasPressure()) {
			v.findViewById(R.id.barometer_value).setEnabled(false);
		}
	}

	private void disabledInput() {
		enabled = false;
		v.findViewById(R.id.altitude_value).setEnabled(false);
		v.findViewById(R.id.barometer_value).setEnabled(false);
		v.findViewById(R.id.temp_value).setEnabled(false);
		v.findViewById(R.id.humidity_value).setEnabled(false);
		v.findViewById(R.id.sensors_button).setVisibility(View.GONE);
		atmoSensors.pause();
		densityAltCheck.setEnabled(false);
		atmoCheck.setEnabled(false);
	}

	private void enableInput() {
		enabled = true;
		v.findViewById(R.id.altitude_value).setEnabled(densityAltCheck.isChecked());
		v.findViewById(R.id.barometer_value).setEnabled(atmoCheck.isChecked());
		v.findViewById(R.id.temp_value).setEnabled(atmoCheck.isChecked());
		v.findViewById(R.id.humidity_value).setEnabled(atmoCheck.isChecked());
		if (atmoCheck.isChecked()) {
			if (atmoSensors.hasHumidity() || atmoSensors.hasTemp() || atmoSensors.hasPressure()) {
				v.findViewById(R.id.sensors_button).setVisibility(View.VISIBLE);
				if (sensorsOn) enableSensors();
				atmoSensors.resume(this);
			} else {
				v.findViewById(R.id.sensors_button).setVisibility(View.GONE);
			}
		} else {
			v.findViewById(R.id.sensors_button).setVisibility(View.GONE);
			atmoSensors.pause();
		}
		densityAltCheck.setEnabled(true);
		atmoCheck.setEnabled(true);
	}

	// Values MUST be in feet, in-hg and F.  Call setUnitValues first!
	public void setValues(boolean atmoOn, Atmosphere a) {
		if (a == null) a = Atmosphere.ICAO_STANDARD;
		if (a.altitude > 0) {
			densityAltCheck.setChecked(true);
			atmoCheck.setChecked(false);
		} else {
			densityAltCheck.setChecked(false);
			atmoCheck.setChecked(true);
		}
		double altitude = a.altitude;
		double barometer = a.pressure;
		double temp = a.temperature;
		double humidity = a.humidity;
		if (atmoOn) {
			((Button)v.findViewById(R.id.atmo_on_button)).setText("On");
			enableInput();
		} else {
			((Button)v.findViewById(R.id.atmo_on_button)).setText("Off");
			disabledInput();
		}
		EditText t = (EditText)v.findViewById(R.id.altitude_value);
		if (!isAltImperial()) {
			altitude = Conversions.feetToMeters(altitude);
		}
		t.setText(df2.format(altitude));
		t = (EditText)v.findViewById(R.id.barometer_value);
		if (!isInHg()) {
			barometer = Conversions.inhgTommhg(barometer);
		}
		t.setText(df2.format(barometer));
		t = (EditText)v.findViewById(R.id.temp_value);
		if (!isFahrenheit()) {
			temp = Conversions.fahrenheitToCelsius(temp);
		}
		t.setText(df2.format(temp));
		t = (EditText)v.findViewById(R.id.humidity_value);
		t.setText(df2.format(humidity));

		double density = a.density;
		if (denSel == 1) {
			density = Conversions.lbft3Tokgm3(density);
		}
		((TextView)v.findViewById(R.id.density_value)).setText(df5.format(density));
		double mach = a.mach;
		if (machSel == 1){// if 1 convert to Meters
			mach = Conversions.feetToMeters(a.mach);
		}
		((TextView)v.findViewById(R.id.mach_value)).setText(df2.format(mach));
	}

	protected Atmosphere getAtmosphere() {
		Atmosphere ret;
		if (densityAltCheck.isChecked()) {
			double alt = getVal(R.id.altitude_value, "Density Altitude");
			if (!isAltImperial()) {
				alt = (float)Conversions.metersToFeet(alt);
			}
			ret = new Atmosphere("", alt);
		} else {
			double pressure = getVal(R.id.barometer_value, "Barometer");
			if (!isInHg()) {
				pressure = (float)Conversions.mmhgToinhg(pressure);
			}
			double temp = getVal(R.id.temp_value, "Temperature");
			if (!isFahrenheit()) {
				temp = (float)Conversions.celsiusToFahrenheit(temp);
			}
			double humidity = getVal(R.id.humidity_value, "Humidity");
			ret = new Atmosphere("", pressure, temp, humidity);
		}
		return ret;
	}

	protected boolean isAtmoOn() {
		return ((Button)v.findViewById(R.id.atmo_on_button)).getText().toString().equals("On");
	}

	private boolean isAltImperial() {
		return altUnitSpinner.getSelectedItemPosition()==0;
	}

	private boolean isFahrenheit() {
		return tempUnitSpinner.getSelectedItemPosition()==0;
	}

	private boolean isInHg() {
		return barometerUnitSpinner.getSelectedItemPosition()==0;
	}

	@Override
	public void update() {
		Callback c = BallisticDisplayActivity.i();
		c.setAtmo(isAtmoOn(), getAtmosphere());
	}

	@Override
	public void temp(float temp) {
		if (sensorsOn) {
			EditText t = ((EditText)v.findViewById(R.id.temp_value));
			if (!isFahrenheit()){
				t.setText(""+df2.format(temp));
			}else{
				temp = (float) Conversions.celsiusToFahrenheit(temp);
				t.setText(""+df2.format(temp));
			}

		}
	}

	@Override
	public void pressure(float pressure) {
		if (sensorsOn) {
			EditText t = ((EditText)v.findViewById(R.id.barometer_value));
			if (!isInHg()){// if 1 convert to mmhg
				pressure = (float) Conversions.inhgTommhg(pressure);
				t.setText(""+df2.format(pressure));

			}else{
				t.setText(""+df2.format(pressure));
			}
		}
	}

	@Override
	public void humidity(float humidity) {
		if (sensorsOn) ((EditText)v.findViewById(R.id.humidity_value)).setText(df2.format(humidity));
	}
}
