package transapps.ballistic.db.dvo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import transapps.ballistic.db.BallisticDBHelper;
import transapps.ballistic.db.Query;
import transapps.ballistic.lib.data.Atmosphere;
import transapps.ballistic.lib.data.Bullet;
import transapps.ballistic.lib.data.Weapon;


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
						+ WeaponDVO.Content._ID + " INTEGER PRIMARY KEY AUTO_INCREMENT, "
						+ WeaponDVO.Content.NAME + " VARCHAR(255), "
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

	public WeaponDVO(Weapon weapon) {
		this(weapon.id, weapon.name, weapon.description, weapon.velocity,
				weapon.sightHeight, weapon.rightTwist, weapon.barrelTwist,
				weapon.zeroRange, weapon.atmosphere, weapon.bullet);
	}

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

	public static WeaponDVO create(ResultSet cursor) {
		WeaponDVO ret = null;
		try {
			Integer id = cursor.getInt(Content._ID);
			String name = cursor.getString(Content.NAME);
			String description = cursor.getString(Content.DESCRIPTION);
			double velocity = cursor.getDouble(Content.VELOCITY);
			boolean rightTwist = cursor.getInt(Content.BARREL_RIGHT_TWIST) == 1;
			double barrelTwist = cursor.getDouble(Content.BARREL_TWIST);
			double sightHeight = cursor.getDouble(Content.SIGHTHEIGHT);
			double zeroRange = cursor.getDouble(Content.ZERORANGE);
			AtmosphereDVO atmosphere = null;
			if (cursor.getObject(Content.ALTITUDE) != null) {
				double altitude = cursor.getDouble(Content.ALTITUDE);
				double temperature = cursor.getDouble(Content.TEMPERATURE);
				double humidity = cursor.getDouble(Content.HUMIDITY);
				double barometer = cursor.getDouble(Content.BAROMETER);
				atmosphere = new AtmosphereDVO("", altitude, barometer, temperature, humidity);
			}
			int function = cursor.getInt(Content.BULLET_FUNCTION);
			double coefficient = cursor.getDouble(Content.BULLET_COEFFICIENT);
			double calibre = cursor.getDouble(Content.BULLET_CALIBRE);
			double weight = cursor.getDouble(Content.BULLET_WEIGHT);
			double length = cursor.getDouble(Content.BULLET_LENGTH);
			BulletDVO bullet = new BulletDVO("", "", function, coefficient, calibre, weight, length);
			ret = new WeaponDVO(id, name, description, velocity, sightHeight,
					rightTwist, barrelTwist, zeroRange, atmosphere, bullet);
		} catch (SQLException ex) { System.out.println(TAG + " SQL Exception: " + ex.getMessage()); }
		return ret;
	}

	/**
	 * Return an identical weapon object (with same id) but new zero settings.
	 * @param zeroRange New zero range in yards.
	 * @param atmo New atmospheric settings for zero.
	 * @return New WeaponDVO with updated zero settings.
	 */
	public WeaponDVO newZero(double zeroRange, Atmosphere atmo) {
		return new WeaponDVO(id, name, description, velocity,
			sightHeight, rightTwist, barrelTwist, zeroRange, atmo, bullet);
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
		BallisticDBHelper.i().deleteWeapon(id);
	}

	public static List<WeaponDVO> allWeapons() {
		List<WeaponDVO> ret = new ArrayList<WeaponDVO>();
		try {
			String orderBy = WeaponDVO.Content.NAME + " ASC";
			Query c = null;
			try {
				c = BallisticDBHelper.i().query(WeaponDVO.Content.TABLE_NAME, WeaponDVO.Content.PROJECTION, null, null, orderBy);
				if (c != null && c.rs != null) {
					while (c.rs.next()) {
						ret.add(WeaponDVO.create(c.rs));
					}
				}
			} finally {
				if (c != null) c.close();
			}
		} catch (Exception ex) {
			System.out.println(TAG+" Error querying list of weapons: "+ex.getMessage());
		}
		return ret;
	}

	public static WeaponDVO[] allWeaponsArray() {
		List<WeaponDVO> l = allWeapons();
		return l.toArray(new WeaponDVO[l.size()]);
	}
}
