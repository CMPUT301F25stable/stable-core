package com.example.eventlottery.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventlottery.R;
import com.example.eventlottery.events.Event;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class QRActivity extends AppCompatActivity {
    private ArrayList<Event> generateEvents(int amount) {
        ArrayList<Event> events = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            String randomName = String.valueOf(UUID.randomUUID());
            events.add(new Event(
                    randomName,
                    randomName.concat(" Description"),
                    randomName.concat(" Location"),
                    randomName.concat(" Organizer ID"),
                    R.drawable.dance,
                    new Date(System.currentTimeMillis()),
                    new Date(System.currentTimeMillis() + 7200000L))
            );
        }
        return events;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_qractivity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ArrayList<Event> testEvents = generateEvents(10);
        testEvents.add(8, new Event(
                "ed765a4c-9216-4f70-820a-68722922d6eb",
                "Event Name",
                "Event Description",
                "Event Location",
                "Event Organizer",
                R.drawable.anime,
                new Date(System.currentTimeMillis()),
                new Date(System.currentTimeMillis()))
        );

        ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() != null) {
                String content = result.getContents();
                if (content.length() == 36) {
                    int eventIndex = Event.findEventById(testEvents, content);
                    if (eventIndex > -1) {
                        Event event = testEvents.get(eventIndex);
                        Intent intent = new Intent(this, EventActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.putExtra("Event", event);
                        startActivity(intent);
                    }
                }
                Log.d("QRActivity", content);
            }
        });

        Button scanButton = findViewById(R.id.scanButton);
        scanButton.setOnClickListener(v -> {
            ScanOptions scanOptions = new ScanOptions();
            scanOptions.setOrientationLocked(true);
            scanOptions.setCaptureActivity(QRScanActivity.class);
            barcodeLauncher.launch(scanOptions);
        });
    }
}