package transapps.ballistic.db.dvo;

import java.sql.ResultSet;
import java.sql.SQLException;

import transapps.ballistic.app.BLog;
import transapps.ballistic.db.BallisticDBHelper;
import transapps.ballistic.lib.data.Atmosphere;


public class AtmosphereDVO extends Atmosphere {
	private static final String TAG = "AtmosphereDVO";
	public static final AtmosphereDVO STANDARD = new AtmosphereDVO("Standard Atmosphere Settings",
			DEFAULT_ALTITUDE, DEFAULT_BAROMETER, DEFAULT_TEMPERATURE, DEFAULT_HUMIDITY);

	public static final class Content {
		public static final String TABLE_NAME = "atmosphere";

		public static final String _ID = "_atmo_id";
		public static final String NAME = "atmo_name";
		public static final String ALTITUDE = "atmo_altitude";
		public static final String BAROMETER = "atmo_barometer";
		public static final String TEMPERATURE = "atmo_temperature";
		public static final String HUMIDITY = "atmo_humidity";

		public static final String[] PROJECTION = {
			_ID,
			NAME,
			ALTITUDE,
			BAROMETER,
			TEMPERATURE,
			HUMIDITY
		};

		public static final String CREATE_SQL =
				"CREATE TABLE "+TABLE_NAME+" ("
						+ Content._ID + " INTEGER PRIMARY KEY, "
						+ Content.NAME + " VARCHAR(255), "
						+ Content.ALTITUDE + " REAL, "
						+ Content.BAROMETER + " REAL, "
						+ Content.TEMPERATURE + " REAL, "
						+ Content.HUMIDITY + " REAL"
						+ ");";
	}

	public AtmosphereDVO(Atmosphere atmosphere) {
		this(atmosphere.id, atmosphere.name, atmosphere.altitude, atmosphere.pressure,
				atmosphere.temperature, atmosphere.humidity);
	}

	public AtmosphereDVO(String name, double altitude,
			double barometer, double temperature, double humidity) {
		this(null, name, altitude, barometer, temperature, humidity);
	}

	public AtmosphereDVO(Integer id, String name, double altitude, double barometer,
			double temperature, double humidity) {
		super(id, name, altitude, barometer, temperature, humidity);
	}

	public static AtmosphereDVO create(ResultSet cursor) {
		AtmosphereDVO ret = null;
		try {
			Integer id = cursor.getInt(Content._ID);
			String name = cursor.getString(Content.NAME);
			double altitude = cursor.getDouble(Content.ALTITUDE);
			double barometer = cursor.getDouble(Content.BAROMETER);
			double temperature = cursor.getDouble(Content.TEMPERATURE);
			double humidity = cursor.getDouble(Content.HUMIDITY);
			ret = new AtmosphereDVO(id, name, altitude, barometer, temperature, humidity);
		} catch (SQLException e) { BLog.e(TAG, "Create failure", e); }
		return ret;
	}

	@Override
	public String toString() {
		return name;
	}

	public void save() {
		BallisticDBHelper.i().insert(this);
	}

	public void update() {
		BallisticDBHelper.i().update(this);
	}

	public void delete() {
		BallisticDBHelper.i().deleteAtmosphere(id);
	}
}
