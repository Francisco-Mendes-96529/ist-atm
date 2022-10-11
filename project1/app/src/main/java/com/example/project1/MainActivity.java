package com.example.project1;

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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static SensorManager sensorManager;
    private static Sensor sLight;
    private static Sensor sTemperature;
    private static Sensor sHumidity;

    // Files
    protected static final String ALARM_FILE_NAME = "alarmFile.txt";
    protected static File alarmFile = null;
    protected static final String HISTORY_FILE_NAME = "historyFile.txt";
    protected static File historyFile = null;

    protected static final List<Float> lux = new ArrayList<>();
    protected static final List<Float> temperature = new ArrayList<>();
    protected static final List<Float> humidity = new ArrayList<>();

    protected static String lightTopThreshold = "";
    protected static String lightBotThreshold = "0";
    protected static String temperatureTopThreshold = "100";
    protected static String temperatureBotThreshold = "0";
    protected static String humidityTopThreshold = "100";
    protected static String humidityBotThreshold = "0";
    protected static String lightTopFlag = "";
    protected static String lightBotFlag = "";
    protected static String temperatureTopFlag = "";
    protected static String temperatureBotFlag = "";
    protected static String humidityTopFlag = "";
    protected static String humidityBotFlag = "";
    protected static final String[] Alarm_array = {"0","false","1","false","0","false","1","false","0","false","1","false"};//{lightBotThreshold,lightBotFlag,lightTopThreshold,lightTopFlag,temperatureBotThreshold,temperatureBotFlag,temperatureTopThreshold,temperatureTopFlag,humidityBotThreshold,humidityBotFlag,humidityTopThreshold,humidityTopFlag};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        sLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sTemperature = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        sHumidity = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);

        float maximumRange = sLight.getMaximumRange();
        lightTopThreshold = String.valueOf(maximumRange);
        TextView maxView = findViewById(R.id.textView);
        maxView.setText(lightTopThreshold);

        alarmFile = new File(this.getFilesDir(),ALARM_FILE_NAME);
        if(alarmFile.exists()) {
            // Read file
            int length = (int) alarmFile.length();
            byte[] bytes = new byte[length];
            try (FileInputStream fis = new FileInputStream(alarmFile)) {
                //noinspection ResultOfMethodCallIgnored
                fis.read(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String bytesString = new String(bytes);
            String[] alarms = bytesString.split("\n");

            for(int i=0; i<12; i++){
                Alarm_array[i]=alarms[i];
            }

        }
        else {
            // Create file
            try (FileOutputStream fos = new FileOutputStream(alarmFile)) {
                StringBuilder contents = new StringBuilder();
                for(int i=0; i<12; i++){
                    contents.append(Alarm_array[i]).append("\n");
                }
                fos.write(contents.toString().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        historyFile = new File(this.getFilesDir(),HISTORY_FILE_NAME);
        if(historyFile.exists()) {
            // Read file
            int length = (int) historyFile.length();
            byte[] bytes = new byte[length];
            try (FileInputStream fis = new FileInputStream(historyFile)) {
                //noinspection ResultOfMethodCallIgnored
                fis.read(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String bytesString = new String(bytes);
            String[] history = bytesString.split("\n");
            /////////////////////////////////////////////////// assign values to the strings
        }
        else {
            // Create file
            try (FileOutputStream fos = new FileOutputStream(historyFile)) {
                String contents = ""; // write the structure of the file
                fos.write(contents.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
            case Sensor.TYPE_LIGHT:
                // code block
                lux.add(value);
                if(lux.size()>10)
                    lux.remove(0);
                StringBuilder lux_values = new StringBuilder(getString(R.string.lux_placeholder) + "\n");
                for(int i = 0; i<lux.size(); i++)
                    lux_values.append(lux.get(i).toString()).append("\n");
                luxView.setText(lux_values.toString());
                break;

            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                // code block
                temperature.add(value);
                if(temperature.size()>10)
                    temperature.remove(0);
                StringBuilder temperature_values = new StringBuilder(getString(R.string.temperature_placeholder) + "\n");
                for(int i = 0; i<temperature.size(); i++)
                    temperature_values.append(temperature.get(i).toString()).append("\n");
                temperatureView.setText(temperature_values.toString());
                break;

            case Sensor.TYPE_RELATIVE_HUMIDITY:
                // code block
                humidity.add(value);
                if(humidity.size()>10)
                    humidity.remove(0);
                StringBuilder humidity_values = new StringBuilder(getString(R.string.humidity_placeholder) + "\n");
                for(int i = 0; i<humidity.size(); i++)
                    humidity_values.append(humidity.get(i).toString()).append("\n");
                humidityView.setText(humidity_values.toString());
                break;

            default:
                // code block
                break;
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

    public void openAlarmActivity(View view) {
        Intent intent = new Intent(this, AlarmActivity.class);
        startActivity(intent);
    }


}

