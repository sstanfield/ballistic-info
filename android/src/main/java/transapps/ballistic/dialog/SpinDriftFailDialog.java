package transapps.ballistic.dialog;

import android.app.DialogFragment;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

public class SpinDriftFailDialog extends DialogFragment {
	public static SpinDriftFailDialog newInstance() {
		return new SpinDriftFailDialog();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String message = "Lack data to estimate spin drift, add bullet calibre, weight, length and barrel twist info!";
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(message)
		.setCancelable(false)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int i) {
			}
		});
		return builder.create();
	}
}
