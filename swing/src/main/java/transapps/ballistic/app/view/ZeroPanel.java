package transapps.ballistic.app.view;

import java.text.DecimalFormat;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import transapps.ballistic.app.BLog;
import transapps.ballistic.lib.Ballistics;
import transapps.ballistic.lib.data.Atmosphere;
import transapps.ballistic.lib.util.Conversions;

public class ZeroPanel extends JPanel {
	private static final String TAG = "ZeroPanel";

	private static final long serialVersionUID = -8005874393266238902L;
	private final DecimalFormat df2 = new DecimalFormat("0.00");

	private UpdateViewInterface updater;

	private boolean imperial;
	private double range;
	private Atmosphere atmo;

	private final JTextField rangeField;
	private final JLabel rangeUnitLabel;

	private final AtmoPanel zeroAtmoPanel;

	private long r2(final double i) {
		return (long)((i + .005) * 100);
	}

	public ZeroPanel(boolean i, double r, Atmosphere a, UpdateViewInterface u) {
		super();
		this.updater = u;
		this.atmo = a;
		this.range = r;

		rangeUnitLabel = new JLabel("Meters");
		JLabel rangeLabel = new JLabel("Zero Range:");
		add(rangeLabel);
		rangeField = new JTextField();
		rangeField.setText("");
		add(rangeField);
		rangeField.setColumns(10);
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
					if (!imperial) i = Conversions.metersToYards(i);
					if (r2(range) != r2(i)) {
						range = i;
						if (updater != null) updater.updateView();
					}
				}
				return ret;
			}
		};
		rangeField.setInputVerifier(rangeVerify);
		add(rangeUnitLabel);
		zeroAtmoPanel = new AtmoPanel(atmo, updater);
		zeroAtmoPanel.setBorder(new TitledBorder(null, "Atmosphere", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(zeroAtmoPanel);
		setImperial(i);
	}

	public void setData(boolean i, double r, Atmosphere a) {
		UpdateViewInterface tu = updater;
		updater = null;
		range = r;
		setImperial(i);
		atmo = a;
		zeroAtmoPanel.setAtmo(atmo);
		updater = tu;
	}

	public void setImperial(boolean imperial) {
		this.imperial = imperial;
		UpdateViewInterface tu = updater;
		updater = null;
		if (!imperial) {
			rangeUnitLabel.setText("Meters");
			rangeField.setText(""+df2.format(Conversions.yardsToMeters(range)));
		} else {
			rangeUnitLabel.setText("Yards");
			rangeField.setText(""+df2.format(range));
		}
		updater = tu;
	}

	public double getRange() {
		return range;
	}

	public Atmosphere getAtmo() {
		return zeroAtmoPanel.getAtmo();
	}
}
