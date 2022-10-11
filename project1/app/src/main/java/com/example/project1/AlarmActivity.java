package com.example.project1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ToggleButton;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class AlarmActivity extends AppCompatActivity {
private static final int[] id_button_array = {R.id.toggleButton_min_light,R.id.toggleButton_max_light,R.id.toggleButton_min_temperature,R.id.toggleButton_max_temperature,R.id.toggleButton_min_humidity,R.id.toggleButton_max_humidity};
private static final int[] id_text_array = {R.id.lightbotalarm,R.id.lighttopalarm,R.id.temperaturebotalarm,R.id.temperaturetopalarm,R.id.humiditybotalarm,R.id.humiditytopalarm};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        for(int i=0; i<6; i++){
            ToggleButton toggle = (ToggleButton) findViewById(id_button_array[i]);
            int finalI = i*2+1;
            toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    MainActivity.Alarm_array[finalI]= String.valueOf(isChecked);
                    saveFile(toggle);
                }
            });
        }
    }

    public void saveFile(View view) {
        for (int i=0; i<6; i++){
            EditText editText = findViewById(id_text_array[i]);
            int finalI = i*2;
            MainActivity.Alarm_array[finalI] = String.valueOf(editText.getText());
        }
        try (FileOutputStream fos = new FileOutputStream(MainActivity.alarmFile)) {
            StringBuilder contents = new StringBuilder();
            for(int i=0; i<12; i++){
                contents.append(MainActivity.Alarm_array[i]).append("\n");
            }
            fos.write(contents.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}