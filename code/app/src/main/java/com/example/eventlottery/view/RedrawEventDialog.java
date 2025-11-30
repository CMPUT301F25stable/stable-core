package com.example.eventlottery.view;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.eventlottery.R;
import com.example.eventlottery.events.Event;
import com.example.eventlottery.events.NotificationSystem;
import com.example.eventlottery.users.User;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Dialog fragment that allows organizers to manually draw replacement entrants
 * from the waitlist when winners decline or cancel.
 *
 * User Story: US 02.05.03 - As an organizer I want to be able to draw a
 * replacement applicant from the pooling system when a previously selected
 * applicant cancels or rejects the invitation.
 */
public class RedrawEventDialog extends DialogFragment {
    private static final String TAG = "RedrawEventDialog";
    private static final String ARG_EVENT_ID = "eventId";
    private static final String ARG_EVENT_NAME = "eventName";
    private static final String ARG_WAITLIST_SIZE = "waitlistSize";
    private static final String ARG_ORGANIZER_ID = "organizerId";
    private static final String ARG_ORGANIZER_NAME = "organizerName";

    private String eventId;
    private String eventName;
    private int waitlistSize;
    private String organizerId;
    private String organizerName;
    private FirebaseFirestore db;
    private NotificationSystem notificationSystem;

    private TextView waitlistCountText;
    private EditText numberOfEntrantsInput;
    private Button cancelButton;
    private Button confirmButton;

    public interface OnRedrawCompleteListener {
        void onRedrawComplete(int drawnCount);
    }

    private OnRedrawCompleteListener listener;

    public static RedrawEventDialog newInstance(String eventId, String eventName, int waitlistSize,
                                                String organizerId, String organizerName) {
        RedrawEventDialog dialog = new RedrawEventDialog();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_EVENT_NAME, eventName);
        args.putInt(ARG_WAITLIST_SIZE, waitlistSize);
        args.putString(ARG_ORGANIZER_ID, organizerId);
        args.putString(ARG_ORGANIZER_NAME, organizerName);
        dialog.setArguments(args);
        return dialog;
    }

    public void setOnRedrawCompleteListener(OnRedrawCompleteListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
            eventName = getArguments().getString(ARG_EVENT_NAME);
            waitlistSize = getArguments().getInt(ARG_WAITLIST_SIZE);
            organizerId = getArguments().getString(ARG_ORGANIZER_ID);
            organizerName = getArguments().getString(ARG_ORGANIZER_NAME);
        }
        db = FirebaseFirestore.getInstance();

        // Initialize notification system
        if (getContext() != null) {
            notificationSystem = new NotificationSystem(getContext(), organizerId, organizerName);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_redraw_event, null);

        // Initialize views
        waitlistCountText = view.findViewById(R.id.waitlistCountText);
        numberOfEntrantsInput = view.findViewById(R.id.numberOfEntrantsInput);
        cancelButton = view.findViewById(R.id.cancelRedrawButton);
        confirmButton = view.findViewById(R.id.confirmRedrawButton);

        // Set waitlist count
        waitlistCountText.setText(String.valueOf(waitlistSize));

        // Set click listeners
        cancelButton.setOnClickListener(v -> dismiss());
        confirmButton.setOnClickListener(v -> performRedraw());

        builder.setView(view);
        return builder.create();
    }

    /**
     * Performs the redraw operation by selecting random users from the waitlist
     */
    private void performRedraw() {
        String input = numberOfEntrantsInput.getText().toString().trim();

        if (input.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a number", Toast.LENGTH_SHORT).show();
            return;
        }

        int numToDraw;
        try {
            numToDraw = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (numToDraw <= 0) {
            Toast.makeText(getContext(), "Please enter a positive number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (numToDraw > waitlistSize) {
            Toast.makeText(getContext(), "Not enough users on waitlist", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button to prevent double-clicks
        confirmButton.setEnabled(false);
        confirmButton.setAlpha(0.5f);

        // Fetch event and perform redraw
        db.collection("event-p4").document(eventId)
                .get()
                .addOnSuccessListener(eventDoc -> {
                    if (!eventDoc.exists()) {
                        Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                        dismiss();
                        return;
                    }

                    // Get waitlist
                    Map<String, Object> waitlistMap = (Map<String, Object>) eventDoc.get("waitlist");
                    if (waitlistMap == null) {
                        Toast.makeText(getContext(), "No waitlist found", Toast.LENGTH_SHORT).show();
                        dismiss();
                        return;
                    }

                    ArrayList<Map<String, Object>> waitlistedUsers =
                            (ArrayList<Map<String, Object>>) waitlistMap.get("waitlistedUsers");

                    if (waitlistedUsers == null || waitlistedUsers.isEmpty()) {
                        Toast.makeText(getContext(), "Waitlist is empty", Toast.LENGTH_SHORT).show();
                        dismiss();
                        return;
                    }

                    // Draw random users
                    ArrayList<Map<String, Object>> drawnUsers = new ArrayList<>();
                    Random random = new Random();
                    int actualDrawn = Math.min(numToDraw, waitlistedUsers.size());

                    for (int i = 0; i < actualDrawn; i++) {
                        int randomIndex = random.nextInt(waitlistedUsers.size());
                        drawnUsers.add(waitlistedUsers.remove(randomIndex));
                    }

                    // Update event waitlist
                    Map<String, Object> eventUpdates = new HashMap<>();
                    eventUpdates.put("waitlist.waitlistedUsers", waitlistedUsers);

                    db.collection("event-p4").document(eventId)
                            .update(eventUpdates)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Waitlist updated successfully");

                                // Update each drawn user
                                updateDrawnUsers(drawnUsers, actualDrawn);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to update waitlist: " + e.getMessage());
                                Toast.makeText(getContext(),
                                        "Failed to update waitlist: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                dismiss();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to retrieve event: " + e.getMessage());
                    Toast.makeText(getContext(),
                            "Failed to retrieve event: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    dismiss();
                });
    }

    /**
     * Updates all drawn users to "Notified" status and adds them to selectedIds
     */
    private void updateDrawnUsers(ArrayList<Map<String, Object>> drawnUsers, int totalDrawn) {
        int[] successCount = {0};
        List<User> notifiedUsers = new ArrayList<>();

        for (Map<String, Object> userMap : drawnUsers) {
            String userId = (String) userMap.get("id");
            String userName = (String) userMap.get("name");
            String userEmail = (String) userMap.get("emailAddress");

            if (userId == null || userId.isEmpty()) {
                continue;
            }

            // Fetch the full user object to get FCM token and notification preferences
            db.collection("users-p4").document(userId)
                    .get()
                    .addOnSuccessListener(userDoc -> {
                        if (!userDoc.exists()) {
                            Log.w(TAG, "User document not found for userId: " + userId);
                            return;
                        }

                        User user = userDoc.toObject(User.class);
                        if (user == null) {
                            Log.w(TAG, "Failed to parse user object for userId: " + userId);
                            return;
                        }

                        // Update user status to Notified
                        Map<String, Object> userUpdates = new HashMap<>();
                        userUpdates.put("registeredEvents." + eventId, "Notified");

                        db.collection("users-p4").document(userId)
                                .update(userUpdates)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "User " + userId + " notified");

                                    // Add to selectedIds
                                    db.collection("event-p4").document(eventId)
                                            .update("selectedIds", FieldValue.arrayUnion(userId))
                                            .addOnSuccessListener(aVoid2 -> {
                                                Log.d(TAG, "User " + userId + " added to selectedIds");
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.w(TAG, "Failed to add user to selectedIds: " + e.getMessage());
                                            });

                                    // Remove from waitlistedEvents array
                                    db.collection("users-p4").document(userId)
                                            .update("waitlistedEvents", FieldValue.arrayRemove(eventId))
                                            .addOnSuccessListener(aVoid3 -> {
                                                Log.d(TAG, "Event removed from user's waitlist");
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.w(TAG, "Failed to remove from waitlistedEvents: " + e.getMessage());
                                            });

                                    // Add user to notification list
                                    notifiedUsers.add(user);
                                    successCount[0]++;

                                    // If all users updated successfully
                                    if (successCount[0] == totalDrawn) {
                                        // Send notifications to all drawn users
                                        sendNotificationsToDrawnUsers(notifiedUsers, totalDrawn);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to notify user: " + e.getMessage());
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to fetch user document: " + e.getMessage());
                    });
        }
    }

    /**
     * Sends notifications to all users who were drawn from the waitlist
     */
    private void sendNotificationsToDrawnUsers(List<User> drawnUsers, int count) {
        if (notificationSystem == null) {
            Log.w(TAG, "NotificationSystem not initialized, skipping notifications");
            showSuccessAndDismiss(count);
            return;
        }

        if (drawnUsers.isEmpty()) {
            Log.w(TAG, "No users to notify");
            showSuccessAndDismiss(count);
            return;
        }

        // Use the existing notifySelectedEntrants method since "selected" = "notified"
        String message = "Congratulations! You've been selected for " + eventName +
                ". Please check your invitations to accept or decline.";

        notificationSystem.notifySelectedEntrants(drawnUsers, eventName, eventId, message);

        Log.d(TAG, "Notifications sent to " + drawnUsers.size() + " newly selected users");

        showSuccessAndDismiss(count);
    }

    /**
     * Shows success message and dismisses the dialog
     */
    private void showSuccessAndDismiss(int count) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(),
                        "Successfully drew " + count + " replacement(s) and sent notifications",
                        Toast.LENGTH_LONG).show();

                if (listener != null) {
                    listener.onRedrawComplete(count);
                }

                dismiss();
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Cleanup notification system
        if (notificationSystem != null) {
            notificationSystem.shutdown();
        }
    }
}