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
import transapps.ballistic.lib.data.Bullet;
import transapps.ballistic.lib.util.Conversions;

public class BulletPanel extends JPanel {
	private static final String TAG = "BulletPanel";

	private static final long serialVersionUID = 6774833098631794258L;
	private Bullet bullet;
	private final DecimalFormat df2 = new DecimalFormat("0.00");
	private final DecimalFormat df3 = new DecimalFormat("0.000");
	private final DecimalFormat df4 = new DecimalFormat("0.0000");
	private final JComboBox tableCombo;
	private final JTextField bcField;
	private final JTextField weightField;
	private final JComboBox weightCombo;
	private final JTextField calibreField;
	private final JComboBox calibreCombo;
	private final JTextField lengthField;
	private final JComboBox lengthCombo;
	private UpdateViewInterface updater;

	private long r2(final double i) {
		return (long)((i + .005) * 100);
	}

	private long r3(final double i) {
		return (long)((i + .0005) * 1000);
	}

	private long r4(final double i) {
		return (long)((i + .00005) * 10000);
	}

	public BulletPanel(Bullet b, UpdateViewInterface u) {
		super();
		this.bullet = b;
		this.updater = u;
		JPanel panel = this;
		panel.setLayout(new GridLayout(0, 3, 0, 0));

		panel.add(new JLabel("Table:"));
		tableCombo = new JComboBox(new String[] {"G1", "G2", "G5", "G6", "G7", "G8"});
		tableCombo.setSelectedIndex(bullet.function);
		tableCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox)e.getSource();
				if (bullet.function != cb.getSelectedIndex()) {
					bullet = new Bullet(bullet.id, bullet.name, bullet.description, cb.getSelectedIndex(),
							bullet.coefficient, bullet.calibre, bullet.weight, bullet.length);
					if (updater != null) updater.updateView();
				}
			}
		});
		panel.add(tableCombo);
		panel.add(new JLabel("")); // Filler...

		panel.add(new JLabel("BC:"));
		bcField = new JTextField();
		panel.add(bcField);
		bcField.setColumns(10);
		bcField.setText(""+df4.format(bullet.coefficient));
		panel.add(new JLabel("")); // Filler...
		InputVerifier bcVerify = new InputVerifier() {
			private double i;
			@Override
			public boolean verify(JComponent input) {
				String val = ((JTextField)input).getText();
				boolean ret = false;
				if (val != null) {
					val = val.trim();
					try {
						i = Double.parseDouble(val);
						if (i >= .01 && i <= 10.0) ret = true;
					} catch (Exception ex) { BLog.w(TAG, "Invalid BC: "+val); }
				}
				return ret;
			}
			@Override
			public boolean shouldYieldFocus(JComponent input) {
				boolean ret = super.shouldYieldFocus(input);
				if (ret) {
					if (r4(i) != r4(bullet.coefficient)) {
						bullet = new Bullet(bullet.id, bullet.name, bullet.description, bullet.function,
								i, bullet.calibre, bullet.weight, bullet.length);
						if (updater != null) updater.updateView();
					}
				}
				return ret;
			}
		};
		bcField.setInputVerifier(bcVerify);

		panel.add(new JLabel("Bullet Weight:"));
		weightField = new JTextField();
		panel.add(weightField);
		weightField.setColumns(10);
		weightField.setText(""+df2.format(bullet.weight));
		weightCombo = new JComboBox(new String[] {"gr", "gm"});
		weightCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox)e.getSource();
				int sel = cb.getSelectedIndex();
				double w = 0;
				if (sel == 0) w = bullet.weight;
				else if (sel == 1) w = Conversions.grainToGrams(bullet.weight);
				weightField.setText(""+df2.format(w));
			}
		});
		panel.add(weightCombo);
		InputVerifier weightVerify = new InputVerifier() {
			private double i;
			@Override
			public boolean verify(JComponent input) {
				String val = ((JTextField)input).getText();
				boolean ret = false;
				if (val != null) {
					val = val.trim();
					try {
						i = Double.parseDouble(val);
						if (i >= .1 && i <= 5000.0) ret = true;
					} catch (Exception ex) { BLog.w(TAG, "Invalid weight: "+val); }
				}
				return ret;
			}
			@Override
			public boolean shouldYieldFocus(JComponent input) {
				boolean ret = super.shouldYieldFocus(input);
				if (ret) {
					if (weightCombo.getSelectedIndex() == 1) i = Conversions.gramsToGrains(i);
					if (r2(bullet.weight) != r2(i)) {
						bullet = new Bullet(bullet.id, bullet.name, bullet.description, bullet.function,
								bullet.coefficient, bullet.calibre, i, bullet.length);
						if (updater != null) updater.updateView();
					}
				}
				return ret;
			}
		};
		weightField.setInputVerifier(weightVerify);

		panel.add(new JLabel("Bullet Calibre:"));
		calibreField = new JTextField();
		panel.add(calibreField);
		calibreField.setColumns(10);
		calibreField.setText(""+df3.format(bullet.calibre));
		calibreCombo = new JComboBox(new String[] {"in", "mm"});
		calibreCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox)e.getSource();
				int sel = cb.getSelectedIndex();
				double w = 0;
				if (sel == 0) w = bullet.calibre;
				else if (sel == 1) w = Conversions.inTomm(bullet.calibre);
				calibreField.setText(""+df3.format(w));
			}
		});
		panel.add(calibreCombo);
		InputVerifier calibreVerify = new InputVerifier() {
			private double i;
			@Override
			public boolean verify(JComponent input) {
				String val = ((JTextField)input).getText();
				boolean ret = false;
				if (val != null) {
					val = val.trim();
					try {
						i = Double.parseDouble(val);
						if (i >= .1 && i <= 100.0) ret = true;
					} catch (Exception ex) { BLog.w(TAG, "Invalid calibre: "+val); }
				}
				return ret;
			}
			@Override
			public boolean shouldYieldFocus(JComponent input) {
				boolean ret = super.shouldYieldFocus(input);
				if (ret) {
					if (calibreCombo.getSelectedIndex() == 1) i = Conversions.mmToin(i);
					if (r3(bullet.calibre) != r3(i)) {
						bullet = new Bullet(bullet.id, bullet.name, bullet.description, bullet.function,
								bullet.coefficient, i, bullet.weight, bullet.length);
						if (updater != null) updater.updateView();
					}
				}
				return ret;
			}
		};
		calibreField.setInputVerifier(calibreVerify);

		panel.add(new JLabel("Bullet Length:"));
		lengthField = new JTextField();
		panel.add(lengthField);
		lengthField.setColumns(10);
		lengthField.setText(""+df2.format(bullet.length));
		lengthCombo = new JComboBox(new String[] {"in", "mm"});
		lengthCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox)e.getSource();
				int sel = cb.getSelectedIndex();
				double w = 0;
				if (sel == 0) w = bullet.length;
				else if (sel == 1) w = Conversions.inTomm(bullet.length);
				lengthField.setText(""+df2.format(w));
			}
		});
		panel.add(lengthCombo);
		InputVerifier lengthVerify = new InputVerifier() {
			private double i;
			@Override
			public boolean verify(JComponent input) {
				String val = ((JTextField)input).getText();
				boolean ret = false;
				if (val != null) {
					val = val.trim();
					try {
						i = Double.parseDouble(val);
						if (i >= .1 && i <= 100.0) ret = true;
					} catch (Exception ex) { BLog.w(TAG, "Invalid length: "+val); }
				}
				return ret;
			}
			@Override
			public boolean shouldYieldFocus(JComponent input) {
				boolean ret = super.shouldYieldFocus(input);
				if (ret) {
					if (lengthCombo.getSelectedIndex() == 1) i = Conversions.mmToin(i);
					if (r2(bullet.length) != r2(i)) {
						bullet = new Bullet(bullet.id, bullet.name, bullet.description, bullet.function,
								bullet.coefficient, bullet.calibre, bullet.weight, i);
						if (updater != null) updater.updateView();
					}
				}
				return ret;
			}
		};
		lengthField.setInputVerifier(lengthVerify);
	}

	public void setBullet(Bullet b) {
		this.bullet = b;
		UpdateViewInterface tu = updater;
		updater = null;  // No need to notify client of changes...
		tableCombo.setSelectedIndex(bullet.function);
		bcField.setText(""+df4.format(bullet.coefficient));
		if (weightCombo.getSelectedIndex() == 1) weightField.setText(""+df2.format(Conversions.grainToGrams(bullet.weight)));
		else weightField.setText(""+df2.format(bullet.weight));
		if (calibreCombo.getSelectedIndex() == 1) calibreField.setText(""+df3.format(Conversions.inTomm(bullet.calibre)));
		else calibreField.setText(""+df3.format(bullet.calibre));
		if (lengthCombo.getSelectedIndex() == 1) lengthField.setText(""+df2.format(Conversions.inTomm(bullet.length)));
		else lengthField.setText(""+df2.format(bullet.length));
		updater = tu;
	}

	public Bullet getBullet() {
		return bullet;
	}

	public void setEnabled(boolean e) {
		tableCombo.setEnabled(e);
		bcField.setEditable(e);
		weightField.setEditable(e);
		calibreField.setEditable(e);
		lengthField.setEditable(e);
	}
}
