package transapps.ballistic.fragment;

import java.text.DecimalFormat;
import java.util.List;

import transapps.ballistic.BallisticSettings;
import transapps.ballistic.R;
import transapps.ballistic.sensor.Orientation;
import transapps.ballistic.view.AngleOverlay;
import transapps.ballistic.widgets.EditNumberSliders;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

public class AngleFragment extends BaseFragment implements FragmentUpdate,
		SurfaceHolder.Callback, Orientation.Update {
	private static final String TAG = "AngleFragment";
	public final DecimalFormat df2 = new DecimalFormat("0.00");
	private static final String UP_ANGLE = "Uphill";
	private static final String DOWN_ANGLE = "Downhill";
	private int value;
	private int large;
	private int small;
	private int autoAngleVal = 0;
	private EditNumberSliders config;
	private Button angleButton;
	private View v;
	private Camera camera;
	private boolean cameraConfigured = false;
	private TextView autoAngle;
	private SurfaceView preview;
	private ZoomControls zoom;
	private TextView zoomRatio;
	private int displayOrientation;
	private int maxZoomLevel = 0;
	private int currentZoomLevel = 0;
	private List<Integer> zoomRatios;
	private boolean isInited = false;
	private Orientation sensors;
	private AngleOverlay angleOverlay;

	// Suppress this warning because not using the deprecated call will break camera preview for MANY devices...
	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.angle_dialog, container, false);
		getActivity().setTitle("Select Shooting Angle");

		angleButton = (Button)v.findViewById(R.id.angle_up_down);
		angleButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (angleButton.getText().equals(UP_ANGLE)) {
					angleButton.setText(DOWN_ANGLE);
					config.setValue("-"+value);
				} else {
					angleButton.setText(UP_ANGLE);
					config.setValue(""+value);
				}
			}
		});

		Button lockButton = (Button)v.findViewById(R.id.angle_lock);
		lockButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setAngle(autoAngleVal);
			}
		});

		config = (EditNumberSliders)v.findViewById(R.id.angle_config);
		config.setLabels("Angle:", "deg");
		config.setMaxPositions(80, 9);
		config.setCallbacks(new EditNumberSliders.EditNumberClass() {
			@Override
			public int getLargeScale() {
				return 10;
			}
			@Override
			public String update(int l, int s) {
				large = l;
				small = s;
				value = large + small;
				if (angleButton.getText().toString().equals(DOWN_ANGLE)) {
					return "-"+value;
				}
				return ""+value;
			}
			@Override
			public boolean isValid(String val) {
				int v = Integer.decode(val);
				return (v >= 0 && v <= 90);
			}
		});

		setAngle(BallisticSettings.i().shootingAngle);
		if (savedInstanceState != null) {
			value = savedInstanceState.getInt("_ANGLE");
			large = savedInstanceState.getInt("_ANGLE_LARGE");
			small = savedInstanceState.getInt("_ANGLE_SMALL");
			config.setPositions(large, small);
			boolean uphill = savedInstanceState.getBoolean("_UPHILL", false);
			if (uphill) {
				angleButton.setText(UP_ANGLE);
				config.setValue(""+value);
			} else {
				angleButton.setText(DOWN_ANGLE);
				config.setValue("-"+value);
			}
		}
		preview = (SurfaceView)v.findViewById(R.id.camera_view);
		SurfaceHolder previewHolder = preview.getHolder();
//		// Have to do this (setType call) despite what docs say or many devices will break...
		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		previewHolder.addCallback(this);

		zoom = (ZoomControls)v.findViewById(R.id.angle_zoom_controls);
		autoAngle = (TextView)v.findViewById(R.id.auto_angle);
		zoomRatio = (TextView)v.findViewById(R.id.angle_zoom_ratio);
		angleOverlay = (AngleOverlay)v.findViewById(R.id.angle_overlay);
		sensors = Orientation.i();

		return v;
	}

	@Override
	public void onPause() {
		super.onPause();
		if (camera != null) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}
		sensors.pause();
	}

	@Override
	public void onResume() {
		super.onResume();
		displayOrientation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
		if (sensors.isUsable()) {//accelerometer != null && magnometer != null) {
			if (camera == null) camera = Camera.open();
			sensors.resume(this);
		}
		isInited = false;
	}

	@Override
	public void onSaveInstanceState (Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("_ANGLE", value);
		outState.putInt("_ANGLE_LARGE", large);
		outState.putInt("_ANGLE_SMALL", small);
		outState.putBoolean("_UPHILL", angleButton.getText().equals(UP_ANGLE));
	}

	private int getAngle() {
		if (angleButton.getText().toString().equals(DOWN_ANGLE)) {
			return -value;
		}
		return value;
	}

	private void setAngle(int angle) {
		angleButton.setText(UP_ANGLE);
		int absAngle = angle;
		if (angle < 0) {
			angleButton.setText(DOWN_ANGLE);
			absAngle = -angle;
		}
		large = (absAngle / 10) * 10;
		small = (absAngle % 10);
		this.value = absAngle;
		EditNumberSliders ens = (EditNumberSliders)v.findViewById(R.id.angle_config);
		ens.setPositions(large, small);
		ens.setValue(""+angle);
	}

	@Override
	public void update() {
		int angle = getAngle();
		BallisticSettings s = BallisticSettings.i();
		if (s.shootingAngle != angle) {
			s.shootingAngle = angle;
			s.setDirty(true);
		}
	}

	private Camera.Size getBestPreviewSize(int w, int h,
			Camera.Parameters parameters) {
		List<Size> previewSizes = parameters.getSupportedPreviewSizes();
		Size previewSize = previewSizes.get(0);   // Should return something...
		float aspectRatio = (float) h / (float) w;
		float smallestDiff = Float.MAX_VALUE;
		int largestWidth = 0;
		int largestHeight = 0;

		for (Size size : previewSizes) {
			float previewAspectRatio = ((float) size.height / (float) size.width);
			float diff = Math.abs(aspectRatio - previewAspectRatio);

			if ((diff < smallestDiff
					|| (diff == smallestDiff && (size.width > largestWidth || size.height > largestHeight))) ) {
				smallestDiff = diff;
				largestWidth = size.width;
				largestHeight = size.height;
				previewSize = size;
			}
		}
		return previewSize;
	}

	private Camera.Size getSmallestPictureSize(Camera.Parameters parameters) {
		Camera.Size result=null;

		for (Camera.Size size : parameters.getSupportedPictureSizes()) {
			if (result == null) {
				result=size;
			}
			else {
				int resultArea=result.width * result.height;
				int newArea=size.width * size.height;

				if (newArea < resultArea) {
					result=size;
				}
			}
		}

		return(result);
	}

	private void setSurfaceSize(int width, int height) {
		Log.d(TAG, "setting surface view size to: " + width + " x "
				+ height);
		LayoutParams p2 = preview.getLayoutParams();

		p2.width = width;
		p2.height = height;
		preview.setLayoutParams(p2);
	}

	private boolean setOrientation(int o, SurfaceHolder previewHolder, 
			int width, int height, boolean secondTry) {
		try {
			camera.setDisplayOrientation(o);
		} catch (RuntimeException ex) {
			if (secondTry) Log.e(TAG, "Error setting camera display orientation, second try failing.", ex);
			else {
				Log.e(TAG, "Error setting camera display orientation, try again...");
				camera.stopPreview();
				camera.release();
				camera = Camera.open();
				initPreview(previewHolder, width, height, true);
				return false;
			}
		}
		return true;
	}

	private void setZoomRatio() {
		if (zoomRatios != null && currentZoomLevel < zoomRatios.size()) {
			float zr = (float)zoomRatios.get(currentZoomLevel);
			zoomRatio.setText(df2.format(zr/100.0)+"x");
		} else {
			zoomRatio.setText("");
		}
	}

	private void initPreview(SurfaceHolder previewHolder, int width, int height, boolean secondTry) {
		if (camera != null && previewHolder.getSurface() != null) {
			try {
				camera.setPreviewDisplay(previewHolder);
			}
			catch (Throwable t) {
				Log.e(TAG, "Exception setting up camera", t);
				Toast.makeText(getActivity(), t.getMessage(),
						Toast.LENGTH_LONG).show();
			}

			final Camera.Parameters parameters=camera.getParameters();
			Camera.Size size;
			if (displayOrientation == Surface.ROTATION_0 || displayOrientation == Surface.ROTATION_180)
				size=getBestPreviewSize(height, width, parameters);
			else
				size=getBestPreviewSize(width, height, parameters);
			Camera.Size pictureSize=getSmallestPictureSize(parameters);

			if (size != null && pictureSize != null) {
				if(displayOrientation == Surface.ROTATION_0)
				{
					parameters.setPreviewSize(size.width, size.height);
					if (!setOrientation(90, previewHolder, width, height, secondTry)) return;
				}
				if(displayOrientation == Surface.ROTATION_90)
				{
					parameters.setPreviewSize(size.width, size.height);
				}
				if(displayOrientation == Surface.ROTATION_180)
				{
					parameters.setPreviewSize(size.width, size.height);
				}
				if(displayOrientation == Surface.ROTATION_270)
				{
					parameters.setPreviewSize(size.width, size.height);
					if (!setOrientation(180, previewHolder, width, height, secondTry)) return;
				}

				float ar = (float)size.height / (float)size.width;
				if (displayOrientation == Surface.ROTATION_0 || displayOrientation == Surface.ROTATION_180) {
					int w = (int)(height * ar);
					if (w < width) setSurfaceSize(w, height);
				} else {
					int w = (int)((float)height / ar);
					if (w < width) setSurfaceSize(w, height);
				}

				parameters.setPictureSize(pictureSize.width,
						pictureSize.height);
				parameters.setPictureFormat(ImageFormat.JPEG);

				if (parameters.isZoomSupported()) {
					if (parameters.isSmoothZoomSupported()) {
						//most phones
						maxZoomLevel = parameters.getMaxZoom();

						zoom.setIsZoomInEnabled(true);
						zoom.setIsZoomOutEnabled(true);
						zoomRatios = parameters.getZoomRatios();

						zoom.setOnZoomInClickListener(new OnClickListener() {
							public void onClick(View v) {
								if (currentZoomLevel < maxZoomLevel) {
									currentZoomLevel++;
									camera.startSmoothZoom(currentZoomLevel);
									setZoomRatio();
								}
							}
						});

						zoom.setOnZoomOutClickListener(new OnClickListener() {
							public void onClick(View v) {
								if (currentZoomLevel > 0) {
									currentZoomLevel--;
									camera.startSmoothZoom(currentZoomLevel);
									setZoomRatio();
								}
							}
						});
						Log.e(TAG, "Setup for smooth zoom.");
					} else {
						//stupid HTC phones
						maxZoomLevel = parameters.getMaxZoom();

						zoom.setIsZoomInEnabled(true);
						zoom.setIsZoomOutEnabled(true);
						zoomRatios = parameters.getZoomRatios();
						parameters.setZoom(currentZoomLevel);

						zoom.setOnZoomInClickListener(new OnClickListener() {
							public void onClick(View v) {
								if (currentZoomLevel < maxZoomLevel) {
									currentZoomLevel++;
									parameters.setZoom(currentZoomLevel);
									camera.setParameters(parameters);
									setZoomRatio();
								}
							}
						});

						zoom.setOnZoomOutClickListener(new OnClickListener() {
							public void onClick(View v) {
								if (currentZoomLevel > 0) {
									currentZoomLevel--;
									parameters.setZoom(currentZoomLevel);
									camera.setParameters(parameters);
									setZoomRatio();
								}
							}
						});
						Log.e(TAG, "Setup for regular zoom.");
					}
				} else {
					//no zoom on phone
					v.findViewById(R.id.angle_zoom_layout).setVisibility(View.GONE);
					zoomRatios = null;
					Log.e(TAG, "No zoom.");
				}

				camera.setParameters(parameters);
				cameraConfigured=true;
				if (parameters.isZoomSupported() && parameters.isSmoothZoomSupported())
					camera.startSmoothZoom(currentZoomLevel);
				setZoomRatio();
			}
		}
	}

	private void startPreview() {
		if (cameraConfigured && camera != null) {
			camera.startPreview();
		}
	}


	@Override
	public void surfaceCreated(SurfaceHolder holder) { }

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format,
			int width, int height) {
		if (!isInited) {
			initPreview(holder, width, height, false);
			startPreview();
			isInited = true;
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) { }

	public void orientationValues(float tilt, float pitch, float azmuth, float roll) {
		float p = pitch<15.0F?tilt:pitch;
		autoAngle.setText(""+(int)p);
		autoAngleVal = (int)p;
		float r = Math.abs(pitch)<5.0F?0:roll;
		angleOverlay.setRoll(r);
	}
}
