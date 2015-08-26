package transapps.ballistic.lib.data;

import transapps.ballistic.lib.util.Conversions;

/**
 * Encapsulates info about wind.
 * windSpeed The wind velocity, in mi/hr
 * windAngle The angle at which the wind is approaching from, in degrees.
 *			0 degrees is a straight headwind
 *			90 degrees is from right to left
 *			180 degrees is a straight tailwind
 *			-90 or 270 degrees is from left to right.
 * crosswind Calculated crosswind component.
 * headwind Calculated headwind component.
 *
 */
public class Wind {
	/** Wind speed in miles per hour. */
	public final double windSpeed;
	/** Wind angle- 0 is headwind, 90 is right to left cross wind, etc. */
	public final double windAngle;
	/** Caluculated crosswind component in fps. */
	public final double crosswind;
	/** Calculated headwind component in fps. */
	public final double headwind;

	public Wind(Wind wind) {
		this(wind.windSpeed, wind.windAngle);
	}

	public Wind(double windSpeed, double windAngle) {
		this.windSpeed = windSpeed;
		this.windAngle = windAngle;
		double wangle = Conversions.degtorad(windAngle);
		headwind = Math.cos(wangle)*windSpeed*1.46666667;  // Convert to fps
		crosswind = Math.sin(wangle)*windSpeed*1.46666667;
	}
}
