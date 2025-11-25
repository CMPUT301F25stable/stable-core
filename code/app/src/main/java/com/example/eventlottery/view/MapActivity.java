package com.example.eventlottery.view;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventlottery.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

/**
 * The activity for displaying where entrants have joined your event as an organizer.
 * References: https://www.geeksforgeeks.org/android/google-maps-in-android/
 * https://developers.google.com/maps/documentation/android-sdk/get-api-key?setupProd=enable
 */
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private SupportMapFragment mapFragment;
    private ArrayList<Double> latitudes;
    private ArrayList<Double> longitudes;
    private ArrayList<String> names;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Grab all entrants' coordinates & names from intent
        latitudes = (ArrayList<Double>) getIntent().getSerializableExtra("latitudes");
        longitudes = (ArrayList<Double>) getIntent().getSerializableExtra("longitudes");
        names = (ArrayList<String>) getIntent().getSerializableExtra("names");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Check if any location data was even passed in
        // Null case
        if (latitudes == null || longitudes == null || names == null) {
            Toast.makeText(this, "No entrants have joined your event", Toast.LENGTH_SHORT).show();
            return;
        }

        // Empty list case
        if (latitudes.isEmpty() || longitudes.isEmpty() || names.isEmpty()) {
            Toast.makeText(this, "No entrants have joined your event", Toast.LENGTH_SHORT).show();
            return;
        }

        // Loop through every user location
        for (int i = 0; i < latitudes.size(); i++) {
            double latitude = latitudes.get(i);
            double longitude = longitudes.get(i);
            String name = names.get(i);

            LatLng position = new LatLng(latitude, longitude);

            googleMap.addMarker(new MarkerOptions().position(position).title(name));
        }

        // Move to the first user's location
        LatLng firstUser = new LatLng(latitudes.get(0), longitudes.get(0));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstUser, 12f));
    }
}