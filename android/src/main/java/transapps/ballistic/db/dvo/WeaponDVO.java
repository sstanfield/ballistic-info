package transapps.ballistic.db.dvo;

import java.util.ArrayList;
import java.util.List;

import transapps.ballistic.BallisticDBProvider;
import transapps.ballistic.lib.data.Atmosphere;
import transapps.ballistic.lib.data.Bullet;
import transapps.ballistic.lib.data.Weapon;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;


public class WeaponDVO extends Weapon {
	private static final String TAG = "WeaponDVO";

	public static final class Content {
		public static final String TABLE_NAME = "weapon";

		public static final String _ID = "_wep_id";
		public static final String NAME = "wep_name";
		public static final String DESCRIPTION = "wep_description";
		public static final String VELOCITY = "wep_velocity";
		public static final String BULLET_COEFFICIENT = "wep_"+BulletDVO.Content.COEFFICIENT;
		public static final String BULLET_WEIGHT = "wep_"+BulletDVO.Content.WEIGHT;
		public static final String BULLET_FUNCTION = "wep_"+BulletDVO.Content.FUNCTION;
		public static final String BULLET_CALIBRE = "wep_"+BulletDVO.Content.CALIBRE;
		public static final String BULLET_LENGTH = "wep_"+BulletDVO.Content.LENGTH;
		public static final String SIGHTHEIGHT = "wep_sightHieght";
		public static final String BARREL_RIGHT_TWIST = "wep_barrel_right_twist";
		public static final String BARREL_TWIST = "wep_barrel_twist";
		public static final String ZERORANGE = "wep_zeroRange";
		public static final String ALTITUDE = "wep_"+AtmosphereDVO.Content.ALTITUDE;
		public static final String BAROMETER = "wep_"+AtmosphereDVO.Content.BAROMETER;
		public static final String TEMPERATURE = "wep_"+AtmosphereDVO.Content.TEMPERATURE;
		public static final String HUMIDITY = "wep_"+AtmosphereDVO.Content.HUMIDITY;

		public static final String[] PROJECTION = {
			_ID,
			NAME,
			DESCRIPTION,
			VELOCITY,
			BULLET_COEFFICIENT,
			BULLET_WEIGHT,
			BULLET_FUNCTION,
			BULLET_CALIBRE,
			BULLET_LENGTH,
			SIGHTHEIGHT,
			BARREL_RIGHT_TWIST,
			BARREL_TWIST,
			ZERORANGE,
			ALTITUDE,
			BAROMETER,
			TEMPERATURE,
			HUMIDITY
		};

		public static final String CREATE_SQL =
				"CREATE TABLE "+TABLE_NAME+" ("
						+ WeaponDVO.Content._ID + " INTEGER PRIMARY KEY, "
						+ WeaponDVO.Content.NAME + " TEXT, "
						+ WeaponDVO.Content.DESCRIPTION + " TEXT, "
						+ WeaponDVO.Content.VELOCITY + " REAL, "
						+ WeaponDVO.Content.BULLET_COEFFICIENT + " REAL, "
						+ WeaponDVO.Content.BULLET_WEIGHT + " REAL, "
						+ WeaponDVO.Content.BULLET_FUNCTION + " INTEGER, "
						+ WeaponDVO.Content.BULLET_CALIBRE + " REAL, "
						+ WeaponDVO.Content.BULLET_LENGTH + " REAL, "
						+ WeaponDVO.Content.SIGHTHEIGHT + " REAL, "
						+ WeaponDVO.Content.BARREL_RIGHT_TWIST + " INTEGER, "
						+ WeaponDVO.Content.BARREL_TWIST + " REAL, "
						+ WeaponDVO.Content.ZERORANGE + " REAL, "
						+ WeaponDVO.Content.ALTITUDE + " REAL, "
						+ WeaponDVO.Content.BAROMETER + " REAL, "
						+ WeaponDVO.Content.TEMPERATURE + " REAL, "
						+ WeaponDVO.Content.HUMIDITY + " REAL "
						+ ");";
	}

//	public WeaponDVO(Weapon weapon) {
//		this(weapon.id, weapon.name, weapon.description, weapon.velocity,
//				weapon.sightHeight, weapon.rightTwist, weapon.barrelTwist,
//				weapon.zeroRange, weapon.atmosphere, weapon.bullet);
//	}

	public WeaponDVO(String name, String description, double velocity, 
			double sightHeight, boolean rightTwist, double barrelTwist,
			double zeroRange, Atmosphere atmosphere, Bullet bullet) {
		this(null, name, description, velocity,
				sightHeight, rightTwist, barrelTwist, zeroRange, atmosphere, bullet);
	}

	public WeaponDVO(Integer id, String name, String description, double velocity,
			double sightHeight, boolean rightTwist, double barrelTwist,
			double zeroRange, Atmosphere atmosphere, Bullet bullet) {
		super(id, name, description, velocity,
				sightHeight, rightTwist, barrelTwist, zeroRange,
				// Make sure we have DVO instances in Weapon DVO...
				atmosphere==null?null:new AtmosphereDVO(atmosphere),
				bullet==null?null:new BulletDVO(bullet));
	}

	public static WeaponDVO create(Cursor cursor) {
		Integer id = cursor.getInt(cursor.getColumnIndex(Content._ID));
		String name = cursor.getString(cursor.getColumnIndex(Content.NAME));
		String description = cursor.getString(cursor.getColumnIndex(Content.DESCRIPTION));
		double velocity = cursor.getDouble(cursor.getColumnIndex(Content.VELOCITY));
		boolean rightTwist = cursor.getInt(cursor.getColumnIndex(Content.BARREL_RIGHT_TWIST)) == 1;
		double barrelTwist = cursor.getDouble(cursor.getColumnIndex(Content.BARREL_TWIST));
		double sightHeight = cursor.getDouble(cursor.getColumnIndex(Content.SIGHTHEIGHT));
		double zeroRange = cursor.getDouble(cursor.getColumnIndex(Content.ZERORANGE));
		int altCol = cursor.getColumnIndex(Content.ALTITUDE);
		AtmosphereDVO atmosphere = null;
		if (!cursor.isNull(altCol)) {
			double altitude = cursor.getDouble(altCol);
			double temperature = cursor.getDouble(cursor.getColumnIndex(Content.TEMPERATURE));
			double humidity = cursor.getDouble(cursor.getColumnIndex(Content.HUMIDITY));
			double barometer = cursor.getDouble(cursor.getColumnIndex(Content.BAROMETER));
			atmosphere = new AtmosphereDVO("", altitude, barometer, temperature, humidity);
		}
		int function = cursor.getInt(cursor.getColumnIndex(Content.BULLET_FUNCTION));
		double coefficient = cursor.getDouble(cursor.getColumnIndex(Content.BULLET_COEFFICIENT));
		double calibre = cursor.getDouble(cursor.getColumnIndex(Content.BULLET_CALIBRE));
		double weight = cursor.getDouble(cursor.getColumnIndex(Content.BULLET_WEIGHT));
		double length = cursor.getDouble(cursor.getColumnIndex(Content.BULLET_LENGTH));
		BulletDVO bullet = new BulletDVO("", "", function, coefficient, calibre, weight, length);
		return new WeaponDVO(id, name, description, velocity, sightHeight,
				rightTwist, barrelTwist, zeroRange, atmosphere, bullet);
	}

	public static WeaponDVO create(ContentValues cv) {
		Integer id = cv.getAsInteger(Content._ID);
		String name = cv.getAsString(Content.NAME);
		String description = cv.getAsString(Content.DESCRIPTION);
		double velocity = cv.getAsDouble(Content.VELOCITY);
		double sightHeight = cv.getAsDouble(Content.SIGHTHEIGHT);
		boolean rightTwist = cv.getAsBoolean(Content.BARREL_RIGHT_TWIST);
		double barrelTwist = cv.getAsDouble(Content.BARREL_TWIST);
		double zeroRange = cv.getAsDouble(Content.ZERORANGE);
		AtmosphereDVO atmosphere = null;
		if (cv.containsKey(AtmosphereDVO.Content.ALTITUDE)) {
			atmosphere = AtmosphereDVO.create(cv);
		}
		BulletDVO bullet = BulletDVO.create(cv);
		return new WeaponDVO(id, name, description, velocity, sightHeight,
				rightTwist, barrelTwist, zeroRange, atmosphere, bullet);
	}

	/**
	 * Return an identical weapon object (with same id) but new zero settings.
	 * @param zeroRange New zero range
	 * @param atmo New zero atmosphere
	 * @return New WeaponDVO with new zero parameters
	 */
	public WeaponDVO newZero(double zeroRange, Atmosphere atmo) {
		return new WeaponDVO(id, name, description, velocity,
			sightHeight, rightTwist, barrelTwist, zeroRange, atmo, bullet);
	}

	@Override
	public String toString() {
		return name;
	}

	public ContentValues toContentValues(ContentValues cv) {
		if (id != null) cv.put(Content._ID, id);
		cv.put(Content.NAME, name);
		cv.put(Content.DESCRIPTION, description);
		cv.put(Content.VELOCITY, velocity);
		cv.put(Content.SIGHTHEIGHT, sightHeight);
		cv.put(Content.BARREL_RIGHT_TWIST, rightTwist);
		cv.put(Content.BARREL_TWIST, barrelTwist);
		cv.put(Content.ZERORANGE, zeroRange);
		if (atmosphere != null) ((AtmosphereDVO)atmosphere).toContentValues(cv);
		((BulletDVO)bullet).toContentValues(cv);
		return cv;
	}

	public ContentValues toContentValues() {
		ContentValues ret = new ContentValues();
		return toContentValues(ret);
	}

	public void save(Context context) {
		context.getContentResolver().insert(
				BallisticDBProvider.BASE_URI.buildUpon().appendPath(WeaponDVO.Content.TABLE_NAME).build(),
				toContentValues());
	}

	public void update(Context context) {
		context.getContentResolver().update(
				BallisticDBProvider.BASE_URI.buildUpon().appendPath(WeaponDVO.Content.TABLE_NAME).build(),
				toContentValues(), null, null);
	}

	public void delete(Context context) {
		context.getContentResolver().delete(
				BallisticDBProvider.BASE_URI.buildUpon().appendPath(WeaponDVO.Content.TABLE_NAME).build(),
				"id=?", new String[] {""+id});
	}

	public static List<WeaponDVO> allWeapons(Context context) {
		List<WeaponDVO> ret = new ArrayList<WeaponDVO>();
		try {
			String orderBy = WeaponDVO.Content.NAME + " ASC";
			Cursor c = null;
			try {
				c = context.getContentResolver().query(
						BallisticDBProvider.BASE_URI.buildUpon().appendPath(WeaponDVO.Content.TABLE_NAME).build(),
						WeaponDVO.Content.PROJECTION, null, null, orderBy);
				while (c.moveToNext()) {
					ret.add(WeaponDVO.create(c));
				}
			} finally {
				if (c != null) c.close();
			}
		} catch (Exception ex) {
			Log.e(TAG, "Error quering list of weapons", ex);
		}
		return ret;
	}
}
