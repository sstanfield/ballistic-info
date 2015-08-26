package transapps.ballistic.dialog;

import android.app.DialogFragment;
import transapps.ballistic.fragment.WeaponFragment;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Used for error dialogs for a WeaponFragment.  Don't use except from a
 * WeaponFragment.
 */
public class WepErrorDialog extends DialogFragment {
	public static WepErrorDialog newInstance(String message) {
		WepErrorDialog frag = new WepErrorDialog();
		Bundle args = new Bundle();
		args.putString("message", message);
		frag.setArguments(args);
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String message = getArguments().getString("message");
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(message+", abort changes or correct edit?")
		.setCancelable(false)
		.setPositiveButton("Correct", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int i) {
			}
		}).setNegativeButton("Abort", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int i) {
				WeaponFragment.WepDialogCallbacks wep = (WeaponFragment.WepDialogCallbacks)getActivity();
				wep.deleteWepIfNew();
			}
		});
		return builder.create();
	}
}
