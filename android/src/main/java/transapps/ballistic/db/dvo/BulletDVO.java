package transapps.ballistic.db.dvo;

import java.util.ArrayList;
import java.util.List;

import transapps.ballistic.BallisticDBProvider;
import transapps.ballistic.lib.Ballistics;
import transapps.ballistic.lib.data.Bullet;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;


public class BulletDVO extends Bullet {
	private static final String TAG = "BulletDVO";
	public static final BulletDVO DUMMY = new BulletDVO("", "", Ballistics.G7, 0, 0, 0, 0);

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

	public static BulletDVO create(Cursor cursor) {
		Integer id = cursor.getInt(cursor.getColumnIndex(Content._ID));
		String name = cursor.getString(cursor.getColumnIndex(Content.NAME));
		String description = cursor.getString(cursor.getColumnIndex(Content.DESCRIPTION));
		int function = cursor.getInt(cursor.getColumnIndex(Content.FUNCTION));
		double coefficient = cursor.getDouble(cursor.getColumnIndex(Content.COEFFICIENT));
		double calibre = cursor.getDouble(cursor.getColumnIndex(Content.CALIBRE));
		double weight = cursor.getDouble(cursor.getColumnIndex(Content.WEIGHT));
		double length = cursor.getDouble(cursor.getColumnIndex(Content.LENGTH));
		return new BulletDVO(id, name, description, function, coefficient,
				calibre, weight, length);
	}

	public static BulletDVO create(ContentValues cv) {
		Integer id = cv.getAsInteger(Content._ID);
		String name = cv.getAsString(Content.NAME);
		String description = cv.getAsString(Content.DESCRIPTION);
		int function = cv.getAsInteger(Content.FUNCTION);
		double coefficient = cv.getAsDouble(Content.COEFFICIENT);
		double calibre = cv.getAsDouble(Content.CALIBRE);
		double weight = cv.getAsDouble(Content.WEIGHT);
		double length = cv.getAsDouble(Content.LENGTH);
		return new BulletDVO(id, name, description, function, coefficient,
				calibre, weight, length);
	}

	@Override
	public String toString() {
		return name;
	}

	public ContentValues toContentValues(ContentValues cv) {
		if (id != null) cv.put(Content._ID, id);
		cv.put(Content.NAME, name);
		cv.put(Content.DESCRIPTION, description);
		cv.put(Content.FUNCTION, function);
		cv.put(Content.COEFFICIENT, coefficient);
		cv.put(Content.CALIBRE, calibre);
		cv.put(Content.WEIGHT, weight);
		cv.put(Content.LENGTH, length);
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

	public static List<BulletDVO> allBullets(Context context) {
		List<BulletDVO> ret = new ArrayList<BulletDVO>();
		try {
			String orderBy = Content.NAME + " ASC";
			Cursor c = null;
			try {
				c = context.getContentResolver().query(
						BallisticDBProvider.BASE_URI.buildUpon().appendPath(Content.TABLE_NAME).build(),
						Content.PROJECTION, null, null, orderBy);
				while (c.moveToNext()) {
					ret.add(BulletDVO.create(c));
				}
			} finally {
				if (c != null) c.close();
			}
		} catch (Exception ex) {
			Log.e(TAG, "Error quering list of bullets.", ex);
		}
		return ret;
	}
}
