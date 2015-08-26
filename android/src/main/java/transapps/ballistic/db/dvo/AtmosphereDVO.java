package transapps.ballistic.db.dvo;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import transapps.ballistic.BallisticDBProvider;
import transapps.ballistic.lib.data.Atmosphere;


public class AtmosphereDVO extends Atmosphere {
//	private static final String TAG = "AtmosphereDVO";
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
						+ Content.NAME + " TEXT, "
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

	public static AtmosphereDVO create(Cursor cursor) {
		Integer id = cursor.getInt(cursor.getColumnIndex(Content._ID));
		String name = cursor.getString(cursor.getColumnIndex(Content.NAME));
		double altitude = cursor.getDouble(cursor.getColumnIndex(Content.ALTITUDE));
		double barometer = cursor.getDouble(cursor.getColumnIndex(Content.BAROMETER));
		double temperature = cursor.getDouble(cursor.getColumnIndex(Content.TEMPERATURE));
		double humidity = cursor.getDouble(cursor.getColumnIndex(Content.HUMIDITY));
		return new AtmosphereDVO(id, name, altitude, barometer, temperature, humidity);
	}

	public static AtmosphereDVO create(ContentValues cv) {
		Integer id = cv.getAsInteger(Content._ID);
		String name = cv.getAsString(Content.NAME);
		double altitude = cv.getAsDouble(Content.ALTITUDE);
		double barometer = cv.getAsDouble(Content.BAROMETER);
		double temperature = cv.getAsDouble(Content.TEMPERATURE);
		double humidity = cv.getAsDouble(Content.HUMIDITY);
		return new AtmosphereDVO(id, name, altitude, barometer, temperature, humidity);
	}

	public static AtmosphereDVO create(SharedPreferences pref) {
		double altitude = pref.getFloat(Content.ALTITUDE, DEFAULT_ALTITUDE);
		double barometer = pref.getFloat(Content.BAROMETER, DEFAULT_BAROMETER);
		double temperature = pref.getFloat(Content.TEMPERATURE, DEFAULT_TEMPERATURE);
		double humidity = pref.getFloat(Content.HUMIDITY, DEFAULT_HUMIDITY);
		return new AtmosphereDVO(null, null, altitude, barometer, temperature, humidity);
	}

	@Override
	public String toString() {
		return name;
	}

	public void saveToPrefs(SharedPreferences.Editor e) {
		e.putFloat(Content.ALTITUDE, (float)altitude);
		e.putFloat(Content.BAROMETER, (float) pressure);
		e.putFloat(Content.TEMPERATURE, (float) temperature);
		e.putFloat(Content.HUMIDITY, (float) humidity);
	}

	public ContentValues toContentValues(ContentValues cv) {
		if (id != null) cv.put(Content._ID, id);
		cv.put(Content.NAME, name);
		cv.put(Content.ALTITUDE, altitude);
		cv.put(Content.BAROMETER, pressure);
		cv.put(Content.TEMPERATURE, temperature);
		cv.put(Content.HUMIDITY, humidity);
		return cv;
	}

	public ContentValues toContentValues() {
		ContentValues ret = new ContentValues();
		return toContentValues(ret);
	}

	public void save(Context context) {
		context.getContentResolver().insert(
				BallisticDBProvider.BASE_URI.buildUpon().appendPath(Content.TABLE_NAME).build(),
				toContentValues());
	}

	public void update(Context context) {
		context.getContentResolver().update(
				BallisticDBProvider.BASE_URI.buildUpon().appendPath(Content.TABLE_NAME).build(),
				toContentValues(), null, null);
	}

	public void delete(Context context) {
		context.getContentResolver().delete(
				BallisticDBProvider.BASE_URI.buildUpon().appendPath(Content.TABLE_NAME).build(),
				"id=?", new String[] {""+id});
	}
}
