package transapps.ballistic.fragment;
import java.text.DecimalFormat;

import transapps.ballistic.BallisticSettings;
import transapps.ballistic.R;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class CoriolisFragment extends BaseFragment implements FragmentUpdate {
	private View view;
	private DecimalFormat df2 = new DecimalFormat("0.00");

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.coriolis_activity, container, false);
		getActivity().setTitle("Select Coriolis Values");

		disabledInput();
		final Button corBut = (Button)view.findViewById(R.id.coriolis_on_button);
		corBut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String text = corBut.getText().toString();
				if (text.equals("Off")) {
					corBut.setText("On");
					enableInput();
				} else {
					corBut.setText("Off");
					disabledInput();
				}
			}
		});
		BallisticSettings s = BallisticSettings.i();
		boolean onOff = s.coriolisOn;
		double lat = s.latitude;
		double azi = s.azimuth;

		setValues(onOff, lat, azi);
		if (savedInstanceState != null) {
			String coriStr = savedInstanceState.getString("onOff");
			if (coriStr == null) coriStr = "Off";
			((Button)view.findViewById(R.id.coriolis_on_button)).setText(coriStr);
			if (coriStr.equals("Off")) {
				disabledInput();
			} else {
				enableInput();
			}
		}
		return view;
	}

	private float getVal(int field, String name) {
		try {
			return Float.parseFloat(((EditText)view.findViewById(field)).getText().toString());
		} catch (NumberFormatException ex) {
			Toast.makeText(getActivity(), "Invalid "+name+"!", Toast.LENGTH_LONG).show();
		}
		return 0;
	}

	public boolean isOn() {
		return ((Button)view.findViewById(R.id.coriolis_on_button)).getText().toString().equals("On");

	}
	private Double getLatitude() {
		return (double)getVal(R.id.lat_value, "Latitude");
	}

	private Double getAzimuth() {
		return (double)getVal(R.id.azimuth_value, "Azimuth");
	}

	public void setValues(boolean coriOn, double lat, double azi) {
		if (coriOn) {
			((Button)view.findViewById(R.id.coriolis_on_button)).setText("On");
			enableInput();
		} else {
			((Button)view.findViewById(R.id.coriolis_on_button)).setText("Off");
			disabledInput();
		}
		EditText t = (EditText)view.findViewById(R.id.lat_value);
		t.setText(""+df2.format(lat));
		t = (EditText)view.findViewById(R.id.azimuth_value);
		t.setText(""+df2.format(azi));

	}
	private void disabledInput() {
		view.findViewById(R.id.lat_value).setEnabled(false);
		view.findViewById(R.id.azimuth_value).setEnabled(false);
	}

	private void enableInput() {
		view.findViewById(R.id.lat_value).setEnabled(true);
		view.findViewById(R.id.azimuth_value).setEnabled(true);
	}

	@Override
	public void onSaveInstanceState (Bundle outState) {
		super.onSaveInstanceState(outState);
		String coriStr = ((Button)view.findViewById(R.id.coriolis_on_button)).getText().toString();
		outState.putString("onOff", coriStr);
	}

	@Override
	public void update() {
		boolean coriOn = isOn();
		double lat = getLatitude();
		double azi = getAzimuth();
		BallisticSettings s = BallisticSettings.i();
		if (s.latitude != lat || s.azimuth != azi || s.coriolisOn != coriOn) {
			s.latitude = lat;
			s.azimuth = azi;
			s.coriolisOn = coriOn;
			s.setDirty(true);
		}
	}
}