package com.example.project1;

import static android.hardware.Sensor.TYPE_AMBIENT_TEMPERATURE;
import static android.hardware.Sensor.TYPE_LIGHT;
import static android.hardware.Sensor.TYPE_RELATIVE_HUMIDITY;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.File;
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
    protected static String alarmFileName = "alarmFile.txt";
    protected static File alarmFile = null;

    protected static String lightTopThreshold = "";
    protected static String lightBotThreshold = "";
    protected static String temperatureTopThreshold = "";
    protected static String temperatureBotThreshold = "";
    protected static String humidityTopThreshold = "";
    protected static String humidityBotThreshold = "";
    protected static String lightTopFlag = "";
    protected static String lightBotFlag = "";
    protected static String temperatureTopFlag = "";
    protected static String temperatureBotFlag = "";
    protected static String humidityTopFlag = "";
    protected static String humidityBotFlag = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        sLight = sensorManager.getDefaultSensor(TYPE_LIGHT);
        sTemperature = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        sHumidity = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);

        alarmFile = new File(this.getFilesDir(),alarmFileName);
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }
    @Override
    public final void onSensorChanged(SensorEvent event) {
        TextView luxView = findViewById(R.id.textView);
        TextView temperatureView = findViewById(R.id.textView4);
        TextView humidityView = findViewById(R.id.textView5);

        int type_sensor= event.sensor.getType();

        float value = event.values[0];
        switch(type_sensor) {
            case TYPE_LIGHT:
                // code block
                lux.add(value);
                if(lux.size()>10)
                    lux.remove(0);
                String lux_values = getString(R.string.lux_placeholder)+"\n";
                for(int i = 0; i<lux.size(); i++)
                    lux_values += lux.get(i).toString() + "\n";
                luxView.setText(lux_values);
                break;

            case TYPE_AMBIENT_TEMPERATURE:
                // code block
                temperature.add(value);
                if(temperature.size()>10)
                    temperature.remove(0);
                String temperature_values = getString(R.string.temperature_placeholder)+"\n";
                for(int i = 0; i<temperature.size(); i++)
                    temperature_values += temperature.get(i).toString() + "\n";
                temperatureView.setText(temperature_values);
                break;

            case TYPE_RELATIVE_HUMIDITY:
                // code block
                humidity.add(value);
                if(humidity.size()>10)
                    humidity.remove(0);
                String humidity_values = getString(R.string.humidity_placeholder)+"\n";
                for(int i = 0; i<humidity.size(); i++)
                    humidity_values += humidity.get(i).toString() + "\n";
                humidityView.setText(humidity_values);
                break;

            default:
                // code block
        }


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

    public void OpenAlarmActivity(View view) {
        Intent intent = new Intent(this, AlarmActivity.class);
        startActivity(intent);

    }
        // Do something in response to button



}

