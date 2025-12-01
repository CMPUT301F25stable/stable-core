package com.example.eventlottery.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventlottery.R;
import com.example.eventlottery.events.DBConnector;
import com.example.eventlottery.events.Event;
import com.example.eventlottery.model.EventDatabase;
import com.example.eventlottery.users.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class TaskbarFragment extends Fragment {
    private DBConnector db;
    private ListenerRegistration listener;
    private User user;
    private View view;

    /**
     * Creates a new instance of the taskbar, given a user as an input.
     * @param user The user inputted.
     * @return A new fragment instance
     */
    public static TaskbarFragment newInstance(User user) {
        TaskbarFragment fragment = new TaskbarFragment();
        Bundle args = new Bundle();
        args.putSerializable("User", user);
        fragment.setArguments(args);
        return fragment;
    }

    private void updateTaskbar() {
        View adminIcon = view.findViewById(R.id.adminPanelIcon);
        if (user.isAdmin()) {
            adminIcon.setVisibility(View.VISIBLE);
        } else {
            adminIcon.setVisibility(View.GONE);
        }
    }

    private void loadUser() {
        final String userId = db.getUserId();

        listener = db.getUserDoc(userId).addSnapshotListener((snapshot, error) -> {
            if (error != null) return;

            if (snapshot != null && snapshot.exists()) {
                user = snapshot.toObject(User.class);
            }

            if (user != null) {
                if (user.isCreationBan() && (getActivity() instanceof OrganizerPanel) && isResumed()) {
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    Toast.makeText(getContext(), "Your organizer permissions have been revoked due to violation of app policy.", Toast.LENGTH_LONG).show();
                }
                updateTaskbar();
            }
        });
    }

    /**
     * Retrieves newInstance inputs & puts them into a private variable
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DBConnector(getContext());
    }

    /**
     * Inflates the taskbar.
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
        view = inflater.inflate(R.layout.fragment_home_taskbar, container, false);
        loadUser();
        return view;
    }

    /**
     * Adds event listeners for every icon
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
            if (user != null && !user.isCreationBan()) {
                Intent intent = new Intent(getActivity(), OrganizerPanel.class);
                // Create new instance every time so that user info is refreshed & events load properly - John
                startActivity(intent);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Organizer Access Revoked")
                        .setMessage("Your organizer permissions have been revoked due to violation of app policy.")
                        .setPositiveButton("Ok", null)
                        .show();
            }
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
            // Again, create a new instance so that user info is refreshed & events load properly
            // I did it this way because after you create a new event, and go back to this panel,
            // the new event was showing up despite it not being supposed to (this was because the user info 'createdEvents' was not refreshed) - John
            startActivity(intent);
        });

        /**
         * Sets event listener for notification button
         * */
        View notificationIcon = view.findViewById(R.id.notificationIcon);
        notificationIcon.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NotificationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });

        /**
         * Set event listener for admin panel (only shown if the user is an admin)
         */
        View adminIcon = view.findViewById(R.id.adminPanelIcon);
        adminIcon.setOnClickListener(v -> {
            if (user == null) return;

            if (user.isAdmin()) {
                Intent intent = new Intent(getActivity(), AdminPanel.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });

        /**
         * This handles the QR scanner's results and launches the Join/Leave Waitlist activity if the scanned content of the QR code is valid.
         */
        ActivityResultLauncher<ScanOptions> qrLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() != null) {
                String content = result.getContents().strip();
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
                                intent.putExtra("registrationEnd", eventToDisplay.getEndTime());      // Unformatted end date.
                                intent.putExtra("registrationStart", eventToDisplay.getStartTime());  // Unformatted start date.
                                intent.putExtra("geolocation", eventToDisplay.getGeolocation());
                                intent.putExtra("location", eventToDisplay.getLocation());
                                intent.putExtra("organizer", eventToDisplay.getOrganizer());
                                intent.putExtra("image", eventToDisplay.getImage());
                                startActivity(intent);
                            }
                        } else {
                            Toast.makeText(getContext(), "The QR Code that you scanned is invalid.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "The QR Code that you scanned is invalid.", Toast.LENGTH_SHORT).show();
                    }
                });
                Log.d("TaskbarFragment - QRLauncher", content);
            }
        });

        /**
         * Launches the QR scanner when the camera icon is pressed.
         */
        View cameraIcon = view.findViewById(R.id.cameraIcon);
        cameraIcon.setOnClickListener(v -> {
            ScanOptions scanOptions = new ScanOptions();
            scanOptions.setOrientationLocked(true);
            scanOptions.setBeepEnabled(false);
            scanOptions.setCaptureActivity(QRScanActivity.class);
            qrLauncher.launch(scanOptions);
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (listener != null) {
            listener.remove();
            Log.d("TaskbarFragment", "Firebase snapshot listener removed.");
        }
    }
}