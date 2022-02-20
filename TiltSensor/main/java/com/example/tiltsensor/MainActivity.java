package com.example.tiltsensor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private TextView orientationTextView = null;
    private TextView statusTextView = null;
    private TextView acceTextView = null;
    private SensorManager sensorManager;
    private Sensor acce_sensor, magnetic_sensor;

    private Button calibrateButton;
    private float orientation_x;
    private boolean isCalibrated;

    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];
    private double xValue,yValue, zValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isCalibrated = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        orientationTextView = (TextView) findViewById(R.id.orientationTextView);
        statusTextView = (TextView) findViewById(R.id.statusTextView);
        acceTextView = (TextView) findViewById(R.id.acceTextView);
        calibrateButton = (Button) findViewById(R.id.calibrateButton);
        calibrateButton.setOnClickListener(calibrateListener);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        acce_sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetic_sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    protected void onResume(){
        super.onResume();
        sensorManager.registerListener(sensor_listener, acce_sensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensor_listener, magnetic_sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause(){
        super.onPause();
        sensorManager.unregisterListener(sensor_listener);
    }

    private void updateOrientationAngles(){
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading);
        SensorManager.getOrientation(rotationMatrix, orientationAngles);
        orientationTextView.setText("ORIENTATION: " + "\nX: " + orientationAngles[0] + "\nY: " + orientationAngles[1] + "\nZ: " + orientationAngles[2]);
    }

    private void updateStatus(){
        if(!isCalibrated){
            statusTextView.setText("STATUS: CALIBRATE FIRST");
            return;
        }
        if(!((xValue > 7 && xValue < 11) && (yValue > -1 && yValue < 2) && (zValue > -1 && zValue < 1) && (orientationAngles[0]* 10 > (orientation_x - 2) && orientationAngles[0] * 10 < (orientation_x + 2)))){
            statusTextView.setText("STATUS: NOT OK");
        }else{
            statusTextView.setText("STATUS: OK");
        }
    }

    SensorEventListener sensor_listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                float[] values = sensorEvent.values;
                xValue = values[0];
                yValue = values[1];
                zValue =values[2];
                acceTextView.setText("ACCELEROMETER\n" + "X: " + values[0] + "\nY: " + values[1] + "\nZ: " + values[2]);
                System.arraycopy(sensorEvent.values, 0, accelerometerReading, 0, accelerometerReading.length);
            }else if(sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(sensorEvent.values, 0, magnetometerReading, 0, magnetometerReading.length);
            }
            updateOrientationAngles();
            updateStatus();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) { }
    };

    View.OnClickListener calibrateListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(!isCalibrated) {
                orientation_x = orientationAngles[0] * 10;
                calibrateButton.setText("CALIBRATED");
                isCalibrated = true;
            }
        }
    };
}