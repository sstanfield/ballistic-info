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
import transapps.ballistic.lib.data.Atmosphere;
import transapps.ballistic.lib.util.Conversions;

public class AtmoPanel extends JPanel {
	private static final String TAG = "AtmoPanel";

	private static final long serialVersionUID = 6074479039167865581L;

	private final DecimalFormat df2 = new DecimalFormat("0.00");
	private final DecimalFormat df5 = new DecimalFormat("0.00000");

	private Atmosphere atmo;
	private UpdateViewInterface updater;

	private final JTextField pressureField;
	private final JTextField tempField;
	private final JTextField humidityField;
	private final JComboBox tempCombo;
	private final JComboBox barCombo;
	private final JLabel densityField;
	private final JComboBox densityCombo;
	private final JLabel machField;
	private final JComboBox machCombo;

	private long r2(final double i) {
		return (long)((i + .005) * 100);
	}

	private void displayStatic() {
		int sel = densityCombo.getSelectedIndex();
		double density = 0;
		if (sel == 0) density = atmo.density;
		else if (sel == 1) density = Conversions.lbft3Tokgm3(atmo.density);
		densityField.setText(""+df5.format(density));

		sel = machCombo.getSelectedIndex();
		double mach = 0;
		if (sel == 0) mach = atmo.mach;
		else if (sel == 1) mach = Conversions.feetToMeters(atmo.mach);
		machField.setText(""+df2.format(mach));
	}

	public AtmoPanel(Atmosphere a, UpdateViewInterface u) {
		super();
		atmo = a;
		updater = u;

		setLayout(new GridLayout(0, 3, 0, 0));

		JLabel pressureLabel = new JLabel("Barometer:");
		add(pressureLabel);
		final String[] barOptions = new String[] {"in/hg", "mm/hg"};
		barCombo = new JComboBox(barOptions);
		barCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox)e.getSource();
				int sel = cb.getSelectedIndex();
				double pres = 0;
				if (sel == 0) pres = atmo.pressure;
				else if (sel == 1) pres = Conversions.inhgTommhg(atmo.pressure);
				pressureField.setText(""+df2.format(pres));
			}
		});
		pressureField = new JTextField();
		add(pressureField);
		pressureField.setColumns(10);
		pressureField.setText(df2.format(atmo.pressure));
		InputVerifier pressureVerify = new InputVerifier() {
			private double i;
			@Override
			public boolean verify(JComponent input) {
				String val = ((JTextField)input).getText();
				boolean ret = false;
				if (val != null) {
					val = val.trim();
					try {
						double max = 40.0;
						if (barCombo.getSelectedIndex() == 1) max = 1000.0;
						i = Double.parseDouble(val);
						if (i >= 0.0 && i <= max) ret = true;
					} catch (Exception ex) { BLog.w(TAG, "Invalid pressure: " + val); }
				}
				return ret;
			}
			@Override
			public boolean shouldYieldFocus(JComponent input) {
				boolean ret = super.shouldYieldFocus(input);
				if (ret) {
					int sel = barCombo.getSelectedIndex();
					if (sel == 1) i = Conversions.mmhgToinhg(i);
					if (r2(atmo.pressure) != r2(i)) {
						atmo = new Atmosphere("", i, atmo.temperature, atmo.humidity);
						displayStatic();
						if (updater != null) updater.updateView();
					}
				}
				return ret;
			}
		};
		pressureField.setInputVerifier(pressureVerify);
		add(barCombo);

		JLabel tempLabel = new JLabel("Temp:");
		add(tempLabel);
		final String[] tempOptions = new String[] {"F", "C"};
		tempCombo = new JComboBox(tempOptions);
		tempCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox)e.getSource();
				int sel = cb.getSelectedIndex();
				double temp =59.0;
				if (sel == 0) temp = atmo.temperature;
				else if (sel == 1) temp = Conversions.fahrenheitToCelsius(atmo.temperature);
				tempField.setText(""+df2.format(temp));
			}
		});
		tempField = new JTextField();
		add(tempField);
		tempField.setColumns(10);
		tempField.setText(df2.format(atmo.temperature));
		InputVerifier tempVerify = new InputVerifier() {
			private double i;
			@Override
			public boolean verify(JComponent input) {
				String val = ((JTextField)input).getText();
				boolean ret = false;
				if (val != null) {
					val = val.trim();
					try {
						i = Double.parseDouble(val);
						if (i >= -100.0 && i <= 200.0) ret = true;
					} catch (Exception ex) { BLog.w(TAG, "Invalid temp: "+val); }
				}
				return ret;
			}
			@Override
			public boolean shouldYieldFocus(JComponent input) {
				boolean ret = super.shouldYieldFocus(input);
				if (ret) {
					int sel = tempCombo.getSelectedIndex();
					if (sel == 1) i = Conversions.celsiusToFahrenheit(i);
					if (r2(atmo.temperature) != r2(i)) {
						atmo = new Atmosphere("", atmo.pressure, i, atmo.humidity);
						displayStatic();
						if (updater != null) updater.updateView();
					}
				}
				return ret;
			}
		};
		tempField.setInputVerifier(tempVerify);
		add(tempCombo);

		JLabel humidityLabel = new JLabel("Humidity:");
		add(humidityLabel);
		humidityField = new JTextField();
		add(humidityField);
		humidityField.setColumns(10);
		humidityField.setText(df2.format(atmo.humidity));
		InputVerifier humidityVerify = new InputVerifier() {
			private double i;
			@Override
			public boolean verify(JComponent input) {
				String val = ((JTextField)input).getText();
				boolean ret = false;
				if (val != null) {
					val = val.trim();
					try {
						i = Double.parseDouble(val);
						if (i >= 0.0 && i <= 100.0) ret = true;
					} catch (Exception ex) { BLog.w(TAG, "Invalid humidity: "+val); }
				}
				return ret;
			}
			@Override
			public boolean shouldYieldFocus(JComponent input) {
				boolean ret = super.shouldYieldFocus(input);
				if (ret) {
					if (r2(atmo.humidity) != r2(i)) {
						atmo = new Atmosphere("", atmo.pressure, atmo.temperature, i);
						displayStatic();
						if (updater != null) updater.updateView();
					}
				}
				return ret;
			}
		};
		humidityField.setInputVerifier(humidityVerify);
		add(new JLabel());  // Filler.

		JLabel densityLabel = new JLabel("Air Density:");
		add(densityLabel);
		densityField = new JLabel();
		densityField.setText(df5.format(atmo.density));
		add (densityField);
		final String[] densityOptions = new String[] {"lb/ft^3", "kg/m^3"};
		densityCombo = new JComboBox(densityOptions);
		densityCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				displayStatic();
			}
		});
		add(densityCombo);

		JLabel machLabel = new JLabel("Mach 1:");
		add(machLabel);
		machField = new JLabel();
		machField.setText(df5.format(atmo.density));
		add (machField);
		final String[] machOptions = new String[] {"ft/s", "m/s"};
		machCombo = new JComboBox(machOptions);
		machCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				displayStatic();
			}
		});
		add(machCombo);

		displayStatic();
	}

	public void setAtmo(Atmosphere a) {
		atmo = a;
		UpdateViewInterface tu = updater;
		updater = null;

		double pres = 0;
		int sel = barCombo.getSelectedIndex();
		if (sel == 0) pres = atmo.pressure;
		else if (sel == 1) pres = Conversions.inhgTommhg(atmo.pressure);
		pressureField.setText(df2.format(pres));
		double temp = 0;
		sel = tempCombo.getSelectedIndex();
		if (sel == 0) temp = atmo.temperature;
		else if (sel == 1) temp = Conversions.fahrenheitToCelsius(atmo.temperature);
		tempField.setText(df2.format(temp));
		humidityField.setText(df2.format(atmo.humidity));
		displayStatic();
		updater = tu;
	}

	public Atmosphere getAtmo() {
		return atmo;
	}
}
