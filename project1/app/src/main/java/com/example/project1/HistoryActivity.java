package com.example.project1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        TextView luxView = findViewById(R.id.lightView);
        StringBuilder lux_values = new StringBuilder(getString(R.string.lux_placeholder) + "\n");
        for(int i = 0; i<MainActivity.lux.size(); i++) {
            lux_values.append(String.format("%.1f\n", MainActivity.lux.get(i)));
        }
        luxView.setText(lux_values.toString());

    }
}