package transapps.ballistic.sensor;

import transapps.ballistic.BallisticApplication;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;

public class AtmoSensors implements SensorEventListener {
	private final Sensor temp;
	private final Sensor pressure;
	private final Sensor humidity;
	private final SensorManager sensorManager;
	private static final AtmoSensors instance = new AtmoSensors();

	public interface Update {
		public void temp(float temp);
		public void pressure(float pressure);
		public void humidity(float humidity);
	}
	private Update update;

	public AtmoSensors() {
		Context context = BallisticApplication.i();
		sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			temp = null;
			pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
			humidity = null;
		} else {
			temp = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
			pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
			humidity = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
		}
	}

	public static AtmoSensors i() {
		return instance;
	}

	public void pause() {
		sensorManager.unregisterListener(this);
	}

	public void resume(Update u) {
		if (temp != null) {
			sensorManager.registerListener(this, temp, SensorManager.SENSOR_DELAY_NORMAL);
		}
		if (pressure != null) {
			sensorManager.registerListener(this, pressure, SensorManager.SENSOR_DELAY_NORMAL);
		}
		if (humidity != null) {
			sensorManager.registerListener(this, humidity, SensorManager.SENSOR_DELAY_NORMAL);
		}

		update = u;
	}

	public boolean hasTemp() {
		return temp != null;
	}

	public boolean hasPressure() {
		return pressure != null;
	}

	public boolean hasHumidity() {
		return humidity != null;
	}

	public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()) {
		case Sensor.TYPE_AMBIENT_TEMPERATURE:
			update.temp(event.values[0]);
			break;
		case Sensor.TYPE_PRESSURE:
			update.pressure(event.values[0] * 0.0295333727F);
			break;
		case Sensor.TYPE_RELATIVE_HUMIDITY:
			update.humidity(event.values[0]);
			break;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) { }
}
