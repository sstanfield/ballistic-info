package transapps.ballistic.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import transapps.ballistic.BallisticDisplayActivity;
import transapps.ballistic.BallisticSettings;
import transapps.ballistic.R;
import transapps.ballistic.db.dvo.WeaponDVO;
import transapps.ballistic.dialog.SpinDriftFailDialog;
import transapps.ballistic.lib.Ballistics;
import transapps.ballistic.lib.data.RangeData;
import transapps.ballistic.lib.util.Conversions;
import transapps.ballistic.widgets.EditNumberSliders;

import java.text.DecimalFormat;
import java.util.List;

public class BallisticFragment extends BaseFragment {
	//private static final String TAG="BallisticFragment";

	public static final int DIALOG_SPINDRIFT_FAIL = 1;

	private TextView dropUnitLabel;

	private boolean altImperial = false;
	private boolean fahrenheit = false;
	private boolean inhg = false;
	private View v;
	private BallisticSettings s = BallisticSettings.i();

	private String formatDrop(double drop, DecimalFormat df,
			String neg, String pos) {
		double abs = Math.abs(drop);
		String pre = neg;
		if (drop > 0) pre = pos;
		return pre+df.format(abs);
	}

	private void updateRange(int range, int dropUnit) {
		DecimalFormat df0 = new DecimalFormat("0");
		DecimalFormat df1 = new DecimalFormat("0.0");
		DecimalFormat df2 = new DecimalFormat("0.00");
		DecimalFormat df3 = new DecimalFormat("0.000");
		DecimalFormat dfdrop = df0;
		DecimalFormat dfwind = df0;
		double pathMod = 1;
		double windMod = 1;
		String windUnit = " cm";
		RangeData data = s.table.getData(range);
		RangeData dataM = s.table.EMPTY_RANGE;
		RangeData dataP = s.table.EMPTY_RANGE;
		double path = data.getDrop();
		double wind = data.getWindage();
		if (s.units() == Ballistics.UNITS.IMPERIAL) {
			dfdrop = (path>100||path<-100)?df0:df1;
			dfwind = (wind>100||wind<-100)?df0:df1;
			windUnit = " in";
		} else {
			if (path > 999 || path < -999) {
				dfdrop = df1;
				pathMod = 100;
			}
			if (wind > 999 || wind < -999) {
				dfwind = df1;
				windMod = 100;
				windUnit = " m";
			}
		}
		if ((range - 50) >= 0) dataM = s.table.getData(range - 50);
		if ((range + 50) <= s.maxRange) dataP = s.table.getData(range + 50);
		if (dropUnit == BallisticSettings.DROP_UNIT_RULER) {
			((TextView) v.findViewById(R.id.drop_minus)).setText(dataM==s.table.EMPTY_RANGE?"---":formatDrop(dataM.getDrop()/pathMod, dfdrop, "U", "D"));
			((TextView) v.findViewById(R.id.drop)).setText(data==s.table.EMPTY_RANGE?"---":formatDrop(data.getDrop()/pathMod, dfdrop, "U", "D"));
			((TextView) v.findViewById(R.id.drop_plus)).setText(dataP==s.table.EMPTY_RANGE?"---":formatDrop(dataP.getDrop()/pathMod, dfdrop, "U", "D"));
			setDropUnitsDisplay();
			((TextView) v.findViewById(R.id.windage)).setText(formatDrop(data.getWindage()/windMod, dfwind, "L", "R")+windUnit);
		} else if (dropUnit == BallisticSettings.DROP_UNIT_MOA) {
			((TextView) v.findViewById(R.id.drop_minus)).setText(dataM==s.table.EMPTY_RANGE?"---":formatDrop(dataM.getMoa(), df1, "U", "D"));
			((TextView) v.findViewById(R.id.drop)).setText(data==s.table.EMPTY_RANGE?"---":formatDrop(data.getMoa(), df1, "U", "D"));
			((TextView) v.findViewById(R.id.drop_plus)).setText(dataP==s.table.EMPTY_RANGE?"---":formatDrop(dataP.getMoa(), df1, "U", "D"));
			((TextView) v.findViewById(R.id.windage)).setText(formatDrop(data.getWindageMoa(), df1, "L", "R"));
		} else { // MILS
			((TextView) v.findViewById(R.id.drop_minus)).setText(dataM==s.table.EMPTY_RANGE?"---":formatDrop(dataM.getMil(), df1, "U", "D"));
			((TextView) v.findViewById(R.id.drop)).setText(data==s.table.EMPTY_RANGE?"---":formatDrop(data.getMil(), df1, "U", "D"));
			((TextView) v.findViewById(R.id.drop_plus)).setText(dataP==s.table.EMPTY_RANGE?"---":formatDrop(dataP.getMil(), df1, "U", "D"));
			((TextView) v.findViewById(R.id.windage)).setText(formatDrop(data.getWindageMil(), df1, "L", "R"));
		}
		if (s.mph) {
			((TextView) v.findViewById(R.id.windspeed)).setText("" + df1.format(s.windSpeed));
		} else {
			((TextView) v.findViewById(R.id.windspeed)).setText("" + df1.format(Conversions.milesToKm(s.windSpeed)));
		}
		((TextView) v.findViewById(R.id.windspeed_label)).setText((s.mph?"MPH":"KPH"));
		if (s.windAngleUnit == WindFragment.DEGREE_ANGLE) {
			((TextView) v.findViewById(R.id.winddirection)).setText(df0.format(s.windDirection));
		} else if (s.windAngleUnit == WindFragment.MILLS6400_ANGLE) {
			((TextView) v.findViewById(R.id.winddirection)).setText(df0.format((s.windDirection / 360.0) * 6400.0));
		} else {
			((TextView) v.findViewById(R.id.winddirection)).setText(Conversions.angleToClock(s.windDirection));
		}

		((TextView) v.findViewById(R.id.time)).setText(" " + df3.format(s.table.getData(range).getTime())+"s ");
		if (s.units() == Ballistics.UNITS.IMPERIAL) {
			((TextView) v.findViewById(R.id.velocity)).setText(df2.format(s.table.getData(range).getVelocity())+"ft/s ");
			if (s.getWeapon().bullet.weight != 0) {
				((TextView) v.findViewById(R.id.energy)).setText(" " +
						df1.format(s.table.getEnergy(range, s.getWeapon().bullet.weight))+"ft-lb ");
			}
		} else {
			double vm = s.table.getData(range).getVelocity();
			((TextView) v.findViewById(R.id.velocity)).setText(df2.format(vm)+"m/s ");
			if (s.getWeapon().bullet.weight != 0) {
				double grams = s.getWeapon().bullet.weight * 0.06479891;
				((TextView) v.findViewById(R.id.energy)).setText(
						" "+df1.format(.5*(grams / 1000)*(vm*vm))+"J ");
			}
		}
		((TextView) v.findViewById(R.id.zero_range)).setText(""+(int)(s.imperial?s.getWeapon().zeroRange:Conversions.yardsToMeters(s.getWeapon().zeroRange)));
		((TextView) v.findViewById(R.id.shooting_angle)).setText(""+s.shootingAngle);
		displayAtmo();
		displaySpinDrift();
		BallisticDisplayActivity.i().setTitle(s.getWeapon().name);
	}

	private boolean checkSpinDrift(WeaponDVO w) {
		return (w.bullet.calibre > 0 && w.bullet.weight > 0 && w.bullet.length > 0 &&
				w.barrelTwist > 0 && w.velocity > 0);
	}

	private boolean spinDriftOn() {
		return s.spinDriftOn && checkSpinDrift(s.getWeapon());
	}

	private void setAtmoUnits() {
		SharedPreferences pref = BallisticDisplayActivity.i().getSharedPreferences(
				"transapps.ballistic.Atmo.Settings",
				Context.MODE_PRIVATE);
		altImperial = pref.getInt("alt_sel", 0)==0;
		fahrenheit = pref.getInt("temp_sel", 0)==0;
		inhg = pref.getInt("bar_sel", 0)==0;
	}

	private void displayAtmo() {
		if (s.atmoOn()) {
			DecimalFormat df0 = new DecimalFormat("0");
			DecimalFormat df1 = new DecimalFormat("0.0");
			DecimalFormat df2 = new DecimalFormat("0.00");
			String str = "[";
			if (altImperial) str = str+df1.format(s.atmo.altitude)+"ft";
			else str = str+df1.format(Conversions.feetToMeters(s.atmo.altitude))+"m";
			if (inhg) str = str+" "+df2.format(s.atmo.pressure)+"in/hg";
			else str = str+" "+df2.format(Conversions.inhgTommhg(s.atmo.pressure))+"mm/hg";
			if (fahrenheit) str = str+" "+df1.format(s.atmo.temperature)+"F";
			else str = str+" "+df1.format(Conversions.fahrenheitToCelsius(s.atmo.temperature))+"C";
			str = str+" "+df0.format(s.atmo.humidity)+"%]";
			((TextView) v.findViewById(R.id.atmo)).setText(str);
		} else {
			((TextView) v.findViewById(R.id.atmo)).setText("[Atmosphere Off]");
		}
	}

	private void displaySpinDrift() {
		TextView text = ((TextView) v.findViewById(R.id.spindrift));
		if (spinDriftOn()) {
			text.setText("[Spin Drift On]");
		} else {
			text.setText("[Spin Drift Off]");
		}
	}

	@Override
	public void refresh() {
		setAtmoUnits();
		setDropUnitsDisplay();
		setRangeUnits();
		if (v != null && s.table != null) updateRange(s.range, s.dropUnits);
	}

	private View.OnClickListener windClick = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			BallisticDisplayActivity.i().selectItem(BallisticDisplayActivity.NAV_WIND);
		}
	};

	private View.OnClickListener zeroClick = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			BallisticDisplayActivity.i().selectItem(BallisticDisplayActivity.NAV_ZERO);
		}
	};

	private View.OnClickListener angleClick = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			BallisticDisplayActivity.i().selectItem(BallisticDisplayActivity.NAV_ANGLE);
		}
	};

	private View.OnClickListener dropClick = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			cycleDropUnits();
		}
	};

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		v = inflater.inflate(R.layout.ballistic_display_activity, container, false);

		List<WeaponDVO> weapons = WeaponDVO.allWeapons(BallisticDisplayActivity.i());
		boolean found = false;
		for (WeaponDVO w : weapons) {
			if (w.name.equals(s.weaponName)) {
				s.setWeapon(w);
				found = true;
				break;
			}
		}
		if (!found && weapons.size() > 0) s.setWeapon(weapons.get(0));
		BallisticDisplayActivity.i().setTitle(s.getWeapon().name);
		setAtmoUnits();

		if (s.table == null) {
			refresh();
		} else {
			updateRange(s.range, s.dropUnits);
		}

		EditNumberSliders ens = (EditNumberSliders)v.findViewById(R.id.range_slider);
		setRangeUnits();
		ens.setMaxPositions(s.maxRange - 100, 100);
		ens.setCallbacks(new EditNumberSliders.EditNumberClass() {
			@Override
			public int getLargeScale() {
				return 100;
			}
			@Override
			public String update(int large, int small) {
				s.range = large + small;
				updateRange(s.range, s.dropUnits);
				return ""+s.range;
			}
			@Override
			public boolean isValid(String val) {
				int v = Integer.decode(val);
				return (v >= 0 && v <= s.maxRange);
			}
		});
		ens.setPositions(s.largeRange, s.smallRange);
		ens.setValue(""+s.range);

		v.findViewById(R.id.wind_layout).setOnClickListener(windClick);
		v.findViewById(R.id.windage).setOnClickListener(windClick);
		v.findViewById(R.id.zero_range_layout).setOnClickListener(zeroClick);
		v.findViewById(R.id.zero_range).setOnClickListener(zeroClick);
		v.findViewById(R.id.shooting_angle_layout).setOnClickListener(angleClick);
		v.findViewById(R.id.shooting_angle).setOnClickListener(angleClick);

		dropUnitLabel = (TextView)v.findViewById(R.id.drop_unit_label);
		setDropUnitsDisplay();
		v.findViewById(R.id.drop_layout).setOnClickListener(dropClick);
		v.findViewById(R.id.drop).setOnClickListener(dropClick);
		return v;
	}

	public void cycleDropUnits() {
		if (s.dropUnits < 2) s.dropUnits++;
		else s.dropUnits = 0;
		setDropUnitsDisplay();
		updateRange(s.range, s.dropUnits);
	}

	public void toggleSpinDrift() {
		if (!checkSpinDrift(s.getWeapon())) {
			// Lack data to estimate spin drift...
			openDialog(DIALOG_SPINDRIFT_FAIL);
		} else {
			s.spinDriftOn = !s.spinDriftOn;
			s.table.setSpinDrift(s.spinDriftOn);
			updateRange(s.range, s.dropUnits);
		}
	}

	private void setRangeUnits() {
		if (v == null) return;
		EditNumberSliders ens = (EditNumberSliders)v.findViewById(R.id.range_slider);
		TextView zeroLabel = (TextView)v.findViewById(R.id.zero_range_label);
		if (s.imperial) {
			ens.setLabels("Range:", "yards");
			zeroLabel.setText("yards");
		} else {
			ens.setLabels("Range:", "meters");
			zeroLabel.setText("meters");
		}
	}

	private void setDropUnitsDisplay() {
		if (dropUnitLabel == null) return;
		if (s.dropUnits == BallisticSettings.DROP_UNIT_RULER) {
			if (s.units() == Ballistics.UNITS.IMPERIAL) {
				dropUnitLabel.setText("IN");
			} else {
				double path = s.table==null?0:s.table.getData(s.range).getDrop();
				if (path > -999 && path < 999) dropUnitLabel.setText("CM");
				else dropUnitLabel.setText("M");
			}
		} else if (s.dropUnits == BallisticSettings.DROP_UNIT_MOA) {
			dropUnitLabel.setText("MOA");
		} else if (s.dropUnits == BallisticSettings.DROP_UNIT_MIL) {
			dropUnitLabel.setText("MIL");
		} else { // WTF?
			dropUnitLabel.setText("");
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		EditNumberSliders ens =
				(EditNumberSliders)v.findViewById(R.id.range_slider);
		s.largeRange = ens.getLarge();
		s.smallRange = ens.getSmall();
	}

	private void openDialog(int id) {
		DialogFragment f = null;
		switch (id) {
			case DIALOG_SPINDRIFT_FAIL:
				f = SpinDriftFailDialog.newInstance();
				break;
		}
		if (f != null) {
			f.show(BallisticDisplayActivity.i().getFragmentManager(), "dialog");
		}
	}
}
