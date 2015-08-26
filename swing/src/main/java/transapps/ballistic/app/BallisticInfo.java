package transapps.ballistic.app;

import javax.swing.SwingUtilities;

import transapps.ballistic.app.view.MainView;
import transapps.ballistic.db.BallisticDBHelper;
import transapps.ballistic.lib.data.Atmosphere;

public class BallisticInfo {

	public static void main(String[] args) {
		final Settings s = new Settings(3000, 0, 0, false, 0.0, 0.0, Data.defaultWeapons[0],
				Atmosphere.ICAO_STANDARD, false, 0.0, 0.0, 0, false, false);

		BallisticDBHelper.i();  // Get database created/connected/up.
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			@Override
			public void run()
			{
				BallisticDBHelper.i().close();
			}
		});

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				MainView main = new MainView(s);
				main.setVisible(true);
			}
		});
	}

}
