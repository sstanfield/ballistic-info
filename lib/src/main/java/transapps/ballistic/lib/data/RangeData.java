package transapps.ballistic.lib.data;

import transapps.ballistic.lib.util.Conversions;
import transapps.ballistic.lib.util.Vector;

public class RangeData {
	private final double windage;
	private final double time;
	private final double velocity;
	private final double x;
	private final double y;
	private final double spinDrift;
	private final RangeSettings settings;

	public RangeData(RangeData rd, RangeSettings settings) {
		this.settings = settings;
		this.windage = rd.windage;
		this.time = rd.time;
		this.velocity = rd.velocity;
		this.x = rd.x;
		this.y = rd.y;
		this.spinDrift = rd.spinDrift;
	}

	public RangeData(RangeSettings settings, Vector r, double time,
			double velocity, double spinDrift, Coriolis coriolis) {
		this.settings = settings;
		this.windage = (r.z * 12.0) +  // In inches.
				(coriolis!=null?coriolis.getHorizontal(r.x, time):0F);
		this.time = time;  // In secs
		this.velocity = velocity;  // In ft/s
		this.x = r.x;  // In feet
		this.y = r.y * (coriolis!=null?coriolis.getVertical():1);  // In feet
		this.spinDrift = spinDrift;  // In inches
	}

	private double getDrop(boolean imperial) {
		return imperial?(y*12):((y*12)*2.54);
	}

	/**
	 * Returns drop in absolute value, inches for imperial or cm for metric.
	 */
	public double getDrop() {
		return getDrop(settings.isImperial());
	}

	private double getWindage(boolean imperial) {
		double adjust = 0;
		if (settings.zeroWithSpinDrift()) {
			RangeData sdz = settings.spinDriftAtZero();
			if (sdz != null) adjust = (((-sdz.getSpinDrift()/12)*x)/sdz.x)*12;
		}
		return imperial?(windage+getSpinDrift()+adjust):((windage+getSpinDrift()+adjust)*2.54);
	}

	/**
	 * Returns windage in absolute value, inches for imnperial or cm for metric.
	 */
	public double getWindage() {
		return getWindage(settings.isImperial());
	}

	/**
	 * Returns velocity in ft/s for imperial or m/s for metric.
	 */
	public double getVelocity() {
		return settings.isImperial()?velocity:(velocity*0.3048);
	}

	/**
	 * Returns bullet flight time at range.
	 */
	public double getTime() {
		return time;
	}

	/**
	 * Returns spindrift in inches.
	 */
	public double getSpinDrift() {
		return settings.spinDriftOn()?spinDrift:0;
	}

	/**
	 * Returns drop in MOAs.
	 */
	public double getMoa() {
		return x==0?0:Conversions.radtomoa(Math.atan((getDrop(true)/12)/x));
	}

	/**
	 * Returns drop in MILs
	 */
	public double getMil() {
		return getMoa() / 3.438;
	}

	/**
	 * Returns Windage in MOAs.
	 */
	public double getWindageMoa() {
		return x==0?0:Conversions.radtomoa(Math.atan(((getWindage(true))/12)/x));
	}

	/**
	 * Returns Windage in MILs.
	 */
	public double getWindageMil() {
		return getWindageMoa() / 3.438;
	}
}
