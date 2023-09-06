package com.example.geofence;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity
{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button);

        button.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), MapsActivity.class);
            startActivity(intent);
        });

        Button results = findViewById(R.id.ResultButton);
        results.setOnClickListener(view -> {
            Intent intent1 = new Intent(MainActivity.this, ResultsMapsActivity.class);
            startActivity(intent1);
        });

        Button stopButton = findViewById(R.id.stopButton);
        stopButton.setOnClickListener(view -> {
            //checks whether service there is a service or not
            Intent intent = new Intent(MainActivity.this, GeofenceService.class);
            if (!stopService(intent)) {
                Toast.makeText(getApplicationContext(), "No Service Running!", Toast.LENGTH_SHORT).show();
            } else {
                stopService(new Intent(MainActivity.this, GeofenceService.class));
            }
        });
    }


}