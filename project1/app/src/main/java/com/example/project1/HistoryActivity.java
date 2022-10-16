package com.example.project1;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;


public class HistoryActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Spinner spinner = findViewById(R.id.spinner1);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.sensors_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
        String chosen = parent.getItemAtPosition(position).toString();
        TextView unitsView = findViewById(R.id.units);
        TextView textView = findViewById(R.id.history_view);
        TextView maxView = findViewById(R.id.max_value);
        TextView minView = findViewById(R.id.min_value);

        if (chosen.equals("Light")){
            unitsView.setText("Unit: lx");
            if(MainActivity.lux.size()==0){
                clearTextViews();
                if(MainActivity.toast != null)
                    MainActivity.toast.cancel();
                MainActivity.toast = Toast.makeText(this, "No numbers registered", Toast.LENGTH_LONG);
                MainActivity.toast.show();
            }
            else{
                StringBuilder values = new StringBuilder("Oldest\n");
                for(int i = 0; i<MainActivity.lux.size(); i++) {
                    values.append(String.format("%.1f\n", MainActivity.lux.get(i)));
                }
                values.append("Latest");
                textView.setText(values);
                maxView.setText(String.format("%.1f lx - %s", MainActivity.max_light, MainActivity.time_max_light));
                minView.setText(String.format("%.1f lx - %s", MainActivity.min_light, MainActivity.time_min_light));
            }
        }
        else if (chosen.equals("Temperature")){
            unitsView.setText("Unit: ºC");
            if(MainActivity.temperature.size()==0){
                clearTextViews();
                if(MainActivity.toast != null)
                    MainActivity.toast.cancel();
                MainActivity.toast = Toast.makeText(this, "No numbers registered", Toast.LENGTH_LONG);
                MainActivity.toast.show();
            }
            else{
                StringBuilder values = new StringBuilder("Oldest\n");
                for(int i = 0; i<MainActivity.temperature.size(); i++) {
                    values.append(String.format("%.1f\n", MainActivity.temperature.get(i)));
                }
                values.append("Latest");
                textView.setText(values);
                maxView.setText(String.format("%.1f ºC - %s", MainActivity.max_temperature, MainActivity.time_max_temperature));
                minView.setText(String.format("%.1f ºC - %s", MainActivity.min_temperature, MainActivity.time_min_temperature));
            }
        }
        else{
            unitsView.setText("Unit: %");
            if(MainActivity.humidity.size()==0){
                clearTextViews();
                if(MainActivity.toast != null)
                    MainActivity.toast.cancel();
                MainActivity.toast = Toast.makeText(this, "No numbers registered", Toast.LENGTH_LONG);
                MainActivity.toast.show();
            }
            else{
                StringBuilder values = new StringBuilder("Oldest\n");
                for(int i = 0; i<MainActivity.humidity.size(); i++) {
                    values.append(String.format("%.1f\n", MainActivity.humidity.get(i)));
                }
                values.append("Latest");
                textView.setText(values);
                maxView.setText(String.format("%.1f %% - %s", MainActivity.max_humidity, MainActivity.time_max_humidity));
                minView.setText(String.format("%.1f %% - %s", MainActivity.min_humidity, MainActivity.time_min_humidity));
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void clearTextViews(){
        TextView textView = findViewById(R.id.history_view);
        TextView maxView = findViewById(R.id.max_value);
        TextView minView = findViewById(R.id.min_value);

        textView.setText("");
        maxView.setText("");
        minView.setText("");
    }

    public void click_clear_history(View view){
        clearTextViews();

        // clear lists values
        MainActivity.lux.clear();
        MainActivity.temperature.clear();
        MainActivity.humidity.clear();

        // clear file
        try (FileOutputStream fos = new FileOutputStream(MainActivity.historyFile)) {
            String contents = "Light\nmax\nmin\nTemperature\nmax\nmin\nHumidity\nmax\nmin";
            fos.write(contents.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(MainActivity.toast != null)
            MainActivity.toast.cancel();
        MainActivity.toast = Toast.makeText(this, "Cleared with success!", Toast.LENGTH_SHORT);
        MainActivity.toast.show();
    }
}