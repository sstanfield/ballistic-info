package transapps.ballistic.sensor;

import transapps.ballistic.BallisticApplication;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Surface;

public class Orientation implements SensorEventListener {
	private float[] magVals = null;
	private float[] accVals = null;
	private final float[] rawR = new float[16];
	private final float[] outR = new float[16];
	private final float[] I = new float[16];
	private int displayOrientation;
	private final SensorManager sensorManager;
	private final Sensor accelerometer;
	private final Sensor magnometer;
	private static final Orientation instance = new Orientation();

	public interface Update {
		public void orientationValues(float tilt, float pitch, float azmuth, float roll);
	}
	private Update update;

	private Orientation() {
		Context ctx = BallisticApplication.i();
		sensorManager = (SensorManager)ctx.getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	}

	public static Orientation i() {
		return instance;
	}

	public boolean isUsable() {
		return (accelerometer != null && magnometer != null);
	}

	public void pause() {
		sensorManager.unregisterListener(this);
	}

	public void resume(Update u) {
		if (accelerometer != null && magnometer != null) {
			sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
			sensorManager.registerListener(this, magnometer, SensorManager.SENSOR_DELAY_NORMAL);
		}
		BallisticApplication app = BallisticApplication.i();
		displayOrientation = app.getActivity().getWindowManager().getDefaultDisplay().getRotation();
		update = u;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) { }

	private float[] lowPass(final float alpha, final float[] in, float[] out) {
		if ( out == null ) return in;

		for ( int i=0; i < in.length && i < out.length; i++ ) {
			out[i] = out[i] + alpha * (in[i] - out[i]);
		}
		return out;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()) {
		case Sensor.TYPE_MAGNETIC_FIELD:
			magVals = lowPass(.15F, magVals==null?event.values.clone():event.values, magVals);
			break;
		case Sensor.TYPE_ACCELEROMETER:
			accVals = lowPass(.15F, accVals==null?event.values.clone():event.values, accVals);
			break;
		}

		if (magVals != null && accVals != null) {
			if (SensorManager.getRotationMatrix(rawR, I, accVals, magVals)) {
				int ax = SensorManager.AXIS_X;
				int ay = SensorManager.AXIS_Y;
				switch (displayOrientation) {
				case Surface.ROTATION_270:
					ax = SensorManager.AXIS_MINUS_Y;
					ay = SensorManager.AXIS_X;
					break;
				case Surface.ROTATION_180:
					ax = SensorManager.AXIS_MINUS_X;
					ay = SensorManager.AXIS_MINUS_Y;
					break;
				case Surface.ROTATION_90:
					ax = SensorManager.AXIS_Y;
					ay = SensorManager.AXIS_MINUS_X;
					break;
				case Surface.ROTATION_0:
				default:
					break;
				}
				SensorManager.remapCoordinateSystem(rawR, ax, ay, outR);

				float o[] = new float[3];
				SensorManager.getOrientation(outR, o);
				float pitch = (float)((o[1] * 180.0 / Math.PI));
				float angle;
				if (outR[10] > 0) {  // Downhill
					angle = -(90+pitch);
				} else { // Uphill
					angle = 90 + pitch;
				}
				float az = (float) (o[0] * 180.0 / Math.PI);
				az = (az + 360) % 360;
				float roll = (float)(o[2] * 180.0 / Math.PI);  // XXX Do something to massage this?  Have not used roll yet...

				float den = (float) Math.sqrt((accVals[0] * accVals[0]) + (accVals[1] * accVals[1]) + (accVals[2] * accVals[2]));
				float tilt = (float) Math.acos(accVals[2] / den);
				tilt = (float)((tilt * 180.0 / Math.PI)) - 90.0F;

				update.orientationValues(tilt, angle, az, roll);
			}
		}
	}
}
