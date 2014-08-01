package com.edwardyu.gesturerecognizer.gesturerecognizerapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.provider.Settings.Secure;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import net.sf.javaml.distance.fastdtw.dtw.DTW;
import net.sf.javaml.distance.fastdtw.timeseries.TimeSeries;
import net.sf.javaml.distance.fastdtw.timeseries.TimeSeriesPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.TreeSet;


public class MainActivity extends ActionBarActivity {
    private static String TAG = "MainActivity";

    // time
    private Time time;
    private long systemTime;

    // measuring + current run
    private boolean measuring;
    private Button measuringButton;
    private ArrayList<TimeDataContainer> currentRunList;
    private TimeSeries currentRunSeries;
    private String initialTime;


    // library runs
    private TimeSeriesContainer gestureA;
    private TimeSeriesContainer gestureB;

    // sensor
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float accelX, accelY, accelZ;

    // UI
    private TextView runDescription;
    private TextView runGuess;
    private TextView guessDTWDist;

    private String device;

    private SensorEventListener accelerometerListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            // TODO stuff to do when sensor is changed!
            accelX = sensorEvent.values[0];
            accelY = sensorEvent.values[1];
            accelZ = sensorEvent.values[2];
            systemTime = (new Date()).getTime();
            time = new Time();
            time.setToNow();

            if (measuring)
            {
                // add to current run
                currentRunList.add(new TimeDataContainer(systemTime, accelX, accelY, accelZ));

                // upload to database
                new Thread() {
                    public void run() {
                        String formattedTime = time.format("%Y-%m-%d %H:%M:%S");
                        Log.i(TAG, formattedTime + "(accel): " + accelX + ", " + accelY + ", " + accelZ);
                        String systemTimeString = systemTime + "";
                        Network.addToAccelerometerDatabase(systemTimeString, accelX + "", accelY + "", accelZ + "", formattedTime, initialTime, device);
                    }
                }.start();

            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set up device id
        device = android.os.Build.SERIAL;

        // set up accelerometer
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);

        // set up for measuring
        measuring = false;
        measuringButton = (Button) findViewById(R.id.measuring_button);
        measuringButton.setText("Begin Measuring");

        // setup UI
        runDescription = (TextView) findViewById(R.id.run_description);
        runGuess = (TextView) findViewById(R.id.run_guess);
        guessDTWDist = (TextView) findViewById(R.id.guess_dtwDist);

        Log.d(TAG, "Finished basic part of onCreate");


        // get library runs
        new Thread() {
            public void run() {
                String gestureADescription = "gestureA_dragAndReturn";
                String gestureBDescription = "gestureB_leftAndReturn";
                gestureA = makeTimeSeriesContainer(gestureADescription);
                gestureB = makeTimeSeriesContainer(gestureBDescription);
//                if (gestureA == null || gestureB == null) {
//                    Toast.makeText(getApplicationContext(), "Unable to load gestures!", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(getApplicationContext(), "Gestures Loaded!", Toast.LENGTH_SHORT).show();
//                }

            }

            private TimeSeriesContainer makeTimeSeriesContainer(String description) {
                JSONArray jsonArray = Network.sendSQLExpectJSONArray(
                        "SELECT * FROM `gestureLibrary` WHERE `description` = \"" + description + "\";\n");
//                JSONArray jsonArray = Network.sendSQLExpectJSONArray(
//                        "SELECT * FROM `gestureLibrary`;\n");

                if (jsonArray != null) {
                    try {
                        // create list for run which contains entries
                        ArrayList<TimeDataContainer> list = new ArrayList<TimeDataContainer>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            // pick out the JSONObject entry
                            JSONObject obj = jsonArray.getJSONObject(i);
                            // create a container and add it to the list
                            TimeDataContainer container = new TimeDataContainer(obj);
                            list.add(container);
                        }

                        list = new ArrayList<TimeDataContainer>(new TreeSet<TimeDataContainer>(list));

                        // sort the list for the run
                        Collections.sort(list);

                        // loop over the run's list to add data to TimeSeries for Java ML
                        TimeSeries series = new TimeSeries(3);
                        for (TimeDataContainer container : list) {
                            Log.d(TAG, "Adding " + container + " to TimeSeries");
                            series.addLast(container.getTime(), new TimeSeriesPoint(container.getData()));
                        }
                        Log.d(TAG, "Hopefully success!");
                        return new TimeSeriesContainer(description, series);
                    } catch (JSONException e) {
                        Log.w(TAG, "Exception: " + e.getMessage());
                        Toast.makeText(getApplicationContext(), "Exception!", Toast.LENGTH_SHORT).show();

                    }

                }
                return null;
            }
        }.start();

//        if (gestureA == null || gestureB == null) {
//            Toast.makeText(getApplicationContext(), "Unable to load gestures!", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(getApplicationContext(), "Gestures Loaded!", Toast.LENGTH_SHORT).show();
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(accelerometerListener, accelerometer,
                SensorManager.SENSOR_DELAY_GAME);

    }

    @Override
    protected void onStop() {
        sensorManager.unregisterListener(accelerometerListener);
        super.onStop();
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
        if (gestureA == null || gestureB == null) {
            Toast.makeText(getApplicationContext(), "Unable to load library gestures!", Toast.LENGTH_SHORT).show();
        } else {
            if (measuring) {

                measuring = false;
                measuringButton.setText("Begin Measuring");
                Toast.makeText(getApplicationContext(),
                        "Stopping measurements", Toast.LENGTH_SHORT).show();

                currentRunList = new ArrayList<TimeDataContainer>(new TreeSet<TimeDataContainer>(currentRunList));
                Collections.sort(currentRunList);
                for (TimeDataContainer container : currentRunList) {
                    Log.d(TAG, "Adding " + container + " to TimeSeries");
                    currentRunSeries.addLast(container.getTime(), new TimeSeriesPoint(container.getData()));
                }

                runDescription.setText("Latest Description: Accelerometer Reading Beginning At "
                        + initialTime + " on Device " + device);

                double distanceToGestureA = DTW.getWarpDistBetween(gestureA.getTimeSeries(), currentRunSeries);
                double distanceToGestureB = DTW.getWarpDistBetween(gestureB.getTimeSeries(), currentRunSeries);

                if (distanceToGestureA < distanceToGestureB) {
                    runGuess.setText("Guess: " + gestureA.getDescription());
                    guessDTWDist.setText("DTW Distance: " + distanceToGestureA);
                } else {
                    runGuess.setText("Guess: " + gestureB.getDescription());
                    guessDTWDist.setText("DTW Distance: " + distanceToGestureB);
                }


            } else {

                measuring = true;
                measuringButton.setText("Stop Measuring");
                Toast.makeText(getApplicationContext(),
                        "Starting measurements", Toast.LENGTH_SHORT).show();

                initialTime = (new Date()).getTime() + "";
                currentRunList = new ArrayList<TimeDataContainer>();
                currentRunSeries = new TimeSeries(3);

                runDescription.setText("Current Description: Accelerometer Reading Beginning At "
                        + initialTime + " on Device " + device);
                runGuess.setText("[Insert Guess of Run]");
                guessDTWDist.setText("[Insert DTW Dist of Run]");

            }
        }

    }



}
