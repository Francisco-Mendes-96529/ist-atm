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
    protected static Toast toast = null;

    // Files
    protected static final String ALARM_FILE_NAME = "alarmFile.txt";
    protected static File alarmFile = null;
    protected static final String HISTORY_FILE_NAME = "historyFile.txt";
    protected static File historyFile = null;

    protected static final List<Float> lux = new ArrayList<>();
    protected static final List<Float> temperature = new ArrayList<>();
    protected static final List<Float> humidity = new ArrayList<>();

    protected static float max_light;
    protected static float min_light;
    protected static String time_max_light;
    protected static String time_min_light;
    protected static float max_temperature;
    protected static float min_temperature;
    protected static String time_max_temperature;
    protected static String time_min_temperature;
    protected static float max_humidity;
    protected static float min_humidity;
    protected static String time_max_humidity;
    protected static String time_min_humidity;

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

            System.arraycopy(alarms, 0, Alarm_array, 0, 12);
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

        historyFile = new File(this.getCacheDir(),HISTORY_FILE_NAME);
        if(historyFile.exists()) {
            if(lux.size()==0 && temperature.size()==0 && humidity.size()==0) {
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
                int i = 1;
                while (!history[i].equals("max")) {
                    lux.add(Float.parseFloat(history[i]));
                    i++;
                }
                if (!history[++i].equals("min")) {
                    max_light = Float.parseFloat(history[i]);
                    time_max_light = history[++i];
                    i += 2;
                    min_light = Float.parseFloat(history[i]);
                    time_min_light = history[++i];
                }
                i += 2;
                while (!history[i].equals("max")) {
                    temperature.add(Float.parseFloat(history[i]));
                    i++;
                }
                if (!history[++i].equals("min")) {
                    max_temperature = Float.parseFloat(history[i]);
                    time_max_temperature = history[++i];
                    i += 2;
                    min_temperature = Float.parseFloat(history[i]);
                    time_min_temperature = history[++i];
                }
                i += 2;
                while (!history[i].equals("max")) {
                    temperature.add(Float.parseFloat(history[i]));
                    i++;
                }
                if (!history[++i].equals("min")) {
                    max_humidity = Float.parseFloat(history[i]);
                    time_max_humidity = history[++i];
                    i += 2;
                    min_humidity = Float.parseFloat(history[i]);
                    time_min_humidity = history[++i];
                }
            }
        }
        else {
            // Create file
            try (FileOutputStream fos = new FileOutputStream(historyFile)) {
                String contents = "Light\nmax\nmin\nTemperature\nmax\nmin\nHumidity\nmax\nmin"; // write the structure of the file
                fos.write(contents.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(lux.size()!=0){
            TextView testView = findViewById(R.id.light_value);
            String temp = String.format("%.2f",lux.get(lux.size()-1));
            testView.setText(temp);
            testView = findViewById(R.id.max_light_value);
            temp = String.format("%.2f", max_light);
            testView.setText(temp);
            testView = findViewById(R.id.min_light_value);
            temp = String.format("%.2f",min_light);
            testView.setText(temp);
        }

        if(temperature.size()!=0){
            TextView testView = findViewById(R.id.temp_value);
            String temp = String.format("%.2f",temperature.get(temperature.size()-1));
            testView.setText(temp);
            testView = findViewById(R.id.max_temp_value);
            temp = String.format("%.2f", max_temperature);
            testView.setText(temp);
            testView = findViewById(R.id.min_temp_value);
            temp = String.format("%.2f",min_temperature);
            testView.setText(temp);
        }

        if(humidity.size()!=0){
            TextView testView = findViewById(R.id.humidity_value);
            String temp = String.format("%.2f",humidity.get(humidity.size()-1));
            testView.setText(temp);
            testView = findViewById(R.id.max_humidity_value);
            temp = String.format("%.2f", max_humidity);
            testView.setText(temp);
            testView = findViewById(R.id.min_humidity_value);
            temp = String.format("%.2f",min_humidity);
            testView.setText(temp);
        }
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @SuppressLint("DefaultLocale")
    @Override
    public final void onSensorChanged(SensorEvent event) {
        int type_sensor= event.sensor.getType();

        float value = event.values[0];
        switch(type_sensor) {
            case Sensor.TYPE_LIGHT:
                lux.add(value);
                TextView testView = findViewById(R.id.light_value);
                String temp = String.format("%.2f",value);
                testView.setText(temp);

                if(lux.size()==1){
                    max_light=value;
                    min_light=value;
                    time_max_light = getTime();
                    time_min_light = time_max_light;
                    testView = findViewById(R.id.max_light_value);
                    temp = String.format("%.2f",max_light);
                    testView.setText(temp);
                    testView = findViewById(R.id.min_light_value);
                    temp = String.format("%.2f",min_light);
                    testView.setText(temp);
                }
                else{
                    if(value >= max_light){
                        max_light=value;
                        time_max_light = getTime();
                        testView = findViewById(R.id.max_light_value);
                        temp = String.format("%.2f",max_light);
                        testView.setText(temp);
                    }
                    else if(value <= min_light){
                        min_light = value;
                        time_min_light = getTime();
                        testView = findViewById(R.id.min_light_value);
                        temp = String.format("%.2f",min_light);
                        testView.setText(temp);
                    }
                }

                if(lux.size()>10)
                    lux.remove(0);
                /*StringBuilder lux_values = new StringBuilder(getString(R.string.lux_placeholder) + "\n");
                for(int i = 0; i<lux.size(); i++) {
                    lux_values.append(String.format("%.1f\n", lux.get(i)));
                }
                luxView.setText(lux_values.toString());*/

                // Alarms
                if(lux.get(lux.size()-1) <= Float.parseFloat(Alarm_array[0]) && Boolean.parseBoolean(Alarm_array[1])){
                    if (toast != null)
                        toast.cancel();
                    toast = Toast.makeText(this,"WARNING!\n Light low", Toast.LENGTH_LONG);
                    toast.show();
                }
                else if(lux.get(lux.size()-1) >= Float.parseFloat(Alarm_array[2]) && Boolean.parseBoolean(Alarm_array[3])){
                    if (toast != null)
                        toast.cancel();
                    toast = Toast.makeText(this, "WARNING!\n Light high", Toast.LENGTH_LONG);
                    toast.show();
                }
                else {
                    if (toast != null)
                        toast.cancel();
                }
                break;

            case Sensor.TYPE_AMBIENT_TEMPERATURE:

                temperature.add(value);
                testView = findViewById(R.id.temp_value);
                temp = String.format("%.2f",value);
                testView.setText(temp);

                if(temperature.size()==1){
                    max_temperature=value;
                    min_temperature=value;
                    time_max_temperature = getTime();
                    time_min_temperature = time_max_temperature;
                    testView = findViewById(R.id.max_temp_value);
                    temp = String.format("%.2f",max_temperature);
                    testView.setText(temp);
                    testView = findViewById(R.id.min_temp_value);
                    temp = String.format("%.2f",min_temperature);
                    testView.setText(temp);
                }
                else{
                    if(value >= max_temperature){
                        max_temperature=value;
                        time_max_temperature = getTime();
                        testView = findViewById(R.id.max_temp_value);
                        temp = String.format("%.2f",max_temperature);
                        testView.setText(temp);
                    }
                    else if(value <= min_temperature){
                        min_temperature = value;
                        time_min_temperature = getTime();
                        testView = findViewById(R.id.min_temp_value);
                        temp = String.format("%.2f",min_temperature);
                        testView.setText(temp);
                    }
                }

                if(temperature.size()>10)
                    temperature.remove(0);
                /*StringBuilder temperature_values = new StringBuilder(getString(R.string.temperature_placeholder) + "\n");
                for(int i = 0; i<temperature.size(); i++)
                    temperature_values.append(String.format("% .2f\n", temperature.get(i)));
                temperatureView.setText(temperature_values.toString());*/

                // Alarms
                if(temperature.get(temperature.size()-1) <= Float.parseFloat(Alarm_array[4]) && Boolean.parseBoolean(Alarm_array[5])){
                    if (toast != null)
                        toast.cancel();
                    toast = Toast.makeText(this,"     WARNING!\nTemperature low", Toast.LENGTH_LONG);
                    toast.show();
                }
                else if(temperature.get(temperature.size()-1) >= Float.parseFloat(Alarm_array[6]) && Boolean.parseBoolean(Alarm_array[7])){
                    if (toast != null)
                        toast.cancel();
                    toast = Toast.makeText(this,"      WARNING!\nTemperature high", Toast.LENGTH_LONG);
                    toast.show();
                }
                else {
                    if (toast != null)
                        toast.cancel();
                }
                break;

            case Sensor.TYPE_RELATIVE_HUMIDITY:
                // code block
                humidity.add(value);
                testView = findViewById(R.id.humidity_value);
                temp = String.format("%.2f",value);
                testView.setText(temp);


                if(humidity.size()==1){
                    max_humidity=value;
                    min_humidity=value;
                    time_max_humidity = getTime();
                    time_min_humidity = time_max_humidity;
                    testView = findViewById(R.id.max_humidity_value);
                    temp = String.format("%.2f",max_humidity);
                    testView.setText(temp);
                    testView = findViewById(R.id.min_humidity_value);
                    temp = String.format("%.2f",min_humidity);
                    testView.setText(temp);
                }
                else{
                    if(value >= max_humidity){
                        max_humidity=value;
                        time_max_humidity = getTime();
                        testView = findViewById(R.id.max_humidity_value);
                        temp = String.format("%.2f",max_humidity);
                        testView.setText(temp);
                    }
                    else if(value <= min_humidity){
                        min_humidity = value;
                        time_min_humidity = getTime();
                        testView = findViewById(R.id.min_humidity_value);
                        temp = String.format("%.2f",min_humidity);
                        testView.setText(temp);
                    }

                }

                if(humidity.size()>10)
                    humidity.remove(0);

                /*StringBuilder humidity_values = new StringBuilder(getString(R.string.humidity_placeholder) + "\n");
                for(int i = 0; i<humidity.size(); i++)
                    humidity_values.append(String.format("% .2f\n", humidity.get(i)));
                humidityView.setText(humidity_values.toString());*/

                // Alarms
                if(humidity.get(humidity.size()-1) <= Float.parseFloat(Alarm_array[8]) && Boolean.parseBoolean(Alarm_array[9])){
                    if (toast != null)
                        toast.cancel();
                    toast = Toast.makeText(this,"  WARNING!\nHumidity low", Toast.LENGTH_LONG);
                    toast.show();
                }
                else if(humidity.get(humidity.size()-1) >= Float.parseFloat(Alarm_array[10]) && Boolean.parseBoolean(Alarm_array[11])){
                    if (toast != null)
                        toast.cancel();
                    toast = Toast.makeText(this,"   WARNING!\nHumidity high", Toast.LENGTH_LONG);
                    toast.show();
                }
                else {
                    if (toast != null)
                        toast.cancel();
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
        saveHistoryFile();
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

    private void saveHistoryFile() {
        try (FileOutputStream fos = new FileOutputStream(historyFile)) {
            StringBuilder contents = new StringBuilder("Light\n");
            for(int i = 0; i<lux.size(); i++)
                contents.append(lux.get(i).toString()).append("\n");
            contents.append("max\n");
            if(lux.size()!=0)
                contents.append(max_light).append("\n").append(time_max_light).append("\n").append("min\n").append(min_light).append("\n").append(time_min_light).append("\nTemperature\n");
            else
                contents.append("min\nTemperature\n");
            for(int i = 0; i<temperature.size(); i++)
                contents.append(temperature.get(i).toString()).append("\n");
            contents.append("max\n");
            if(temperature.size()!=0)
                contents.append(max_temperature).append("\n").append(time_max_temperature).append("\n").append("min\n").append(min_temperature).append("\n").append(time_min_temperature).append("\nHumidity\n");
            else
                contents.append("min\nHumidity\n");
            for(int i = 0; i<humidity.size(); i++)
                contents.append(humidity.get(i).toString()).append("\n");
            contents.append("max\n");
            if(humidity.size()!=0)
                contents.append(max_humidity).append("\n").append(time_max_humidity).append("\n").append("min\n").append(min_humidity).append("\n").append(time_min_humidity);
            else
                contents.append("min");

            fos.write(contents.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

