package transapps.ballistic.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import transapps.ballistic.BallisticDisplayActivity;
import transapps.ballistic.BallisticSettings;
import transapps.ballistic.R;
import transapps.ballistic.lib.Ballistics;
import transapps.ballistic.lib.data.Bullet;
import transapps.ballistic.lib.data.Coriolis;
import transapps.ballistic.lib.data.RangeData;
import transapps.ballistic.lib.data.Weapon;
import transapps.ballistic.lib.util.Conversions;

import java.text.DecimalFormat;

/**
 * Implement the truing function.
 * Created by sstanf on 4/28/14.
 */
public class TruingFragment extends BaseFragment {
	private final DecimalFormat df0 = new DecimalFormat("0");
	private final DecimalFormat df2 = new DecimalFormat("0.00");
	private final DecimalFormat df4 = new DecimalFormat("0.0000");
	private static final String[] range_unit = {"Meters", "Yards"};
	private static final String[] vel_unit = {"ft/s", "m/s"};
	private static final String[] elevation_unit = {"MIL", "MOA"};
	private static boolean progressUp = false;
	private final Handler activityHandler = new Handler(Looper.getMainLooper());
	private View v;
	private Spinner zeroUnitSpinner;
	private Spinner elevationUnitSpinner;
	private Spinner velUnitSpinner;
	private int zeroSel = 0;
	private int elevationSel = 0;
	private int velSel = 0;

	private int transonic;  // in yards
	private int subsonic;   // in yards

	private Button saveVelocity;
	private Button saveBC;
	private boolean displayVelocityBut;
	private boolean displayBCBut;
	private double newBC;
	private double newVelocity;

	private BallisticSettings s = BallisticSettings.i();

	private static class InitialConditions {
		final int zeroRange;
		final int truRange;
		final double elevation;
		final double bc;
		final double velocity;

		public InitialConditions(final int zeroRange, final int truRange,
								final double elevation, final double bc,
								final double velocity) {
			this.zeroRange = zeroRange;
			this.truRange = truRange;
			this.elevation = elevation;
			this.bc = bc;
			this.velocity = velocity;
		}

		public boolean equals(InitialConditions ic) {
			return zeroRange == ic.zeroRange &&
					truRange == ic.truRange &&
					elevation == ic.elevation &&
					bc == ic.bc &&
					velocity == ic.velocity;
		}

		public static InitialConditions fromDisplay(TruingFragment frag) {
			int zero = frag.fieldToInt(R.id.true_zero);
			zero = frag.zeroSel==0?(int)(Conversions.metersToYards(zero) + .5):zero;
			int truRange = frag.fieldToInt(R.id.true_range);
			truRange = frag.zeroSel==0?(int)(Conversions.metersToYards(truRange) + .5):truRange;
			double mil = frag.fieldToDouble(R.id.true_elevation);
			mil = frag.elevationSel==1?mil / 3.438:mil;
			double bc = frag.fieldToDouble(R.id.true_initial_bc);
			double velocity = frag.fieldToDouble(R.id.true_initial_velocity);
			velocity = frag.velSel==1?Conversions.metersToFeet(velocity):velocity;

			return new InitialConditions(zero, truRange, mil, bc, velocity);
		}

		public void toDisplay(TruingFragment frag) {
			EditText tz = (EditText)frag.v.findViewById(R.id.true_zero);
			EditText tr = (EditText)frag.v.findViewById(R.id.true_range);
			EditText t = (EditText)frag.v.findViewById(R.id.true_elevation);
			t.setText(frag.df2.format(elevation));
			t = (EditText)frag.v.findViewById(R.id.true_initial_bc);
			t.setText(frag.df4.format(bc));
			t = (EditText)frag.v.findViewById(R.id.true_initial_velocity);
			t.setText(frag.df2.format(velocity));
			if (frag.zeroSel==0) {
				tz.setText(""+(int)(Conversions.yardsToMeters(zeroRange) + .5));
				tr.setText(""+(int)(Conversions.yardsToMeters(truRange) + .5));
			} else {
				tz.setText(""+zeroRange);
				tr.setText(""+truRange);
			}
		}
	}

	private long r4(final double i) {
		return (long)((i + .00005) * 10000);
	}

	private void updateMach() {
		double transonic = s.atmo.mach * 1.2;
		int tsr = 0;
		int sr = 0;
		int c = 0;
		for (RangeData r : s.table.getData()) {
			double v = s.imperial?r.getVelocity():Conversions.metersToFeet(r.getVelocity());
			if (v < transonic && tsr == 0) tsr = c;
			if (v < s.atmo.mach) {
				sr = c;
				break;
			}
			c++;
		}
		this.transonic = s.imperial?tsr:(int)(Conversions.metersToYards(tsr)+.5);
		subsonic = s.imperial?sr:(int)(Conversions.metersToYards(sr)+.5);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.truing_fragment, container, false);
		BallisticSettings s = BallisticSettings.i();
		getActivity().setTitle(s.getWeapon().name);

		updateMach();
		final TextView transonict = (TextView)v.findViewById(R.id.true_transonic);

		final TextView subsonict = (TextView)v.findViewById(R.id.true_subsonic);

		if (s.imperial) {
			transonict.setText(""+transonic+" yards");
			subsonict.setText(""+subsonic+" yards");
		} else {
			transonict.setText(""+(int)(Conversions.yardsToMeters(transonic)+.5)+" meters");
			subsonict.setText(""+(int)(Conversions.yardsToMeters(subsonic)+.5)+" meters");
		}

		Weapon nw = s.getWeapon().newZero(s.imperial?100:109, s.atmo);
		Ballistics t = Ballistics.getBallistics(nw, s.atmo, s.shootingAngle,
				s.windSpeed, s.windDirection, transonic+50, Ballistics.UNITS.IMPERIAL/*s.imperial?Ballistics.UNITS.IMPERIAL:Ballistics.UNITS.METRIC*/,
				Ballistics.zeroAngle(nw),
				s.coriolisOn?new Coriolis(s.latitude, s.azimuth, nw.velocity):null);
		InitialConditions ic = new InitialConditions(s.imperial?100:109, transonic,
				-t.getData(transonic).getMil(), s.getWeapon().bullet.coefficient,
				s.getWeapon().velocity);

		zeroUnitSpinner = (Spinner)v.findViewById(R.id.true_zero_unit_value);
		ArrayAdapter<String> aadp = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, range_unit);
		zeroUnitSpinner.setAdapter(aadp);
		aadp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		zeroUnitSpinner.setSelected(false);
		zeroUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
									   int arg2, long arg3) {
				if (zeroUnitSpinner.getSelectedItemPosition() == zeroSel) return;
				zeroSel = zeroUnitSpinner.getSelectedItemPosition();
				EditText zt = (EditText)v.findViewById(R.id.true_zero);
				int zero = Integer.parseInt(zt.getText().toString());
				EditText tt = (EditText) v.findViewById(R.id.true_range);
				int tru = Integer.parseInt(tt.getText().toString());

				if (zeroSel == 0) { // if 0 convert to Meters
					zero = (int)(Conversions.yardsToMeters(zero) + .5);
					zt.setText(""+zero);
					tru = (int)(Conversions.yardsToMeters(tru) + .5);
					tt.setText(""+tru);
					transonict.setText(""+(int)(Conversions.yardsToMeters(transonic)+.5)+" meters");
					subsonict.setText(""+(int)(Conversions.yardsToMeters(subsonic)+.5)+" meters");
				} else {
					zero = (int)(Conversions.metersToYards(zero) + .5);
					zt.setText(""+zero);
					tru = (int)(Conversions.metersToYards(tru) + .5);
					tt.setText(""+tru);
					transonict.setText(""+transonic+" yards");
					subsonict.setText(""+subsonic+" yards");
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		elevationUnitSpinner = (Spinner)v.findViewById(R.id.true_elevation_unit_value);
		aadp = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, elevation_unit);
		elevationUnitSpinner.setAdapter(aadp);
		aadp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		elevationUnitSpinner.setSelected(false);
		elevationUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
									   int arg2, long arg3) {
				if (elevationUnitSpinner.getSelectedItemPosition() == elevationSel)
					return;
				elevationSel = elevationUnitSpinner.getSelectedItemPosition();
				EditText t = (EditText) v.findViewById(R.id.true_elevation);
				double pro = Double.parseDouble(t.getText().toString());

				if (elevationSel == 0) { // if 0 convert to MIL
					pro /= 3.438;
					t.setText("" + df2.format(pro));
				} else {
					pro *= 3.438;
					t.setText("" + df2.format(pro));
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		velUnitSpinner = (Spinner)v.findViewById(R.id.true_velocity_unit_value);
		aadp = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, vel_unit);
		velUnitSpinner.setAdapter(aadp);
		aadp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		velUnitSpinner.setSelected(false);
		velUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
									   int arg2, long arg3) {
				if (velUnitSpinner.getSelectedItemPosition() == velSel)
					return;
				velSel = velUnitSpinner.getSelectedItemPosition();
				TextView t = (TextView) v.findViewById(R.id.true_initial_velocity);
				double pro = Double.parseDouble(t.getText().toString());

				if (velSel == 0) { // if 0 convert to Meters
					pro = Conversions.metersToFeet(pro);
					t.setText("" + df0.format(pro));
				} else {
					pro = Conversions.feetToMeters(pro);
					t.setText("" + df0.format(pro));
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		saveBC = (Button)v.findViewById(R.id.true_savebc_button);
		saveBC.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				InitialConditions ic = InitialConditions.fromDisplay(TruingFragment.this);
				BallisticDisplayActivity.i().truedWep(ic.velocity, newBC);
			}
		});

		saveVelocity = (Button)v.findViewById(R.id.true_savevelocity_button);
		saveVelocity.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				InitialConditions ic = InitialConditions.fromDisplay(TruingFragment.this);
				BallisticDisplayActivity.i().truedWep(newVelocity, ic.bc);
			}
		});

		Button solve = (Button)v.findViewById(R.id.true_solve_button);
		solve.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				TextView t = (TextView)v.findViewById(R.id.true_solved_bc);
				t.setText("CALCULATING...");
				t = (TextView)v.findViewById(R.id.true_solved_velocity);
				t.setText("CALCULATING...");
				final InitialConditions current = InitialConditions.fromDisplay(TruingFragment.this);
				saveBC.setVisibility(View.INVISIBLE);
				displayBCBut = false;
				saveVelocity.setVisibility(View.INVISIBLE);
				displayVelocityBut = false;

				progressUp = true;
				BallisticDisplayActivity.i().setProgressBarIndeterminateVisibility(true);
				new Thread(new Runnable() {
					@Override
					public void run() {
						calcBC(current);
						calcVelocity(current);
						activityHandler.post(new Runnable() {
							@Override
							public void run() {
								progressUp = false;
								BallisticDisplayActivity.i().setProgressBarIndeterminateVisibility(false);
								if (displayBCBut) saveBC.setVisibility(View.VISIBLE);
								if (displayVelocityBut) saveVelocity.setVisibility(View.VISIBLE);
							}
						});

					}
				}).start();
			}
		});

		ic.toDisplay(this);
		BallisticDisplayActivity.i().setProgressBarIndeterminateVisibility(progressUp);
		if (s.imperial) zeroUnitSpinner.setSelection(1);
		else zeroUnitSpinner.setSelection(0);
		return v;
	}

	private int fieldToInt(int field) {
		EditText t = (EditText)v.findViewById(field);
		return Integer.parseInt(t.getText().toString());
	}
	private double fieldToDouble(int field) {
		EditText t = (EditText)v.findViewById(field);
		return Double.parseDouble(t.getText().toString());
	}

	private void finishBC(final boolean failed, final double newBC, final InitialConditions ic) {
		Fragment frag = BallisticDisplayActivity.i().currentFragRaw();
		if (frag != null && frag instanceof TruingFragment) {
			TruingFragment trueFrag = (TruingFragment) frag;
			if (!failed) {
				double nv = ic.velocity;
				if (velSel == 1) nv = Conversions.feetToMeters(ic.velocity);
				TextView t = (TextView) trueFrag.v.findViewById(R.id.true_solved_bc);
				t.setText(df4.format(newBC)+" / "+df2.format(nv)+(velSel==0?" ft/s":" m/s"));
				this.newBC = newBC;
				displayBCBut = true;
			} else {
				TextView t = (TextView) trueFrag.v.findViewById(R.id.true_solved_bc);
				t.setText("ERROR");
			}
		}
	}

	private void calcBC(final InitialConditions ic) {
		double newBC = ic.bc;//s.getWeapon().bullet.coefficient;
		Weapon nw = s.getWeapon().newZero(ic.zeroRange, s.atmo);
		Bullet b = new Bullet("", "", nw.bullet.function, newBC,
				nw.bullet.calibre, nw.bullet.weight, nw.bullet.length);
		nw = nw.newBullet(b);
		boolean dirup = true;
		double factor = .1;
		boolean done = false;
		boolean failed = false;
		while (!done) {
			Ballistics t = Ballistics.getBallistics(nw, s.atmo, s.shootingAngle,
					s.windSpeed, s.windDirection, ic.truRange, Ballistics.UNITS.IMPERIAL/*s.imperial?Ballistics.UNITS.IMPERIAL:Ballistics.UNITS.METRIC*/,
					Ballistics.zeroAngle(nw),
					s.coriolisOn?new Coriolis(s.latitude, s.azimuth, nw.velocity):null);
			double m = -t.getData(ic.truRange).getMil();
			if (r4(ic.elevation) == r4(m)) done = true;
			else {
				if (ic.elevation < m) {
					if (!dirup) factor /= 2;
					newBC += factor;
					dirup = true;
				} else {
					if (dirup) factor /= 2;
					newBC -= factor;
					dirup = false;
				}
				b = new Bullet("", "", nw.bullet.function, newBC,
						nw.bullet.calibre, nw.bullet.weight, nw.bullet.length);
				nw = nw.newBullet(b);
				// Check for failures, break out...
				if (newBC < .01 || newBC > 2.0) {
					done = true;
					failed = true;
				}
			}
		}
		final boolean t_failed = failed;
		final double t_newBC = newBC;
		activityHandler.post(new Runnable() {
			@Override
			public void run() {
				finishBC(t_failed, t_newBC, ic);
			}
		});
	}

	private void finishVelocity(final boolean failed, final double newVelocity, final InitialConditions ic) {
		Fragment frag = BallisticDisplayActivity.i().currentFragRaw();
		if (frag != null && frag instanceof TruingFragment) {
			TruingFragment trueFrag = (TruingFragment) frag;
			if (!failed) {
				TextView t = (TextView)trueFrag.v.findViewById(R.id.true_solved_velocity);
				double nv = newVelocity;
				if (velSel == 1) nv = Conversions.feetToMeters(newVelocity);
				t.setText(df4.format(ic.bc)+" / "+df2.format(nv)+(velSel==0?" ft/s":" m/s"));
				this.newVelocity = newVelocity;
				displayVelocityBut = true;
			} else {
				TextView t = (TextView)trueFrag.v.findViewById(R.id.true_solved_velocity);
				t.setText("ERROR");
			}
		}
	}

	private void calcVelocity(final InitialConditions ic) {
		double newVelocity = s.getWeapon().velocity;
		Weapon nw = s.getWeapon().newZero(ic.zeroRange, s.atmo);
		Bullet b = new Bullet("", "", nw.bullet.function, ic.bc,
				nw.bullet.calibre, nw.bullet.weight, nw.bullet.length);
		nw = nw.newBullet(b);
		boolean dirup = true;
		double factor = 100;
		boolean done = false;
		boolean failed = false;
		while (!done) {
			Ballistics t = Ballistics.getBallistics(nw, s.atmo, s.shootingAngle,
					s.windSpeed, s.windDirection, ic.truRange, Ballistics.UNITS.IMPERIAL,/*s.imperial?Ballistics.UNITS.IMPERIAL:Ballistics.UNITS.METRIC,*/
					Ballistics.zeroAngle(nw),
					s.coriolisOn?new Coriolis(s.latitude, s.azimuth, nw.velocity):null);
			double m = -t.getData(ic.truRange).getMil();
			if (r4(ic.elevation) == r4(m)) done = true;
			else {
				if (ic.elevation < m) {
					if (!dirup) factor /= 2;
					newVelocity += factor;
					dirup = true;
				} else {
					if (dirup) factor /= 2;
					newVelocity -= factor;
					dirup = false;
				}
				nw = nw.newVelocity(newVelocity);
			}
			if (newVelocity < 0 || newVelocity > 10000) {
				done = true;
				failed = true;
			}
		}
		final boolean t_failed = failed;
		final double t_newVelocity = newVelocity;
		activityHandler.post(new Runnable() {
			@Override
			public void run() {
				finishVelocity(t_failed, t_newVelocity, ic);
			}
		});
	}

}
