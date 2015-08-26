package transapps.ballistic.fragment;

import java.text.DecimalFormat;

import transapps.ballistic.BallisticSettings;
import transapps.ballistic.R;
import transapps.ballistic.lib.Ballistics;
import transapps.ballistic.lib.data.RangeData;
import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

public class RangeTableAdapter implements ListAdapter {
	private final Ballistics table;
	private final int start;
	private final int end;
	private final int increment;
	private final Activity context;
	
	private DecimalFormat df0 = new DecimalFormat("0");
	private DecimalFormat df2 = new DecimalFormat("0.00");
	private DecimalFormat df4 = new DecimalFormat("0.0000");

	public RangeTableAdapter(Activity context, int start, int end, int increment) {
		table = BallisticSettings.i().table;
		this.start = start;
		this.end = end;
		this.increment = increment;
		this.context = context;
	}

	@Override
	public int getCount() {
		return ((end - start) / increment) + 1;
	}

	@Override
	public Object getItem(int position) {
		return table.getData(position * increment);
	}

	@Override
	public long getItemId(int position) {
		return position * increment;
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater)context.getSystemService
					(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.range_row, null);
		} else {
			v = convertView;
		}
		Display display = context.getWindowManager().getDefaultDisplay();
		@SuppressWarnings("deprecation")
		int cellWidth = (display.getWidth() / 8);
		BallisticSettings s = BallisticSettings.i();
		RangeData r = (RangeData)getItem(position);
		TextView range = (TextView)v.findViewById(R.id.range);
		TextView velocity = (TextView)v.findViewById(R.id.velocity);
		TextView energy = (TextView)v.findViewById(R.id.energy);
		TextView drop = (TextView)v.findViewById(R.id.drop);
		TextView tof = (TextView)v.findViewById(R.id.tof);
		TextView drift = (TextView)v.findViewById(R.id.drift);
		range.setWidth(cellWidth);
		range.setText(""+(position*increment));
		velocity.setWidth(cellWidth);
		velocity.setText(""+df0.format(r.getVelocity()));
		energy.setWidth(cellWidth);
		if (s.imperial) {
			energy.setText(""+df0.format(s.table.getEnergy((position*increment), s.getWeapon().bullet.weight)));
		} else {
			double vm = r.getVelocity();
			double grams = s.getWeapon().bullet.weight * 0.06479891;
			energy.setText(""+df0.format(.5*(grams / 1000)*(vm*vm)));
		}
		drop.setWidth(cellWidth * 2);
		if (s.dropUnits == BallisticSettings.DROP_UNIT_RULER) {
			drop.setText(""+df2.format(r.getDrop()));
		} else if (s.dropUnits == BallisticSettings.DROP_UNIT_MOA) {
			drop.setText(""+df2.format(r.getMoa()));
		} else {  // MIL
			drop.setText(""+df2.format(r.getMil()));
		}
		tof.setWidth(cellWidth + (cellWidth / 2));
		tof.setText(""+df4.format(r.getTime()));
		drift.setWidth(cellWidth + (cellWidth / 2));
		if (s.dropUnits == BallisticSettings.DROP_UNIT_RULER) {
			drift.setText(""+df2.format(r.getWindage()));
		} else if (s.dropUnits == BallisticSettings.DROP_UNIT_MOA) {
			drift.setText(""+df2.format(r.getWindageMoa()));
		} else {  // MIL
			drift.setText(""+df2.format(r.getWindageMil()));
		}

		return v;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}

}
