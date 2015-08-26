package transapps.ballistic.app.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import transapps.ballistic.app.BLog;
import transapps.ballistic.app.Settings;
import transapps.ballistic.db.BallisticDBHelper;
import transapps.ballistic.lib.Ballistics;
import transapps.ballistic.lib.data.Bullet;
import transapps.ballistic.lib.data.Coriolis;
import transapps.ballistic.lib.data.Weapon;
import transapps.ballistic.lib.util.Conversions;

public class TruingPanel extends JPanel {
	private static final String TAG = "TruingPanel";

	private static final long serialVersionUID = -5648410561046562238L;

	private final DecimalFormat df2 = new DecimalFormat("0.00");
	private final DecimalFormat df4 = new DecimalFormat("0.0000");

	private double range = 100.0;
	private int truRange = 100;
	private double mil = 0.0;
	private int transonic = 0;

	private Settings s;
	private final JTextField rangeField;
	private final JComboBox rangeCombo;
	private final JTextField trueField;
	private final JTextArea trueRangeLabel;
	private final JTextField elevationField;
	private final JLabel newbcField;
	private final JLabel newvelocityField;
	private final JComboBox elevationCombo;
	private final JButton saveButton;
	private final JComboBox velocityCombo;

	private final UpdateViewInterface updater;
	private final WeaponPanel wepPanel;
	private final BulletPanel bulletPanel;

	private double newBC;
	private double newVelocity;

	private long r2(final double i) {
		return (long)((i + .005) * 100);
	}

	private long r4(final double i) {
		return (long)((i + .00005) * 10000);
	}

	private void displayVelocity(double velocity) {
		int vsel = velocityCombo.getSelectedIndex();
		double v = 0;
		if (vsel == 0) v = velocity;
		else if (vsel == 1) v = Conversions.feetToMeters(velocity);
		newvelocityField.setText("Velocity: "+df2.format(v));
	}

	private void calcBC() {
		newBC = s.weapon.bullet.coefficient;
		newVelocity = s.weapon.velocity;
		Weapon nw = s.weapon.newZero(Conversions.metersToYards(range), s.atmo);
		boolean dirup = true;
		double factor = .1;
		boolean done = false;
		boolean failed = false;
		while (!done) {
			Ballistics t = Ballistics.getBallistics(nw, s.atmo, s.shootingAngle,
					s.windSpeed, s.windDirection, truRange, s.imperial?Ballistics.UNITS.IMPERIAL:Ballistics.UNITS.METRIC,
					Ballistics.zeroAngle(nw),
					s.coriolisOn?new Coriolis(s.latitude, s.azimuth, nw.velocity):null);
			double m = -t.getData(truRange).getMil();
			if (r4(mil) == r4(m)) done = true;
			else {
				if (mil < m) {
					if (!dirup) factor /= 2;
					newBC += factor;
					dirup = true;
				} else {
					if (dirup) factor /= 2;
					newBC -= factor;
					dirup = false;
				}
				Bullet b = new Bullet("", "", nw.bullet.function, newBC,
						nw.bullet.calibre, nw.bullet.weight, nw.bullet.length);
				nw = nw.newBullet(b);
				// Check for failures, break out...
				if (newBC < .01 || newBC > 2.0) {
					done = true;
					failed = true;
				}
			}
		}
		if (!failed) {
			newbcField.setText("BC: "+df4.format(newBC));
			displayVelocity(nw.velocity);
			saveButton.setEnabled(!BallisticDBHelper.isDefaultWeapon(s.weapon));
		} else {
			newbcField.setText("BC: Failed");
			displayVelocity(nw.velocity);
			saveButton.setEnabled(false);
		}
	}

	private void calcVelocity() {
		newBC = s.weapon.bullet.coefficient;
		newVelocity = s.weapon.velocity;
		Weapon nw = s.weapon.newZero(Conversions.metersToYards(range), s.atmo);
		boolean dirup = true;
		double factor = 100;
		boolean done = false;
		boolean failed = false;
		while (!done) {
			Ballistics t = Ballistics.getBallistics(nw, s.atmo, s.shootingAngle,
					s.windSpeed, s.windDirection, truRange, s.imperial?Ballistics.UNITS.IMPERIAL:Ballistics.UNITS.METRIC,
					Ballistics.zeroAngle(nw),
					s.coriolisOn?new Coriolis(s.latitude, s.azimuth, nw.velocity):null);
			double m = -t.getData(truRange).getMil();
			if (r4(mil) == r4(m)) done = true;
			else {
				if (mil < m) {
					if (!dirup) factor /= 2;
					newVelocity += factor;
					dirup = true;
				} else {
					if (dirup) factor /= 2;
					newVelocity -= factor;
					dirup = false;
				}
				nw = nw.newVelocity(newVelocity);
			}
			if (newVelocity < 0 || newVelocity > 10000) {
				done = true;
				failed = true;
			}
		}
		if (!failed) {
			newbcField.setText("BC: "+df4.format(nw.bullet.coefficient));
			displayVelocity(newVelocity);
			saveButton.setEnabled(!BallisticDBHelper.isDefaultWeapon(s.weapon));
		} else {
			newbcField.setText("BC: "+df4.format(nw.bullet.coefficient));
			newvelocityField.setText("Velocity: Failed");
			saveButton.setEnabled(false);
		}
	}

	private void save() {
		Weapon w = s.weapon.newVelocity(newVelocity);
		Bullet b = new Bullet("", "", w.bullet.function, newBC,
				w.bullet.calibre, w.bullet.weight, w.bullet.length);
		w = w.newBullet(b);
		wepPanel.setWeapon(w);
		bulletPanel.setBullet(b);
		s = s.newWeapon(w);
		if (updater != null) updater.updateView();
	}

	public TruingPanel(Settings s, UpdateViewInterface upd, WeaponPanel wepPanel, BulletPanel bulletPanel) {
		super();
		this.s = s;
		this.updater = upd;
		this.wepPanel = wepPanel;
		this.bulletPanel = bulletPanel;

		setLayout(new BorderLayout());
		JPanel p1 = new JPanel(new GridLayout(0, 6, 0, 0));
		final JLabel trueunitLabel = new JLabel("meters");
		JLabel zeroLabel = new JLabel("Zero range:");
		p1.add(zeroLabel);
		rangeField = new JTextField();
		rangeField.setText(df2.format(range));
		p1.add(rangeField);
		rangeField.setColumns(10);
		final String[] rangeOptions = new String[] {"meters", "yards"};
		rangeCombo = new JComboBox(rangeOptions);
		rangeCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox)e.getSource();
				int sel = cb.getSelectedIndex();
				double r = 0;
				if (sel == 0) {
					r = range;
					trueunitLabel.setText("meters");
				} else {
					if (sel == 1) r = Conversions.metersToYards(range);
					trueunitLabel.setText("yards");
				}
				rangeField.setText(""+df2.format(r));
				setTransonic(false, transonic, TruingPanel.this.s);
			}
		});
		p1.add(rangeCombo);

		InputVerifier rangeVerify = new InputVerifier() {
			private double i;
			@Override
			public boolean verify(JComponent input) {
				String val = ((JTextField)input).getText();
				boolean ret = false;
				if (val != null) {
					val = val.trim();
					try {
						i = Double.parseDouble(val);
						if (i >= 0.0 && i <= Ballistics.MAXRANGE) ret = true;
					} catch (Exception ex) { BLog.w(TAG, "Invalid range: "+val); }
				}
				return ret;
			}
			@Override
			public boolean shouldYieldFocus(JComponent input) {
				boolean ret = super.shouldYieldFocus(input);
				if (ret) {
					int sel = rangeCombo.getSelectedIndex();
					if (sel == 1) i = Conversions.yardsToMeters(i);
					if (r2(range) != r2(i)) {
						range = i;
					}
				}
				return ret;
			}
		};
		rangeField.setInputVerifier(rangeVerify);

		JLabel trueLabel = new JLabel("Truing range:");
		p1.add(trueLabel);
		trueField = new JTextField();
		trueField.setText(df2.format(transonic));
		p1.add(trueField);
		trueField.setColumns(10);
		p1.add(trueunitLabel);

		InputVerifier trueVerify = new InputVerifier() {
			private int i;
			@Override
			public boolean verify(JComponent input) {
				String val = ((JTextField)input).getText();
				boolean ret = false;
				if (val != null) {
					val = val.trim();
					try {
						i = Integer.parseInt(val);
						if (i >= 0 && i <= Ballistics.MAXRANGE) ret = true;
					} catch (Exception ex) { BLog.w(TAG, "Invalid true range: "+val); }
				}
				return ret;
			}
			@Override
			public boolean shouldYieldFocus(JComponent input) {
				boolean ret = super.shouldYieldFocus(input);
				if (ret) {
					int sel = rangeCombo.getSelectedIndex();
					if (sel == 1) i = (int)(Conversions.yardsToMeters(i)+.5);
					if (r2(range) != r2(i)) {
						truRange = i;
					}
				}
				return ret;
			}
		};
		trueField.setInputVerifier(trueVerify);
		trueRangeLabel = new JTextArea();

		JLabel elevationLabel = new JLabel("Elevation to target:");
		p1.add(elevationLabel);
		elevationField = new JTextField();
		elevationField.setText("");
		p1.add(elevationField);
		elevationField.setColumns(10);
		final String[] elevationOptions = new String[] {"MIL", "MOA"};
		elevationCombo = new JComboBox(elevationOptions);
		elevationCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox)e.getSource();
				int sel = cb.getSelectedIndex();
				double r = 0;
				if (sel == 0) {
					r = mil;
				} else {
					if (sel == 1) r = mil * 3.438;
				}
				elevationField.setText(""+df2.format(r));
			}
		});
		p1.add(elevationCombo);

		InputVerifier elevationVerify = new InputVerifier() {
			private double i;
			@Override
			public boolean verify(JComponent input) {
				String val = ((JTextField)input).getText();
				boolean ret = false;
				if (val != null) {
					val = val.trim();
					try {
						i = Double.parseDouble(val);
						if (i >= 0.0 && i <= 200.0) ret = true;
					} catch (Exception ex) { BLog.w(TAG, "Invalid elevation: "+val); }
				}
				return ret;
			}
			@Override
			public boolean shouldYieldFocus(JComponent input) {
				boolean ret = super.shouldYieldFocus(input);
				if (ret) {
					int sel = elevationCombo.getSelectedIndex();
					if (sel == 1) i = i / 3.438;
					if (r2(mil) != r2(i)) {
						mil = i;
					}
				}
				return ret;
			}
		};
		elevationField.setInputVerifier(elevationVerify);

		JButton calcButton = new JButton("Calculate BC");
		calcButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				calcBC();
			}
		});
		newbcField = new JLabel();
		p1.add(newbcField);

		JButton vcalcButton = new JButton("Calculate Velocity");
		vcalcButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				calcVelocity();
			}
		});
		newvelocityField = new JLabel();
		p1.add(newvelocityField);
		velocityCombo = new JComboBox(new String[] {"ft/s", "m/s"});
		velocityCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				displayVelocity(newVelocity);
			}
		});
		p1.add(velocityCombo);
		p1.add(calcButton);
		p1.add(vcalcButton);
		saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});
		p1.add(saveButton);

		add(p1, BorderLayout.NORTH);
		trueRangeLabel.setRows(5);
		JScrollPane infoScroll = new JScrollPane(trueRangeLabel);
		add(infoScroll, BorderLayout.SOUTH);
	}

	private void setInfoText() {
		String info = (BallisticDBHelper.isDefaultWeapon(s.weapon)?"NOTE: Must copy weapon to save (default weapon selected).\n":"")+
				"- Set zero used for trueing (generally use 100 meters)\n"+
				"- Set Trueing range, should be transonic (mach 1.2) +/- 50, "+
				(transonic - 50)+" to "+(transonic+50)+"\n"+
				"- Select elevation required to hit minimal sized target at range (either MIL or MAO)\n"+
				"- Select a Calculate button, if initial velocity is trusted then calculate BC,\n"+
				"  if BC is trusted selected velocity\n"+
				"- If satisfied with result select save to update your current weapon to new velocity or BC\n"+
				"NOTE: you need to set the atmospheric conditions for when test is fired on atmosperic tab.\n"+
				"  These settings are used for both zero and range shots (i.e. zero rifle on same day as truing).";
		trueRangeLabel.setText(info);
	}

	public void setTransonic(boolean imperial, int transonic, Settings s) {
		this.s = s;
		this.transonic = imperial?(int)(Conversions.yardsToMeters(transonic)+.5):transonic;
		int t = transonic;
		if (rangeCombo.getSelectedIndex() == 1) t = (int)(Conversions.yardsToMeters(transonic)+.5);
		trueField.setText(df2.format(t));
		truRange = t;
		setInfoText();
		int esel = elevationCombo.getSelectedIndex();

		Weapon nw = s.weapon.newZero(imperial?range:Conversions.metersToYards(range), s.atmo);
		Ballistics table = Ballistics.getBallistics(nw, s.atmo, s.shootingAngle,
				s.windSpeed, s.windDirection, transonic+50, imperial?Ballistics.UNITS.IMPERIAL:Ballistics.UNITS.METRIC,
				Ballistics.zeroAngle(nw),
				s.coriolisOn?new Coriolis(s.latitude, s.azimuth, nw.velocity):null);

		mil = -table.getData(transonic).getMil();
		elevationField.setText(df2.format(esel==0?-table.getData(transonic).getMil():-table.getData(transonic).getMoa()));
		newbcField.setText("BC: "+df4.format(s.weapon.bullet.coefficient));
		newVelocity = s.weapon.velocity;
		displayVelocity(s.weapon.velocity);
		saveButton.setEnabled(false);
	}
}
