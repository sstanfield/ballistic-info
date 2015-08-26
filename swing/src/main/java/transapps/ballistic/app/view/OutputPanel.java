package transapps.ballistic.app.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.InputVerifier;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import transapps.ballistic.app.BLog;
import transapps.ballistic.app.Settings;
import transapps.ballistic.lib.Ballistics;

public class OutputPanel extends JPanel {
	private static final String TAG = "OutputPanel";

	private static final long serialVersionUID = 1972756593495514322L;

	private final UpdateViewInterface updater;
	private int unit;
	private int max;
	private int increment;
	private int drop;
	private boolean spinDrift;

	private final JLabel transonicLabel;
	private final JLabel subsonicLabel;

	public OutputPanel(boolean imperial, int m, int i, int d, boolean sd, 
			UpdateViewInterface upd) {
		super();
		this.updater = upd;
		this.unit = (imperial?1:0);
		this.max = m;
		this.increment = i;
		this.drop = d;
		this.spinDrift = sd;

		JPanel top = new JPanel();
		JComboBox unitCombo = new JComboBox(new String[] {"Metric", "Imperial"});
		if (imperial) unitCombo.setSelectedIndex(1);
		else unitCombo.setSelectedIndex(0);
		unitCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox)e.getSource();
				unit = cb.getSelectedIndex();
				if (updater != null) updater.updateView();
			}
		});
		setLayout(new BorderLayout());//new FlowLayout(FlowLayout.CENTER, 5, 5));
		top.add(unitCombo);
		JComboBox dropCombo = new JComboBox(new String[] {"Absolute", "MOA", "MIL"});
		if (drop == Settings.DROP_UNIT_RULER) dropCombo.setSelectedIndex(0);
		else if (drop == Settings.DROP_UNIT_MOA) dropCombo.setSelectedIndex(1);
		else dropCombo.setSelectedIndex(2);
		dropCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox)e.getSource();
				int d = cb.getSelectedIndex();
				if (d == 0) drop = Settings.DROP_UNIT_RULER;
				else if (d == 1) drop = Settings.DROP_UNIT_MOA;
				else drop = Settings.DROP_UNIT_MIL;
				if (updater != null) updater.updateView();
			}
		});
		JPanel t1 = new JPanel();
		t1.add(new JLabel("Max Range:"));
		JTextField maxRange = new JTextField("   "+max);
		InputVerifier maxVerify = new InputVerifier() {
			private int i;
			@Override
			public boolean verify(JComponent input) {
				String val = ((JTextField)input).getText();
				boolean ret = false;
				if (val != null) {
					val = val.trim();
					try {
						i = Integer.parseInt(val);
						if (i > 100 && i < Ballistics.MAXRANGE) ret = true;
					} catch (Exception ex) { BLog.w(TAG, "Invalid max range: "+val); }
				}
				return ret;
			}
			@Override
			public boolean shouldYieldFocus(JComponent input) {
				boolean ret = super.shouldYieldFocus(input);
				if (ret) {
					if (max != i) {
						max = i;
						if (updater != null) updater.updateView();
					}
				}
				return ret;
			}
		};
		maxRange.setInputVerifier(maxVerify);

		t1.add(maxRange);
		top.add(t1);
		t1 = new JPanel();
		t1.add(new JLabel("Range Increment:"));
		JTextField rangeIncrement = new JTextField("  "+increment);
		t1.add(rangeIncrement);
		InputVerifier incrementVerify = new InputVerifier() {
			private int i;
			@Override
			public boolean verify(JComponent input) {
				String val = ((JTextField)input).getText();
				boolean ret = false;
				if (val != null) {
					val = val.trim();
					try {
						i = Integer.parseInt(val);
						if (i > 1 && i < 1000) ret = true;
					} catch (Exception ex) { BLog.w(TAG, "Invalid range increment: "+val);}
				}
				return ret;
			}
			@Override
			public boolean shouldYieldFocus(JComponent input) {
				boolean ret = super.shouldYieldFocus(input);
				if (ret) {
					if (increment != i) {
						increment = i;
						if (updater != null) updater.updateView();
					}
				}
				return ret;
			}
		};
		rangeIncrement.setInputVerifier(incrementVerify);

		top.add(t1);
		t1 = new JPanel();
		t1.add(new JLabel("Units:"));
		t1.add(dropCombo);
		top.add(t1);

		JCheckBox chckbxSpinDrift = new JCheckBox("Spin Drift");
		chckbxSpinDrift.setToolTipText("Enable/Disable Spin Drift");
		chckbxSpinDrift.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					spinDrift = true;
					if (updater != null) updater.updateView();
				} else {
					spinDrift = false;
					if (updater != null) updater.updateView();
				}
			}
		});
		top.add(chckbxSpinDrift);
		add(top, BorderLayout.NORTH);

		JPanel stats = new JPanel();
		transonicLabel = new JLabel();
		stats.add(transonicLabel);
		subsonicLabel = new JLabel();
		stats.add(new JLabel("/"));
		stats.add(subsonicLabel);
		add(stats, BorderLayout.SOUTH);
	}

	public void setMachs(int transonic, int sonic) {
		transonicLabel.setText("Transonic (< Mach 1.2) at "+transonic+(isImperial()?" yards":" meters"));
		subsonicLabel.setText("Subsonic (< Mach 1) at "+sonic+(isImperial()?" yards":" meters"));
	}

	public boolean isImperial() {
		return unit == 1;
	}

	public int getMax() {
		return max;
	}

	public int getIncrement() {
		return increment;
	}

	public int getDrop() {
		return drop;
	}

	public boolean isSpinDrift() {
		return spinDrift;
	}
}
