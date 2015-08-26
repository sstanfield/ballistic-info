package transapps.ballistic.db.dvo;

import java.sql.ResultSet;
import java.sql.SQLException;

import transapps.ballistic.app.BLog;
import transapps.ballistic.lib.Ballistics;
import transapps.ballistic.lib.data.Bullet;


public class BulletDVO extends Bullet {
	private static final String TAG = "BulletDVO";
	public static final BulletDVO DUMMY = new BulletDVO("Sierra MK 175", "Sierra MatchKing 175gr 308", Ballistics.G7, .243, .308, 175, 1.24);

	public static final class Content {
		public static final String TABLE_NAME = "bullet";

		public static final String _ID = "_bullet_id";
		public static final String NAME = "bullet_name";
		public static final String DESCRIPTION = "bullet_description";
		public static final String FUNCTION = "bullet_function";
		public static final String COEFFICIENT = "bullet_coefficient";
		public static final String CALIBRE = "bullet_calibre";
		public static final String WEIGHT = "bullet_weight";
		public static final String LENGTH = "bullet_length";

		public static final String[] PROJECTION = {
			_ID,
			NAME,
			DESCRIPTION,
			FUNCTION,
			COEFFICIENT,
			CALIBRE,
			WEIGHT,
			LENGTH
		};

		public static final String CREATE_SQL =
				"CREATE TABLE "+TABLE_NAME+" ("
						+ Content._ID + " INTEGER PRIMARY KEY, "
						+ Content.NAME + " TEXT, "
						+ Content.DESCRIPTION + " TEXT, "
						+ Content.FUNCTION + " INTEGER, "
						+ Content.COEFFICIENT + " REAL, "
						+ Content.CALIBRE + " REAL, "
						+ Content.WEIGHT + " REAL, "
						+ Content.LENGTH + " REAL "
						+ ");";
	}

	public BulletDVO(Bullet bullet) {
		this(bullet.id, bullet.name, bullet.description, bullet.function, 
				bullet.coefficient, bullet.calibre, bullet.weight, bullet.length);
	}

	public BulletDVO(String name, String description, int function,
			double coefficient, double calibre, double weight, double length) {
		this(null, name, description, function, coefficient, calibre,
				weight, length);
	}

	public BulletDVO(Integer id, String name, String description, int function,
			double coefficient, double calibre, double weight, double length) {
		super(id, name, description, function, coefficient, calibre,
				weight, length);
	}

	public static BulletDVO create(ResultSet cursor) {
		BulletDVO ret = null;
		try {
			Integer id = cursor.getInt(Content._ID);
			String name = cursor.getString(Content.NAME);
			String description = cursor.getString(Content.DESCRIPTION);
			int function = cursor.getInt(Content.FUNCTION);
			double coefficient = cursor.getDouble(Content.COEFFICIENT);
			double calibre = cursor.getDouble(Content.CALIBRE);
			double weight = cursor.getDouble(Content.WEIGHT);
			double length = cursor.getDouble(Content.LENGTH);
			ret = new BulletDVO(id, name, description, function, coefficient,
					calibre, weight, length);
		} catch (SQLException e) { BLog.e(TAG, "Create failure", e); }
		return ret;
	}

	@Override
	public String toString() {
		return name;
	}

//	public void save() {
//		context.getContentResolver().insert(
//				BallisticDBProvider.BASE_URI.buildUpon().appendPath(Content.TABLE_NAME).build(),
//				toContentValues());
//	}
//
//	public void update() {
//		context.getContentResolver().update(
//				BallisticDBProvider.BASE_URI.buildUpon().appendPath(Content.TABLE_NAME).build(),
//				toContentValues(), null, null);
//	}
//
//	public void delete() {
//		context.getContentResolver().delete(
//				BallisticDBProvider.BASE_URI.buildUpon().appendPath(Content.TABLE_NAME).build(),
//				"id=?", new String[] {""+id});
//	}
//
//	public static List<BulletDVO> allBullets() {
//		List<BulletDVO> ret = new ArrayList<BulletDVO>();
//		try {
//			String orderBy = Content.NAME + " ASC";
//			Cursor c = null;
//			try {
//				c = context.getContentResolver().query(
//						BallisticDBProvider.BASE_URI.buildUpon().appendPath(Content.TABLE_NAME).build(),
//						Content.PROJECTION, null, null, orderBy);
//				while (c.moveToNext()) {
//					ret.add(BulletDVO.create(c));
//				}
//			} finally {
//				if (c != null) c.close();
//			}
//		} catch (Exception ex) {
//			Log.e(TAG, "Error quering list of bullets.", ex);
//		}
//		return ret;
//	}
}
