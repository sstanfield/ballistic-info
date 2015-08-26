package transapps.ballistic.app.view;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.InputVerifier;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import transapps.ballistic.app.BLog;
import transapps.ballistic.lib.util.Conversions;

public class WindPanel extends JPanel {
	private static final String TAG = "WindPanel";

	private static final long serialVersionUID = -5174357762618562343L;

	private final DecimalFormat df2 = new DecimalFormat("0.00");
	private final UpdateViewInterface updater;

	private double speed;
	private double direction;
	private int angleUnit;

	private final JTextField windSpeedField;
	private final JTextField windDirectionField;
	private final JComboBox dirCombo;
	private final JComboBox speedCombo;

	private long r2(final double i) {
		return (long)((i + .005) * 100);
	}

	public WindPanel(double s, double d, int au, boolean mph, UpdateViewInterface u) {
		super();
		this.speed = s;
		this.direction = d;
		this.updater = u;
		this.angleUnit = au;

		setLayout(new GridLayout(0, 3, 0, 0));

		JLabel windSpeedLabel = new JLabel("Wind Speed:");
		add(windSpeedLabel);
		final String[] speedOptions = new String[] {"mph", "kph"};
		speedCombo = new JComboBox(speedOptions);
		speedCombo.setSelectedIndex(mph?0:1);
		speedCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox)e.getSource();
				double spd;
				if (cb.getSelectedIndex() == 0) spd = speed;
				else spd = Conversions.milesToKm(speed);
				windSpeedField.setText(""+df2.format(spd));
			}
		});
		windSpeedField = new JTextField();
		add(windSpeedField);
		windSpeedField.setColumns(10);
		windSpeedField.setText(df2.format(speed));
		InputVerifier windSpeedVerify = new InputVerifier() {
			private double i;
			@Override
			public boolean verify(JComponent input) {
				String val = ((JTextField)input).getText();
				boolean ret = false;
				if (val != null) {
					val = val.trim();
					try {
						i = Double.parseDouble(val);
						if (i >= 0.0 && i <= 300.0) ret = true;
					} catch (Exception ex) { BLog.w(TAG, "Invalid wind speed: "+val); }
				}
				return ret;
			}
			@Override
			public boolean shouldYieldFocus(JComponent input) {
				boolean ret = super.shouldYieldFocus(input);
				if (ret) {
					if (speedCombo.getSelectedIndex() == 1) i = Conversions.kmToMile(i);
					if (r2(speed) != r2(i)) {
						speed = i;
						if (updater != null) updater.updateView();
					}
				}
				return ret;
			}
		};
		windSpeedField.setInputVerifier(windSpeedVerify);
		add(speedCombo);

		JLabel windSDirectionLabel = new JLabel("Wind Direction:");
		add(windSDirectionLabel);

		final String[] dirOptions = new String[] {"Clock", "Degrees", "Mils (6400)"};
		dirCombo = new JComboBox(dirOptions);
		dirCombo.setSelectedIndex(angleUnit);
		dirCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox)e.getSource();
				angleUnit = cb.getSelectedIndex();
				String val = "";
				if (angleUnit == 0) val = Conversions.angleToClock(direction);
				else if (angleUnit == 1) val = ""+df2.format(direction);
				else if (angleUnit == 2) val = ""+df2.format(((direction/360.0)*6400));
				windDirectionField.setText(val);
			}
		});
		windDirectionField = new JTextField();
		add(windDirectionField);
		windDirectionField.setColumns(10);
		String val = "";
		if (angleUnit == 0) val = Conversions.angleToClock(direction);
		else if (angleUnit == 1) val = ""+df2.format(direction);
		else if (angleUnit == 2) val = ""+df2.format(((direction/360.0)*6400));
		windDirectionField.setText(val);
		InputVerifier windDirectionVerify = new InputVerifier() {
			private double i;
			@Override
			public boolean verify(JComponent input) {
				String val = ((JTextField)input).getText();
				boolean ret = false;
				if (val != null) {
					val = val.trim();
					double max = 360.0;
					try {
						int sel = dirCombo.getSelectedIndex();
						if (sel == 2) max = 6400.0;
						if (sel == 0) {
							String hm[] = val.split(":");
							if (hm.length == 2) {
								int h = Integer.parseInt(hm[0]);
								int m = Integer.parseInt(hm[1]);
								i = (double)(h * 30) + ((double)m / 2.0);
								ret = true;
							}
						} else {
							i = Double.parseDouble(val);
							if (i >= 0.0 && i <= max) ret = true;
							if (sel == 2) i = (i / 6400.0) * 360.0;
						}
					} catch (Exception ex) { BLog.w(TAG, "Invalid wind direction: "+val); }
				}
				return ret;
			}
			@Override
			public boolean shouldYieldFocus(JComponent input) {
				boolean ret = super.shouldYieldFocus(input);
				if (ret) {
					if (r2(direction) != r2(i)) {
						direction = i;
						if (updater != null) updater.updateView();
					}
				}
				return ret;
			}
		};
		windDirectionField.setInputVerifier(windDirectionVerify);
		add(dirCombo);

		// Filler so it lines up with the atmo panel...
		add(new JLabel(""));
	}

	public double getSpeed() {
		return speed;
	}

	public double getDirection() {
		return direction;
	}

	public int getAngleUnit() {
		return angleUnit;
	}

	public boolean isMph() {
		return speedCombo.getSelectedIndex() == 0;
	}
}
