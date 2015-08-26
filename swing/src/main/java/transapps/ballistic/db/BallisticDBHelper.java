package transapps.ballistic.db;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import transapps.ballistic.app.BLog;
import transapps.ballistic.db.dvo.AtmosphereDVO;
import transapps.ballistic.db.dvo.BulletDVO;
import transapps.ballistic.db.dvo.WeaponDVO;
import transapps.ballistic.lib.Ballistics;
import transapps.ballistic.lib.data.Weapon;


public class BallisticDBHelper {
	private static final String TAG = "BallisticDBHelper";

	private static BallisticDBHelper instance;
	
	public static final String CREATE_META_SQL =
			"CREATE TABLE meta ("
					+ "_id INTEGER PRIMARY KEY AUTO_INCREMENT, "
					+ " version INTEGER "
					+ ");";

	private static final int DATABASE_VERSION = 1;

	private Connection db;
	private PreparedStatement insertWeapon;
	private PreparedStatement updateWeapon;
	private PreparedStatement deleteWeapon;
	private PreparedStatement insertAtmo;
	private PreparedStatement updateAtmo;
	private PreparedStatement deleteAtmo;

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

	public static boolean isDefaultWeapon(Weapon w) {
		boolean ret = false;
		if (w.id != null && w.id <= defaultWeapons.length) ret = true;
		return ret;
	}

	private int getVersion() {
		int ret = -1;
		ResultSet rs = null;
		boolean hasMeta = false;
		try {
			rs = db.getMetaData().getTables(null, null/*"public"*/, "%" ,new String[] {"TABLE"} );
			while (rs.next()) {
				String name = rs.getString("TABLE_NAME");
				if (name != null && name.toLowerCase().equals("meta")) {
					hasMeta = true;
				}
			}
		} catch (SQLException e) {
			BLog.e(TAG, "SQL Error", e);
			e.printStackTrace();
		} finally {
			if (rs != null) try { rs.close(); } catch (SQLException ex) { BLog.e(TAG, "SQL Error", ex); }
		}
		if (hasMeta) {
			Query q = query("meta", null, null, null, null);
			if (q != null) {
				try {
					if (q.rs.last()) {
						ret = q.rs.getInt("version");
					}
				} catch (SQLException ex) { BLog.e(TAG, "SQL Error", ex); }
				q.close();
			}
		}
		return ret;
	}

	private BallisticDBHelper() {
//		super(context, "ballistics.db", null, DATABASE_VERSION);
		try {
			Class.forName("org.h2.Driver");
			db = DriverManager.getConnection("jdbc:h2:ballisticinfo");
			int ver = getVersion();
			if (ver != DATABASE_VERSION) create();
		} catch (ClassNotFoundException e) {
			BLog.e(TAG, "Database driver error", e);
		} catch (SQLException e) {
			BLog.e(TAG, "Database driver SQL error", e);
		}
	}

	public static BallisticDBHelper i() {
		if (instance == null) {
			instance = new BallisticDBHelper();
		}
		return instance;
	}

	private void execSQL(String sql) {
		Statement s = null;
		try {
			s = db.createStatement();
			s.execute(sql);
		} catch (SQLException ex) {
			BLog.e(TAG, "SQL Exec Error", ex);
		} finally {
			try { if (s != null) s.close(); } catch (SQLException ex) { BLog.e(TAG, "SQL Close Error", ex); }
		}
	}

	public synchronized void create() {
		execSQL("DROP TABLE IF EXISTS meta");
		execSQL(CREATE_META_SQL);
		PreparedStatement ver = null;
		try {
			ver = db.prepareStatement("INSERT INTO meta (version) VALUES (?)");
			ver.setInt(1, DATABASE_VERSION);
			ver.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (ver != null) try { ver.close(); } catch (SQLException ex) { BLog.e(TAG, "SQL Create Error", ex); }
		}
		execSQL("DROP INDEX IF EXISTS index_name");
		execSQL("DROP TABLE IF EXISTS "+WeaponDVO.Content.TABLE_NAME);
		execSQL(WeaponDVO.Content.CREATE_SQL);
		execSQL("CREATE INDEX index_name ON "+WeaponDVO.Content.TABLE_NAME+" ("+WeaponDVO.Content.NAME+")");
		execSQL("DROP TABLE IF EXISTS "+AtmosphereDVO.Content.TABLE_NAME);
		execSQL(AtmosphereDVO.Content.CREATE_SQL);

		execSQL("DROP TABLE IF EXISTS "+BulletDVO.Content.TABLE_NAME);
		execSQL(BulletDVO.Content.CREATE_SQL);

		for (WeaponDVO w : defaultWeapons) {
			insert(w);
		}
	}

	private void closeStatement(Statement s) {
		try {
			s.close();
		} catch (SQLException ex) { BLog.e(TAG, "SQL Close Statement Error", ex); }
	}

	public synchronized void close() {
		if (insertWeapon != null) closeStatement(insertWeapon);
		insertWeapon = null;
		if (updateWeapon != null) closeStatement(updateWeapon);
		updateWeapon = null;
		if (deleteWeapon != null) closeStatement(deleteWeapon);
		deleteWeapon = null;
		if (insertAtmo != null) closeStatement(insertAtmo);
		insertAtmo = null;
		if (updateAtmo != null) closeStatement(updateAtmo);
		updateAtmo = null;
		if (deleteAtmo != null) closeStatement(deleteAtmo);
		deleteAtmo = null;
		try {
			if (db != null) db.close();
			db = null;
		} catch (SQLException e) { BLog.e(TAG, "SQL CLOSE Error", e); }
	}

	public synchronized Query query(String table, String[] projection, String selection,
			String[] selectionArgs, String orderBy) {
		Query ret = null;
		String sql = "SELECT ";
		if (projection != null && projection.length > 0) {
			boolean first = true;
			for (String s: projection) {
				if (first) sql += s;
				else sql += ", "+s;
				first = false;
			}
		} else {
			sql += "*";
		}
		sql += " FROM " + table + (selection==null?"":" WHERE " + selection);
		sql += (orderBy==null?"":" ORDER BY "+orderBy);
		PreparedStatement ps;
		try {
			ps = db.prepareStatement(sql);
			if (selectionArgs != null) {
				int c = 1;
				for (String s: selectionArgs) {
					ps.setObject(c++, s);
				}
			}
			ret = new Query(ps, ps.executeQuery());
		} catch (SQLException ex) {
			BLog.e(TAG, "SQL Query Error", ex);
		}
		return ret;
	}

	public synchronized void insert(WeaponDVO w) {
		try {
			if (insertWeapon == null) {
				insertWeapon = db.prepareStatement("INSERT INTO "+
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
			insertWeapon.setDouble(1, w.bullet.weight);
			insertWeapon.setDouble(2, w.bullet.coefficient);
			insertWeapon.setString(3, w.description);
			insertWeapon.setLong(4, w.bullet.function);
			insertWeapon.setString(5, w.name);
			insertWeapon.setDouble(6, w.sightHeight);
			insertWeapon.setDouble(7, w.velocity);
			insertWeapon.setDouble(8, w.zeroRange);
			if (w.atmosphere != null) {
				insertWeapon.setDouble(9, w.atmosphere.altitude);
				insertWeapon.setDouble(10, w.atmosphere.pressure);
				insertWeapon.setDouble(11, w.atmosphere.temperature);
				insertWeapon.setDouble(12, w.atmosphere.humidity);
			} else {
				insertWeapon.setNull(9, Types.DOUBLE);
				insertWeapon.setNull(10, Types.DOUBLE);
				insertWeapon.setNull(11, Types.DOUBLE);
				insertWeapon.setNull(12, Types.DOUBLE);
			}
			insertWeapon.setDouble(13, w.barrelTwist);
			insertWeapon.setDouble(14, w.bullet.calibre);
			insertWeapon.setDouble(15, w.bullet.length);
			insertWeapon.setLong(16, w.rightTwist?1:0);
			insertWeapon.execute();
		} catch (SQLException ex) {
			BLog.e(TAG, "SQL Inert Error", ex);
		}
	}

	public synchronized void update(WeaponDVO w) {
		if (w.id == null) {
//			Log.e(TAG, "No id when trying to update weapon: "+w.name);
			return;
		}
		try {
			if (updateWeapon == null) {
				updateWeapon = db.prepareStatement("UPDATE "+
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
			updateWeapon.setDouble(1, w.bullet.weight);
			updateWeapon.setDouble(2, w.bullet.coefficient);
			updateWeapon.setString(3, w.description);
			updateWeapon.setLong(4, w.bullet.function);
			updateWeapon.setString(5, w.name);
			updateWeapon.setDouble(6, w.sightHeight);
			updateWeapon.setDouble(7, w.velocity);
			updateWeapon.setDouble(8, w.zeroRange);
			if (w.atmosphere != null) {
				updateWeapon.setDouble(9, w.atmosphere.altitude);
				updateWeapon.setDouble(10, w.atmosphere.pressure);
				updateWeapon.setDouble(11, w.atmosphere.temperature);
				updateWeapon.setDouble(12, w.atmosphere.humidity);
			} else {
				updateWeapon.setNull(9, Types.DOUBLE);
				updateWeapon.setNull(10, Types.DOUBLE);
				updateWeapon.setNull(11, Types.DOUBLE);
				updateWeapon.setNull(12, Types.DOUBLE);
			}
			updateWeapon.setDouble(13, w.barrelTwist);
			updateWeapon.setDouble(14, w.bullet.calibre);
			updateWeapon.setDouble(15, w.bullet.length);
			updateWeapon.setLong(16, w.rightTwist?1:0);
			updateWeapon.setLong(17, w.id);
			updateWeapon.execute();
		} catch (SQLException ex) { BLog.e(TAG, "SQL update weapon error", ex);}
	}

	public synchronized void deleteWeapon(int id) {
		try {
			if (deleteWeapon == null) {
				deleteWeapon = db.prepareStatement("DELETE FROM "+
						WeaponDVO.Content.TABLE_NAME+ " WHERE "+WeaponDVO.Content._ID+" = ?");
			}
			deleteWeapon.setLong(1, id);
			deleteWeapon.execute();
		} catch (SQLException ex) { BLog.e(TAG, "SQL delete weapon error", ex); }
	}

	public synchronized long insert(AtmosphereDVO a) {
		long rowId = 0;
		try {
			if (insertAtmo == null) {
				insertAtmo = db.prepareStatement("INSERT INTO "+
						AtmosphereDVO.Content.TABLE_NAME+ " ("+
						AtmosphereDVO.Content.ALTITUDE + ", "+
						AtmosphereDVO.Content.BAROMETER + ", "+
						AtmosphereDVO.Content.HUMIDITY + ", "+
						AtmosphereDVO.Content.TEMPERATURE + ", "+
						AtmosphereDVO.Content.NAME +
						") VALUES ("+
						"?, ?, ?, ?, ?)"
						);
			}
			insertAtmo.setDouble(1, a.altitude);
			insertAtmo.setDouble(2, a.pressure);
			insertAtmo.setDouble(3, a.humidity);
			insertAtmo.setDouble(4, a.temperature);
			insertAtmo.setString(5, a.name);
			insertAtmo.execute();
		} catch (SQLException ex) { BLog.e(TAG, "SQL insert atmosphere error", ex);}
		return rowId;
	}

	public synchronized void update(AtmosphereDVO a) {
		if (a.id == null) {
			String n = a.name ==null?"":a.name;
			BLog.e(TAG, "Trying to update atmosphere with no id: "+n);
			return;
		}
		try {
			if (updateAtmo == null) {
				updateAtmo = db.prepareStatement("UPDATE "+
						AtmosphereDVO.Content.TABLE_NAME+ " SET "+
						AtmosphereDVO.Content.ALTITUDE + "=?, "+
						AtmosphereDVO.Content.BAROMETER + "=?, "+
						AtmosphereDVO.Content.HUMIDITY + "=?, "+
						AtmosphereDVO.Content.TEMPERATURE + "=?, "+
						AtmosphereDVO.Content.NAME + "=? "+
						"WHERE "+AtmosphereDVO.Content._ID+" = ?"
						);
			}
			updateAtmo.setDouble(1, a.altitude);
			updateAtmo.setDouble(2, a.pressure);
			updateAtmo.setDouble(3, a.humidity);
			updateAtmo.setDouble(4, a.temperature);
			updateAtmo.setString(5, a.name);
			updateAtmo.setLong(6, a.id);
			updateAtmo.execute();
		} catch (SQLException ex) { BLog.e(TAG, "SQL update atmosphere error", ex); }
	}

	public synchronized void deleteAtmosphere(int id) {
		try {
			if (deleteAtmo == null) {
				deleteAtmo = db.prepareStatement("DELETE FROM "+
						AtmosphereDVO.Content.TABLE_NAME+ " WHERE "+AtmosphereDVO.Content._ID+" = ?");
			}
			deleteAtmo.setLong(1, id);
			deleteAtmo.execute();
		} catch (SQLException ex) { BLog.e(TAG, "SQL delete atmosphere error", ex); }
	}
}
