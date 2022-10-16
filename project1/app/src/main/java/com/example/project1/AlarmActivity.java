package com.example.project1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.FileOutputStream;
import java.io.IOException;

public class AlarmActivity extends AppCompatActivity {
    private static final int[] id_button_array = {R.id.toggleButton_min_light,R.id.toggleButton_max_light,R.id.toggleButton_min_temperature,R.id.toggleButton_max_temperature,R.id.toggleButton_min_humidity,R.id.toggleButton_max_humidity};
    private static final int[] id_text_array = {R.id.lightbotalarm,R.id.lighttopalarm,R.id.temperaturebotalarm,R.id.temperaturetopalarm,R.id.humiditybotalarm,R.id.humiditytopalarm};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        for(int i=0; i<6; i++) {
            int numI = 2*i;
            int buttonI = 2*i + 1;
            EditText editText = findViewById(id_text_array[i]);
            editText.setText(MainActivity.Alarm_array[numI]);
            ToggleButton toggle = findViewById(id_button_array[i]);
            toggle.setChecked(Boolean.parseBoolean(MainActivity.Alarm_array[buttonI]));
        }

        for(int i=0; i<6; i++){
            ToggleButton toggle = findViewById(id_button_array[i]);
            int finalI = i*2+1;
            toggle.setOnCheckedChangeListener((buttonView, isChecked) -> MainActivity.Alarm_array[finalI]= String.valueOf(isChecked));
        }
    }

    public void saveFile(View view) {
        if(condition()) {
            for (int i = 0; i < 6; i++) {
                EditText editText = findViewById(id_text_array[i]);
                int finalI = i * 2;
                MainActivity.Alarm_array[finalI] = String.valueOf(Float.parseFloat(String.valueOf(editText.getText())));
            }
            try (FileOutputStream fos = new FileOutputStream(MainActivity.alarmFile)) {
                StringBuilder contents = new StringBuilder();
                for (int i = 0; i < 12; i++) {
                    contents.append(MainActivity.Alarm_array[i]).append("\n");
                }
                fos.write(contents.toString().getBytes());

                if (MainActivity.toast != null)
                    MainActivity.toast.cancel();
                MainActivity.toast = Toast.makeText(this, "Alarms saved with success!", Toast.LENGTH_SHORT);
                MainActivity.toast.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            if (MainActivity.toast != null)
                MainActivity.toast.cancel();
            MainActivity.toast = Toast.makeText(this, "  Values invalid!\nAlarms not saved!", Toast.LENGTH_SHORT);
            MainActivity.toast.show();
        }
    }
    protected boolean condition(){
        float[] values = {0,0,0,0,0,0};
        for (int i = 0; i < 6; ++i){
            EditText editText = findViewById(id_text_array[i]);
            values[i] = Float.parseFloat(String.valueOf(editText.getText()));
        }
        for(int i = 0; i<6; i += 2){
            if (values[i]>values[i+1])
                return false;
        }
        if (values[2] < -273.15)
            return false;
        if (values[5] > 100)
            return false;
    return true;
    }

}



