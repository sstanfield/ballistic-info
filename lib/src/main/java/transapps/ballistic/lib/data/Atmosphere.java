package transapps.ballistic.lib.data;

import transapps.ballistic.lib.util.Conversions;


/**
 * Encapsulate atmospheric conditions
 * altitude The altitude above sea level in feet.
 * 		Standard altitude is 0 feet above sea level.
 * barometer The barometric pressure in inches of mercury (in Hg).
 * 		This IS "absolute" pressure, it is not
 * 		the "standardized" pressure reported in the papers and news.
 * 		Standard pressure is 29.92 in Hg.
 * temperature The temperature in Fahrenheit.
 * 		Standard temperature is 59 degrees F.
 * relativeHumidity The relative humidity as a percent.  Ranges from
 * 		0.00 to 100.00, with 50 being 50% relative humidity.  Standard
 * 		humidity is 0%
 */
public class Atmosphere {
	public static final float DEFAULT_ALTITUDE = 0F;
	public static final float DEFAULT_BAROMETER = 29.9213F;
	public static final float DEFAULT_TEMPERATURE = 59F;
	public static final float DEFAULT_HUMIDITY = 0F;
	public static final float MIN_BAROMETER = 20F;
	public static final float MAX_BAROMETER = 35F;
	public static final Atmosphere ICAO_STANDARD = new Atmosphere(null, "ICAO Standard Atmosphere Settings",
			DEFAULT_ALTITUDE, DEFAULT_BAROMETER, DEFAULT_TEMPERATURE, DEFAULT_HUMIDITY);
//	public static final Atmosphere METRO_STANDARD = new Atmosphere(null, "Metro Standard Atmosphere Settings",
//			0, 29.5275, 59, 78);

	private static final double ATMOS_T0 = 459.67;
	private static final double ATMOS_VV1 = 49.0223;

	public final Integer id;
	public final String name;
	/** The altitude above sea level in feet. */
	public final double altitude;
	/** Barometric pressure in inches of mercury, absolute (station) pressure. */
	public final double pressure;
	/** Temperature in Fahrenheit. */
	public final double temperature;
	/** Relative humidity as a percent. */
	public final double humidity;
	/** Air density for these settings. */
	public final double density;
	/** Speed of sounds in this atmosphere. */
	public final double mach;

	public Atmosphere(Atmosphere atmosphere) {
		this(atmosphere.id, atmosphere.name, atmosphere.altitude, atmosphere.pressure,
				atmosphere.temperature, atmosphere.humidity);
	}

	protected Atmosphere(Integer id, String name, double altitude, double pressure,
			double temperature, double humidity) {
		this.id = id;
		this.name = name;
		this.altitude = altitude;
		this.pressure = pressure;
		this.temperature = temperature;
		this.humidity = humidity;
		this.density = airDensity();
		this.mach = Math.sqrt(temperature + ATMOS_T0)*ATMOS_VV1;
	}

	public Atmosphere(String name,
			double barometer, double temperature, double humidity) {
		this(null, name, barometer, temperature, humidity);
	}

	public Atmosphere(Integer id, String name, double pressure,
			double temperature, double humidity) {
		this(id, name, DEFAULT_ALTITUDE, pressure, temperature, humidity);
	}

	public Atmosphere(String name, double altitude) {
		this(null, name, altitude);
	}

	public Atmosphere(Integer id, String name, double altitude) {
		this(id, name, altitude, standardPressureForAlt(altitude),
				DEFAULT_TEMPERATURE, DEFAULT_HUMIDITY);
	}

	public static double standardPressureForAlt(double altitude_ft) {
		double ret;
		double h = Conversions.feetToMeters(altitude_ft);
		ret = (101325.0 * Math.pow((1.0 - 2.25577 * 1e-5D * h), 5.25588)) / 3386.389;
		return ret;
	}

	/**
	 * A method to correct a "standard" Drag Coefficient for differing
	 * atmospheric conditions.  Returns the corrected drag coefficient for
	 * supplied drag coefficient and atmospheric conditions.
	 * @param coefficient Coefficient to correct.
	 * @return Coefficient corrected for atmospheric conditions.
	 */
	public double correct(double coefficient) {
		return coefficient*(ICAO_STANDARD.density/density);
	}

	private double es(double T) {
//		double temp = Conversions.fahrenheitToCelsius(temperature);
//		double Esexp = (7.5 * temp)/(237.3 + temp);
//		return 6.1078 * Math.pow(10, Esexp);
		
		double c0 = 0.99999683;
		double c1 = -0.90826951e-2;
		double c2 = 0.78736169e-4;
		double c3 = -0.61117958e-6;
		double c4 = 0.43884187e-8;
		double c5 = -0.29883885e-10;
		double c6 = 0.21874425e-12;
		double c7 = -0.17892321e-14;
		double c8 = 0.11112018e-16;
		double c9 = -0.30994571e-19;
		double Eso = 6.1078;
		double p = (c0+T*(c1+T*(c2+T*(c3+T*(c4+T*(c5+T*(c6+T*(c7+T*(c8+T*(c9))))))))));
		return Eso/Math.pow(p, 8.0);
		
	}

	private double airDensity() {
		double temp = Conversions.fahrenheitToCelsius(temperature);
		double Es = es(temp);
		double Pv = (Es * (humidity / 100.0)) * 100;
		double P = Conversions.inhgTommhg(pressure) * 1.33322368 * 100.0;
		double Pd = P - Pv;
		double Tk = temp + 273.15;
		double D = (Pd/(287.05*Tk))+(Pv/(461.495*Tk));
		return D * 0.06242796;
	}

	public boolean checkAtmo() {
		return pressure >= MIN_BAROMETER &&
				pressure <= MAX_BAROMETER &&
				humidity >= 0 &&
				humidity <= 100;
	}
}
