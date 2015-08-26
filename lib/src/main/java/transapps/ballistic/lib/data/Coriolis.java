package transapps.ballistic.lib.data;

import transapps.ballistic.lib.Ballistics;

/**
 * Encapsulate latitude and azimuth and allow Coriolis effect computations.
 */
public class Coriolis {
	public static final double EARTH_ROTATION = .00007292;  // rad/s
	/** latitude of shooter. */
	public final double latitude;
	/** azimuth of shot being fired. */
	public final double azimuth;
	/** Muzzle velocity for weapon in use.  ft/s */
	public final double vi;

	public Coriolis(double latitude, double azimuth, double vi) {
		this.latitude = latitude;
		this.azimuth = azimuth;
		this.vi = vi;
	}

	/**
	 * Calculate the gravity modifier for drop, multiple this by absolute drop.
	 * @return Gravity modifier for this latitude/azimuth/muzzle velocity.
	 */
	public double getVertical() {
		return 1 - ((2.0*EARTH_ROTATION*vi)/-Ballistics.GRAVITY)*
				Math.cos(Math.toRadians(latitude))*
				Math.sin(Math.toRadians(azimuth));
	}

	/**
	 * Return the horizontal deflection for for range and time of flight.
	 * @param range Range to target in feet.
	 * @param tof Time of flight to target.
	 * @return Deflection in inches.
	 */
	public double getHorizontal(double range, double tof) {
		return (EARTH_ROTATION*range*Math.sin(Math.toRadians(latitude))*tof) * 12.0;
	}
}
