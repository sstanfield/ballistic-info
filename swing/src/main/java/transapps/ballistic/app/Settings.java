package transapps.ballistic.app;

import transapps.ballistic.lib.Ballistics;
import transapps.ballistic.lib.data.Atmosphere;
import transapps.ballistic.lib.data.Coriolis;
import transapps.ballistic.lib.data.Weapon;

public class Settings {
	public static final int DROP_UNIT_RULER=0;
	public static final int DROP_UNIT_MOA=1;
	public static final int DROP_UNIT_MIL=2;

	public final int maxRange;
	public final int dropUnits;
	public final int shootingAngle;
	public final boolean coriolisOn;
	public final double latitude;
	public final double azimuth;
	public final Weapon weapon;
	public final Atmosphere atmo;
	public final boolean spinDriftOn;
	public final double windSpeed;
	public final double windDirection;
	public final int windAngleUnit;
	public final boolean imperial;
	public final boolean mph;
	public final Ballistics table;

	public Settings() {
		this(3000, 2, 0, false, 0.0, 0.0, Data.defaultWeapons[0],
				Atmosphere.ICAO_STANDARD, false, 0.0, 0.0, 0, false, false);
	}

	public Settings(int maxRange, int dropUnits, int shootingAngle, boolean coriolisOn,
			double latitude, double azimuth, Weapon weapon, Atmosphere atmo, 
			boolean spinDriftOn, double windSpeed, double windDirection, 
			int windAngleUnit, boolean imperial, boolean mph) {
		this(maxRange, dropUnits, shootingAngle, coriolisOn, latitude, azimuth, weapon, atmo, spinDriftOn, windSpeed, windDirection, windAngleUnit, imperial, mph,
				Ballistics.getBallistics(weapon, atmo, shootingAngle, 
						windSpeed, windDirection, maxRange, imperial?Ballistics.UNITS.IMPERIAL:Ballistics.UNITS.METRIC,
						Ballistics.zeroAngle(weapon),
						coriolisOn?new Coriolis(latitude, azimuth, weapon.velocity):null));
		table.setSpinDrift(spinDriftOn);
	}

	public Settings(int maxRange, int dropUnits, int shootingAngle, boolean coriolisOn,
			double latitude, double azimuth, Weapon weapon, Atmosphere atmo, 
			boolean spinDriftOn, double windSpeed, double windDirection, 
			int windAngleUnit, boolean imperial, boolean mph, Ballistics table) {
		this.maxRange = maxRange;
		this.dropUnits = dropUnits;
		this.shootingAngle = shootingAngle;
		this.coriolisOn = coriolisOn;
		this.latitude = latitude;
		this.azimuth = azimuth;
		this.weapon = weapon;
		this.atmo = atmo;
		this.spinDriftOn = spinDriftOn;
		this.windSpeed = windSpeed;
		this.windDirection = windDirection;
		this.windAngleUnit = windAngleUnit;
		this.imperial = imperial;
		this.mph = mph;
		this.table = table;
	}

	public Settings(Settings s) {
		this(s.maxRange, s.dropUnits, s.shootingAngle, s.coriolisOn, s.latitude,
				s.azimuth, s.weapon, s.atmo, s.spinDriftOn, s.windSpeed,
				s.windDirection, s.windAngleUnit, s.imperial, s.mph, s.table);
	}
	
	public Settings newWeapon(Weapon wep) {
		return new Settings(maxRange, dropUnits, shootingAngle, coriolisOn, latitude,
				azimuth, wep, atmo, spinDriftOn, windSpeed,
				windDirection, windAngleUnit, imperial, mph);
	}

	public Settings newImperial(boolean imp) {
		return new Settings(maxRange, dropUnits, shootingAngle, coriolisOn, latitude,
				azimuth, weapon, atmo, spinDriftOn, windSpeed,
				windDirection, windAngleUnit, imp, mph);
	}

	public Settings newDropUnits(int drop) {
		return new Settings(maxRange, drop, shootingAngle, coriolisOn, latitude,
				azimuth, weapon, atmo, spinDriftOn, windSpeed,
				windDirection, windAngleUnit, imperial, mph, table);
	}

	public Settings newMaxRange(int max) {
		return new Settings(max, dropUnits, shootingAngle, coriolisOn, latitude,
				azimuth, weapon, atmo, spinDriftOn, windSpeed,
				windDirection, windAngleUnit, imperial, mph);
	}

	public Settings newWind(double speed, double direction, int unit) {
		return new Settings(maxRange, dropUnits, shootingAngle, coriolisOn, latitude,
				azimuth, weapon, atmo, spinDriftOn, speed,
				direction, unit, imperial, mph);
	}

	public Settings newAtmo(Atmosphere a) {
		return new Settings(maxRange, dropUnits, shootingAngle, coriolisOn, latitude,
				azimuth, weapon, a, spinDriftOn, windSpeed,
				windDirection, windAngleUnit, imperial, mph);
	}
}
