package insectsrobotics.imagemaipulations;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;


public class SensorClass implements SensorEventListener {
    SensorManager mSensorManager;
    Sensor mGyroscope;
    Sensor mLight;

    public double getAzimuth() {
        return azimuth;
    }

    public int getAzimuth_int() {
        return azimuth_int;
    }

    public double azimuth = 0;
    int azimuth_int = 0;
    public int azimuth_tara = 0;
    public float light_lx;
    private static final String TAG = "OCVSample::Activity";

    public SensorClass(Context context) {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    }

    public void tara_Azimuth() {
        azimuth_tara = (int) azimuth;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] rotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            determineOrientation(rotationMatrix);
            //Log.e(TAG, "SensorChanged: " + event);
        } else if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            light_lx = event.values[0];
            Log.e(TAG, "Light in lx: " + light_lx);
        }


    }

    private void determineOrientation(float[] rotationMatrix) {
        float[] orientationValues = new float[3];
        SensorManager.getOrientation(rotationMatrix, orientationValues);
        azimuth = Math.toDegrees(orientationValues[0]);
        double pitch = Math.toDegrees(orientationValues[1]);
        double roll = Math.toDegrees(orientationValues[2]);

        azimuth = (-0.0000000136 * (azimuth * azimuth * azimuth * azimuth)) - (0.0000136 * (azimuth * azimuth * azimuth)) + (0.0017 * azimuth * azimuth) + (1.36 * azimuth) + 82.5;
        //azimuth = (-0.0000000143*(azimuth*azimuth*azimuth*azimuth))-(0.0000175*(azimuth*azimuth*azimuth)) + (0.00497*azimuth*azimuth)+(1.17*azimuth)-70.7;

        //azimuth = azimuth - azimuth_tara;

        if (azimuth > 180) {
            azimuth = azimuth - 360;
        }
        //Log.e(TAG, "SensorClass - Azimuth: " + azimuth);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void register() {
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_GAME);
    }

    public void unregister() {
        mSensorManager.unregisterListener(this);
    }
}
