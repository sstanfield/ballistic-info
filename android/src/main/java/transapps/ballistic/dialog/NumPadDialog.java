package transapps.ballistic.dialog;

import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import transapps.ballistic.R;

/**
 * Implement a numeric pad dialog as an alternate entry method for sliders.
 * Created by sstanf on 4/15/14.
 */
public class NumPadDialog extends DialogFragment {
//	private static final String TAG = "NumPadDialog";
	private TextView numPadValue;
	private String val = "";
	private boolean decimal = false;

	public interface NumPadCallback {
		boolean isValid(String value);
		void numPadDone(String value);
	}
	private NumPadCallback callback;

	private class PadClick implements View.OnClickListener {
		private int digit;
		public PadClick(int digit) {
			this.digit = digit;
		}

		@Override
		public void onClick(View v) {
			String temp = val+digit;
			if (callback != null && callback.isValid(temp)) {
				val = val + digit;
				numPadValue.setText(val);
			}
		}
	}

	public static NumPadDialog newInstance(String val, boolean decimal) {
		NumPadDialog f = new NumPadDialog();
		Bundle args = new Bundle();
		args.putString("val", val);
		args.putBoolean("decimal", decimal);
		f.setArguments(args);

		return f;
	}

	public void setCallback(NumPadCallback callback) {
		this.callback = callback;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(STYLE_NO_TITLE, 0);
		val = getArguments().getString("val");
		val = val==null?"":val;
		decimal = getArguments().getBoolean("decimal", false);
		if (savedInstanceState != null && savedInstanceState.getString("val") != null) {
			val = savedInstanceState.getString("val");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.edit_number_pad, container, false);
		numPadValue = (TextView)v.findViewById(R.id.numpadvalue);
		numPadValue.setText(val);
		final View numOne = v.findViewById(R.id.numpad1);
		numOne.setOnClickListener(new PadClick(1));
		final View numTwo = v.findViewById(R.id.numpad2);
		numTwo.setOnClickListener(new PadClick(2));
		final View numThree = v.findViewById(R.id.numpad3);
		numThree.setOnClickListener(new PadClick(3));
		final View numFour = v.findViewById(R.id.numpad4);
		numFour.setOnClickListener(new PadClick(4));
		final View numFive = v.findViewById(R.id.numpad5);
		numFive.setOnClickListener(new PadClick(5));
		final View numSix = v.findViewById(R.id.numpad6);
		numSix.setOnClickListener(new PadClick(6));
		final View numSeven = v.findViewById(R.id.numpad7);
		numSeven.setOnClickListener(new PadClick(7));
		final View numEight = v.findViewById(R.id.numpad8);
		numEight.setOnClickListener(new PadClick(8));
		final View numNine = v.findViewById(R.id.numpad9);
		numNine.setOnClickListener(new PadClick(9));
		final View numZero = v.findViewById(R.id.numpad0);
		numZero.setOnClickListener(new PadClick(0));
		final TextView numDot = (TextView)v.findViewById(R.id.numpaddot);
		if (!decimal || val.contains(".")) numDot.setEnabled(false);
		else numDot.setEnabled(true);
		if (!decimal) numDot.setText(" ");
		numDot.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				val = val + ".";
				numPadValue.setText(val);
				numDot.setEnabled(false);
			}
		});
		View backspace = v.findViewById(R.id.numpadbackspace);
		backspace.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (val.length() > 0) {
					if (val.charAt(val.length() - 1) == '.') numDot.setEnabled(true);
					val = val.substring(0, val.length() - 1);
				}
				numPadValue.setText(val);
			}
		});
		View retBut = v.findViewById(R.id.numpadreturn);
		retBut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String value = numPadValue.getText().toString();
				if (value != null && value.length() > 0) {
					if (callback != null) callback.numPadDone(value);
				}
				dismiss();
			}
		});
		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		getDialog().setCanceledOnTouchOutside(false);
		return v;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("val", val);
	}
}
