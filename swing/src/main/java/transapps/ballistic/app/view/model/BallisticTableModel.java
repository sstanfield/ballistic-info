package transapps.ballistic.app.view.model;

import java.text.DecimalFormat;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import transapps.ballistic.app.Settings;
import transapps.ballistic.lib.data.RangeData;

public class BallisticTableModel implements TableModel {
	private DecimalFormat df0 = new DecimalFormat("0");
	private DecimalFormat df2 = new DecimalFormat("0.00");
	private DecimalFormat df4 = new DecimalFormat("0.0000");

	private final int start;
	private final int end;
	private final int increment;
	private final Settings s;

	public BallisticTableModel(Settings settings, int start,
			int end, int increment) {
		s = settings;
		this.start = start;
		this.end = end;
		this.increment = increment;
	}

	@Override
	public int getRowCount() {
		return ((end - start) / increment) + 1;
	}

	@Override
	public int getColumnCount() {
		return 6;
	}

	@Override
	public String getColumnName(int columnIndex) {
		String name = "NA";
		String dropUnit = "";
		if (s.dropUnits == Settings.DROP_UNIT_RULER) {
			if (s.imperial) {
				dropUnit = "IN";
			} else {
				dropUnit = "CM";
			}
		} else if (s.dropUnits == Settings.DROP_UNIT_MOA) {
			dropUnit = "MOA";
		} else {  // MIL
			dropUnit = "MIL";
		}

		switch (columnIndex) {
		case 0:
			if (s.imperial) name = "Range (yards)";
			else name = "Range (meters)";
			break;
		case 1:
			if (s.imperial) name = "Speed (ft/s)";
			else name = "Speed (m/s)";
			break;
		case 2:
			if (s.imperial) name = "Energy (ft-lb)";
			else  name = "Energy (Joules)";
			break;
		case 3:
			name = "Drop ("+dropUnit+")";
			break;
		case 4:
			name = "TOF (sec)";
			break;
		case 5:
			name = "Drift ("+dropUnit+")";
			break;
		}
		return name;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		String ret = "";
		RangeData r = s.table.getData((rowIndex * increment) + start);
		switch (columnIndex) {
		case 0:
			ret = ""+((rowIndex*increment) + start);
			break;
		case 1:
			ret = ""+df0.format(r.getVelocity());
			break;
		case 2:
			if (s.imperial) {
				ret = ""+df0.format(s.table.getEnergy((rowIndex*increment) + start, s.weapon.bullet.weight));
			} else {
				double vm = r.getVelocity();
				double grams = s.weapon.bullet.weight * 0.06479891;
				ret = ""+df0.format(.5*(grams / 1000)*(vm*vm));
			}
			break;
		case 3:
			if (s.dropUnits == Settings.DROP_UNIT_RULER) {
				ret = ""+df2.format(r.getDrop());
			} else if (s.dropUnits == Settings.DROP_UNIT_MOA) {
				ret = ""+df2.format(r.getMoa());
			} else {  // MIL
				ret = ""+df2.format(r.getMil());
			}
			break;
		case 4:
			ret = ""+df4.format(r.getTime());
			break;
		case 5:
			if (s.dropUnits == Settings.DROP_UNIT_RULER) {
				ret = ""+df2.format(r.getWindage());
			} else if (s.dropUnits == Settings.DROP_UNIT_MOA) {
				ret = ""+df2.format(r.getWindageMoa());
			} else {  // MIL
				ret = ""+df2.format(r.getWindageMil());
			}
			break;
		}
		return ret;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	}

	@Override
	public void addTableModelListener(TableModelListener l) {
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
	}

}
