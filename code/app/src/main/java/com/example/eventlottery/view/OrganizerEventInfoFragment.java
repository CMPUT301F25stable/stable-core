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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;


/**
 * Shows counts and allows notifications for waiting list, selected, and cancelled entrants.
 * @author Jensen Lee
 */
public class OrganizerEventInfoFragment extends Fragment {
    // constant variables
    private static final String TAG = "OrganizerEventInfo";
    private static final String ARG_EVENT_ID = "event_id";
    private static final String ARG_EVENT_NAME = "event_name";
    private static final String ARG_WAITLIST_COUNT = "waitlist_count";
    private static final String ARG_SELECTED_COUNT = "selected_count";
    private static final String ARG_CANCELLED_COUNT = "cancelled_count";
    private static final String ARG_ACCEPTED_COUNT = "accepted_count";

    private enum EntrantType {
        WAITLIST,
        SELECTED,
        CANCELLED,
        ACCEPTED
    }

    // UI elements
    private TextView eventName;
    private TextView waitingCountText;
    private TextView selectedCountText;
    private TextView cancelledCountText;
    private TextView acceptedCountText;
    private CardView waitingListCard;
    private CardView selectedListCard;
    private CardView cancelledListCard;
    private CardView acceptedListCard;

    // Data fields
    private String eventId;
    private int waitlistCount;
    private int selectedCount;
    private int cancelledCount;
    private int acceptedCount;
    private String organizerId;
    private String organizerName;

    // Firebase
    private FirebaseFirestore db;
    private ListenerRegistration eventListener;

    /**
     * Method to create a new instance of this fragment with event data
     *
     * @param eventId The unique ID of the event
     * @param eventName The name of the event
     * @param waitlistCount The number of users on the waitlist
     */
    public static OrganizerEventInfoFragment newInstance(String eventId, String eventName, int waitlistCount,
                                                         int selectedCount, int cancelledCount, int acceptedCount,
                                                         String organizerId, String organizerName) {
        OrganizerEventInfoFragment fragment = new OrganizerEventInfoFragment();

        // Create a Bundle to store arguments - this ensures data survives configuration changes
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_EVENT_NAME, eventName);
        args.putInt(ARG_WAITLIST_COUNT, waitlistCount);
        args.putInt(ARG_SELECTED_COUNT, selectedCount);
        args.putInt(ARG_CANCELLED_COUNT, cancelledCount);
        args.putInt(ARG_ACCEPTED_COUNT, acceptedCount);
        args.putString("organizerId", organizerId);
        args.putString("organizerName", organizerName);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called when the fragment is first created.
     * @param savedInstanceState If the fragment is being re-created from a previous state,
     *                           this is the saved instance state
     *                           of the previous version of the fragment.
     *                           Note that this can be null if this is the first creation.
     *                           This is used to restore the state of the fragment
     * */
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
            selectedCount = getArguments().getInt(ARG_SELECTED_COUNT, 0);
            cancelledCount = getArguments().getInt(ARG_CANCELLED_COUNT, 0);
            acceptedCount = getArguments().getInt(ARG_ACCEPTED_COUNT, 0);
            organizerId = getArguments().getString("organizerId");
            organizerName = getArguments().getString("organizerName");
        }
    }


    /**
     * Called to create the view hierarchy associated with the fragment.
     * @param inflater The LayoutInflater object that can be used to inflate
     *                 any views in the fragment
     * @param container If non-null, this is the parent view that the fragment's
     *                  UI should be attached to.
     * @param savedInstanceState If the fragment is being re-created from a previous state,
     *                           this is the saved instance state of the previous version of the fragment.
     *
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.organizer_event_info, container, false);
    }

    /**
     * Called immediately after onCreateView() when the fragment's view hierarchy has been created.
     * This is where we initialize UI components and set up listeners.
     * @param view The root view of the fragment's layout
     * @param savedInstanceState If the fragment is being re-created from a previous state,
     *                           this is the saved instance state
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI elements by finding them in the fragment's view hierarchy
        eventName = view.findViewById(R.id.headerText);
        waitingCountText = view.findViewById(R.id.waitingCount);
        selectedCountText = view.findViewById(R.id.selectedCount);
        cancelledCountText = view.findViewById(R.id.cancelledCount);
        acceptedCountText = view.findViewById(R.id.acceptedCount);

        // Get references to the clickable cards
        waitingListCard = view.findViewById(R.id.waitingListCard);
        selectedListCard = view.findViewById(R.id.selectedCard);
        cancelledListCard = view.findViewById(R.id.cancelledCard);
        acceptedListCard = view.findViewById(R.id.acceptedCard);

        // Close Button
        ImageButton closeButton = view.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> {
            if (requireActivity() instanceof OrganizerPanel) {
                ((OrganizerPanel) requireActivity()).showEventList();
            } else {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });


        // Populate the UI with data from arguments
        if (getArguments() != null) {
            eventName.setText(getArguments().getString(ARG_EVENT_NAME));
            waitingCountText.setText(String.valueOf(waitlistCount));
            selectedCountText.setText(String.valueOf(selectedCount));
            cancelledCountText.setText(String.valueOf(cancelledCount));
            acceptedCountText.setText(String.valueOf(acceptedCount));
        }

        // Set up click listeners for user interactions
        setClickListeners();

        // Set up real-time listener for count updates
        setupRealtimeListener();
    }

    /**
     * Sets up a real-time listener for event data changes
     */
    private void setupRealtimeListener() {
        if (eventId == null) return;

        eventListener = db.collection("event-p4")
                .document(eventId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error listening to event updates", error);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        updateCountsFromSnapshot(documentSnapshot);
                    }
                });
    }

    /**
     * Updates counts from a Firestore document snapshot
     * @param snapshot The Firestore document snapshot to update from
     */
    private void updateCountsFromSnapshot(DocumentSnapshot snapshot) {
        // Get waitlist count
        Map<String, Object> waitlist = (Map<String, Object>) snapshot.get("waitlist");
        if (waitlist != null) {
            List<Map<String, Object>> waitlistedUsers =
                    (List<Map<String, Object>>) waitlist.get("waitlistedUsers");
            waitlistCount = (waitlistedUsers != null) ? waitlistedUsers.size() : 0;
        } else {
            waitlistCount = 0;
        }

        // Get selected count
        List<String> selectedIds = (List<String>) snapshot.get("selectedIds");
        selectedCount = (selectedIds != null) ? selectedIds.size() : 0;

        // Get cancelled count
        List<String> cancelledEntrants = (List<String>) snapshot.get("cancelledEntrants");
        cancelledCount = (cancelledEntrants != null) ? cancelledEntrants.size() : 0;

        // Get accepted count
        List<String> acceptedIds = new ArrayList<>();
        Map<String, Object> finalizedList = (Map<String, Object>) snapshot.get("finalizedList");
        if (finalizedList != null) {
            List<Map<String, Object>> finalizedUsers =
                    (List<Map<String, Object>>) finalizedList.get("finalizedUsers");
            acceptedCount = (finalizedUsers != null) ? finalizedUsers.size() : 0;
        }
        else {
            acceptedCount = 0;
        }

        // Update UI
        if (waitingCountText != null) {
            waitingCountText.setText(String.valueOf(waitlistCount));
        }
        if (selectedCountText != null) {
            selectedCountText.setText(String.valueOf(selectedCount));
        }
        if (cancelledCountText != null) {
            cancelledCountText.setText(String.valueOf(cancelledCount));
        }
        if (acceptedCountText != null) {
            acceptedCountText.setText(String.valueOf(acceptedCount));
        }
    }

    /**
     * Called when the fragment is no longer visible to the user.
     * Stops the real-time listener.
     * */
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Remove the Firestore listener when the view is destroyed
        if (eventListener != null) {
            eventListener.remove();
            eventListener = null;
        }
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
                showNotificationDialog(EntrantType.WAITLIST);
            } else {
                Toast.makeText(requireContext(),
                        "No entrants on waiting list",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Click listener for selected list card
        selectedListCard.setOnClickListener(v -> {
            if (selectedCount > 0) {
                showNotificationDialog(EntrantType.SELECTED);
            } else {
                Toast.makeText(requireContext(),
                        "No entrants selected or notified",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Click listener for cancelled list card
        cancelledListCard.setOnClickListener(v -> {
            if (cancelledCount > 0) {
                showNotificationDialog(EntrantType.CANCELLED);
            }
            else {
                Toast.makeText(requireContext(),
                        "No entrants has cancelled",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Click listener for acceptedCard
        acceptedListCard.setOnClickListener(v -> {
            if (acceptedCount > 0) {
                showNotificationDialog(EntrantType.ACCEPTED);
            }
            else {
                Toast.makeText(requireContext(),
                        "No entrants have accepted",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Shows a Material 3 styled dialog with quick suggestions allowing the organizer
     * to compose and send a notification to entrants.
     *
     * @param entrantType The type of entrants to notify (WAITLIST, SELECTED, or CANCELLED)
     */
    private void showNotificationDialog(EntrantType entrantType) {
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

        // Determine count and recipient text based on entrant type
        int count;
        if (entrantType == EntrantType.WAITLIST) {
            count = waitlistCount;
        } else if (entrantType == EntrantType.SELECTED) {
            count = selectedCount;
        } else if (entrantType == EntrantType.ACCEPTED) {
            count = acceptedCount;
        }
        else {
            count = cancelledCount;
        }

        String recipientText = count + " " + (count == 1 ? "recipient" : "recipients");
        recipientCount.setText(recipientText);

        // Create and configure the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Make dialog background transparent for rounded corners to show
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Quick suggestion templates - customize based on entrant type
        Map<Chip, String> suggestionTemplates = new HashMap<>();
        String eventNameStr = getArguments().getString(ARG_EVENT_NAME);

        if (entrantType == EntrantType.WAITLIST) {
            suggestionTemplates.put(suggestionChip1,
                    "Thank you for joining the waitlist for " + eventNameStr + "! We'll notify you of any updates.");
            suggestionTemplates.put(suggestionChip2,
                    "Important update regarding " + eventNameStr + ": ");
            suggestionTemplates.put(suggestionChip3,
                    "Reminder: The event " + eventNameStr + " is coming up soon!");
        } else if (entrantType == EntrantType.SELECTED) {
            suggestionTemplates.put(suggestionChip1,
                    "Congratulations! You've been selected for " + eventNameStr + ". Please confirm your attendance.");
            suggestionTemplates.put(suggestionChip2,
                    "Important information for selected participants of " + eventNameStr + ": ");
            suggestionTemplates.put(suggestionChip3,
                    "Reminder: Don't forget to confirm your spot for " + eventNameStr + "!");
        } else if (entrantType == EntrantType.ACCEPTED) {
            suggestionTemplates.put(suggestionChip1,
                    "Thank you for accepting! Here are the details for " + eventNameStr + ":");
            suggestionTemplates.put(suggestionChip2,
                    "Important information for accepted participants of " + eventNameStr + ": ");
            suggestionTemplates.put(suggestionChip3,
                    "Reminder: We're looking forward to seeing you at " + eventNameStr + "!");
        }

        else { // CANCELLED
            suggestionTemplates.put(suggestionChip1,
                    "We noticed you cancelled your spot for " + eventNameStr + ". You're welcome to join again!");
            suggestionTemplates.put(suggestionChip2,
                    "Update regarding " + eventNameStr + ": New spots have become available.");
            suggestionTemplates.put(suggestionChip3,
                    "Thank you for your interest in " + eventNameStr + ". We hope to see you at future events!");
        }

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

                // Animate send button when enabled
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

                // Call appropriate notification method based on entrant type
                if (entrantType == EntrantType.WAITLIST) {
                    sendWaitlistNotifications(message);
                } else if (entrantType == EntrantType.SELECTED) {
                    sendSelectedNotifications(message);
                } else if (entrantType == EntrantType.ACCEPTED) {
                    sendAcceptedNotifications(message);
                }

                else { // CANCELLED
                    sendCancelledNotifications(message);
                }
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
     * Fetches waitlisted entrants from Firebase and sends notifications to them.
     * Retrieves FCM tokens from the users-p4 collection.
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
                                fetchUsersAndSendNotifications(waitlistedUsersData, message, "waitlist");
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
     * Fetches selected entrants from Firebase and sends notifications to them.
     * Retrieves FCM tokens from the users-p4 collection.
     * Handles selectedIds as an array of user ID strings.
     *
     * @param message The custom message to send to selected entrants
     */
    private void sendSelectedNotifications(String message) {
        Toast.makeText(requireContext(), "Sending notifications...", Toast.LENGTH_SHORT).show();

        // Fetch the event document from Firestore
        db.collection("event-p4")
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Get selected user IDs from the selectedIds array
                        List<String> selectedUserIds = (List<String>) documentSnapshot.get("selectedIds");

                        if (selectedUserIds != null && !selectedUserIds.isEmpty()) {
                            // Convert List<String> to List<Map<String, Object>> format
                            List<Map<String, Object>> selectedUsersData = new ArrayList<>();
                            for (String userId : selectedUserIds) {
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("id", userId);
                                selectedUsersData.add(userData);
                            }

                            fetchUsersAndSendNotifications(selectedUsersData, message, "selected");
                        } else {
                            Toast.makeText(requireContext(),
                                    "No selected entrants found",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(),
                                "Event not found",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching selected users data", e);
                    Toast.makeText(requireContext(),
                            "Failed to send notifications: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Fetches cancelled entrants from Firebase and sends notifications to them.
     * Retrieves FCM tokens from the users-p4 collection.
     * Handles cancelledEntrants as an array of user ID strings.
     *
     * @param message The custom message to send to cancelled entrants
     */
    private void sendCancelledNotifications(String message) {
        Toast.makeText(requireContext(), "Sending notifications...", Toast.LENGTH_SHORT).show();

        // Fetch the event document from Firestore
        db.collection("event-p4")
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Get cancelled user IDs from the cancelledIds array
                        List<String> cancelledUserIds = (List<String>) documentSnapshot.get("cancelledEntrants");

                        if (cancelledUserIds != null && !cancelledUserIds.isEmpty()) {
                            // Convert List<String> to List<Map<String, Object>> format
                            List<Map<String, Object>> cancelledUsersData = new ArrayList<>();
                            for (String userId : cancelledUserIds) {
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("id", userId);
                                cancelledUsersData.add(userData);
                            }

                            fetchUsersAndSendNotifications(cancelledUsersData, message, "cancelled");
                        } else {
                            Toast.makeText(requireContext(),
                                    "No cancelled entrants found",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(),
                                "Event not found",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching cancelled users data", e);
                    Toast.makeText(requireContext(),
                            "Failed to send notifications: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Fetches accepted entrants from Firebase and sends notifications to them.
     */
    private void sendAcceptedNotifications(String message) {
        Toast.makeText(requireContext(), "Sending notifications...", Toast.LENGTH_SHORT).show();

        db.collection("event-p4")
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> finalizedList =
                                (Map<String, Object>) documentSnapshot.get("finalizedList");

                        if (finalizedList != null) {
                            List<Map<String, Object>> finalizedUsers =
                                    (List<Map<String, Object>>) finalizedList.get("finalizedUsers");

                            if (finalizedUsers != null && !finalizedUsers.isEmpty()) {
                                fetchUsersAndSendNotifications(finalizedUsers, message, "accepted");
                            } else {
                                Toast.makeText(requireContext(),
                                        "No accepted entrants found",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(requireContext(),
                                    "No accepted entrants found",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(),
                                "Event not found",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching accepted users data", e);
                    Toast.makeText(requireContext(),
                            "Failed to send notifications: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Common method to fetch user documents and send notifications.
     * Works with users-p4 collection and checks for FCM tokens.
     *
     * @param usersData List of user data maps containing user IDs
     * @param message The notification message to send
     * @param type The type of notification ("waitlist", "selected", or "cancelled")
     */
    private void fetchUsersAndSendNotifications(List<Map<String, Object>> usersData, String message, String type) {
        List<User> usersWithTokens = new ArrayList<>();
        int totalUsers = usersData.size();
        int[] fetchedCount = {0};

        // Fetch each user's full data including FCM token from users-p4
        for (Map<String, Object> userData : usersData) {
            String userId = (String) userData.get("id");

            Log.d(TAG, "Attempting to fetch user document: users-p4/" + userId);

            if (userId != null) {
                // Fetch full user document from users-p4 collection
                db.collection("users-p4")
                        .document(userId)
                        .get()
                        .addOnSuccessListener(userDoc -> {
                            fetchedCount[0]++;

                            if (userDoc.exists()) {
                                User user = userDoc.toObject(User.class);
                                if (user != null && user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {
                                    usersWithTokens.add(user);
                                    Log.d(TAG, "Added user with FCM token: " + user.getName());
                                } else {
                                    Log.d(TAG, "User has no FCM token: " + userId);
                                }
                            }

                            // When all users are fetched, send notifications
                            if (fetchedCount[0] == totalUsers) {
                                if (type.equals("waitlist")) {
                                    sendNotificationsToWaitlistedUsers(usersWithTokens, message);
                                } else if (type.equals("selected")) {
                                    sendNotificationsToSelectedUsers(usersWithTokens, message);
                                } else if (type.equals("accepted")) {
                                    sendNotificationsToAcceptedUsers(usersWithTokens, message);
                                }
                                else { // cancelled
                                    sendNotificationsToCancelledUsers(usersWithTokens, message);
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            fetchedCount[0]++;
                            Log.e(TAG, "Failed to fetch user: " + userId, e);

                            // Continue even if one user fetch fails
                            if (fetchedCount[0] == totalUsers) {
                                if (type.equals("waitlist")) {
                                    sendNotificationsToWaitlistedUsers(usersWithTokens, message);
                                } else if (type.equals("selected")) {
                                    sendNotificationsToSelectedUsers(usersWithTokens, message);
                                } else { // cancelled
                                    sendNotificationsToCancelledUsers(usersWithTokens, message);
                                }
                            }
                        });
            } else {
                fetchedCount[0]++;
            }
        }
    }

    /**
     * Helper method to send notifications to waitlisted users with FCM tokens.
     * @param users The list of users to send notifications to
     * @param message The notification message to send
     */
    private void sendNotificationsToWaitlistedUsers(List<User> users, String message) {
        if (users.isEmpty()) {
            Toast.makeText(requireContext(),
                    "No users with notification tokens found",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        NotificationSystem notificationSystem = new NotificationSystem(requireContext(), organizerId, organizerName);
        String eventNameStr = getArguments().getString(ARG_EVENT_NAME);

        // Use the NotificationSystem instead of calling Cloud Function directly
        notificationSystem.notifyWaitlistedEntrants(
                users,
                eventNameStr,
                eventId,
                message
        );

        Toast.makeText(requireContext(),
                "Notifications sent to " + users.size() + " waitlisted entrants",
                Toast.LENGTH_LONG).show();

        Log.d(TAG, "Successfully sent notifications to " + users.size() + " waitlisted entrants");
    }


    /**
     * Helper method to send notifications to selected users with FCM tokens.
     * @param users The list of users to send notifications to
     * @param message The notification message to send
     */
    private void sendNotificationsToSelectedUsers(List<User> users, String message) {
        if (users.isEmpty()) {
            Toast.makeText(requireContext(),
                    "No users with notification tokens found",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        NotificationSystem notificationSystem = new NotificationSystem(requireContext(), organizerId, organizerName);
        String eventNameStr = getArguments().getString(ARG_EVENT_NAME);

        // Use the selected entrants notification method
        notificationSystem.notifySelectedEntrants(
                users,
                eventNameStr,
                eventId,
                message
        );

        Toast.makeText(requireContext(),
                "Notifications sent to " + users.size() + " selected entrants",
                Toast.LENGTH_LONG).show();

        Log.d(TAG, "Successfully sent notifications to " + users.size() + " selected entrants");
    }

    /**
     * Helper method to send notifications to cancelled users with FCM tokens.
     * @param users The list of users to send notifications to
     * @param message The notification message to send
     */
    private void sendNotificationsToCancelledUsers(List<User> users, String message) {
        if (users.isEmpty()) {
            Toast.makeText(requireContext(),
                    "No users with notification tokens found",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        NotificationSystem notificationSystem = new NotificationSystem(requireContext(), organizerId, organizerName);
        String eventNameStr = getArguments().getString(ARG_EVENT_NAME);

        // Use the cancelled entrants notification method
        notificationSystem.notifyCancelledEntrants(
                users,
                eventNameStr,
                eventId,
                message
        );

        Toast.makeText(requireContext(),
                "Notifications sent to " + users.size() + " cancelled entrants",
                Toast.LENGTH_LONG).show();

        Log.d(TAG, "Successfully sent notifications to " + users.size() + " cancelled entrants");
    }

    /**
     * Helper method to send notifications to accepted users with FCM tokens.
     */
    private void sendNotificationsToAcceptedUsers(List<User> users, String message) {
        if (users.isEmpty()) {
            Toast.makeText(requireContext(),
                    "No users with notification tokens found",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        NotificationSystem notificationSystem = new NotificationSystem(requireContext(), organizerId, organizerName);
        String eventNameStr = getArguments().getString(ARG_EVENT_NAME);

        notificationSystem.notifyAcceptedEntrants(
                users,
                eventNameStr,
                eventId,
                message
        );

        Toast.makeText(requireContext(),
                "Notifications sent to " + users.size() + " accepted entrants",
                Toast.LENGTH_LONG).show();

        Log.d(TAG, "Successfully sent notifications to " + users.size() + " accepted entrants");
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
}