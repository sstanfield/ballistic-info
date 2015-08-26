package transapps.ballistic.widgets;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import transapps.ballistic.BallisticDisplayActivity;
import transapps.ballistic.R;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import transapps.ballistic.dialog.NumPadDialog;

/**
 * Compound widget to easily edit numbers using two sliders.
 * Also may allow editing with a floating dialog number pad.
 * User: sstanf
 * Date: 5/8/11
 * Time: 2:40 PM
 */
public class EditNumberSliders extends LinearLayout implements NumPadDialog.NumPadCallback {
	private SeekBar largeBar;
	private SeekBar smallBar;
	protected int largeValue;
	protected int smallValue;
	private boolean largeFromTool = false;
	private boolean smallFromTool = false;
	private String label = "";

	private TextView editNumberLabel;
	private TextView editNumber;
	private TextView editNumberUnitLabel;

	private int dpToPixels(float dp) {
		final float scale = getResources().getDisplayMetrics().density;
		return (int) (dp * scale + 0.5f);
	}

	/*
	Ideally this would be read from a layout file however doing so will cause
	android under some conditions to confuse controls if more then one is used
	on a containing layout.  Building the control dynamically solves the issue.
	 */
	private void makeLayout(final Context ctx) {
		int dp5 = dpToPixels(5);
		int dp15 = dpToPixels(15);
		LinearLayout smallRow = new LinearLayout(ctx);
		addView(smallRow);
		smallRow.setOrientation(HORIZONTAL);
		ViewGroup.LayoutParams params = smallRow.getLayoutParams();
		params.width = ViewGroup.LayoutParams.MATCH_PARENT;
		params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		smallRow.setLayoutParams(params);

//		<TextView android:id="@+id/_edit_number_label"
//		android:layout_width="wrap_content"
//		android:layout_height="wrap_content"
//		android:layout_gravity="center_vertical"
//		android:text=""
//		android:textSize="20dp"
//				/>
		editNumberLabel = new TextView(ctx);
		smallRow.addView(editNumberLabel);
		editNumberLabel.setGravity(Gravity.CENTER_VERTICAL);
		editNumberLabel.setText("");
		editNumberLabel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
		params = editNumberLabel.getLayoutParams();
		params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
		params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		editNumberLabel.setLayoutParams(params);

//		<TextView android:id="@+id/_edit_number"
//		android:layout_width="wrap_content"
//		android:layout_height="wrap_content"
//		android:text=""
//		android:textSize="30dp"
//		android:textColor="@color/red"
//		android:padding="5dp"
//				/>
		editNumber = new TextView(ctx);
		smallRow.addView(editNumber);
		editNumber.setText("");
		editNumber.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
		editNumber.setTextColor(getResources().getColor(R.color.red));
		editNumber.setPadding(dp5, dp5, dp5, dp5);
		params = editNumber.getLayoutParams();
		params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
		params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		editNumber.setLayoutParams(params);

//		<TextView android:id="@+id/_edit_number_unit_label"
//		android:layout_width="wrap_content"
//		android:layout_height="wrap_content"
//		android:text=""
//		android:textSize="10dp"
//				/>
		editNumberUnitLabel = new TextView(ctx);
		smallRow.addView(editNumberUnitLabel);
		editNumberUnitLabel.setText("");
		editNumberUnitLabel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
		params = editNumberUnitLabel.getLayoutParams();
		params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
		params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		editNumberUnitLabel.setLayoutParams(params);

//		<SeekBar android:id="@+id/_edit_number_slide_small"
//		style="?android:attr/progressBarStyleHorizontal"
//		android:layout_width="wrap_content"
//		android:layout_height="wrap_content"
//		android:layout_gravity="center"
//		android:paddingTop="5dp"
//		android:paddingLeft="15dp"
//		android:paddingBottom="5dp"
//		android:layout_weight="1"
//		android:progressDrawable="@drawable/blue_progress"
//				/>
		// Setting the style via the constructor causes display issues, so do not do it.
		smallBar = new SeekBar(ctx);//, null, android.R.attr.progressBarStyleHorizontal);
		smallRow.addView(smallBar);
		smallBar.setPadding(dp15, dp5, dp15, dp5);
		smallBar.setProgressDrawable(getResources().getDrawable(R.drawable.blue_progress));
		LayoutParams p2 = (LayoutParams)smallBar.getLayoutParams();
		p2.gravity = Gravity.CENTER;
		p2.width = ViewGroup.LayoutParams.WRAP_CONTENT;
		p2.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		p2.weight = 1;
		smallBar.setLayoutParams(p2);

//		<SeekBar android:id="@+id/_edit_number_slide_large"
//		style="?android:attr/progressBarStyleHorizontal"
//		android:layout_width="fill_parent"
//		android:layout_height="wrap_content"
//		android:layout_gravity="center"
//		android:paddingTop="5dp"
//		android:paddingBottom="5dp"
//		android:progressDrawable="@drawable/blue_progress"
//				/>
		largeBar = new SeekBar(ctx);
		addView(largeBar);
		largeBar.setPadding(dp15, dp5, dp15, dp5);
		largeBar.setProgressDrawable(getResources().getDrawable(R.drawable.blue_progress));
		p2 = (LayoutParams)largeBar.getLayoutParams();
		p2.gravity = Gravity.CENTER;
		p2.width = ViewGroup.LayoutParams.MATCH_PARENT;
		p2.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		largeBar.setLayoutParams(p2);
	}

	private void init(final Context ctx) {
		setOrientation(VERTICAL);
		makeLayout(ctx);

		largeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
										  boolean fromUser) {
				if (fromUser || largeFromTool) {
					EditNumberCallbacks c = getCallbacks();
					int newProgress = c.convertLargeProgress(progress);
					if (newProgress != progress) {
						seekBar.setProgress(newProgress);
						progress = newProgress;
					}
					largeValue = progress;
					editNumber.setText(c.update(largeValue, smallValue));
				}
				largeFromTool = false;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});

		smallBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
										  boolean fromUser) {
				if (fromUser || smallFromTool) {
					EditNumberCallbacks c = getCallbacks();
					int newProgress = c.convertSmallProgress(progress);
					if (newProgress != progress) {
						seekBar.setProgress(newProgress);
						progress = newProgress;
					}
					smallValue = progress;
					editNumber.setText(c.update(largeValue, smallValue));
				}
				smallFromTool = false;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});

		editNumber.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (getCallbacks().supportNumPad()) {
					NumPadDialog pad = NumPadDialog.newInstance(
							"", getCallbacks().supportDecimal());
					pad.setCallback(EditNumberSliders.this);
					// Use the label so we can find this specific dialog if the phone rotates.
					pad.show(BallisticDisplayActivity.i().getFragmentManager(), "Number Pad Dialog-"+label);
				}
			}
		});
	}

	public interface EditNumberCallbacks {
		@SuppressWarnings("unused") // Should not need this...
		int getLargeScale();
		int convertLargeProgress(int progress);
		int convertSmallProgress(int progress);
		String update(int large, int small);
		boolean supportNumPad();
		boolean supportDecimal();
		int getLarge(String val);
		int getSmall(String val);
		boolean isValid(String val);
	}
	private EditNumberCallbacks callbacks;

	public static class EditNumberClass implements EditNumberCallbacks {
		@Override
		public int getLargeScale() {
			return 1;
		}
		@Override
		public int convertLargeProgress(int progress) {
			int ls = getLargeScale();
			return ((progress + (ls / 2)) / ls) * ls;
		}
		@Override
		public int convertSmallProgress(int progress) {
			return progress;
		}
		@Override
		public String update(int large, int small) {
			return "" + (large + small);
		}
		@Override
		public boolean supportNumPad() {
			return true;
		}
		@Override
		public boolean supportDecimal() {
			return false;
		}
		@Override
		public int getLarge(String val) {
			int v = Integer.decode(val);
			int ls = getLargeScale();
			ls = ls==0?1:ls;
			return (v / ls) * ls;
		}
		@Override
		public int getSmall(String val) {
			int v = Integer.decode(val);
			int ls = getLargeScale();
			ls = ls==0?1:ls;
			return (v % ls);
		}
		@Override
		public boolean isValid(String val) {
			return true;
		}
	}

	@Override
	public boolean isValid(String value) {
		EditNumberCallbacks c = getCallbacks();
		return c == null || c.isValid(value);
	}

	@Override
	public void numPadDone(String value) {
		EditNumberCallbacks c = getCallbacks();
		largeValue = c.getLarge(value);
		smallValue = c.getSmall(value);
		c.update(largeValue, smallValue);
		largeBar.setProgress(largeValue);
		smallBar.setProgress(smallValue);
		setValue(value);
	}

	private EditNumberCallbacks getCallbacks() {
		if (callbacks == null) {
			callbacks = new EditNumberClass();
		}
		return callbacks;
	}

	public void setCallbacks(EditNumberCallbacks callbacks) {
		this.callbacks = callbacks;
	}

	@SuppressWarnings("unused")
	public EditNumberSliders(Context context) {
		super(context);
		init(context);
	}

	@SuppressWarnings("unused")
	public EditNumberSliders(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public void setLabels(String label, String unitLabel) {
		this.label = label;
		editNumberLabel.setText(label);
		editNumberUnitLabel.setText(unitLabel);
		// If the num dialog was open then we need to set the callbacks so it will work.
		NumPadDialog prevNumPad = (NumPadDialog) BallisticDisplayActivity.i().
				getFragmentManager().findFragmentByTag("Number Pad Dialog-"+label);
		if (prevNumPad != null) {
			prevNumPad.setCallback(EditNumberSliders.this);
		}
	}

	public void setMaxPositions(int largeMax, int smallMax) {
		largeBar.setMax(largeMax);
		smallBar.setMax(smallMax);
	}

	public void setPositions(int large, int small) {
		largeValue = large;
		largeBar.setProgress(large);
		smallValue = small;
		smallBar.setProgress(small);
	}

	/**
	 * This is intended to testing tools- will allow callbacks to fire.
	 * @param large Large slide value
	 * @param small Small slide value
	 */
	@SuppressWarnings("unused")
	public void setToolPositions(int large, int small) {
		largeFromTool = true;
		smallFromTool = true;
		setPositions(large, small);
	}

	public void setValue(String val) {
		editNumber.setText(val);
	}

	public int getLarge() {
		return largeValue;
	}

	public int getSmall() {
		return smallValue;
	}
}
