package com.kavinraj.cse535individualassignment1;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;



public class respiratory_rate extends Service implements SensorEventListener {

    private SensorManager accelerator_Manager;
    private Sensor Accelerator_sensor;
    private ArrayList<Integer> X_values = new ArrayList<>();
    private ArrayList<Integer> Y_values = new ArrayList<>();
    private ArrayList<Integer> Z_values = new ArrayList<>();

    @Override
    public void onCreate(){

        Log.i("log", "Accel Service started");
        accelerator_Manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Accelerator_sensor = accelerator_Manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accelerator_Manager.registerListener(this, Accelerator_sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        X_values.clear();
        Y_values.clear();
        Z_values.clear();
        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        Sensor genericSensor = sensorEvent.sensor;
        if (genericSensor.getType() == Sensor.TYPE_ACCELEROMETER) {


            X_values.add((int)(sensorEvent.values[0] * 100));
            Y_values.add((int)(sensorEvent.values[1] * 100));
            Z_values.add((int)(sensorEvent.values[2] * 100));


            if(X_values.size() >= 230){
                stopSelf();
            }
        }
    }

    @Override
    public void onDestroy(){

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {


                accelerator_Manager.unregisterListener(respiratory_rate.this);
                Log.i("service", "Service stopping");


                Intent intent = new Intent("broadcastingAccelData");
                Bundle b = new Bundle();
                b.putIntegerArrayList("accelValuesX", X_values);
                intent.putExtras(b);
                LocalBroadcastManager.getInstance(respiratory_rate.this).sendBroadcast(intent);
            }
        });
        thread.start();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
