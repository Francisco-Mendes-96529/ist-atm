package com.example.project1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;

import java.io.FileInputStream;
import java.io.IOException;

public class AlarmActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        // Read file
        int length = (int) MainActivity.alarmFile.length();
        byte[] bytes = new byte[length];
        try (FileInputStream fis = new FileInputStream(MainActivity.alarmFile)) {
            //noinspection ResultOfMethodCallIgnored
            fis.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String bytesString = new String(bytes);
        String[] alarms = bytesString.split("\n");

        EditText test = findViewById(R.id.lightbotalarm);
        test.setText(MainActivity.lightBotThreshold);

        test = findViewById(R.id.lighttopalarm);
        test.setText(MainActivity.lightTopThreshold);

        test = findViewById(R.id.temperaturebotalarm);
        test.setText(MainActivity.temperatureBotThreshold);

        test = findViewById(R.id.temperaturetopalarm);
        test.setText(MainActivity.temperatureTopThreshold);

        test = findViewById(R.id.humiditybotalarm);
        test.setText(MainActivity.humidityBotThreshold);

        test = findViewById(R.id.humiditytopalarm);
        test.setText(MainActivity.humidityTopThreshold);


    }
}