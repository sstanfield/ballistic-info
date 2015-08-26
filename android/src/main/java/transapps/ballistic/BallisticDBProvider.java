package transapps.ballistic;

import transapps.ballistic.db.BallisticDBHelper;
import transapps.ballistic.db.dvo.WeaponDVO;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;


public class BallisticDBProvider extends ContentProvider {
	private final String TAG = "BallisticDBProvider";

	/**
	 * Authority...used internally
	 */
	public static final String AUTHORITY = "transapps.ballistic.db";

	/**
	 * Base URI...used internally
	 */
	public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

	private static final int BALLISTIC_ITEMS = 0;
	private static final int BALLISTIC_ITEM = 1;
	private static final int BALLISTIC_ITEM_COUNT = 2;

	private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	private BallisticDBHelper dbHelper;

	static {
		MATCHER.addURI(AUTHORITY, "", BALLISTIC_ITEMS); // TODO- This is broken.
		MATCHER.addURI(AUTHORITY, "/#", BALLISTIC_ITEM);
		MATCHER.addURI(AUTHORITY, "/count", BALLISTIC_ITEM_COUNT);
	}

	@Override
	public boolean onCreate() {
		dbHelper = new BallisticDBHelper(getContext());
		return true;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		if (uri.toString().endsWith(WeaponDVO.Content.TABLE_NAME) && selectionArgs.length == 1) {
			int id = Integer.parseInt(selectionArgs[0]);
			Log.i(TAG, "Deleting weapon for id: "+id);
			dbHelper.deleteWeapon(id);
		}
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (uri.toString().endsWith(WeaponDVO.Content.TABLE_NAME)) {
			WeaponDVO weapon = WeaponDVO.create(values);
			Log.i(TAG, "Adding new weapon: "+weapon.name);
			dbHelper.insert(weapon);
		}
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		String table = getTable();
		Cursor c = dbHelper.query(table, projection, 
				selection, selectionArgs, null, null, sortOrder);
		if (c != null) {
			// Tell the cursor what uri to watch, so it knows when its source data changes
			c.setNotificationUri(getContext().getContentResolver(), uri);
		}
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		if (uri.toString().endsWith(WeaponDVO.Content.TABLE_NAME)) {
			WeaponDVO weapon = WeaponDVO.create(values);
			Log.i(TAG, "Updating weapon: "+weapon.name);
			dbHelper.update(weapon);
		}
		return 0;
	}

	private String getTable() {
		return WeaponDVO.Content.TABLE_NAME;
	}
}
