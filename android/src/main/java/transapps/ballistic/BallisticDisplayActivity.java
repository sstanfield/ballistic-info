package transapps.ballistic;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import transapps.ballistic.db.BallisticDBHelper;
import transapps.ballistic.db.dvo.AtmosphereDVO;
import transapps.ballistic.db.dvo.BulletDVO;
import transapps.ballistic.db.dvo.WeaponDVO;
import transapps.ballistic.dialog.AboutDialog;
import transapps.ballistic.dialog.ConfirmDeleteDialog;
import transapps.ballistic.dialog.WepNewCopyDialog;
import transapps.ballistic.fragment.*;
import transapps.ballistic.lib.data.Atmosphere;
import transapps.ballistic.lib.data.Bullet;
import transapps.ballistic.lib.util.Conversions;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: sstanf
 * Date: 5/2/11
 * Time: 3:41 PM
 */
public class BallisticDisplayActivity extends Activity
		implements AtmoFragment.Callback, ZeroFragment.Callback,
		WeaponFragment.WepDialogCallbacks {
	private static final String TAG = "BallisticDisplayActivity";

	public static final int NAV_HOME       = 1;
	public static final int NAV_ATMOSPHERE = 1 << 1;
	public static final int NAV_WIND       = 1 << 2;
	public static final int NAV_ANGLE      = 1 << 3;
	public static final int NAV_ZERO       = 1 << 4;
	public static final int NAV_CORIOLIS   = 1 << 5;
	public static final int NAV_HELP       = 1 << 6;
	public static final int NAV_RANGE      = 1 << 7;
	public static final int NAV_WEAPON     = 1 << 8;
	public static final int NAV_TRUE       = 1 << 9;

	private static int mode = NAV_HOME;
	private static boolean started = false;
	private DrawerLayout drawer;
	private ActionBarDrawerToggle drawerToggle;
	private View drawerList;
	private boolean prevWasRangeTable = false;
	private String fragmentTag = FRAG;
	private int wepPos;
	private Spinner wepSpinner;
	private boolean newWeapon = false;
	private static final String FRAG = "BALLISTIC_FRAG";

	private static BallisticDisplayActivity instance;
	public static BallisticDisplayActivity i() { return instance; }

	public Fragment currentFragRaw() {
		return getFragmentManager().findFragmentByTag(fragmentTag);
	}

	public Fragment currentFrag() {
		Fragment frag = currentFragRaw();
		// This should not happen but I have seen it, if no fragment from system
		// then go ahead and make a new one...
		if (frag == null) frag = selectItem(mode);
		return frag;
	}

	private void updatePreviousFragment() {
		Fragment currentFrag = currentFragRaw();
		if (currentFrag != null && currentFrag instanceof FragmentUpdate) {
			((FragmentUpdate)currentFrag).update();
		}
	}

	private void setNavDrawerState() {
		drawerToggle.setDrawerIndicatorEnabled(hasMode(NAV_HOME));// | NAV_RANGE));
		if (!hasMode(NAV_HOME)) drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		else drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
	}

	public void setFragmentTag(String tag) {
		fragmentTag = tag;

		if (drawer != null) {  // Do not try this if not setup yet...
			drawer.closeDrawer(drawerList);
			setNavDrawerState();
			invalidateOptionsMenu();
		}
	}

	public void goNavMenu(View v) {
		switch (v.getId())
		{
			case (R.id.menu_atmo):
				selectItem(NAV_ATMOSPHERE);
				break;
			case (R.id.menu_wind):
				selectItem(NAV_WIND);
				break;
			case (R.id.menu_angle):
				selectItem(NAV_ANGLE);
				break;
			case (R.id.menu_zero):
				selectItem(NAV_ZERO);
				break;
			case (R.id.menu_ce):
				selectItem(NAV_CORIOLIS);
				break;
			case (R.id.menu_table):
				selectItem(NAV_RANGE);
				break;
			case (R.id.menu_true):
				selectItem(NAV_TRUE);
				break;

		}
		drawer.closeDrawer(drawerList);
	}

	public boolean hasMode(int mode, int flags) {
		return (mode & flags) != 0;
	}

	public boolean hasMode(int flags) {
		return hasMode(mode, flags);
	}

	/** Swaps fragments in the main content view */
	public Fragment selectItem(int position) {
		BallisticSettings s = BallisticSettings.i();
		Fragment frag = null;
		if (hasMode(position, NAV_HOME)) {
			updatePreviousFragment();
			if (hasMode(NAV_WEAPON)) {
				setWepList();
			}
			if (prevWasRangeTable && !(hasMode(NAV_RANGE))) {
				// If changes are made from the range table then stay in it.
				return selectItem(NAV_RANGE);
			} else {
				frag = new BallisticFragment();
				s.refreshTable();
			}
		}
		prevWasRangeTable = (mode == NAV_RANGE);
		updatePreviousFragment();
		if (hasMode(position, NAV_WEAPON)) {
			frag = new WeaponFragment();
			newWeapon = false;
			((WeaponFragment)frag).setView();
		}
		if (hasMode(position, NAV_ATMOSPHERE)) frag = AtmoFragment.newInstance(s.atmoOn, s.atmo);
		if (hasMode(position, NAV_WIND)) frag = new WindFragment();
		if (hasMode(position, NAV_ANGLE)) frag = new AngleFragment();
		if (hasMode(position, NAV_ZERO)) {
			AtmosphereDVO a = s.getWeapon().atmosphere==null?null:new AtmosphereDVO(s.getWeapon().atmosphere);
			frag = ZeroFragment.newInstance(s.imperial?s.getWeapon().zeroRange:Conversions.yardsToMeters(s.getWeapon().zeroRange),
					s.imperial, s.getWeapon().atmosphere != null, a);
		}
		if (hasMode(position, NAV_HELP)) frag = new HelpFragment();
		if (hasMode(position, NAV_CORIOLIS)) frag = new CoriolisFragment();
		if (hasMode(position, NAV_RANGE)) {
			frag = RangeTableFragment.newInstance(0, s.maxRange, 100);
			s.refreshTable();
		}
		if (hasMode(position, NAV_TRUE)) frag = new TruingFragment();

		// Insert the fragment by replacing any existing fragment
		if (frag != null) {
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.content_frame, frag, FRAG+"-"+frag.getClass())
					.commit();
		}
		mode = position;
		return frag;
	}

	public void tableGenerated() {
		BaseFragment frag = (BaseFragment)currentFragRaw();
		if (frag != null) frag.refresh();
	}

	private boolean amHome() {
		return hasMode(NAV_HOME);
	}

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		instance = this;
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		BallisticApplication.i().setActivity(this);  // Tell the application singleton about us...
		setContentView(R.layout.main);
		BallisticSettings.loadIfNew(this);
		drawer = (DrawerLayout)findViewById(R.id.drawer_layout);

		drawerToggle = new ActionBarDrawerToggle(
				this,                  /* host Activity */
				drawer,                /* DrawerLayout object */
				R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
				R.string.drawer_open,  /* "open drawer" description */
				R.string.drawer_close  /* "close drawer" description */
				) {

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				invalidateOptionsMenu();
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				invalidateOptionsMenu();
			}
		};

		setNavDrawerState();
		// Set the drawer toggle as the DrawerListener
		drawer.setDrawerListener(drawerToggle);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		drawerList = findViewById(R.id.left_drawer);

		wepSpinner = (Spinner)findViewById(R.id.wep_spinner);
		setWepList();

		final Spinner unitSpinner = (Spinner)findViewById(R.id.unit_spinner);
		final ArrayAdapter<String> uadp = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
				new String[] {"Metric", "Imperial"});
		uadp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		unitSpinner.setAdapter(uadp);
		unitSpinner.setSelection(BallisticSettings.i().imperial?1:0);
		unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				int pos = unitSpinner.getSelectedItemPosition();
				boolean imp = pos==1;
				BallisticSettings s = BallisticSettings.i();
				if (s.imperial != imp) {
					s.imperial = imp;
					s.setDirty(true);
				}
				BallisticSettings.i().refreshTable();
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) { }
		});

		// Make sure we have a fragment.
		if (!started) {
			selectItem(NAV_HOME);
			started = true;
		}
		setProgressBarIndeterminateVisibility(false);
	}

	private void setWepList() {
		List<WeaponDVO> weapons = WeaponDVO.allWeapons(this);
		int wpos = 0;
		int x = 0;
		String wname = BallisticSettings.i().weaponName;
		for (WeaponDVO w : weapons) {
			if (w.name.equals(wname)) {
				wpos = x;
				break;
			}
			x++;
		}

		final ArrayAdapter<WeaponDVO> wadp = new ArrayAdapter<WeaponDVO>(this, android.R.layout.simple_list_item_1,
				weapons);
		wadp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		wepSpinner.setAdapter(wadp);
		wepPos = wpos;
		wepSpinner.setSelection(wpos);
		wepSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				int pos = wepSpinner.getSelectedItemPosition();
				if (wepPos != pos) {
					WeaponDVO weapon = wadp.getItem(pos);
					if (weapon != null) BallisticSettings.i().setWeapon(weapon);
					BallisticSettings.i().refreshTable();
					wepPos = pos;
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) { }
		});
	}

	@Override
	public void onBackPressed() {
		if (amHome()) {
			super.onBackPressed();
		} else {
			boolean done = true;
			if (hasMode(NAV_WEAPON)) {
				WeaponFragment frag = (WeaponFragment)currentFragRaw();
				if (frag != null && frag.editing()) {
					int s = frag.save();
					if (s != 0) {
						done = false;
						frag.showDialog(s);
					}
				}
			}
			if (done) {
				selectItem(NAV_HOME);
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		BallisticSettings.i().save(this);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		drawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}

	public void setAtmo(boolean atmoOn, Atmosphere atmo) {
		BallisticSettings s = BallisticSettings.i();
		s.atmoOn = atmoOn;
		s.atmo = new AtmosphereDVO(atmo);
		s.setDirty(true);
		if (atmoOn && !s.atmoOn()) {
			// Invalid settings, warn user it is disabled.
			Toast.makeText(this, "Invalid atmospheric settings, not using.", Toast.LENGTH_LONG).show();
		}
	}
	public void setZero(int zero, boolean atmoOn, Atmosphere atmo) {
		BallisticSettings s = BallisticSettings.i();
		Atmosphere zatmo = null;
		if (atmoOn) {
			zatmo = atmo;
			if (!zatmo.checkAtmo()) {
				zatmo = null;
				Toast.makeText(this, "Invalid atmospheric settings, not using.", Toast.LENGTH_LONG).show();
			}
		}

		s.setWeapon(s.getWeapon().newZero(s.imperial?zero:Conversions.metersToYards(zero), zatmo));
		s.setDirty(true);
		if (s.getWeapon().id != null) s.getWeapon().update(this);  // Save new zero values.
		s.zeroangle = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.ballistic_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Fragment currentFrag = currentFrag();
		if (currentFrag == null) {
			Log.e(TAG, "Trying to prepare a menu with no fragment!");
			return super.onPrepareOptionsMenu(menu);
		}
		boolean editing = (currentFrag instanceof WeaponFragment && ((WeaponFragment)currentFrag).editing());
		boolean drawerOpen = drawer.isDrawerOpen(drawerList);
		menu.findItem(R.id.m_drop).setVisible(!drawerOpen && hasMode(NAV_HOME | NAV_RANGE));
		menu.findItem(R.id.m_spindrift).setVisible(!drawerOpen && amHome());
		menu.findItem(R.id.m_spindrift).setChecked(BallisticSettings.i().spinDriftOn);

		menu.findItem(R.id.m_weapon).setVisible(!drawerOpen && amHome());
		menu.findItem(R.id.m_edit).setVisible(!drawerOpen && hasMode(NAV_HOME | NAV_WEAPON) && !editing);
		menu.findItem(R.id.m_delete).setVisible(!drawerOpen && hasMode(NAV_HOME | NAV_WEAPON));
		menu.findItem(R.id.m_new).setVisible(!drawerOpen && hasMode(NAV_HOME | NAV_WEAPON) && !editing);
		menu.findItem(R.id.m_help).setVisible(!drawerOpen && !hasMode(NAV_HELP));
		menu.findItem(R.id.m_about).setVisible(!drawerOpen);
		menu.findItem(R.id.m_cancel).setVisible(!drawerOpen && editing);

		BallisticSettings s = BallisticSettings.i();
		if (s.getWeapon() != null) {
			if (BallisticDBHelper.isDefaultWeapon(s.getWeapon())) {
				menu.findItem(R.id.m_edit).setEnabled(false);
				menu.findItem(R.id.m_delete).setEnabled(false);
			} else {
				menu.findItem(R.id.m_edit).setEnabled(true);
				menu.findItem(R.id.m_delete).setEnabled(true);
			}
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
//		if (drawerToggle.onOptionsItemSelected(item)) {
//			return true;
//		}
		// Can not do the above because of MenuItem mismatches so do the
		// below instead- if ABS goes away the above code should work.
		if (item.getItemId() == android.R.id.home){
			if (amHome()) {
				if (drawer.isDrawerOpen(drawerList)) {
					drawer.closeDrawer(drawerList);
				} else {
					drawer.openDrawer(drawerList);
				}
			} else {
				selectItem(NAV_HOME);
			}
			return true;
		}

		if (item.getItemId() == R.id.m_drop) {
			if (hasMode(NAV_HOME)) {
				BallisticFragment frag = (BallisticFragment)currentFrag();
				if (frag != null) frag.cycleDropUnits();
			}
			if (hasMode(NAV_RANGE)) {
				BallisticSettings s = BallisticSettings.i();
				if (s.dropUnits < 2) s.dropUnits++;
				else s.dropUnits = 0;
				selectItem(NAV_RANGE);
			}
		} else if (item.getItemId() == R.id.m_spindrift) {
			if (hasMode(NAV_HOME)) {
				BallisticFragment frag = (BallisticFragment)currentFrag();
				if (frag != null) frag.toggleSpinDrift();
			}
			item.setChecked(BallisticSettings.i().spinDriftOn);
		} else if (item.getItemId() == R.id.m_help) {
			selectItem(NAV_HELP);
		} else if (item.getItemId() == R.id.m_about) {
			AboutDialog d = new AboutDialog();
			d.show(getFragmentManager(), "dialog");
		} else if (item.getItemId() == R.id.m_weapon) {
			selectItem(NAV_WEAPON);
		} else if (item.getItemId() == R.id.m_new) {
			WepNewCopyDialog.newInstance().show(getFragmentManager(), "dialog");
		} else if (item.getItemId() == R.id.m_edit) {
			WeaponFragment frag;
			if (!hasMode(NAV_WEAPON)) {
				frag = (WeaponFragment)selectItem(NAV_WEAPON);
			} else {
				frag = (WeaponFragment) currentFrag();
			}
			if (frag != null) frag.setEdit();
			invalidateOptionsMenu();
		} else if (item.getItemId() == R.id.m_delete) {
			BallisticSettings s = BallisticSettings.i();
			List<WeaponDVO> weapons = WeaponDVO.allWeapons(this);
			if (weapons.size() <= 1) {
				Toast.makeText(this, "Can not delete last weapon, need at least one defined!", Toast.LENGTH_LONG).show();
			} else if (BallisticDBHelper.isDefaultWeapon(s.getWeapon())) {
				Toast.makeText(this, "Can not delete a default weapon.", Toast.LENGTH_LONG).show();
			} else {
				ConfirmDeleteDialog.newInstance(s.getWeapon().name).show(getFragmentManager(), "dialog");
			}
		} else if (item.getItemId() == R.id.m_cancel) {
			if (newWeapon) deleteWep();
			else selectItem(NAV_HOME);
		} else {
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	public void deleteWepIfNew() {
		if (newWeapon) deleteWep();
	}

	public void deleteWep() {
		BallisticSettings s = BallisticSettings.i();
		s.getWeapon().delete(this);
		List<WeaponDVO> weapons = WeaponDVO.allWeapons(this);
		s.setWeapon(weapons.get(0));
		if (hasMode(NAV_WEAPON)) {
			WeaponFragment frag = (WeaponFragment)currentFrag();
			if (frag != null) {
				frag.setWeaponList();
				frag.fromWeapon(s.getWeapon());
			}
		}
		selectItem(BallisticDisplayActivity.NAV_HOME);
		setWepList();
	}

	public void truedWep(double velocity, double coefficient) {
		BallisticSettings s = BallisticSettings.i();
		WeaponDVO wep = s.getWeapon();
		if (BallisticDBHelper.isDefaultWeapon(wep)) {
			copyWep("Trued ", velocity, coefficient);
		} else {
			Bullet b = wep.bullet;
			if (b.coefficient != coefficient)
				b = b.newCoefficient(coefficient);
			wep = new WeaponDVO(wep.id, wep.name, wep.description, velocity,
					wep.sightHeight, wep.rightTwist, wep.barrelTwist,
					wep.zeroRange, wep.atmosphere, b);
			s.setWeapon(wep);
			s.getWeapon().update(this);
			WeaponFragment frag = (WeaponFragment)selectItem(NAV_WEAPON);
			if (frag != null) {
				frag.setEdit();
			}
		}
	}

	public void copyWep() {
		BallisticSettings s = BallisticSettings.i();
		copyWep("Copy ", s.getWeapon().velocity, s.getWeapon().bullet.coefficient);
	}

	// Parameters are for use of truing function.
	public void copyWep(String prefix, double velocity, double coefficient) {
		BallisticSettings s = BallisticSettings.i();
		Bullet b = s.getWeapon().bullet;
		if (b.coefficient != coefficient)
			b = b.newCoefficient(coefficient);
		s.setWeapon(new WeaponDVO(WeaponFragment.newName(prefix, " of "+s.getWeapon().name, this),
				s.getWeapon().description, velocity,
				s.getWeapon().sightHeight, s.getWeapon().rightTwist,
				s.getWeapon().barrelTwist, s.getWeapon().zeroRange,
				s.getWeapon().atmosphere, b));
		s.getWeapon().save(this);
		if (hasMode(NAV_WEAPON)) {
			WeaponFragment frag = (WeaponFragment)currentFrag();
			if (frag != null) {
				frag.setWeaponList();
				frag.fromWeapon(s.getWeapon());
				frag.setEdit();
			}
		} else {
			WeaponFragment frag = (WeaponFragment)selectItem(NAV_WEAPON);
			if (frag != null) {
				frag.setEdit();
			}
		}
		newWeapon = true;
		setWepList();
		invalidateOptionsMenu();
	}

	public void newWep() {
		BallisticSettings s = BallisticSettings.i();
		s.setWeapon(new WeaponDVO(WeaponFragment.newName("New Weapon ", "", this), "", 0,
				0, true, 0, 328.084, AtmosphereDVO.STANDARD, BulletDVO.DUMMY));
		s.getWeapon().save(this);
		if (hasMode(NAV_WEAPON)) {
			WeaponFragment frag = (WeaponFragment)currentFrag();
			if (frag != null) {
				frag.setWeaponList();
				frag.fromWeapon(s.getWeapon());
				frag.setEdit();
			}
		} else {
			WeaponFragment frag = (WeaponFragment)selectItem(NAV_WEAPON);
			if (frag != null) {
				frag.setEdit();
			}
		}
		newWeapon = true;
		setWepList();
		invalidateOptionsMenu();
	}
}
