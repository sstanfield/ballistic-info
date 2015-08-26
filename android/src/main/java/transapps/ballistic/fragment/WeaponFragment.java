package transapps.ballistic.fragment;

import java.text.DecimalFormat;
import java.util.List;

import android.content.ContentValues;
import transapps.ballistic.BallisticSettings;
import transapps.ballistic.R;
import transapps.ballistic.db.dvo.BulletDVO;
import transapps.ballistic.db.dvo.WeaponDVO;
import transapps.ballistic.dialog.WepErrorDialog;
import transapps.ballistic.lib.util.Conversions;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class WeaponFragment extends BaseFragment {
	private static final String[] models = {"G1", "G2", "G5", "G6", "G7", "G8"};
	private static final String[] twists = {"Right", "Left"};
	private static final String[] vel_unit = {"ft/s", "m/s"};
	private static final String[] incm_unit = {"in", "cm"};
	private static final String[] bweight_unit = {"gr", "gm"};
	private static final String[] bcalibre_unit = {"in", "mm"};
	private static final String[] blength_unit = {"in", "mm"};
	private BallisticSettings s = BallisticSettings.i();
	protected Spinner wspinner, btspinner,bt2spinner,vspinner,shspinner,
		bwspinner,bcspinner,blspinner;
	private static final int DIALOG_FIX_SAVE_NAME = 2;
	private static final int DIALOG_FIX_SAVE_NUMERIC = 3;
	private static final int DIALOG_FIX_SAVE_BC_ZERO = 4;
	private static final int DIALOG_FIX_SAVE_VELOCITY_ZERO = 5;
	private static final int DIALOG_FIX_SAVE_WEIGHT_ZERO = 6;
	private static final DecimalFormat df2 = new DecimalFormat("0.00");
	private static final DecimalFormat df3 = new DecimalFormat("0.000");
	private boolean editMode = false;

	protected View view;

	public interface WepDialogCallbacks {
		public void deleteWepIfNew();
		public void deleteWep();
		public void copyWep();
		public void newWep();
	}

	public boolean isZero(Spinner spinner){
		return spinner.getSelectedItemPosition() == 0;
	}

	public void setWeaponList() {
		if (s.getWeapon() == null) return;
		List<WeaponDVO> weapons = WeaponDVO.allWeapons(getActivity());
		int wpos = 0;
		int x = 0;
		String wname = s.getWeapon().name;
		for (WeaponDVO w : weapons) {
			if (w.name.equals(wname)) {
				s.setWeapon(w); // Make sure we have the id...
				wpos = x;
				break;
			}
			x++;
		}
		ArrayAdapter<WeaponDVO> wadp = new ArrayAdapter<WeaponDVO>(getActivity(),
				android.R.layout.simple_list_item_1, weapons);
		wadp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		wspinner.setAdapter(wadp);
		wspinner.setSelection(wpos);
	}

	private boolean isNameDuplicate(WeaponDVO wep) {
		boolean ret = false;
		List<WeaponDVO> weapons = WeaponDVO.allWeapons(getActivity());
		String wname = wep.name;
		for (WeaponDVO w : weapons) {
			if ((w.name.equals(wname)) && (!w.id.equals(wep.id))) {
				ret = true;
				break;
			}
		}
		return ret;
	}

	public void setEdit() {
		editMode = true;
		if (view == null) return;
		getActivity().setTitle("Edit Weapon");
		wspinner.setEnabled(false);
		view.findViewById(R.id.name_value).setEnabled(true);
		view.findViewById(R.id.description_value).setEnabled(true);
		view.findViewById(R.id.bc_value).setEnabled(true);
		view.findViewById(R.id.table_value).setEnabled(true);
		view.findViewById(R.id.velocity_value).setEnabled(true);
		view.findViewById(R.id.sight_height_value).setEnabled(true);
		view.findViewById(R.id.weight_value).setEnabled(true);
		view.findViewById(R.id.calibre_value).setEnabled(true);
		view.findViewById(R.id.length_value).setEnabled(true);
		view.findViewById(R.id.twist_value).setEnabled(true);
		view.findViewById(R.id.twist_direction_value).setEnabled(true);
	}

	public void setView() {
		editMode = false;
		if (view == null) return;
		view.findViewById(R.id.name_value).setEnabled(false);
		view.findViewById(R.id.description_value).setEnabled(false);
		view.findViewById(R.id.bc_value).setEnabled(false);
		view.findViewById(R.id.table_value).setEnabled(false);
		view.findViewById(R.id.velocity_value).setEnabled(false);
		view.findViewById(R.id.sight_height_value).setEnabled(false);
		view.findViewById(R.id.weight_value).setEnabled(false);
		view.findViewById(R.id.twist_value).setEnabled(false);
		view.findViewById(R.id.calibre_value).setEnabled(false);
		view.findViewById(R.id.length_value).setEnabled(false);
		view.findViewById(R.id.twist_direction_value).setEnabled(false);
	}

	public void convertFtPerSec(final Spinner spinner){
		spinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if (spinner.getSelectedItemPosition() == s.velSel) return;
				s.velSel = spinner.getSelectedItemPosition();
				EditText t = (EditText)view.findViewById(R.id.velocity_value);
				double ftPerSec = Double.parseDouble(t.getText().toString());
				if (isZero(spinner)) // 0 is ft/s
					ftPerSec = Conversions.metersToFeet(ftPerSec);
				else
					ftPerSec = Conversions.feetToMeters(ftPerSec);

				t.setText(""+df2.format(ftPerSec));
			}
			public void onNothingSelected(AdapterView<?> arg0) {}
		});

	}

	public void sightHeightInCm(final Spinner spinner){
		spinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if (spinner.getSelectedItemPosition() == s.sightSel) return;
				s.sightSel = spinner.getSelectedItemPosition();
				EditText height = (EditText)view.findViewById(R.id.sight_height_value);
				double inchesCm = Double.parseDouble(height.getText().toString());
				if (isZero(spinner)) // 0 is in.
					inchesCm = Conversions.cmToin(inchesCm);
				else
					inchesCm = Conversions.inTocm(inchesCm);
				height.setText(""+df2.format(inchesCm));
			}
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
	}

	public void twistsInCm(final Spinner spinner){
		spinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if (spinner.getSelectedItemPosition() == s.twistSel) return;
				s.twistSel = spinner.getSelectedItemPosition();
				EditText mytwist = (EditText)view.findViewById(R.id.twist_value);
				double inchesCm = Double.parseDouble(mytwist.getText().toString());
				if (isZero(spinner)) // 0 is in.
					inchesCm = Conversions.cmToin(inchesCm);
				else
					inchesCm = Conversions.inTocm(inchesCm);
				mytwist.setText(""+df2.format(inchesCm));

			}
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
	}

	//wrong
	public void convertWeight(final Spinner spinner){
		spinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if (spinner.getSelectedItemPosition() == s.weightSel) return;
				s.weightSel = spinner.getSelectedItemPosition();
				EditText weight = (EditText)view.findViewById(R.id.weight_value);
				double convert = Double.parseDouble(weight.getText().toString());
				if (isZero(spinner)) // 0 is grains.
					convert = Conversions.gramsToGrains(convert);
				else
					convert = Conversions.grainToGrams(convert);
				weight.setText(""+df2.format(convert));
			}
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
	}

	public void convertCaliber(final Spinner spinner){
		spinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if (spinner.getSelectedItemPosition() == s.calSel) return;
				s.calSel = spinner.getSelectedItemPosition();
				EditText caliber = (EditText)view.findViewById(R.id.calibre_value);
				double convert = Double.parseDouble(caliber.getText().toString());
				if (isZero(spinner)) // 0 is in.
					convert = Conversions.mmToin(convert);
				else
					convert = Conversions.inTomm(convert); // mm
				caliber.setText(""+df3.format(convert));
			}
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
	}

	public void convertLength(final Spinner spinner){
		spinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if (spinner.getSelectedItemPosition() == s.lenSel) return;
				s.lenSel = spinner.getSelectedItemPosition();
				EditText length = (EditText)view.findViewById(R.id.length_value);
				double convert = Double.parseDouble(length.getText().toString());
				if (isZero(spinner)) // 0 is in.
					convert = Conversions.mmToin(convert);
				else
					convert = Conversions.inTomm(convert); // mm
				length.setText(""+df2.format(convert));
			}
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		view = inflater.inflate(R.layout.weapon_view, container, false);
		getActivity().setTitle("Select Weapon");

		WeaponDVO savedWep = null;
		if (savedInstanceState != null) {
			editMode = savedInstanceState.getBoolean("_EDIT");
			ContentValues cv = savedInstanceState.getParcelable("_WEAPON");
			savedWep = WeaponDVO.create(cv);
		}

		wspinner = (Spinner)view.findViewById(R.id.weapon_spinner);
		setWeaponList();
		if (editMode) setEdit();
		else setView();
		Spinner table = (Spinner)view.findViewById(R.id.table_value);
		ArrayAdapter<String> aadp = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, models);
		table.setAdapter(aadp);
		aadp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		btspinner = (Spinner)view.findViewById(R.id.twist_direction_value);
		aadp = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, twists);
		btspinner.setAdapter(aadp);
		aadp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		vspinner = (Spinner)view.findViewById(R.id.velocity_unit_value);
		aadp = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, vel_unit);
		vspinner.setAdapter(aadp);
		aadp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		vspinner.setSelection(s.velSel);
		convertFtPerSec(vspinner);

		shspinner = (Spinner)view.findViewById(R.id.height_unit_value);
		aadp = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, incm_unit);
		shspinner.setAdapter(aadp);
		aadp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sightHeightInCm(shspinner);

		bt2spinner = (Spinner)view.findViewById(R.id.btwist_unit_value);
		aadp = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, incm_unit);
		bt2spinner.setAdapter(aadp);
		aadp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		twistsInCm(bt2spinner);

		bwspinner = (Spinner)view.findViewById(R.id.bweight_unit_value);
		aadp = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, bweight_unit );
		bwspinner.setAdapter(aadp);
		aadp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		convertWeight(bwspinner);

		bcspinner = (Spinner)view.findViewById(R.id.bcalibre_unit_value);
		aadp = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, bcalibre_unit );
		bcspinner.setAdapter(aadp);
		aadp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		convertCaliber(bcspinner);

		blspinner = (Spinner)view.findViewById(R.id.blength_unit_value);
		aadp = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, blength_unit );
		blspinner.setAdapter(aadp);
		aadp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		convertLength(blspinner);

		if (savedWep == null) fromWeapon(s.getWeapon());
		else fromWeapon(savedWep);

		return view;
	}

	@Override
	public void onSaveInstanceState (Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("_EDIT", editMode);
		WeaponDVO w = toWeapon();
		outState.putParcelable("_WEAPON", w.toContentValues());
	}

	public boolean editing() {
		return editMode;
	}

	public int save() {
		int ret = 0;
		WeaponDVO w = toWeapon();
		if (w != null) {
			if (w.bullet.coefficient <= 0) {
				ret = DIALOG_FIX_SAVE_BC_ZERO;
			} else if (w.velocity <= 0) {
				ret = DIALOG_FIX_SAVE_VELOCITY_ZERO;
			} else if (w.bullet.weight <= 0) {
				ret = DIALOG_FIX_SAVE_WEIGHT_ZERO;
			} else if (isNameDuplicate(w)) {
				ret = DIALOG_FIX_SAVE_NAME;
			} else {
				w.update(getActivity());
				s.setWeapon(w);
			}
		} else {
			ret = DIALOG_FIX_SAVE_NUMERIC;
		}
		return ret;
	}

	public WeaponDVO toWeapon() {
		String name = ((EditText)view.findViewById(R.id.name_value)).getText().toString();
		String desc = ((EditText)view.findViewById(R.id.description_value)).getText().toString();
		double velocity = 0;
		double coefficient = 0;
		double bulletWeight = 0;
		int function = 0;
		double sightHeight = 0;
		double calibre = 0;
		double length = 0;
		boolean rightTwist = true;

		double barrelTwist = 0;
		boolean ok = true;
		try {
			velocity = Double.parseDouble(((EditText)view.findViewById(R.id.velocity_value)).getText().toString());
			if (s.velSel == 1) velocity = Conversions.metersToFeet(velocity);
			coefficient = Double.parseDouble(((EditText)view.findViewById(R.id.bc_value)).getText().toString());
			bulletWeight = Double.parseDouble(((EditText)view.findViewById(R.id.weight_value)).getText().toString());
			if (s.weightSel == 1) bulletWeight = Conversions.gramsToGrains(bulletWeight);
			function = ((Spinner)view.findViewById(R.id.table_value)).getSelectedItemPosition();
			sightHeight = Double.parseDouble(((EditText)view.findViewById(R.id.sight_height_value)).getText().toString());
			if (s.sightSel==1) sightHeight = Conversions.cmToin(sightHeight);
			calibre = Double.parseDouble(((EditText)view.findViewById(R.id.calibre_value)).getText().toString());
			if (s.calSel == 1) calibre = Conversions.mmToin(calibre);
			length = Double.parseDouble(((EditText)view.findViewById(R.id.length_value)).getText().toString());
			if (s.lenSel == 1) length = Conversions.mmToin(length);
			rightTwist = btspinner.getSelectedItemPosition()==0;
			barrelTwist = Double.parseDouble(((EditText)view.findViewById(R.id.twist_value)).getText().toString());
			if (s.twistSel == 1) barrelTwist = Conversions.cmToin(barrelTwist);
		} catch (NumberFormatException ex) {
			ok = false;
		}
		WeaponDVO w = null;
		if (ok) {
			w = new WeaponDVO(s.getWeapon().id, name, desc, velocity,
					sightHeight, rightTwist, barrelTwist, s.getWeapon().zeroRange, s.getWeapon().atmosphere,
					new BulletDVO("", "", function, coefficient, calibre, bulletWeight, length));
		}
		return w;
	}

	public void fromWeapon(WeaponDVO weapon) {
		if (view == null) return;
		DecimalFormat df2 = new DecimalFormat("0.00");
		DecimalFormat df3 = new DecimalFormat("0.000");
		DecimalFormat df4 = new DecimalFormat("0.0000");
		EditText name = (EditText)view.findViewById(R.id.name_value);
		name.setText(weapon.name);
		EditText desc = (EditText)view.findViewById(R.id.description_value);
		desc.setText(weapon.description);
		EditText bc = (EditText)view.findViewById(R.id.bc_value);
		bc.setText(df4.format(weapon.bullet.coefficient));
		Spinner table = (Spinner)view.findViewById(R.id.table_value);
		// See Ballistics.G[1-8], this should be usable as is.
		table.setSelection(weapon.bullet.function);
		btspinner.setSelection(weapon.rightTwist?0:1);
		EditText velocity = (EditText)view.findViewById(R.id.velocity_value);
		velocity.setText(""+df2.format((s.velSel==0?weapon.velocity:Conversions.feetToMeters(weapon.velocity))));
		EditText sightHeight = (EditText)view.findViewById(R.id.sight_height_value);
		sightHeight.setText(""+df3.format((s.sightSel==0?weapon.sightHeight:Conversions.inTocm(weapon.sightHeight))));
		EditText weight = (EditText)view.findViewById(R.id.weight_value);
		weight.setText(""+df3.format((s.weightSel==0?weapon.bullet.weight:Conversions.grainToGrams(weapon.bullet.weight))));
		EditText calibre = (EditText)view.findViewById(R.id.calibre_value);
		calibre.setText(""+df3.format((s.calSel==0?weapon.bullet.calibre:Conversions.inTomm(weapon.bullet.calibre))));
		EditText length = (EditText)view.findViewById(R.id.length_value);
		length.setText(""+df3.format((s.lenSel==0?weapon.bullet.length:Conversions.inTomm(weapon.bullet.length))));
		EditText twist = (EditText)view.findViewById(R.id.twist_value);
		twist.setText(""+df3.format((s.twistSel==0?weapon.barrelTwist:Conversions.inTocm(weapon.barrelTwist))));
		vspinner.setSelection(s.velSel);
		shspinner.setSelection(s.sightSel);
		bt2spinner.setSelection(s.twistSel);
		bwspinner.setSelection(s.weightSel);
		bcspinner.setSelection(s.calSel);
		blspinner.setSelection(s.lenSel);
	}

	public static String newName(List<WeaponDVO> weapons, int n, String prefix, String postfix) {
		String ret = prefix+n+postfix;
		for (WeaponDVO w : weapons) {
			if (w.name.equals(ret)) {
				ret = newName(weapons, n+1, prefix, postfix);
				break;
			}
		}
		return ret;
	}

	public static String newName(String prefix, String postfix, Activity activity) {
		List<WeaponDVO> weapons = WeaponDVO.allWeapons(activity);
		return newName(weapons, 1, prefix, postfix);
	}

	public void showDialog(int id) {
		if (id == DIALOG_FIX_SAVE_NAME) {
			String name = ((EditText)view.findViewById(R.id.name_value)).getText().toString();
			WepErrorDialog.newInstance("Weapon name "+name+" already used").
				show(getFragmentManager(), "dialog");
		}
		if (id == DIALOG_FIX_SAVE_NUMERIC) {
			WepErrorDialog.newInstance("Error in numeric field").
				show(getFragmentManager(), "dialog");
		}
		if (id == DIALOG_FIX_SAVE_BC_ZERO) {
			WepErrorDialog.newInstance("Ballistic Coefficient can not be 0 or negative").
				show(getFragmentManager(), "dialog");
		}
		if (id == DIALOG_FIX_SAVE_VELOCITY_ZERO) {
			WepErrorDialog.newInstance("Velocity can not be 0 or negative").
				show(getFragmentManager(), "dialog");
		}
		if (id == DIALOG_FIX_SAVE_WEIGHT_ZERO) {
			WepErrorDialog.newInstance("Bullet Weight can not be 0 or negative").
				show(getFragmentManager(), "dialog");
		}
	}
}
