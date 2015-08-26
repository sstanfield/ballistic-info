package transapps.ballistic.fragment;

import transapps.ballistic.BallisticSettings;
import transapps.ballistic.R;
import transapps.ballistic.lib.util.Conversions;
import transapps.ballistic.widgets.EditNumberSliders;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Dialog to get wind speed and direction.
 * Date: 5/7/11
 * Time: 10:14 PM
 */
public class WindFragment extends BaseFragment implements FragmentUpdate {
	private double speed;
	private int speedLarge;
	private int speedSmall;
	private EditNumberSliders angleConfig;
	private int angleLarge;
	private int angleSmall;
	private double angle;
	private Spinner windSpeedUnitSpinner;
	private Spinner windAngleUnitSpinner;
	private static final String[] WINDSPEED_UNITS = {"mph", "kph"};
	public static final int CLOCK_ANGLE = 0;
	public static final int DEGREE_ANGLE = 1;
	public static final int MILLS6400_ANGLE = 2;
	private static final String[] WINDANGLE_UNITS = {"Clock", "Degrees", "Mils (6400)"};
	private View v;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.wind_dialog, container, false);
		getActivity().setTitle("Select Wind Settings");

		windSpeedUnitSpinner = (Spinner)v.findViewById(R.id.windspeed_unit_value);
		ArrayAdapter<String> aadp = new ArrayAdapter<String>(getActivity(), 
				android.R.layout.simple_list_item_1, WINDSPEED_UNITS);
		windSpeedUnitSpinner.setAdapter(aadp);
		aadp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		windAngleUnitSpinner = (Spinner)v.findViewById(R.id.windangle_unit_value);
		aadp = new ArrayAdapter<String>(getActivity(), 
				android.R.layout.simple_list_item_1, WINDANGLE_UNITS);
		windAngleUnitSpinner.setAdapter(aadp);
		aadp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		windAngleUnitSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				setAngleUnit(position);
			}
			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
			}
		});

		final EditNumberSliders speedConfig = (EditNumberSliders)v.findViewById(R.id.wind_speed_config);
		speedConfig.setLabels("Speed:", "");
		speedConfig.setMaxPositions(90, 9);
		speedConfig.setCallbacks(new EditNumberSliders.EditNumberClass() {
			@Override
			public int getLargeScale() {
				return 1;
			}
			@Override
			public String update(int large, int small) {
				speedLarge = large;
				speedSmall = small;
				speed = (double)large + ((double)small / 10);
				return ""+speed;
			}
			@Override
			public int getLarge(String val) {
				double v = Double.parseDouble(val);
				return (int)v;  // Drop decimals...
			}
			@Override
			public int getSmall(String val) {
				double v = Double.parseDouble(val);
				return (int)(v * 10.0) % 10;
			}
			@Override
			public boolean supportDecimal() {
				return true;
			}
			@Override
			public boolean isValid(String val) {
				double v = Double.parseDouble(val);
				if (val.contains(".")) {
					int x = val.indexOf('.');
					// Only allow one decimal place.
					if (val.substring(x, val.length()).length() > 2) return false;
				}
				return (v >= 0 && v <= 100);
			}
		});

		angleConfig = (EditNumberSliders)v.findViewById(R.id.wind_angle_config);
		angleConfig.setLabels("Direction:", "");
		angleConfig.setMaxPositions(11, 59);
		angleConfig.setCallbacks(new EditNumberSliders.EditNumberClass() {
			@Override
			public String update(int large, int small) {
				String ret = "";
				angleLarge = large;
				angleSmall = small;
				if (getAngleUnit() == DEGREE_ANGLE) {
					angle = (large * 10) + small;
					ret = ""+((large * 10) + small);
				} else if (getAngleUnit() == MILLS6400_ANGLE) {
					angle = ((double)((large * 100) + small) / 6400.0) * 360.0;
					ret = ""+((large * 100) + small);
				} else if (getAngleUnit() == CLOCK_ANGLE) {
					angle = (double)((large==11?0:(large+1)) * 30) + ((double)small / 2.0);
					ret = Conversions.angleToClock(angle);
				}
				return ret;
			}
			@Override
			public boolean supportNumPad() {
				return false;
			}
		});

		BallisticSettings s = BallisticSettings.i();
		boolean mph = s.mph;
		windSpeedUnitSpinner.setSelection(mph?0:1);
		speed = s.windSpeed;
		setSpeed(speed);
		angle = s.windDirection;
		int angleUnit = s.windAngleUnit;
		setAngleUnit(angleUnit);
		if (savedInstanceState != null) {
			angle = savedInstanceState.getDouble("_ANGLE");
			angleLarge = savedInstanceState.getInt("_ANGLE_LARGE");
			angleSmall = savedInstanceState.getInt("_ANGLE_SMALL");
			angleConfig.setPositions(angleLarge, angleSmall);
			angleConfig.setValue(Conversions.angleToClock(angle));
			speed = savedInstanceState.getDouble("_SPEED");
			speedLarge = savedInstanceState.getInt("_SPEED_LARGE");
			speedSmall = savedInstanceState.getInt("_SPEED_SMALL");
			speedConfig.setPositions(speedLarge, speedSmall);
			speedConfig.setValue(""+speed);
		}

		return v;
	}

	@Override
	public void onSaveInstanceState (Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putDouble("_ANGLE", angle);
		outState.putInt("_ANGLE_LARGE", angleLarge);
		outState.putInt("_ANGLE_SMALL", angleSmall);
		outState.putDouble("_SPEED", speed);
		outState.putInt("_SPEED_LARGE", speedLarge);
		outState.putInt("_SPEED_SMALL", speedSmall);
	}

	private double getSpeed() {
		double s = speed;
		if (!isMph()) s = Conversions.kmToMile(speed);
		return s;
	}

	private void setSpeed(double speed) {
		if (!isMph()) {
			speed = Conversions.milesToKm(speed);
		}
		// Round speed to one decimal place.
		speed = (int)((speed + .05) * 10);
		speed = speed / 10;
		speedLarge = (int)speed;
		speedSmall = ((int)(speed * 10) % 10);
		this.speed = speed;
		EditNumberSliders ens = (EditNumberSliders)v.findViewById(R.id.wind_speed_config);
		ens.setPositions(speedLarge, speedSmall);
		ens.setValue(""+speed);
	}

	private double getAngle() {
		return angle;
	}

	private boolean isMph() {
		return windSpeedUnitSpinner.getSelectedItemPosition()==0;
	}

	private void setAngleUnit(int unit) {
		EditNumberSliders ens = (EditNumberSliders)v.findViewById(R.id.wind_angle_config);
		TextView help = (TextView)v.findViewById(R.id.wind_angle_help);
		if (unit == DEGREE_ANGLE) {
			angleConfig.setMaxPositions(35, 9);
			angleLarge = (int)angle / 10;
			angleSmall = (int)(angle - (angleLarge * 10));
			ens.setPositions(angleLarge, angleSmall);
			ens.setValue(""+(int)(angle+.5));
			help.setText("0/360 headwind, 90 right to left cross wind, 180 tailwind, etc");
		} else if (unit == MILLS6400_ANGLE) {
			angleConfig.setMaxPositions(63, 99);
			double mils = (angle  / 360.0) * 6400.0;
			angleLarge = (int)mils / 100;
			angleSmall = (int)(mils - (angleLarge * 100));
			ens.setPositions(angleLarge, angleSmall);
			ens.setValue(""+((angleLarge * 100) + angleSmall));
			help.setText("0/6400 headwind, 1600 right to left cross wind, 3200 tailwind, etc");
		} else {
			angleConfig.setMaxPositions(11, 59);
			angleLarge = (int)angle / 30;
			angleSmall = (int)(angle - (angleLarge * 30)) * 2;
			if (angleLarge == 0) angleLarge = 12;
			angleLarge--;
			ens.setPositions(angleLarge, angleSmall);
			ens.setValue(Conversions.angleToClock(angle));
			help.setText("12:00 headwind, 3:00 right to left cross wind, 6:00 tailwind, etc");
		}
		windAngleUnitSpinner.setSelection(unit);
	}

	private int getAngleUnit() {
		return windAngleUnitSpinner.getSelectedItemPosition();
	}

	@Override
	public void update() {
		double speed = getSpeed();
		double angle = getAngle();
		boolean mph = isMph();
		int angleUnit = getAngleUnit();
		BallisticSettings s = BallisticSettings.i();
		if (s.mph != mph || s.windSpeed != speed || s.windDirection != angle ||
				s.windAngleUnit != angleUnit) {
			s.mph = mph;
			s.windSpeed = speed;
			s.windDirection = angle;
			s.windAngleUnit = angleUnit;
			s.setDirty(true);
		}
	}
}
