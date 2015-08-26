package transapps.ballistic.app.view;

import transapps.ballistic.app.BLog;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;

import javax.swing.InputVerifier;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class CoriolisPanel extends JPanel {
	private static final String TAG = "CoriolisPanel";

	private static final long serialVersionUID = -6839417999560890161L;
	private final DecimalFormat df2 = new DecimalFormat("0.00");

	private final UpdateViewInterface updater;

	private double latitude;
	private double azimuth;
	private boolean on;

	private long r2(final double i) {
		return (long)((i + .005) * 100);
	}

	public CoriolisPanel(double l, double a, boolean o, UpdateViewInterface u) {
		super();
		this.updater = u;
		this.latitude = l;
		this.azimuth = a;
		this.on = o;

		JCheckBox onCheck = new JCheckBox("Coriolis On");
		onCheck.setToolTipText("Enable/Disable Coriolis Effect");
		onCheck.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					on = true;
					if (updater != null) updater.updateView();
				} else {
					on = false;
					if (updater != null) updater.updateView();
				}
			}
		});
		add(onCheck);

		JLabel latLabel = new JLabel("Latitude:");
		add(latLabel);
		JTextField latField = new JTextField();
		latField.setText(""+df2.format(latitude));
		add(latField);
		latField.setColumns(10);
		InputVerifier latVerify = new InputVerifier() {
			private double i;
			@Override
			public boolean verify(JComponent input) {
				String val = ((JTextField)input).getText();
				boolean ret = false;
				if (val != null) {
					val = val.trim();
					try {
						i = Double.parseDouble(val);
						if (i >= -90 && i <= 90) ret = true;
					} catch (Exception ex) { BLog.w(TAG, "Invalid lat: " + val); }
				}
				return ret;
			}
			@Override
			public boolean shouldYieldFocus(JComponent input) {
				boolean ret = super.shouldYieldFocus(input);
				if (ret) {
					if (r2(latitude) != r2(i)) {
						latitude = i;
						if (updater != null) updater.updateView();
					}
				}
				return ret;
			}
		};
		latField.setInputVerifier(latVerify);

		JLabel azLabel = new JLabel("Azimuth:");
		add(azLabel);
		JTextField azField = new JTextField();
		azField.setText(""+df2.format(azimuth));
		add(azField);
		azField.setColumns(10);
		InputVerifier azVerify = new InputVerifier() {
			private double i;
			@Override
			public boolean verify(JComponent input) {
				String val = ((JTextField)input).getText();
				boolean ret = false;
				if (val != null) {
					val = val.trim();
					try {
						i = Double.parseDouble(val);
						if (i >= 0 && i <= 360) ret = true;
					} catch (Exception ex) { BLog.w(TAG, "Invalid azimuth: "+val); }
				}
				return ret;
			}
			@Override
			public boolean shouldYieldFocus(JComponent input) {
				boolean ret = super.shouldYieldFocus(input);
				if (ret) {
					if (r2(azimuth) != r2(i)) {
						azimuth = i;
						if (updater != null) updater.updateView();
					}
				}
				return ret;
			}
		};
		azField.setInputVerifier(azVerify);
	}

	public double getLatitude() {
		return latitude;
	}

	public double getAzimuth() {
		return azimuth;
	}

	public boolean isOn() {
		return on;
	}
}
