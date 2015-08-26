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
import transapps.ballistic.lib.Ballistics;
import transapps.ballistic.lib.data.Weapon;
import transapps.ballistic.lib.util.Conversions;

public class WeaponPanel extends JPanel {
	private static final String TAG = "WeaponPanel";

	private static final long serialVersionUID = 6774833098631794258L;
	private Weapon wep;
	private final DecimalFormat df2 = new DecimalFormat("0.00");
	private JTextField nameField;
//	private JTextField descriptionField;
	private final JTextField velocityField;
	private final JComboBox velocityCombo;
	private final JTextField sightField;
	private final JTextField twistField;
	private final JComboBox sightCombo;
	private final JComboBox twistCombo;
	private final JComboBox twistUnitCombo;

	private UpdateViewInterface updater;

	private long r2(final double i) {
		return (long)((i + .005) * 100);
	}

	public WeaponPanel(Weapon w, UpdateViewInterface u) {
		super();
		this.wep = w;
		this.updater = u;
		setLayout(new GridLayout(0, 3, 0, 0));

		add(new JLabel("Name:"));
		nameField = new JTextField();
		add(nameField);
		nameField.setColumns(10);
		nameField.setText(wep.name);
		InputVerifier nameVerify = new InputVerifier() {
			private String val;
			@Override
			public boolean verify(JComponent input) {
				val = ((JTextField)input).getText();
				boolean ret = false;
				if (val != null) {
					val = val.trim();
					if (val.length() < 255) ret = true;
				}
				return ret;
			}
			@Override
			public boolean shouldYieldFocus(JComponent input) {
				boolean ret = super.shouldYieldFocus(input);
				if (ret) {
					if (!wep.name.equals(val)) {
						wep = new Weapon(wep.id, val, wep.description, wep.velocity,
								wep.sightHeight, wep.rightTwist, wep.barrelTwist,
								wep.zeroRange, wep.atmosphere, wep.bullet);
						if (updater != null) updater.updateView();
					}
				}
				return ret;
			}
		};
		nameField.setInputVerifier(nameVerify);
		add(new JLabel("")); // Filler...

		add(new JLabel("Velocity:"));
		velocityField = new JTextField();
		add(velocityField);
		velocityField.setColumns(10);
		velocityField.setText(""+df2.format(wep.velocity));
		velocityCombo = new JComboBox(new String[] {"ft/s", "m/s"});
		velocityCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox)e.getSource();
				int sel = cb.getSelectedIndex();
				double v = 0;
				if (sel == 0) v = wep.velocity;
				else if (sel == 1) v = Conversions.feetToMeters(wep.velocity);
				velocityField.setText(""+df2.format(v));
			}
		});
		add(velocityCombo);
		InputVerifier velocityVerify = new InputVerifier() {
			private double i;
			@Override
			public boolean verify(JComponent input) {
				String val = ((JTextField)input).getText();
				boolean ret = false;
				if (val != null) {
					val = val.trim();
					try {
						i = Double.parseDouble(val);
						if (i >= 10.0 && i <= 10000.0) ret = true;
					} catch (Exception ex) { BLog.w(TAG, "Invalid velocity: "+val); }
				}
				return ret;
			}
			@Override
			public boolean shouldYieldFocus(JComponent input) {
				boolean ret = super.shouldYieldFocus(input);
				if (ret) {
					if (velocityCombo.getSelectedIndex() == 1) i = Conversions.metersToFeet(i);
					if (r2(wep.velocity) != r2(i)) {
						wep = new Weapon(wep.id, wep.name, wep.description, i,
								wep.sightHeight, wep.rightTwist, wep.barrelTwist,
								wep.zeroRange, wep.atmosphere, wep.bullet);
						if (updater != null) updater.updateView();
					}
				}
				return ret;
			}
		};
		velocityField.setInputVerifier(velocityVerify);

		add(new JLabel("Sight Height:"));
		sightField = new JTextField();
		add(sightField);
		sightField.setColumns(10);
		sightField.setText(""+df2.format(wep.sightHeight));
		sightCombo = new JComboBox(new String[] {"in", "cm"});
		sightCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox)e.getSource();
				int sel = cb.getSelectedIndex();
				double s = 0;
				if (sel == 0) s = wep.sightHeight;
				else if (sel == 1) s = Conversions.inTocm(wep.sightHeight);
				sightField.setText(""+df2.format(s));
			}
		});
		add(sightCombo);
		InputVerifier sightVerify = new InputVerifier() {
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
					} catch (Exception ex) { BLog.w(TAG, "Invalid sight height: "+val); }
				}
				return ret;
			}
			@Override
			public boolean shouldYieldFocus(JComponent input) {
				boolean ret = super.shouldYieldFocus(input);
				if (ret) {
					if (sightCombo.getSelectedIndex() == 1) i = Conversions.cmToin(i);
					if (r2(wep.sightHeight) != r2(i)) {
						wep = new Weapon(wep.id, wep.name, wep.description, wep.velocity,
								i, wep.rightTwist, wep.barrelTwist,
								wep.zeroRange, wep.atmosphere, wep.bullet);
						if (updater != null) updater.updateView();
					}
				}
				return ret;
			}
		};
		sightField.setInputVerifier(sightVerify);

		add(new JLabel("Barrel Twist Direction:"));
		twistCombo = new JComboBox(new String[] {"Right", "Left"});
		twistCombo.setSelectedIndex(wep.rightTwist?0:1);
		twistCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox)e.getSource();
				boolean isRight = cb.getSelectedIndex()==0;
				if (wep.rightTwist != isRight) {
					wep = new Weapon(wep.id, wep.name, wep.description, wep.velocity,
							wep.sightHeight, isRight, wep.barrelTwist,
							wep.zeroRange, wep.atmosphere, wep.bullet);
					if (updater != null) updater.updateView();
				}
			}
		});
		add(twistCombo);
		add(new JLabel("")); // Filler...

		add(new JLabel("Barrel Twist (per turn):"));
		twistField = new JTextField();
		add(twistField);
		twistField.setColumns(10);
		twistField.setText(""+df2.format(wep.barrelTwist));
		twistUnitCombo = new JComboBox(new String[] {"in", "cm"});
		twistUnitCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox)e.getSource();
				int sel = cb.getSelectedIndex();
				double v = 0;
				if (sel == 0) v = wep.barrelTwist;
				else if (sel == 1) v = Conversions.inTocm(wep.barrelTwist);
				twistField.setText(""+df2.format(v));
			}
		});
		add(twistUnitCombo);
		InputVerifier twistVerify = new InputVerifier() {
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
					} catch (Exception ex) { BLog.w(TAG, "Invalid twist: "+val); }
				}
				return ret;
			}
			@Override
			public boolean shouldYieldFocus(JComponent input) {
				boolean ret = super.shouldYieldFocus(input);
				if (ret) {
					if (velocityCombo.getSelectedIndex() == 1) i = Conversions.cmToin(i);
					if (r2(wep.barrelTwist) != r2(i)) {
						wep = new Weapon(wep.id, wep.name, wep.description, wep.velocity,
								wep.sightHeight, wep.rightTwist, i,
								wep.zeroRange, wep.atmosphere, wep.bullet);
						if (updater != null) updater.updateView();
					}
				}
				return ret;
			}
		};
		twistField.setInputVerifier(twistVerify);
	}

	public void setWeapon(Weapon w) {
		this.wep = w;
		UpdateViewInterface tu = updater;
		updater = null;  // No need to notify client of changes...
		nameField.setText(wep.name);
//		descriptionField.setText(wep.description);
		if (velocityCombo.getSelectedIndex() == 1) velocityField.setText(""+df2.format(Conversions.feetToMeters(wep.velocity)));
		else velocityField.setText(""+df2.format(wep.velocity));
		if (sightCombo.getSelectedIndex() == 1) sightField.setText(""+df2.format(Conversions.inTocm(wep.sightHeight)));
		else sightField.setText(""+df2.format(wep.sightHeight));
		twistCombo.setSelectedIndex(wep.rightTwist?0:1);
		if (twistUnitCombo.getSelectedIndex() == 1) twistField.setText(""+df2.format(Conversions.inTocm(wep.barrelTwist)));
		else twistField.setText(""+df2.format(wep.barrelTwist));
		updater = tu;
	}

	public Weapon getWeapon() {
		return wep;
	}

	public void setEnabled(boolean e) {
		nameField.setEditable(e);
		velocityField.setEditable(e);
		sightField.setEditable(e);
		twistCombo.setEnabled(e);
		twistField.setEditable(e);
	}
}
