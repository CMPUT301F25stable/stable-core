package com.example.eventlottery.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventlottery.R;
import com.example.eventlottery.events.DBConnector;
import com.example.eventlottery.events.EventGenerator;
import com.google.firebase.FirebaseApp;

public class SplashScreen extends AppCompatActivity {

    Handler handler = new Handler();
    String userID;
    DBConnector dbConnector;
    EventGenerator generator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseApp.initializeApp(this);

        // TESTING: Comment out if you do not want these events here:
        // Add a small delay to make sure Firebase finishes initialization
        // TESTING: create sample events
        // generator = new EventGenerator();
        // generator.createTestEvents();


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(intent);
                finish(); // optional, to close the splash screen
            }
        }, 3000);

        // Link to DB to add new user
        dbConnector = new DBConnector(this);
        dbConnector.saveNewUser(this);



    }
}