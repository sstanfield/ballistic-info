package transapps.ballistic.lib.util;


public class Conversions {

	// There are .3048 meters per foot.
	public static double feetToMeters(double feet) {
		return feet / 3.280839895;
	}

	public static double metersToFeet(double meters) {
		return meters * 3.280839895;
	}

	public static double yardsToMeters(double yards) {
		return yards * 0.9144;
	}

	public static double metersToYards(double meters) {
		return meters / 0.9144;
	}

	public static double inhgTommhg(double inhg) {
		return inhg / 0.0393700791974;
	}

	public static double mmhgToinhg(double mmhg) {
		return mmhg * 0.0393700791974;
	}

	public static double fahrenheitToCelsius(double f) {
		return (f - 32) / 1.8;
	}

	public static double celsiusToFahrenheit(double c) {
		return (c * 1.8) + 32;
	}

	public static double milesToKm(double miles) {
		return miles * 1.60934;
	}

	public static double kmToMile(double kms) {
		return kms / 1.60934;
	}

	public static double inTocm(double in) {
		return in * 2.54;
	}

	public static double cmToin(double cm) {
		return cm / 2.54;
	}

	public static double inTomm(double in) {
		return in * 25.4;
	}

	public static double mmToin(double mm) {
		return mm / 25.4;
	}

	public static double grainToGrams(double grain) {
		return grain * 0.06479891;
	}

	public static double gramsToGrains(double grams) {
		return grams / 0.06479891;
	}

	public static double lbft3Tokgm3(double v) {
		return v * 16.0184634;
	}

	// Specialty angular conversion functions
	public static double degtorad(double deg) {
		return deg*Math.PI/180;
	}
	public static double moatorad(double moa) {
		return moa/60*Math.PI/180;
	}
	public static double radtodeg(double rad) {
		return rad*180/Math.PI;
	}
	public static double radtomoa(double rad) {
		return rad*60*180/Math.PI;
	}

	public static String angleToClock(double angle) {
		int angleLarge = (int)angle / 30;
		int angleSmall = (int)((angle - (angleLarge * 30.0)) * 2.0);
		if (angleLarge == 0) angleLarge = 12;
		return angleLarge+":"+(angleSmall<10?"0"+angleSmall:angleSmall);
	}
}
