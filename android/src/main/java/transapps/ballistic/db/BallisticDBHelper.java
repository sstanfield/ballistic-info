package transapps.ballistic.db;

import transapps.ballistic.db.dvo.AtmosphereDVO;
import transapps.ballistic.db.dvo.BulletDVO;
import transapps.ballistic.db.dvo.WeaponDVO;
import transapps.ballistic.lib.Ballistics;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;


public class BallisticDBHelper extends SQLiteOpenHelper {
	private static final String TAG = "BallisticDBHelper";

	private static final int DATABASE_VERSION = 3;

	private SQLiteStatement insertWeapon;
	private SQLiteStatement updateWeapon;
	private SQLiteStatement deleteWeapon;
	private SQLiteStatement insertAtmo;
	private SQLiteStatement updateAtmo;
	private SQLiteStatement deleteAtmo;

	private static final BulletDVO m855 = new BulletDVO("M855", "M855, 5.56", Ballistics.G7, .151, .224, 62, .906);
	private static final BulletDVO m118 = new BulletDVO("M118", "M118, , Sierra MK 175", Ballistics.G7, .243, .308, 175, 1.24);
	private static final BulletDVO m33 = new BulletDVO("M33 Ball", "M33 Ball, .50 cal", Ballistics.G7, .340, .51, 650, 2.28);
	private static final BulletDVO smk190 = new BulletDVO("Sierra MK 190", "Sierra MatchKing 190gr 308", Ballistics.G7, .268, .308, 190, 1.353);
	private static final BulletDVO smk175 = new BulletDVO("Sierra MK 175", "Sierra MatchKing 175gr 308", Ballistics.G7, .243, .308, 175, 1.24);
	private static final BulletDVO smk168 = new BulletDVO("Sierra MK 168", "Sierra MatchKing 168gr 308", Ballistics.G7, .218, .308, 168, 1.215);
	private static final BulletDVO mk262 = new BulletDVO("Mk 262", "Mk 262, Sierra MK 77, 5.56", Ballistics.G7, .190, .224, 77, .994);
	private static final BulletDVO mk248mod0 = new BulletDVO("Mk248mod0", "Mk248mod0, Sierra MK 190", Ballistics.G7, .268, .308, 190, 1.353);
	private static final BulletDVO mk248mod1 = new BulletDVO("Mk248mod1", "Mk248mod1, Sierra MK 220", Ballistics.G7, .310, .308, 220, 1.489);
	private static final BulletDVO m118lr = new BulletDVO("M118 LR", "M118 Long Range, Sierra MK 175", Ballistics.G7, .243, .308, 175, 1.24);
	// M193 length .76, M196 trace length .91, M856 tracer length 1.15- google "m855 bullet dimensions"- image
	// m855a1 "about" 1/8inch longer then m855

	public static final BulletDVO[] defaultBullets = {
			m855,
			mk262,
			m118,
			smk168,
			smk175,
			smk190,
			m33,
			mk248mod0,
			mk248mod1,
	};

	// 328.084 yards = 300 meters.
	public static final WeaponDVO[] defaultWeapons = {
		new WeaponDVO("M4, M855, ACOG", "", 2902, 3.755, true, 7, 328.084, AtmosphereDVO.STANDARD, defaultBullets[0]),
//		new Weapon("AR15 Carbine, M855, ACOG", "AR15 Carbine, 16\" barrel", 2984, .151, 62, Ballistics.G7, 3.755, 328.084, Atmosphere.STANDARD),
		new WeaponDVO("M16, M855, ACOG", "", 3112, 3.755, true, 7, 328.084, AtmosphereDVO.STANDARD, m855),
		new WeaponDVO("M24, M4 Leopold, M118LR", "", 2700, 1.7, true, 11.25, 328.084, AtmosphereDVO.STANDARD, m118lr),
		new WeaponDVO("M82A2, M33", "M82A2, Barret Light 50", 2750, 2.5, true, 15, 328.084, AtmosphereDVO.STANDARD, m33),
		new WeaponDVO("XM2010, mk248mod0", "", 2975, 2.5, true, 10, 328.084, AtmosphereDVO.STANDARD, mk248mod0), // XXX verify
		new WeaponDVO("M110, M118LR", "", 2580, 2.0, true, 11.25, 328.084, AtmosphereDVO.STANDARD, m118lr), // XXX Verify
	};

	public static boolean isDefaultWeapon(WeaponDVO w) {
		boolean ret = false;
		if (w.id != null && w.id <= defaultWeapons.length) ret = true;
		return ret;
	}

	public BallisticDBHelper(Context context) {
		super(context, "ballistics.db", null, DATABASE_VERSION);
	}

	@Override
	public synchronized void onCreate(SQLiteDatabase db) {
		db.execSQL("DROP INDEX IF EXISTS index_name");
		db.execSQL("DROP TABLE IF EXISTS "+WeaponDVO.Content.TABLE_NAME);
		db.execSQL(WeaponDVO.Content.CREATE_SQL);
		db.execSQL("CREATE INDEX index_name ON "+WeaponDVO.Content.TABLE_NAME+" ("+WeaponDVO.Content.NAME+")");
		db.execSQL("DROP TABLE IF EXISTS "+AtmosphereDVO.Content.TABLE_NAME);
		db.execSQL(AtmosphereDVO.Content.CREATE_SQL);

		db.execSQL("DROP TABLE IF EXISTS "+BulletDVO.Content.TABLE_NAME);
		db.execSQL(BulletDVO.Content.CREATE_SQL);

		for (WeaponDVO w : defaultWeapons) {
			insert(db, w);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (newVersion > oldVersion) onCreate(db);
	}

	public synchronized void close() {
		if (insertWeapon != null) insertWeapon.close();
		insertWeapon = null;
		if (updateWeapon != null) updateWeapon.close();
		updateWeapon = null;
		if (deleteWeapon != null) deleteWeapon.close();
		deleteWeapon = null;
		if (insertAtmo != null) insertAtmo.close();
		insertAtmo = null;
		if (updateAtmo != null) updateAtmo.close();
		updateAtmo = null;
		if (deleteAtmo != null) deleteAtmo.close();
		deleteAtmo = null;
	}

	public synchronized Cursor query(String table, String[] projection, String selection,
			String[] selectionArgs, String groupBy, String having, String sortOrder) {
		Cursor c;
		SQLiteDatabase db = getReadableDatabase();
		c = db.query(table, projection, 
				selection, selectionArgs, groupBy, having, sortOrder);
		return c;
	}

	public synchronized long insert(SQLiteDatabase db, WeaponDVO w) {
		if (insertWeapon == null) {
			insertWeapon = db.compileStatement("INSERT INTO "+
				WeaponDVO.Content.TABLE_NAME+ " ("+
				WeaponDVO.Content.BULLET_WEIGHT + ", "+
				WeaponDVO.Content.BULLET_COEFFICIENT + ", "+
				WeaponDVO.Content.DESCRIPTION + ", "+
				WeaponDVO.Content.BULLET_FUNCTION + ", "+
				WeaponDVO.Content.NAME + ", "+
				WeaponDVO.Content.SIGHTHEIGHT + ", "+
				WeaponDVO.Content.VELOCITY + ", "+
				WeaponDVO.Content.ZERORANGE + ", "+
				WeaponDVO.Content.ALTITUDE + ", "+
				WeaponDVO.Content.BAROMETER + ", "+
				WeaponDVO.Content.TEMPERATURE + ", "+
				WeaponDVO.Content.HUMIDITY + ", "+
				WeaponDVO.Content.BARREL_TWIST + ", "+
				WeaponDVO.Content.BULLET_CALIBRE + ", "+
				WeaponDVO.Content.BULLET_LENGTH + ", "+
				WeaponDVO.Content.BARREL_RIGHT_TWIST +
				") VALUES ("+
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
				);
		}
		long rowId;
		insertWeapon.bindDouble(1, w.bullet.weight);
		insertWeapon.bindDouble(2, w.bullet.coefficient);
		insertWeapon.bindString(3, w.description);
		insertWeapon.bindLong(4, w.bullet.function);
		insertWeapon.bindString(5, w.name);
		insertWeapon.bindDouble(6, w.sightHeight);
		insertWeapon.bindDouble(7, w.velocity);
		insertWeapon.bindDouble(8, w.zeroRange);
		if (w.atmosphere != null) {
			insertWeapon.bindDouble(9, w.atmosphere.altitude);
			insertWeapon.bindDouble(10, w.atmosphere.pressure);
			insertWeapon.bindDouble(11, w.atmosphere.temperature);
			insertWeapon.bindDouble(12, w.atmosphere.humidity);
		} else {
			insertWeapon.bindNull(9);
			insertWeapon.bindNull(10);
			insertWeapon.bindNull(11);
			insertWeapon.bindNull(12);
		}
		insertWeapon.bindDouble(13, w.barrelTwist);
		insertWeapon.bindDouble(14, w.bullet.calibre);
		insertWeapon.bindDouble(15, w.bullet.length);
		insertWeapon.bindLong(16, w.rightTwist?1:0);
		rowId = insertWeapon.executeInsert();
		if (rowId < 0) {
			Log.e(TAG, "Failed to insert weapon row: "+w.name);
		}
		return rowId;
	}

	public synchronized long insert(WeaponDVO w) {
		return insert(getWritableDatabase(), w);
	}

	public synchronized void update(WeaponDVO w) {
		if (w.id == null) {
			Log.e(TAG, "No id when trying to update weapon: "+w.name);
			return;
		}
		if (updateWeapon == null) {
			updateWeapon = getWritableDatabase().compileStatement("UPDATE "+
					WeaponDVO.Content.TABLE_NAME+ " SET "+
					WeaponDVO.Content.BULLET_WEIGHT + "=?, "+
					WeaponDVO.Content.BULLET_COEFFICIENT + "=?, "+
					WeaponDVO.Content.DESCRIPTION + "=?, "+
					WeaponDVO.Content.BULLET_FUNCTION + "=?, "+
					WeaponDVO.Content.NAME + "=?, "+
					WeaponDVO.Content.SIGHTHEIGHT + "=?, "+
					WeaponDVO.Content.VELOCITY + "=?, "+
					WeaponDVO.Content.ZERORANGE + "=?, "+
					WeaponDVO.Content.ALTITUDE + "=?, "+
					WeaponDVO.Content.BAROMETER + "=?, "+
					WeaponDVO.Content.TEMPERATURE + "=?, "+
					WeaponDVO.Content.HUMIDITY + "=?, "+
					WeaponDVO.Content.BARREL_TWIST + "=?, "+
					WeaponDVO.Content.BULLET_CALIBRE + "=?, "+
					WeaponDVO.Content.BULLET_LENGTH + "=?, "+
					WeaponDVO.Content.BARREL_RIGHT_TWIST + "=? "+
					"WHERE "+WeaponDVO.Content._ID+" = ?"
					);
		}
		updateWeapon.bindDouble(1, w.bullet.weight);
		updateWeapon.bindDouble(2, w.bullet.coefficient);
		updateWeapon.bindString(3, w.description);
		updateWeapon.bindLong(4, w.bullet.function);
		updateWeapon.bindString(5, w.name);
		updateWeapon.bindDouble(6, w.sightHeight);
		updateWeapon.bindDouble(7, w.velocity);
		updateWeapon.bindDouble(8, w.zeroRange);
		if (w.atmosphere != null) {
			updateWeapon.bindDouble(9, w.atmosphere.altitude);
			updateWeapon.bindDouble(10, w.atmosphere.pressure);
			updateWeapon.bindDouble(11, w.atmosphere.temperature);
			updateWeapon.bindDouble(12, w.atmosphere.humidity);
		} else {
			updateWeapon.bindNull(9);
			updateWeapon.bindNull(10);
			updateWeapon.bindNull(11);
			updateWeapon.bindNull(12);
		}
		updateWeapon.bindDouble(13, w.barrelTwist);
		updateWeapon.bindDouble(14, w.bullet.calibre);
		updateWeapon.bindDouble(15, w.bullet.length);
		updateWeapon.bindLong(16, w.rightTwist?1:0);
		updateWeapon.bindLong(17, w.id);
		updateWeapon.execute();
	}

	public synchronized void deleteWeapon(int id) {
		if (deleteWeapon == null) {
			deleteWeapon = getWritableDatabase().compileStatement("DELETE FROM "+
				WeaponDVO.Content.TABLE_NAME+ " WHERE "+WeaponDVO.Content._ID+" = ?");
		}
		deleteWeapon.bindLong(1, id);
		deleteWeapon.execute();
	}

//	public synchronized long insert(SQLiteDatabase db, AtmosphereDVO a) {
//		if (insertAtmo == null) {
//			insertAtmo = db.compileStatement("INSERT INTO "+
//				AtmosphereDVO.Content.TABLE_NAME+ " ("+
//				AtmosphereDVO.Content.ALTITUDE + ", "+
//				AtmosphereDVO.Content.BAROMETER + ", "+
//				AtmosphereDVO.Content.HUMIDITY + ", "+
//				AtmosphereDVO.Content.TEMPERATURE + ", "+
//				AtmosphereDVO.Content.NAME +
//				") VALUES ("+
//				"?, ?, ?, ?, ?)"
//				);
//		}
//		long rowId;
//		insertAtmo.bindDouble(1, a.altitude);
//		insertAtmo.bindDouble(2, a.pressure);
//		insertAtmo.bindDouble(3, a.humidity);
//		insertAtmo.bindDouble(4, a.temperature);
//		insertAtmo.bindString(5, a.name);
//		rowId = insertAtmo.executeInsert();
//		if (rowId < 0) {
//			Log.e(TAG, "Failed to insert Atmosphere row: "+a.name);
//		}
//		return rowId;
//	}

//	public synchronized long insert(AtmosphereDVO a) {
//		return insert(getWritableDatabase(), a);
//	}

	public synchronized void update(AtmosphereDVO a) {
		if (a.id == null) {
			Log.e(TAG, "No id when trying to update atmosphere: "+a.name);
			return;
		}
		if (updateAtmo == null) {
			updateAtmo = getWritableDatabase().compileStatement("UPDATE "+
					AtmosphereDVO.Content.TABLE_NAME+ " SET "+
					AtmosphereDVO.Content.ALTITUDE + "=?, "+
					AtmosphereDVO.Content.BAROMETER + "=?, "+
					AtmosphereDVO.Content.HUMIDITY + "=?, "+
					AtmosphereDVO.Content.TEMPERATURE + "=?, "+
					AtmosphereDVO.Content.NAME + "=? "+
					"WHERE "+AtmosphereDVO.Content._ID+" = ?"
					);
		}
		updateAtmo.bindDouble(1, a.altitude);
		updateAtmo.bindDouble(2, a.pressure);
		updateAtmo.bindDouble(3, a.humidity);
		updateAtmo.bindDouble(4, a.temperature);
		updateAtmo.bindString(5, a.name);
		updateAtmo.bindLong(6, a.id);
		updateAtmo.execute();
	}

//	public synchronized void deleteAtmosphere(int id) {
//		if (deleteAtmo == null) {
//			deleteAtmo = getWritableDatabase().compileStatement("DELETE FROM "+
//				AtmosphereDVO.Content.TABLE_NAME+ " WHERE "+AtmosphereDVO.Content._ID+" = ?");
//		}
//		deleteAtmo.bindLong(1, id);
//		deleteAtmo.execute();
//	}
}
