package transapps.ballistic.app.view;

import transapps.ballistic.app.BLog;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class AnglePanel extends JPanel {
	private static final String TAG = "AnglePanel";

	private static final long serialVersionUID = 5872112775494489482L;

	private final UpdateViewInterface updater;

	private int angle;

	public AnglePanel(int a, UpdateViewInterface u) {
		super();
		this.angle = a;
		this.updater = u;

		JLabel angleLabel = new JLabel("Angle (positive uphill, negative downhill):");
		add(angleLabel);
		JTextField angleField = new JTextField();
		angleField.setText(""+angle);
		add(angleField);
		angleField.setColumns(10);
		InputVerifier angleVerify = new InputVerifier() {
			private int i;
			@Override
			public boolean verify(JComponent input) {
				String val = ((JTextField)input).getText();
				boolean ret = false;
				if (val != null) {
					val = val.trim();
					try {
						i = Integer.parseInt(val);
						if (i >= -90 && i <= 90) ret = true;
					} catch (Exception ex) { BLog.w(TAG, "Invalid angle: " + val); }
				}
				return ret;
			}
			@Override
			public boolean shouldYieldFocus(JComponent input) {
				boolean ret = super.shouldYieldFocus(input);
				if (ret) {
					if (angle != i) {
						angle = i;
						if (updater != null) updater.updateView();
					}
				}
				return ret;
			}
		};
		angleField.setInputVerifier(angleVerify);
	}

	public int getAngle() {
		return angle;
	}
}
