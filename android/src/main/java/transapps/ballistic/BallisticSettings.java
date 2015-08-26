package transapps.ballistic;

import android.os.Handler;
import android.os.Looper;
import transapps.ballistic.db.dvo.AtmosphereDVO;
import transapps.ballistic.db.dvo.BulletDVO;
import transapps.ballistic.db.dvo.WeaponDVO;
import transapps.ballistic.lib.Ballistics;
import android.content.Context;
import android.content.SharedPreferences;
import transapps.ballistic.lib.data.Coriolis;
import transapps.ballistic.lib.data.Weapon;

public class BallisticSettings {
	private static final String SNAME = "transapps.ballistic.Ballistic.Settings";

	public static final int DROP_UNIT_RULER = 0;
	public static final int DROP_UNIT_MOA = 1;
	public static final int DROP_UNIT_MIL = 2;

	public final int maxRange = 3000;
	public int range = 100;
	public int largeRange = 100;
	public int smallRange = 0;
	public int dropUnits = 2;
	public int shootingAngle = 0;
	public boolean coriolisOn = false;
	public double latitude = 0;
	public double azimuth = 0;
	public String weaponName = "";
	public AtmosphereDVO atmo = new AtmosphereDVO(AtmosphereDVO.ICAO_STANDARD);
	public boolean atmoOn = false;
	public boolean spinDriftOn = false;
	public double windSpeed = 0;
	public double windDirection = 0;
	public int windAngleUnit = 0;
	public boolean imperial = false;
	public boolean mph = false;

	public Ballistics table;

	// Setting for WeaponFragment
	public int velSel = 0;
	public int sightSel = 0;
	public int twistSel = 0;
	public int weightSel = 0;
	public int calSel = 0;
	public int lenSel = 0;

	// Do not save these....
	public Double zeroangle = null;


	private WeaponDVO weapon = new WeaponDVO("NA", "", 1000, 1, true, 10, 328.084, AtmosphereDVO.STANDARD, BulletDVO.DUMMY);
	private boolean dirty = true;

	private static BallisticSettings instance;

	private BallisticSettings() {
		super();
	}

	public void setWeapon(WeaponDVO weapon) {
		this.weapon = weapon;
		weaponName = weapon.name;
		setDirty(true);
	}

	public WeaponDVO getWeapon() {
		return weapon;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public boolean isDirty() {
		return dirty;
	}

	// Verify that atmo settings are active and that they make sense.
	public boolean atmoOn() {
		return atmoOn && atmo.checkAtmo();
	}

	public static BallisticSettings i() {
		if (instance == null) {
			instance = new BallisticSettings();
		}
		return instance;
	}

	public static BallisticSettings loadIfNew(Context context) {
		if (instance == null) {
			i().load(context);
		}
		return instance;
	}

	public void load(Context context) {
		SharedPreferences pref = context.getSharedPreferences(
				SNAME, Context.MODE_PRIVATE);
		range = pref.getInt("range", range);
		largeRange = pref.getInt("largeRange", largeRange);
		smallRange = pref.getInt("smallRange", smallRange);
		dropUnits = pref.getInt("inches_moa", dropUnits);
		shootingAngle = pref.getInt("shootingAngle", shootingAngle);
		coriolisOn = pref.getBoolean("onOff", coriolisOn);
		latitude = pref.getFloat("latitude", (float) latitude);
		azimuth = pref.getFloat("azimuth", (float) azimuth);
		weaponName = pref.getString("weaponName", weaponName);
		atmo = AtmosphereDVO.create(pref);
		atmoOn = pref.getBoolean("atmoOn", atmoOn);
		spinDriftOn = pref.getBoolean("spinDriftOn", spinDriftOn);
		windSpeed = pref.getFloat("windSpeed", (float) windSpeed);
		windDirection = pref.getFloat("windAngle", (float) windDirection);
		windAngleUnit = pref.getInt("windAngleUnit", windAngleUnit);
		imperial = pref.getBoolean("imperial", imperial);
		mph = pref.getBoolean("mph", mph);

		velSel = pref.getInt("vel_sel", velSel);
		sightSel = pref.getInt("sight_sel", sightSel);
		twistSel = pref.getInt("twist_sel", twistSel);
		weightSel = pref.getInt("weight_sel", weightSel);
		calSel = pref.getInt("cal_sel", calSel);
		lenSel = pref.getInt("len_sel", lenSel);
	}

	public void save(Context context) {
		SharedPreferences pref = context.getSharedPreferences(
				SNAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor e = pref.edit();
		e.putInt("range", range);
		e.putInt("largeRange", largeRange);
		e.putInt("smallRange", smallRange);
		e.putInt("inches_moa", dropUnits);
		e.putInt("shootingAngle", shootingAngle);
		e.putBoolean("onOff", coriolisOn);
		e.putFloat("latitude", (float) latitude);
		e.putFloat("azimuth", (float) azimuth);
		e.putString("weaponName", weaponName);
		e.putBoolean("atmoOn", atmoOn);
		e.putBoolean("spinDriftOn", spinDriftOn);
		e.putFloat("windSpeed", (float) windSpeed);
		e.putFloat("windAngle", (float) windDirection);
		e.putInt("windAngleUnit", windAngleUnit);
		e.putBoolean("imperial", imperial);
		e.putBoolean("mph", mph);
		if (atmo != null) atmo.saveToPrefs(e);

		e.putInt("vel_sel", velSel);
		e.putInt("sight_sel", sightSel);
		e.putInt("twist_sel", twistSel);
		e.putInt("weight_sel", weightSel);
		e.putInt("cal_sel", calSel);
		e.putInt("len_sel", lenSel);

		e.commit();
	}

	public Ballistics.UNITS units() {
		return (imperial?Ballistics.UNITS.IMPERIAL:Ballistics.UNITS.METRIC);
	}

	public void refreshTable() {
		if (isDirty()) {
			setDirty(false);
			final Handler activityHandler = new Handler(Looper.getMainLooper());

			final int t_shootingAngle = shootingAngle;
			final boolean t_coriolisOn = coriolisOn;
			final double t_latitude = latitude;
			final double t_azimuth = azimuth;
			final AtmosphereDVO t_atmo = atmo;
			final boolean t_spinDriftOn = spinDriftOn;
			final double t_windSpeed = windSpeed;
			final double t_windDirection = windDirection;
			final Ballistics.UNITS t_units = units();
			final Weapon t_weapon = getWeapon();
			final boolean t_atmoOn = atmoOn();

			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					Ballistics bt;
					final Double za = Ballistics.zeroAngle(t_weapon);
					if (t_coriolisOn) {
						bt = Ballistics.getBallistics(t_weapon, (t_atmoOn ? t_atmo : null),
								t_shootingAngle, t_windSpeed, t_windDirection, maxRange, t_units,
								za, new Coriolis(t_latitude, t_azimuth, t_weapon.velocity));
					} else {
						bt = Ballistics.getBallistics(t_weapon, (t_atmoOn ? t_atmo : null),
								t_shootingAngle, t_windSpeed, t_windDirection, maxRange, t_units,
								za, null/*coriolis*/);
					}
					bt.setSpinDrift(t_spinDriftOn);
					final Ballistics bt2 = bt;
					activityHandler.post(new Runnable() {
						@Override
						public void run() {
							zeroangle = za;
							table = bt2;
							BallisticDisplayActivity.i().tableGenerated();
						}
					});
				}
			});
			t.start();
		}
	}
}
