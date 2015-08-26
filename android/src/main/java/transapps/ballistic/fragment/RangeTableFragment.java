package transapps.ballistic.fragment;

import transapps.ballistic.BallisticDisplayActivity;
import transapps.ballistic.BallisticSettings;
import transapps.ballistic.R;
import transapps.ballistic.lib.Ballistics;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class RangeTableFragment extends BaseFragment {
	private int start = 0;
	private int end = Ballistics.MAXRANGE;
	private int increment = 100;

	public static RangeTableFragment newInstance(int start, int end, int increment) {
		RangeTableFragment f = new RangeTableFragment();
		Bundle args = new Bundle();
		args.putInt("start", start);
		args.putInt("end", end);
		args.putInt("increment", increment);
		f.setArguments(args);
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		start = getArguments().getInt("start", start);
		end = getArguments().getInt("end", end);
		increment = getArguments().getInt("increment", increment);

		View v = inflater.inflate(R.layout.range_card, container, false);
		BallisticSettings s = BallisticSettings.i();
		getActivity().setTitle(s.getWeapon().name);

		ListView lv = (ListView)v.findViewById(R.id.range_list);
		lv.setAdapter(new RangeTableAdapter(getActivity(), start, end, increment));

		Display display = getActivity().getWindowManager().getDefaultDisplay();
		@SuppressWarnings("deprecation")
		int cellWidth = (display.getWidth() / 8);
		TextView range = (TextView)v.findViewById(R.id.range_label);
		range.setWidth(cellWidth);
		TextView velocity = (TextView)v.findViewById(R.id.velocity_label);
		velocity.setWidth(cellWidth);
		TextView energy = (TextView)v.findViewById(R.id.energy_label);
		energy.setWidth(cellWidth);
		TextView drop = (TextView)v.findViewById(R.id.drop_label);
		drop.setWidth(cellWidth * 2);
		TextView tof = (TextView)v.findViewById(R.id.tof_label);
		tof.setWidth(cellWidth + (cellWidth / 2));
		TextView drift = (TextView)v.findViewById(R.id.drift_label);
		drift.setWidth(cellWidth + (cellWidth / 2));

		range = (TextView)v.findViewById(R.id.range_unit);
		range.setWidth(cellWidth);
		velocity = (TextView)v.findViewById(R.id.velocity_unit);
		velocity.setWidth(cellWidth);
		energy = (TextView)v.findViewById(R.id.energy_unit);
		energy.setWidth(cellWidth);
		drop = (TextView)v.findViewById(R.id.drop_unit);
		drop.setWidth(cellWidth * 2);
		tof = (TextView)v.findViewById(R.id.tof_unit);
		tof.setWidth(cellWidth + (cellWidth / 2));
		drift = (TextView)v.findViewById(R.id.drift_unit);
		drift.setWidth(cellWidth + (cellWidth / 2));

		tof.setText("sec");
		if (s.imperial) {
			range.setText("yds");
			velocity.setText("ft/s");
			energy.setText("ft-lb");
		} else {
			range.setText("M");
			velocity.setText("m/s");
			energy.setText("J");
		}
		if (s.dropUnits == BallisticSettings.DROP_UNIT_RULER) {
			if (s.imperial) {
				drop.setText("IN");
				drift.setText("IN");
			} else {
				drop.setText("CM");
				drift.setText("CM");
			}
		} else if (s.dropUnits == BallisticSettings.DROP_UNIT_MOA) {
			drop.setText("MOA");
			drift.setText("MOA");
		} else {  // MIL
			drop.setText("MIL");
			drift.setText("MIL");
		}

		return v;
	}

	@Override
	public void refresh() {
		BallisticDisplayActivity.i().selectItem(BallisticDisplayActivity.NAV_RANGE);
	}
}
