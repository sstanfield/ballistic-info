package transapps.ballistic.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class AngleOverlay extends View {
	private final Paint paint = new Paint();
	private float roll;

	public AngleOverlay(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AngleOverlay(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setRoll(float roll) {
		if ((int)this.roll != (int)roll) {
			this.roll = roll;
			invalidate();
		}
	}

	@Override
	protected void onDraw (Canvas canvas) {
		super.onDraw(canvas);
		int w = getMeasuredWidth();
		int h = getMeasuredHeight();
		float halfH = (float)h / 2.0F;
		float halfW = (float)w / 2.0F;

		float absR = Math.abs(roll);
		absR = absR > 90?Math.abs(absR - 180):absR;
		if (absR > 2) {
			float A = (float) ((roll * Math.PI) / 180.0);
			float c = (float) (halfW / Math.cos(A));
			float a = (float) (Math.sin(A) * c);
			int red = 75 + ((int)absR * 2);
			paint.setARGB(255, red, 0, 0);
			paint.setStrokeWidth(5);
			canvas.drawLine(0, halfH-a, w, halfH+a, paint);
		}

		paint.setARGB(80, 0, 0, 0);
		paint.setStrokeWidth(5);
		canvas.drawLine(0, halfH, w, halfH, paint);
		canvas.drawLine(halfW, halfH+50, halfW, halfH-50, paint);

		paint.setARGB(80, 255, 255, 255);
		paint.setStrokeWidth(2);
		canvas.drawLine(0, halfH, w, halfH, paint);
		canvas.drawLine(halfW, halfH+50, halfW, halfH-50, paint);
	}
}
