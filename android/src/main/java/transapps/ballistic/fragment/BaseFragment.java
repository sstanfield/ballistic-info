package transapps.ballistic.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import transapps.ballistic.BallisticDisplayActivity;

/**
 * Base Fragment class to handle some common house keeping.
 * Created by sstanf on 4/22/14.
 */
public class BaseFragment extends Fragment {
	protected BallisticDisplayActivity activity;

	public void refresh() {  // Called when a new ballistic able is generated.

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = (BallisticDisplayActivity) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity.setFragmentTag(getTag());
	}
}
