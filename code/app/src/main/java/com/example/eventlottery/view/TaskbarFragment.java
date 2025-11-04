package com.example.eventlottery.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventlottery.R;
import com.example.eventlottery.events.Event;
import com.example.eventlottery.model.EventDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class TaskbarFragment extends Fragment {
    /**
     * Inflates the home taskbar by default. TODO: Make it dynamically display taskbars later
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_taskbar, container, false);
    }

    /**
     * Right now, it just adds an event listener to the 'person' icon to start OrganizerPanel.
     * TODO: Add event listeners to all icons, and have the 'person' icon go to the user profile.
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EventDatabase eventDatabase = new EventDatabase();

        /**
         * Sets event listener for starting OrganizerPanel
         */
        View organizerIcon = view.findViewById(R.id.OrganizerIcon);
        organizerIcon.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), OrganizerPanel.class);
            // If exists in stack, retrieve it instead of making a new one
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });

        /**
         * Sets event listener for starting user profile
         */
        View personIcon = view.findViewById(R.id.personIcon);
        personIcon.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), UserPanel.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });

        /**
         * Sets event listener for home page
         */
        View homeIcon = view.findViewById(R.id.homeIcon);
        homeIcon.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });

        ActivityResultLauncher<ScanOptions> qrLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() != null) {
                String content = result.getContents().strip();
                if (content.length() == 36) {
                    eventDatabase.get(content, task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();
                            if (doc.exists()) {
                                Event eventToDisplay = doc.toObject(Event.class);
                                if (eventToDisplay != null) {
                                    Intent intent = new Intent(getActivity(), EventJoinAndLeave.class);
                                    intent.putExtra("id", eventToDisplay.getId());
                                    intent.putExtra("name", eventToDisplay.getName());
                                    intent.putExtra("description", eventToDisplay.getDescription());
                                    intent.putExtra("dateStart", eventToDisplay.getFormattedStartDate());
                                    intent.putExtra("timeStart", eventToDisplay.getFormattedStartTime());
                                    intent.putExtra("dateEnd", eventToDisplay.getFormattedEndDate());
                                    intent.putExtra("timeEnd", eventToDisplay.getFormattedEndTime());
                                    intent.putExtra("location", eventToDisplay.getLocation());
                                    intent.putExtra("organizer", eventToDisplay.getOrganizer());
                                    intent.putExtra("image", eventToDisplay.getImage());
                                    startActivity(intent);
                                }
                            }
                        }
                    });
                }
                Log.d("TaskbarFragment - QRLauncher", content);
            }
        });

        View cameraIcon = view.findViewById(R.id.cameraIcon);
        cameraIcon.setOnClickListener(v -> {
            ScanOptions scanOptions = new ScanOptions();
            scanOptions.setOrientationLocked(true);
            scanOptions.setBeepEnabled(false);
            scanOptions.setCaptureActivity(QRScanActivity.class);
            qrLauncher.launch(scanOptions);
        });
    }
}
