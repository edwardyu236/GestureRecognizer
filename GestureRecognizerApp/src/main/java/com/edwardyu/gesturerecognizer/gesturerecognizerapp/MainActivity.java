package com.edwardyu.gesturerecognizer.gesturerecognizerapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Date;


public class MainActivity extends ActionBarActivity {
    // sensor
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SensorEventListener accelerometerListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            // TODO stuff to do when sensor is changed!
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    // state
    private boolean measuring;

    // button
    private Button measuringButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set up accelerometer
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);

        // set up for measuring
        measuring = false;
        measuringButton = (Button) findViewById(R.id.measuring_button);
        measuringButton.setText("Begin Measuring");

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void toggleMeasuring(View view) {
        if (measuring) {

            measuring = false;
            measuringButton.setText("Begin Measuring");
            Toast.makeText(getApplicationContext(),
                    "Stopping measurements", Toast.LENGTH_SHORT).show();

        } else {

            measuring = true;
            measuringButton.setText("Stop Measuring");
            Toast.makeText(getApplicationContext(),
                    "Starting measurements", Toast.LENGTH_SHORT).show();

//            initialTime = (new Date()).getTime() + "";
        }
    }



}
