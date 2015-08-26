package transapps.ballistic.dialog;

import android.app.DialogFragment;
import transapps.ballistic.fragment.WeaponFragment;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

public class WepNewCopyDialog extends DialogFragment {
	public static WepNewCopyDialog newInstance() {
		return new WepNewCopyDialog();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String message = "Copy current weapon or create new weapon?";
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(message)
		.setCancelable(false)
		.setPositiveButton("Copy", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int i) {
				WeaponFragment.WepDialogCallbacks wep = (WeaponFragment.WepDialogCallbacks)getActivity();
				wep.copyWep();
			}
		}).setNeutralButton("New", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int i) {
				WeaponFragment.WepDialogCallbacks wep = (WeaponFragment.WepDialogCallbacks)getActivity();
				wep.newWep();
			}
		}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int i) {
			}
		});
		return builder.create();
	}
}
