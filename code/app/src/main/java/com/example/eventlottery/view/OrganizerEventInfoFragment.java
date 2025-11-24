package com.example.eventlottery.view;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.eventlottery.R;
import com.example.eventlottery.events.NotificationSystem;
import com.example.eventlottery.users.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;


/**
 * Fragment that displays detailed information about a specific event for organizers.
 * Shows counts and allows notifications for waiting list, selected, and cancelled entrants.
 */
public class OrganizerEventInfoFragment extends Fragment {
    // constant variables
    private static final String TAG = "OrganizerEventInfo";
    private static final String ARG_EVENT_ID = "event_id";
    private static final String ARG_EVENT_NAME = "event_name";
    private static final String ARG_WAITLIST_COUNT = "waitlist_count";

    // UI elements
    private TextView eventName;
    private TextView waitingCountText;
    private CardView waitingListCard;

    // Data fields
    private String eventId;
    private int waitlistCount;

    // Firebase
    private FirebaseFirestore db;

    /**
     * Method to create a new instance of this fragment with event data
     *
     * @param eventId The unique ID of the event
     * @param eventName The name of the event
     * @param waitlistCount The number of users on the waitlist
     */
    public static OrganizerEventInfoFragment newInstance(String eventId, String eventName, int waitlistCount) {
        OrganizerEventInfoFragment fragment = new OrganizerEventInfoFragment();

        // Create a Bundle to store arguments - this ensures data survives configuration changes
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_EVENT_NAME, eventName);
        args.putInt(ARG_WAITLIST_COUNT, waitlistCount);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Retrieve arguments passed to the fragment
        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
            String name = getArguments().getString(ARG_EVENT_NAME);
            waitlistCount = getArguments().getInt(ARG_WAITLIST_COUNT, 0);
        }
    }


    /**
     * Called to create the view hierarchy associated with the fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.organizer_event_info, container, false);
    }

    /**
     * Called immediately after onCreateView() when the fragment's view hierarchy has been created.
     * This is where we initialize UI components and set up listeners.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI elements by finding them in the fragment's view hierarchy
        eventName = view.findViewById(R.id.headerText);
        waitingCountText = view.findViewById(R.id.waitingCount);

        // Get references to the clickable cards
        waitingListCard = view.findViewById(R.id.waitingListCard);
        // TODO: add SelectedCard and cancelledCard when I add the firebase implementation of those first

        // Close Button
        ImageButton closeButton = view.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> {
            // Close the fragment
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Populate the UI with data from arguments
        if (getArguments() != null) {
            eventName.setText(getArguments().getString(ARG_EVENT_NAME));
            waitingCountText.setText(String.valueOf(waitlistCount));
            //TODO: Add select and cancel cards as well
        }

        // Set up click listeners for user interactions
        setClickListeners();
    }

    /**
     * Sets up click listeners for the three interactive cards in the fragment.
     * Each card represents a different entrant state and allows the organizer to
     * send notifications to all entrants in that state.
     */
    private void setClickListeners() {
        // Click listener for waiting list card - notifies all waitlisted entrants
        waitingListCard.setOnClickListener(v -> {
            if (waitlistCount > 0) {
                showNotificationDialog();
            } else {
                Toast.makeText(requireContext(),
                        "No entrants on waiting list",
                        Toast.LENGTH_SHORT).show();
            }
        });
        // TODO: Add click listeners for selected and cancelled cards
    }

    /**
     * Shows a Material 3 styled dialog with quick suggestions allowing the organizer
     * to compose and send a notification to all waitlisted entrants.
     */
    private void showNotificationDialog() {
        // Inflate custom dialog layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_send_notification, null);

        // Find views in the custom layout
        TextInputEditText messageInput = dialogView.findViewById(R.id.messageInput);
        Chip recipientCount = dialogView.findViewById(R.id.recipientCount);
        MaterialButton sendButton = dialogView.findViewById(R.id.sendButton);
        MaterialButton cancelButton = dialogView.findViewById(R.id.cancelButton);

        // Quick suggestion chips
        Chip suggestionChip1 = dialogView.findViewById(R.id.suggestionChip1);
        Chip suggestionChip2 = dialogView.findViewById(R.id.suggestionChip2);
        Chip suggestionChip3 = dialogView.findViewById(R.id.suggestionChip3);

        // Set the recipient count with proper grammar
        recipientCount.setText(waitlistCount + " " +
                (waitlistCount == 1 ? "recipient" : "recipients"));

        // Create and configure the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Make dialog background transparent for rounded corners to show
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Quick suggestion templates
        Map<Chip, String> suggestionTemplates = new HashMap<>();
        suggestionTemplates.put(suggestionChip1,
                "Thank you for joining the waitlist for " + getArguments().getString(ARG_EVENT_NAME) + "! We'll notify you of any updates.");
        suggestionTemplates.put(suggestionChip2,
                "Important update regarding " + getArguments().getString(ARG_EVENT_NAME) + ": ");
        suggestionTemplates.put(suggestionChip3,
                "Reminder: The event " + getArguments().getString(ARG_EVENT_NAME) + " is coming up soon!");

        // Set up quick suggestion chip click listeners
        for (Map.Entry<Chip, String> entry : suggestionTemplates.entrySet()) {
            entry.getKey().setOnClickListener(v -> {
                messageInput.setText(entry.getValue());
                messageInput.setSelection(entry.getValue().length()); // Move cursor to end

                // Animate chip selection
                entry.getKey().animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(100)
                        .withEndAction(() ->
                                entry.getKey().animate()
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .setDuration(100)
                                        .start())
                        .start();
            });
        }

        // Character counter and send button state
        messageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Enable/disable send button based on input
                boolean hasText = s.length() > 0;
                sendButton.setEnabled(hasText);
                sendButton.setAlpha(hasText ? 1f : 0.5f);

                // Animate send button when enabled (only when transitioning from empty to non-empty)
                if (hasText && before == 0 && count > 0) {
                    sendButton.animate()
                            .scaleX(1.05f)
                            .scaleY(1.05f)
                            .setDuration(100)
                            .withEndAction(() ->
                                    sendButton.animate()
                                            .scaleX(1f)
                                            .scaleY(1f)
                                            .setDuration(100)
                                            .start())
                            .start();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Initially disable send button
        sendButton.setEnabled(false);
        sendButton.setAlpha(0.5f);

        // Set up the send button with loading state
        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                // Disable button and show loading state
                sendButton.setEnabled(false);
                sendButton.setText("Sending...");
                sendButton.setIcon(null);

                dialog.dismiss();
                sendWaitlistNotifications(message);
            } else {
                // Shake animation for error
                shakeView(messageInput);
            }
        });

        // Set up the cancel button
        cancelButton.setOnClickListener(v -> {
            // Fade out animation
            dialogView.animate()
                    .alpha(0f)
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(150)
                    .withEndAction(dialog::dismiss)
                    .start();
        });

        // Show dialog with scale and fade in animation
        dialog.show();
        dialogView.setAlpha(0f);
        dialogView.setScaleX(0.9f);
        dialogView.setScaleY(0.9f);
        dialogView.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(250)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();

        // Focus on input and show keyboard
        messageInput.requestFocus();
        messageInput.postDelayed(() -> {
            if (getActivity() != null) {
                android.view.inputmethod.InputMethodManager imm =
                        (android.view.inputmethod.InputMethodManager)
                                getActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(messageInput,
                            android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                }
            }
        }, 100);
    }

    /**
     * Adds a shake animation to a view (used for error feedback)
     * @param view The view to animate
     */
    private void shakeView(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX",
                0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f);
        animator.setDuration(500);
        animator.start();
    }

    /**
     * Fetches waitlisted entrants from Firebase and sends notifications to them.
     * Now fetches full user data including FCM tokens from the users collection.
     *
     * @param message The custom message to send to waitlisted entrants
     */
    private void sendWaitlistNotifications(String message) {
        Toast.makeText(requireContext(), "Sending notifications...", Toast.LENGTH_SHORT).show();

        // Fetch the event document from Firestore
        db.collection("event-p4")
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> waitlist = (Map<String, Object>) documentSnapshot.get("waitlist");

                        if (waitlist != null) {
                            List<Map<String, Object>> waitlistedUsersData =
                                    (List<Map<String, Object>>) waitlist.get("waitlistedUsers");

                            if (waitlistedUsersData != null && !waitlistedUsersData.isEmpty()) {
                                // List to collect users with FCM tokens
                                List<User> usersWithTokens = new ArrayList<>();
                                int totalUsers = waitlistedUsersData.size();
                                int[] fetchedCount = {0}; // Counter for async operations

                                // Fetch each user's full data including FCM token
                                for (Map<String, Object> userData : waitlistedUsersData) {
                                    String userId = (String) userData.get("id");

                                    if (userId != null) {
                                        // Fetch full user document from users collection
                                        db.collection("users-p4")
                                                .document(userId)
                                                .get()
                                                .addOnSuccessListener(userDoc -> {
                                                    fetchedCount[0]++;

                                                    if (userDoc.exists()) {
                                                        User user = userDoc.toObject(User.class);
                                                        if (user != null && user.canReceiveNotifications()) {
                                                            usersWithTokens.add(user);
                                                            Log.d(TAG, "Added user with FCM token: " + user.getName());
                                                        } else {
                                                            Log.d(TAG, "User has no FCM token: " + userId);
                                                        }
                                                    }

                                                    // When all users are fetched, send notifications
                                                    if (fetchedCount[0] == totalUsers) {
                                                        sendNotificationsToUsers(usersWithTokens, message);
                                                    }
                                                })
                                                .addOnFailureListener(e -> {
                                                    fetchedCount[0]++;
                                                    Log.e(TAG, "Failed to fetch user: " + userId, e);

                                                    // Continue even if one user fetch fails
                                                    if (fetchedCount[0] == totalUsers) {
                                                        sendNotificationsToUsers(usersWithTokens, message);
                                                    }
                                                });
                                    } else {
                                        fetchedCount[0]++;
                                    }
                                }
                            } else {
                                Toast.makeText(requireContext(),
                                        "No entrants on waiting list",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(requireContext(),
                                    "Waitlist data not found",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(),
                                "Event not found",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching waitlist data", e);
                    Toast.makeText(requireContext(),
                            "Failed to send notifications: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Helper method to send notifications to the list of users with FCM tokens
     */
    private void sendNotificationsToUsers(List<User> users, String message) {
        if (users.isEmpty()) {
            Toast.makeText(requireContext(),
                    "No users with notification tokens found",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        NotificationSystem notificationSystem = new NotificationSystem(requireContext());
        String eventNameStr = getArguments().getString(ARG_EVENT_NAME);

        notificationSystem.notifyWaitlistedEntrants(
                users,
                eventNameStr,
                eventId,
                message
        );

        Toast.makeText(requireContext(),
                "Notifications sent to " + users.size() + " entrants",
                Toast.LENGTH_LONG).show();

        Log.d(TAG, "Successfully sent notifications to " + users.size() + " waitlisted entrants");
    }

    /**
     * Updates the counts displayed in the fragment for all three entrant categories.
     * This method can be called after the fragment is created to refresh the displayed
     * numbers without recreating the entire fragment.
     *
     * @param waitlist Number of users currently on the waiting list
     */
    public void updateCounts(int waitlist) {
        // Update the stored count values
        this.waitlistCount = waitlist;

        // Update the UI only if the views have been created
        // This null check prevents crashes if called before onViewCreated()
        if (waitingCountText != null) {
            waitingCountText.setText(String.valueOf(waitlist));
        }
    }
}