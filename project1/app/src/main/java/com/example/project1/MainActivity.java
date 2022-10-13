package com.example.project1;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static SensorManager sensorManager;
    private static Sensor sLight;
    private static Sensor sTemperature;
    private static Sensor sHumidity;
    private static Toast toast = null;

    // Files
    protected static final String ALARM_FILE_NAME = "alarmFile.txt";
    protected static File alarmFile = null;
    protected static final String HISTORY_FILE_NAME = "historyFile.txt";
    protected static File historyFile = null;

    protected static final List<Float> lux = new ArrayList<>();
    protected static final List<Float> temperature = new ArrayList<>();
    protected static final List<Float> humidity = new ArrayList<>();

    protected static float max_temperature;
    protected static float min_temperature;
    protected static String time_max_temperature;
    protected static String time_min_temperature;





    protected static final String[] Alarm_array = {"0","false","0","false","0","false","0","false","0","false","0","false"};//{lightBotThreshold,lightBotFlag,lightTopThreshold,lightTopFlag,temperatureBotThreshold,temperatureBotFlag,temperatureTopThreshold,temperatureTopFlag,humidityBotThreshold,humidityBotFlag,humidityTopThreshold,humidityTopFlag};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        sLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sTemperature = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        sHumidity = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);

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

    @SuppressLint("DefaultLocale")
    @Override
    public final void onSensorChanged(SensorEvent event) {
        TextView luxView = findViewById(R.id.lightView);
        TextView temperatureView = findViewById(R.id.temperatureView);
        TextView humidityView = findViewById(R.id.humidityView);

        int type_sensor= event.sensor.getType();

        float value = event.values[0];
        switch(type_sensor) {
            case Sensor.TYPE_LIGHT:
                lux.add(value);
                if(lux.size()>10)
                    lux.remove(0);
                StringBuilder lux_values = new StringBuilder(getString(R.string.lux_placeholder) + "\n");
                for(int i = 0; i<lux.size(); i++) {
                    lux_values.append(String.format("%.1f\n", lux.get(i)));
                }
                luxView.setText(lux_values.toString());

                // Alarms
                if(lux.get(lux.size()-1) <= Float.parseFloat(Alarm_array[0]) && Boolean.parseBoolean(Alarm_array[1])){
                    if (toast != null)
                        toast.cancel();
                    toast = Toast.makeText(this,"WARNING!\n Light low", Toast.LENGTH_SHORT);
                    toast.show();
                }
                else if(lux.get(lux.size()-1) >= Float.parseFloat(Alarm_array[2]) && Boolean.parseBoolean(Alarm_array[3])){
                    if (toast != null)
                        toast.cancel();
                    toast = Toast.makeText(this, "WARNING!\n Light high", Toast.LENGTH_SHORT);
                    toast.show();
                }
                else {
                    if (toast != null)
                        toast.cancel();
                    //toast = null;
                }
                break;

            case Sensor.TYPE_AMBIENT_TEMPERATURE:

                temperature.add(value);
                if(temperature.size()==1){
                    max_temperature=value;
                    min_temperature=value;
                    time_max_temperature = getTime();
                    time_min_temperature = time_max_temperature;
                }
                else{
                    if(value >= max_temperature){
                     max_temperature=value;
                     time_max_temperature = getTime();
                    }
                    else if(value <= min_temperature){
                    min_temperature = value;
                    time_min_temperature = getTime();
                    }

                }
                TextView testView = findViewById(R.id.testView);
                String temp = String.format("MAX: %f - %s\nMIN: %f - %s",max_temperature,time_max_temperature,min_temperature,time_min_temperature);
                testView.setText(temp);

                if(temperature.size()>10)
                    temperature.remove(0);
                StringBuilder temperature_values = new StringBuilder(getString(R.string.temperature_placeholder) + "\n");
                for(int i = 0; i<temperature.size(); i++)
                    temperature_values.append(String.format("% .2f\n", temperature.get(i)));
                temperatureView.setText(temperature_values.toString());

                // Alarms
                if(temperature.get(temperature.size()-1) <= Float.parseFloat(Alarm_array[4]) && Boolean.parseBoolean(Alarm_array[5])){
                    Toast.makeText(this, "     WARNING!\nTemperature low", Toast.LENGTH_SHORT).show();
                }
                else if(temperature.get(temperature.size()-1) >= Float.parseFloat(Alarm_array[6]) && Boolean.parseBoolean(Alarm_array[7])){
                    Toast.makeText(this, "      WARNING!\nTemperature high", Toast.LENGTH_SHORT).show();
                }
                break;

            case Sensor.TYPE_RELATIVE_HUMIDITY:
                // code block
                humidity.add(value);
                if(humidity.size()>10)
                    humidity.remove(0);
                StringBuilder humidity_values = new StringBuilder(getString(R.string.humidity_placeholder) + "\n");
                for(int i = 0; i<humidity.size(); i++)
                    humidity_values.append(String.format("% .2f\n", humidity.get(i)));
                humidityView.setText(humidity_values.toString());

                // Alarms
                if(humidity.get(humidity.size()-1) <= Float.parseFloat(Alarm_array[8]) && Boolean.parseBoolean(Alarm_array[9])){
                    Toast.makeText(this, "  WARNING!\nHumidity low", Toast.LENGTH_SHORT).show();
                }
                else if(humidity.get(humidity.size()-1) >= Float.parseFloat(Alarm_array[10]) && Boolean.parseBoolean(Alarm_array[11])){
                    Toast.makeText(this, "   WARNING!\nHumidity high", Toast.LENGTH_SHORT).show();
                }
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
        try (FileOutputStream fos = new FileOutputStream(historyFile)) {
            String contents = "file saved on onPause()"; // write the structure of the file
            fos.write(contents.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openAlarmActivity(View view) {
        Intent intent = new Intent(this, AlarmActivity.class);
        startActivity(intent);
    }

    public void openHistoryActivity(View view) {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }

    private String getTime(){
        long currentTimeMs = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
        Date resultDate = new Date(currentTimeMs);
        return sdf.format(resultDate);
    }

}

