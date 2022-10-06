package com.example.project1;

import static android.hardware.Sensor.TYPE_AMBIENT_TEMPERATURE;
import static android.hardware.Sensor.TYPE_LIGHT;
import static android.hardware.Sensor.TYPE_RELATIVE_HUMIDITY;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor sLight;
    private Sensor sTemperature;
    private Sensor sHumidity;
    private List<Float> lux = new ArrayList<Float>();
    private List<Float> temperature =new ArrayList<Float>();
    private List<Float> humidity = new ArrayList<Float>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        sLight = sensorManager.getDefaultSensor(TYPE_LIGHT);
        sTemperature = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        sHumidity = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);

        //List<Sensor> deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);

        // Capture the layout's TextView and set the string as its text
        //String message = "List sensors \n";
        //String message = String.format("List of sensors (Nº" + " of sensors:%d)\n",deviceSensors.size());

/*
        for (int i=0; i<deviceSensors.size(); i++){
            message= message.concat(deviceSensors.get(i).getName());
            message= message.concat("\n");
        }

        TextView textView = findViewById(R.id.textView);
        textView.setText(message);

 */
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }
    @Override
    public final void onSensorChanged(SensorEvent event) {
        TextView textView = findViewById(R.id.textView);

        int type_sensor= event.sensor.getType();

        float value = event.values[0];
        switch(type_sensor) {
            case TYPE_LIGHT:
                // code block
                lux.add(value);
                if(lux.size()>10)
                    lux.remove(0);
                break;
            case TYPE_AMBIENT_TEMPERATURE:
                // code block
                temperature.add(value);
                if(temperature.size()>10)
                    temperature.remove(0);
                break;
            case TYPE_RELATIVE_HUMIDITY:
                // code block
                humidity.add(value);
                if(humidity.size()>10)
                    humidity.remove(0);
                break;
            default:
                // code block
        }
        String message = lux.toString() + temperature.toString() + humidity.toString();
        textView.setText(message);
    }
    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sLight, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sTemperature, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sHumidity, SensorManager.SENSOR_DELAY_NORMAL);

    }
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }



}

