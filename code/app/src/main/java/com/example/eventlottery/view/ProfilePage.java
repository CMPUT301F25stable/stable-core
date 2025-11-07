package com.example.eventlottery.view;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventlottery.R;


/**
 * Represents the user’s profile page within the Event Lottery app.
 * <p>
 * This activity handles the display of the user's profile information
 * and adjusts layout insets dynamically to accommodate system UI elements
 * (such as the status bar and navigation bar) for a modern, edge-to-edge layout.
 * </p>
 * @see AppCompatActivity
 */
public class ProfilePage extends AppCompatActivity {

    /**
     * Called when the activity is starting.
     * <p>
     * This method initializes the activity, sets the content view to the profile page layout,
     * and configures edge-to-edge display behavior by applying window insets
     * so that the content avoids overlapping with system bars.
     * </p>
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this bundle contains the data it most recently supplied in {@link #onSaveInstanceState(Bundle)}.
     *                           Otherwise, it is {@code null}.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}