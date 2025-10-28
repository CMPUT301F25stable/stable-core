package com.example.eventlottery.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventlottery.R;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private String DEVICE_ID;

    private String getDeviceId(Context context) {
        SharedPreferences storedData = context.getSharedPreferences("DeviceId", Context.MODE_PRIVATE);
        String storedUUID = storedData.getString("UUID", "");

        if (storedUUID.isEmpty()) {
            String randomUUID = String.valueOf(UUID.randomUUID());
            storedUUID = randomUUID;
            SharedPreferences.Editor editor = storedData.edit();
            editor.putString("UUID", randomUUID);
            editor.apply();
        }
        return storedUUID;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        DEVICE_ID = getDeviceId(this);
        // REMOVE LATER
        Intent intent = new Intent(this, OrganizerPanel.class);
        startActivity(intent);
    }
}