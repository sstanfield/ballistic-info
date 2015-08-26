package transapps.ballistic.lib;

import transapps.ballistic.lib.data.Atmosphere;
import transapps.ballistic.lib.data.Coriolis;
import transapps.ballistic.lib.data.RangeData;
import transapps.ballistic.lib.data.RangeSettings;
import transapps.ballistic.lib.data.Weapon;
import transapps.ballistic.lib.data.Wind;
import transapps.ballistic.lib.retard.G1;
import transapps.ballistic.lib.retard.G2;
import transapps.ballistic.lib.retard.G5;
import transapps.ballistic.lib.retard.G6;
import transapps.ballistic.lib.retard.G7;
import transapps.ballistic.lib.retard.G8;
import transapps.ballistic.lib.retard.RetardFunction;
import transapps.ballistic.lib.util.Conversions;
import transapps.ballistic.lib.util.Vector;

/**
 * This class was originally ported from the GNU Exterior Ballistics Computer, 
 * v1.07 written by Derek Yates.
 * http://balcomp.sourceforge.net/
 * This code is why the app needs to be GPL.
 * 
 * Support for Metric is added by converting imperial
 * outputs to metric- i.e. it is imperial internally but client code should not
 * care.
 * 
 * Support for Spin Drift added using Bryan Litz's formula to estimate it.
 * 
 * BC should be in ICAO format, if you have a Standard Metro BC multiply it by
 * .982 to make it ICAO.
 * 
 * Pulled the drag functions and some code/ideas from 
 * James B. Millard's (jbm@cybermesa.com) GPL CGI apps.  Converted to ICAO and
 * got output much closer to Bryan Litz's app and the JBM webapps.
 * http://www.jbmballistics.com/ballistics/downloads/downloads.shtml
 */
public class Ballistics implements RangeSettings {
	public final static double GRAVITY = -32.174;//  or -9.80665 m/s
	public final RangeData EMPTY_RANGE = new RangeData(this, Vector.NULL_VECTOR, 0, 0, 0, null);
	public static final int G1 = 0;
	public static final int G2 = 1;
	public static final int G5 = 2;
	public static final int G6 = 3;
	public static final int G7 = 4;
	public static final int G8 = 5;
	public static final int MAXRANGE = 5000;
	public enum UNITS { IMPERIAL, METRIC }

	private final int range;
	private final RangeData[] data;
	private final UNITS units; 

	protected final RetardFunction function;
	protected final Weapon weapon;
	protected final Atmosphere atmo;
	protected final double shootingAngle;
	protected final double zeroAngle;
	protected final Wind wind;
	protected final Double gyroStability;
	protected final RangeData spinDriftAtZero;
	protected final Coriolis coriolis;

	protected boolean spinDriftOn = false;
	protected boolean zeroWithSpinDrift = true;

	/**
	 * Applies the Miller twist rule (Don Miller) to compute the 
	 * "gyro stability" for the supplied weapon.  If a is not null will apply
	 * an atmospheric correction for temp and pressure.  Also applies a
	 * velocity correction.
	 * @param w Weapon to get the bullet info and barrel twist info.
	 * @param a If not null use the temp and pressure to apply a correction.
	 * @return The computed stability factor or null if not enough data is
	 * 	provided to compute it.
	 */
	public static Double getGyroStability(Weapon w, Atmosphere a) {
		Double ret = null;
		if (w.bullet.calibre > 0 && w.bullet.weight > 0 && w.bullet.length > 0 &&
				w.barrelTwist > 0 && w.velocity > 0) {
			double temp = (a==null?59:a.temperature);
			double pressure = (a==null?29.92:a.pressure);
			double l = w.bullet.length/w.bullet.calibre; // length in calibers
			double t = w.barrelTwist/w.bullet.calibre; // twist in calibers per turn
			double velocityCorrect = Math.pow(w.velocity / 2800.0, 1.0/3.0);
			double atmCorrect = ((temp+460.0) / (59.0+460.0) * 29.92/pressure);
			ret = ((30.0 * w.bullet.weight) / (Math.pow(t, 2) *
					Math.pow(w.bullet.calibre, 3) * l *
					(1+Math.pow(l, 2))));
			ret = ret * velocityCorrect * atmCorrect;
		}
		return ret;
	}

	/**
	 * Apply Bryan Litz's spin drift formula.
	 * @param gyroStab Gyro Stability value calculated with Miller's formula.
	 * @param time Flight time in seconds.
	 * @return Spin drift if gyroStab is not null or 0 otherwise.  In inches.
	 */
	public static double getSpinDrift(Double gyroStab, double time) {
		double ret = 0;
		if (gyroStab != null && time > 0) {
			ret = (1.25 * (gyroStab + 1.2)) * Math.pow(time, 1.83);
		}
		return ret;
	}

	public Double getGyroStability() {
		return gyroStability;
	}

	/**
	 * Solves the table for the above data.  Should only be called by
	 * getBallistics (hence protected).
	 */
	protected void solve() {
		double t;
		double dt;
		double vm;
		Vector v, v1, g, r, w, tv;
		double drg;
		final double vi = weapon.velocity;
		double interval = 3;
		if (units == UNITS.METRIC) interval = 3.28084;
		// This data is useless.
		if (weapon.velocity <= 1 || weapon.bullet.weight <= 0 || weapon.bullet.coefficient <= 0) {
			return;
		}

		g = new Vector (GRAVITY*Math.sin(Conversions.degtorad((shootingAngle + zeroAngle))), GRAVITY*Math.cos(Conversions.degtorad((shootingAngle + zeroAngle))), 0);
		w = new Vector(-wind.headwind, 0, wind.crosswind);
		v =new Vector(vi*Math.cos(Conversions.degtorad(zeroAngle)), vi*Math.sin(Conversions.degtorad(zeroAngle)), 0);
		r = new Vector(0, -weapon.sightHeight/12, 0);
		double coefficient = getCoefficient();
		double mach = atmo==null?Atmosphere.ICAO_STANDARD.mach:atmo.mach;

		int n = 0;
		vm = weapon.velocity;
		for (t=0; n <= range; t=t+dt) {
			if (n == 0 || ((r.x / interval) >= n)) {
				double sd = getSpinDrift(gyroStability, t);
				if (weapon.rightTwist) sd = -sd;
				data[n] = new RangeData(this, r, t, vm, sd, coriolis);
				n++;
			}

			v1 = v;
			dt=0.5/vm;
			tv = v.sub(w);
			// Compute acceleration using the drag function retardation
			drg = vm * function.drag(coefficient, vm / mach);
			v = v.sub(tv.mul(drg).sub(g).mul(dt));
			vm=v.length();

			// Compute position based on average velocity.
			double oldx = r.x;
			Vector a = new Vector((v.x+v1.x)/2, (v.y+v1.y)/2, (v.z+v1.z)/2);
			r = r.add(a.mul(dt));
			if (r.x < oldx) {  // Ran out of steam (angle to high) to get to range.
				n = range+1;
			}
		}
	}

	private static RetardFunction getFunction(int function) {
		RetardFunction ret = null;
		switch (function) {
		case G1: ret = new G1(); break;
		case G2: ret = new G2(); break;
		case G5: ret = new G5(); break;
		case G6: ret = new G6(); break;
		case G7: ret = new G7(); break;
		case G8: ret = new G8(); break;
		}
		return ret;
	}

	/**
	 * Generate a Ballistics class with data in 1 yard/meter
	 * increments, up to supplied range (<= MAXRANGE).
	 *
	 * @param weapon Info on the weapon system being used.
	 * @param atmo Atmospheric conditions in effect while shooting.
	 * @param shootingAngle The uphill or downhill shooting angle, in degrees.
	 * 		Usually 0, but can be anything from 90 (directly up), to -90
	 * 		(directly down).
	 * @param zeroAngle The angle of the sighting system relative to the bore,
	 * 		in degrees.  This can be easily computed using the ZeroAngle()
	 * 		function documented above.
	 * @param wind Encapsulated wind info for solution.
	 * @param range Max range in yards to provide data (must be less then or
	 * 		equal to MAXRANGE).
	 * @param units Units to return data in.
	 * @param coriolis Coriolis (latitude and azimuth) setting.
	*/
	protected Ballistics(Weapon weapon, Atmosphere atmo,
			double shootingAngle, double zeroAngle, Wind wind,
			int range, UNITS units, Coriolis coriolis,
			RangeData spinDriftAtZero) {
		if (range > MAXRANGE) throw new RuntimeException("Invalid range: "+range);
		this.units = units;
		this.range = range;
		data =  new RangeData[range + 1];
		for (int n = 0; n <= range; n++) {
			data[n] = EMPTY_RANGE;
		}
		this.weapon = weapon;
		this.atmo = atmo;
		this.function = getFunction(weapon.bullet.function);
		this.shootingAngle = shootingAngle;
		this.zeroAngle = zeroAngle;
		this.wind = wind;
		this.coriolis = coriolis;
		this.gyroStability = getGyroStability(weapon, atmo);
		solve();
		this.spinDriftAtZero = spinDriftAtZero==null?null:new RangeData(spinDriftAtZero, this);
	}

	/**
	 * Calculates the zero angle for a weapon system.  All variables needed for
	 * this are encapsulated in the supplied weapon.
	 * 
	 * @param weapon the weapon to calculate the zero angle for.
	 *
	 * @return The angle of the bore relative to the sighting system, in degrees.
*	*/
	public static double zeroAngle(Weapon weapon) {
		// Numerical Integration variables
		double t;
		double dt; // The solution accuracy generally doesn't suffer if its within a foot for each second of time.
		double da; // The change in the bore angle used to iterate in on the correct zero angle.
		// State variables for each integration loop.
		double vm; // velocity
		double drg;
		Vector v, v1, g, r;
		double angle; // The actual angle of the bore.
		int quit=0; // We know it's time to quit our successive approximation loop when this is 1.
		double yIntercept = 0;
		double coefficient = weapon.atmosphere==null?weapon.bullet.coefficient:weapon.atmosphere.correct(weapon.bullet.coefficient);

		// Start with a very coarse angular change, to quickly solve even large launch angle problems.
		da=Conversions.degtorad(14);

		// Bad input data...
		if (weapon.velocity <= 1 || coefficient <= 0) {
			return 0;
		}

		RetardFunction function = getFunction(weapon.bullet.function);
		double mach = weapon.atmosphere==null?Atmosphere.ICAO_STANDARD.mach:weapon.atmosphere.mach;
		// The general idea here is to start at 0 degrees elevation, and increase the elevation by 14 degrees
		// until we are above the correct elevation.  Then reduce the angular change by half, and begin reducing
		// the angle.  Once we are again below the correct angle, reduce the angular change by half again, and go
		// back up.  This allows for a fast successive approximation of the correct elevation, usually within less
		// than 20 iterations.
		for (angle=0;quit==0;angle=angle+da){
			v = new Vector (weapon.velocity*Math.cos(angle), weapon.velocity*Math.sin(angle), 0);
			g = new Vector (GRAVITY*Math.sin(angle), GRAVITY*Math.cos(angle), 0);

			r = new Vector(0, -weapon.sightHeight/12.0, 0);
			for (t=0;r.x<=weapon.zeroRange*3;t=t+dt){
				v1 = v;
				vm = v.length();
				dt = 1/vm;

				drg = vm * function.drag(coefficient, vm / mach);
				v = v.sub(v.mul(drg).sub(g).mul(dt));

				Vector a = new Vector((v.x+v1.x)/2, (v.y+v1.y)/2, (v.z+v1.z)/2);
				r = r.add(a.mul(dt));
				// Break early to save CPU time if we won't find a solution.
				if ((v.y<0 && r.y<yIntercept) || (v.y>3*v.x)) {
					break;
				}
			}

			if (r.y>yIntercept && da>0){
				da=-da/2;
			}

			if (r.y<yIntercept && da<0){
				da=-da/2;
			}

			if (Math.abs(da) < Conversions.moatorad(0.01)) quit=1; // If our accuracy is sufficient, we can stop approximating.
			if (angle > Conversions.degtorad(45)) quit=1; // If we exceed the 45 degree launch angle, then the projectile just won't get there, so we stop trying.
		}

		return Conversions.radtodeg(angle); // Convert to degrees for return value.
	}

	/**
	 * A function to generate a Ballistics class with data in 1 yard
	 * increments, up to supplied range (<= MAXRANGE).
	 *
	 * @param weapon The weapon object to generate a table for.
	 * @param atmo Atmospheric conditions for table, null to disable.
	 * @param shootingAngle The uphill or downhill shooting angle, in degrees.
	 * 		Usually 0, but can be anything from 90 (directly up), to -90
	 * 		(directly down).
	 * @param windSpeed The wind velocity, in mi/hr
	 * @param windAngle The angle at which the wind is approaching from, in degrees.
	 *			0 degrees is a straight headwind
	 *			90 degrees is from right to left
	 *			180 degrees is a straight tailwind
	 *			-90 or 270 degrees is from left to right.
	 * @param range Max range in yards to provide data (must be less then or
	 * 		equal to MAXRANGE).
	 * @param units The units to return the table in, either yards for imperial
	 * 			or meters for metric- note this ONLY effects the units for the
	 * 			returned table (returned data for each yard/meter will also be
	 * 			in metric or imperial).  ONLY effects output, inputs need to 
	 * 			be in imperial.
	 * @param zeroangle Angle of weapon to cause it POA == POI for zero range.
	 * @param coriolis Coriolis (latitude and azimuth) setting.
	 *
	 * @return A Ballistics Object containing the calculated data from 1 to
	 * 		range yards/meters in 1 yard/meter increments.
	*/
	public static Ballistics getBallistics(Weapon weapon, Atmosphere atmo,
			double shootingAngle, double windSpeed,
			double windAngle, int range, UNITS units, double zeroangle,
			Coriolis coriolis) {
		if (range > MAXRANGE) return null;
		Wind wind = new Wind(windSpeed, windAngle);
		int zr = (int)(((units==UNITS.IMPERIAL)?weapon.zeroRange:Conversions.yardsToMeters(weapon.zeroRange))+.5);
		// Generate a table based on no wind and zero atmosphere condition, use
		// this table to get the spin drift effects at zero for the real table.
		Ballistics sdTable = new Ballistics(weapon, weapon.atmosphere, 0, zeroangle,
				new Wind(0, 0), zr, units, null/*coriolis XXX needs to be for zero. */, null);
		RangeData spinDriftAtZero = sdTable.getData(zr);
		return new Ballistics(weapon, atmo, shootingAngle, zeroangle,
				wind, range, units, coriolis, spinDriftAtZero);
	}

	private double getCoefficient() {
		return atmo==null?weapon.bullet.coefficient:atmo.correct(weapon.bullet.coefficient);
	}

	public double getEnergy(int range, double weight) {
		double velocity = getData(range).getVelocity();
		return weight*velocity*velocity/450436.0D;
	}

	public RangeData[] getData() {
		return data;
	}

	public RangeData getData(int range) {
		return (range >= 0 && range <= this.range) ? data[range] : EMPTY_RANGE;
	}

	public class PBRData {
		public final int nearZero;
		public final int farZero;
		public final int minimumPBR;
		public final int maximumPBR;
		public final int tin100;

		public PBRData(int nz, int fz, int minp, int maxp, int t100) {
			this.nearZero = nz;
			this.farZero = fz;
			this.minimumPBR = minp;
			this.maximumPBR = maxp;
			this.tin100 = t100;
		}
	}
	// Solves for the maximum Point blank range and associated details.
	// XXX FIXME- This method is BROKEN do not use until fixed!
	public PBRData pbr(double VitalSize) {
		double t;
		double dt;
		double v;
		double vx, vx1, vy, vy1;
		double dv, dvx, dvy;
		double x, y;
		double ShootingAngle=0;
		double ZAngle=0;
		double Step=10;
		int quit=0;
		double zero=-1;
		double farzero=0;
		int vertex_keep;
		double y_vertex=0;
		double min_pbr_range=0;
		int min_pbr_keep;
		double max_pbr_range=0;
		int max_pbr_keep;
		int tin100=0;
		double Gy;
		double Gx;
		final double vi = weapon.velocity;
		double mach = atmo==null?Atmosphere.ICAO_STANDARD.mach:atmo.mach;

		while (quit==0){
			Gy=GRAVITY*Math.cos(Conversions.degtorad((ShootingAngle + ZAngle)));
			Gx=GRAVITY*Math.sin(Conversions.degtorad((ShootingAngle + ZAngle)));

			vx=vi*Math.cos(Conversions.degtorad(ZAngle));
			vy=vi*Math.sin(Conversions.degtorad(ZAngle));

			x=0;
			y=-weapon.sightHeight/12;

			int keep=0;
			int keep2=0;
			int tinkeep;
			min_pbr_keep=0;
			max_pbr_keep=0;
			vertex_keep=0;

			tin100=0;
			tinkeep=0;

			for (t=0;;t=t+dt){

				vx1=vx;
				vy1=vy;
				v=Math.pow(Math.pow(vx,2)+Math.pow(vy,2),0.5);
				dt=0.5/v;

				// Compute acceleration using the drag function retardation
				dv = function.drag(getCoefficient(), v / mach);
				dvx = -(vx/v)*dv;
				dvy = -(vy/v)*dv;

				// Compute velocity, including the resolved gravity vectors.
				vx=vx + dt*dvx + dt*Gx;
				vy=vy + dt*dvy + dt*Gy;

				// Compute position based on average velocity.
				x=x+dt*(vx+vx1)/2;
				y=y+dt*(vy+vy1)/2;

				if (y>0 && keep==0 && vy>=0) {
					zero=x;
					keep=1;
				}

				if (y<0 && keep2==0 && vy<=0){
					farzero=x;
					keep2=1;
				}

				if ((12*y)>-(VitalSize/2) && min_pbr_keep==0){
					min_pbr_range=x;
					min_pbr_keep=1;
				}

				if ((12*y)<-(VitalSize/2) && min_pbr_keep==1 && max_pbr_keep==0){
					max_pbr_range=x;
					max_pbr_keep=1;
				}

				if (x>=300 && tinkeep==0){
					tin100=(int)((float)100*(float)y*(float)12);
					tinkeep=1;
				}

				if (Math.abs(vy)>Math.abs(3*vx)) { return null; }

				// The PBR will be maximum at the point where the vertex is 1/2 vital zone size.
				if (vy<0 && vertex_keep==0){
					y_vertex=y;
					vertex_keep=1;
				}

				if (keep==1 && keep2==1 && min_pbr_keep==1 && max_pbr_keep==1 && vertex_keep==1 && tinkeep==1) {
					break;
				}
			}

			if ((y_vertex*12)>(VitalSize/2)) {
				if (Step>0) Step=-Step/2; // Vertex too high.  Go downwards.
			} else if ((y_vertex*12)<=(VitalSize/2)) { // Vertex too low.  Go upwards.
				if (Step<0) Step =-Step/2;
			}

			ZAngle+=Step;

			if (Math.abs(Step)<(0.01/60)) quit=1;
		}

		return new PBRData((int)(zero/3), (int)(farzero/3),
				(int)(min_pbr_range/3), (int)(max_pbr_range/3), tin100);
	}

	public void setSpinDrift(boolean enabled) {
		spinDriftOn = enabled;
	}

	public boolean spinDriftOn() {
		return spinDriftOn;
	}

	public void setZeroWithSpinDrift(boolean zeroWithSpinDrift) {
		this.zeroWithSpinDrift = zeroWithSpinDrift;
	}

	public boolean zeroWithSpinDrift() {
		return zeroWithSpinDrift;
	}

	public boolean coriolisOn() {
		return coriolis != null;
	}

	public boolean isImperial() {
		return units == UNITS.IMPERIAL;
	}

	public RangeData spinDriftAtZero() {
		return spinDriftAtZero;
	}

	public Coriolis coriolisAtZero() {
		return coriolis;
	}
}
