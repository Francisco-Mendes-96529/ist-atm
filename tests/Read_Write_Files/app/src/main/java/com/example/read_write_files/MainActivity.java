package com.example.read_write_files;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String filename = "thresholdsFile.txt";
    private String initialMessage = "";
    private File file = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialMessage = getString(R.string.file_reading)+"\n\n";
        file = new File(this.getFilesDir(), filename);
    }
    
    public void saveNumber(View view) {
        if(view.equals(findViewById(R.id.max_button))){
            EditText maxText = findViewById(R.id.maximumEditText);
            String max = maxText.getText().toString();
            try (FileOutputStream fos = new FileOutputStream(file)) {
                String contents = initialMessage + "Maximum: " + max;
                fos.write(contents.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void show(View view) {
        TextView textView = findViewById(R.id.fileTextView);
        String contents = readFile();
        textView.setText(contents);
    }

    public String readFile() {
        int length = (int) file.length();
        byte[] bytes = new byte[length];
        try (FileInputStream fis = new FileInputStream(file)) {
            //noinspection ResultOfMethodCallIgnored
            fis.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(bytes);
    }

}