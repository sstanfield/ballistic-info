package transapps.ballistic;

import android.app.Activity;
import android.app.Application;

public class BallisticApplication extends Application {
	private static BallisticApplication instance;
	private Activity activity;

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
	}

	public static BallisticApplication i() {
		return instance;
	}

	public void setActivity(Activity a) {
		activity = a;
	}

	public Activity getActivity() {
		return activity;
	}
}
