package transapps.ballistic.fragment;

import transapps.ballistic.BallisticDisplayActivity;
import transapps.ballistic.R;
import transapps.ballistic.db.dvo.AtmosphereDVO;
import transapps.ballistic.lib.data.Atmosphere;
import transapps.ballistic.widgets.EditNumberSliders;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ZeroFragment extends AtmoFragment {
	private int zero;
	private int zeroLarge;
	private int zeroSmall;
	private EditNumberSliders zeroConfig;
	private boolean imperial;
	private View v;

	public interface Callback {
		public void setZero(int zero, boolean atmoOn, Atmosphere atmo);
	}

	public static ZeroFragment newInstance(double z, boolean imperial, 
			boolean atmoOn, AtmosphereDVO atmo) {
		ZeroFragment f = new ZeroFragment();
		Bundle args = new Bundle();
		if (atmo == null) atmo = new AtmosphereDVO(Atmosphere.ICAO_STANDARD);
		args.putDouble("zero", z);
		args.putBoolean("imperial", imperial);
		args.putBoolean("atmo_on", atmoOn);
		args.putParcelable("atmo", atmo.toContentValues());
		f.setArguments(args);
		return f;
	}

	@Override
	protected int getLayout() {
		return R.layout.zero_dialog;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v = super.onCreateView(inflater, container, savedInstanceState);
		getActivity().setTitle("Select Zero Range");

		zeroConfig = (EditNumberSliders)v.findViewById(R.id.zero_config);
		if (imperial) {
			zeroConfig.setLabels("Zero:", "yards");
		} else {
			zeroConfig.setLabels("Zero:", "meters");
		}
		zeroConfig.setMaxPositions(1000, 50);
		zeroConfig.setCallbacks(new EditNumberSliders.EditNumberClass() {
			@Override
			public int getLargeScale() {
				return 50;
			}
			@Override
			public String update(int large, int small) {
				zeroLarge = large;
				zeroSmall = small;
				zero = large + small;
				return ""+zero;
			}
			@Override
			public boolean isValid(String val) {
				int v = Integer.decode(val);
				return (v >= 0 && v < 1050);
			}
		});

		double z = getArguments().getDouble("zero");
		boolean imperial = getArguments().getBoolean("imperial");
		setZero(z, imperial);
		if (savedInstanceState != null) {

			zero = savedInstanceState.getInt("_ZERO");
			zeroLarge = savedInstanceState.getInt("_ZERO_LARGE");
			zeroSmall = savedInstanceState.getInt("_ZERO_SMALL");
			zeroConfig.setPositions(zeroLarge, zeroSmall);
			zeroConfig.setValue(""+zero);
		}

		return v;
	}

	@Override
	public void onSaveInstanceState (Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("_ZERO", zero);
		outState.putInt("_ZERO_LARGE", zeroLarge);
		outState.putInt("_ZERO_SMALL", zeroSmall);
	}

	private void setZero(double z, boolean imperial) {
		int zero = (int)(z+.5);
		zeroLarge = (zero / 50) * 50;
		zeroSmall = (zero % 50);
		this.zero = zero;
		this.imperial = imperial;
		if (imperial) {
			zeroConfig.setLabels("Zero:", "yards");
		} else {
			zeroConfig.setLabels("Zero:", "meters");
		}
		EditNumberSliders ens = (EditNumberSliders)v.findViewById(R.id.zero_config);
		ens.setPositions(zeroLarge, zeroSmall);
		ens.setValue(""+zero);
	}

	@Override
	public void update() {
		Callback c = BallisticDisplayActivity.i();
		c.setZero(zero, isAtmoOn(), getAtmosphere());
	}
}
