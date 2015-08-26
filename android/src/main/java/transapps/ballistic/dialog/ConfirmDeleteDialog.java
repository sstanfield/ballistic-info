package transapps.ballistic.dialog;

import android.app.DialogFragment;
import transapps.ballistic.fragment.WeaponFragment;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

public class ConfirmDeleteDialog extends DialogFragment {
	public static ConfirmDeleteDialog newInstance(String name) {
		ConfirmDeleteDialog frag = new ConfirmDeleteDialog();
		Bundle args = new Bundle();
		args.putString("name", name);
		frag.setArguments(args);
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String name = getArguments().getString("name");
		String message = "Are you sure you want to delete "+name+"?";
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(message)
		.setCancelable(false)
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int i) {
				WeaponFragment.WepDialogCallbacks wep = (WeaponFragment.WepDialogCallbacks)getActivity();
				wep.deleteWep();
			}
		}).setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int i) {
			}
		});
		return builder.create();
	}
}
