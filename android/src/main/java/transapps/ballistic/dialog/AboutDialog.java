package transapps.ballistic.dialog;

import android.app.DialogFragment;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import transapps.ballistic.R;

public class AboutDialog extends DialogFragment {
	private static final String TAG = "AboutDialog";
	private Activity activity;

	@Override
	public void onAttach (Activity activity) {
		super.onAttach(activity);
		this.activity = activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(STYLE_NO_TITLE, 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.about_dialog, container, false);
		String version = "Unknown";
		if (activity != null) {
			try {
				PackageInfo pinfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
				version = pinfo.versionName;
			} catch (NameNotFoundException e) { Log.e(TAG, "Failed to get version: "+e.getMessage()); }
		}
		TextView ver = (TextView)v.findViewById(R.id.version);
		ver.setText(version);
		return v;
	}
}
