package com.example.eventlottery.view;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.eventlottery.R;
import com.example.eventlottery.users.User;
import com.example.eventlottery.events.NotificationSystem;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;


/**
 * Fragment that displays detailed infromation about a specific event for organizers.
 * Shows counts and allows notifications for waiting list, selected, and cancelled entrants.
 * */
public class OrganizerEventInfoFragment extends Fragment {
    // constant variables
    private static final String ARG_EVENT_ID = "event_id";
    private static final String ARG_EVENT_NAME = "event_name";
    private static final String ARG_WAITLIST_COUNT = "waitlist_count";

    // UI elements

    private TextView eventName;
    private TextView waitingCountText;
    private CardView waitingListCard;

    private String eventId;
    private int waitlistCount;

    /**
     * method to create a new instance of this fragment with event data
     *
     * @param eventId The unique ID of the event
     * */
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

        // Retrieve arguments passed to the fragment
        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
            String name = getArguments().getString(ARG_EVENT_NAME);
            waitlistCount = getArguments().getInt(ARG_WAITLIST_COUNT, 0);
        }
    }


    /**
     * Called to create the view hierarchy associated with the fragment.
     * This inflates the fragment's layout from XML.
     * <p>
     * Unlike Activities that use setContentView(), Fragments must inflate their layout
     * and return the root View. The layout is then attached to the parent container
     * by the FragmentManager.
     * </p>
     *
     * @param inflater The LayoutInflater object that can be used to inflate views
     * @param container The parent view that the fragment's UI should be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     * @return The root View of the inflated layout for this fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Note: Pass 'false' for attachToRoot - the FragmentManager handles attachment
        return inflater.inflate(R.layout.organizer_event_info, container, false);
    }

    /**
     * Called immediately after onCreateView() when the fragment's view hierarchy has been created.
     * This is where we initialize UI components and set up listeners.
     * <p>
     * At this point, findViewById() calls are safe because the view has been inflated.
     * We use view.findViewById() instead of just findViewById() because we need to search
     * within the fragment's view hierarchy, not the entire activity.
     * </p>
     *
     * @param view The View returned by onCreateView()
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
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
            //requireActivity().findViewById(R.id.fragment_container).setVisibility(View.GONE);
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
     * <p>
     * The three states are:
     * <ul>
     *     <li><b>Waiting List:</b> Users who have joined the waitlist but haven't been selected</li>
     *     <li><b>Selected:</b> Users who have been chosen to participate in the event</li>
     *     <li><b>Cancelled:</b> Users whose participation has been cancelled</li>
     * </ul>
     * </p>
     */
    private void setClickListeners() {
        // Click listener for waiting list card - notifies all waitlisted entrants
        waitingListCard.setOnClickListener(v -> {
            // Show a dialog to get custom message from organizer
            showNotificationDialog();
        });
        // TODO: Add click listeners for selected and cancelled cards

    }

    /**
     * Shows a dialog allowing the organizer to compose and send a notification
     * to all waitlisted entrants for this event.
     * */
    private void showNotificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Notify Waiting List");

        // Create input field for custom message
        final EditText input = new EditText(requireContext());
        input.setHint("Enter message for walisted entrants...");
        input.setMinLines(3);
        input.setGravity(Gravity.TOP | Gravity.START);

        builder.setView(input);

        builder.setPositiveButton("Send", (dialog, which) -> {
            // Send notification to all waitlisted entrants
            String message = input.getText().toString().trim();
            if (!message.isEmpty()) {
                sendWaitlistNotifications(message);
            }
            else {
                Toast.makeText(requireContext(), "Please enter a message", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel();
        });

        builder.show();
    }

    /**
     * Fetches waitlisted entrants from Firebase and sends notifications to them
     * @param message The custom message to waitlist entrants
     * */
    private void sendWaitlistNotifications(String message) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("event-p4").document(eventId)
                .collection("waitlist")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<User> waitlistedUsers = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            waitlistedUsers.add(user);
                        }
                    }

                    if (!waitlistedUsers.isEmpty()) {
                        NotificationSystem notificationSystem = new NotificationSystem(requireContext());
                        notificationSystem.notifyWaitlistedEntrants(
                                waitlistedUsers,
                                getArguments().getString(ARG_EVENT_NAME),
                                eventId,
                                message
                        );
                        Toast.makeText(requireContext(),
                                "Notifications sent to " + waitlistedUsers.size() + " entrants",
                                Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(requireContext(),
                                "No entrants on waiting list",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("OrganizerEventInfo", "Error fetching waitlist", e);
                    Toast.makeText(requireContext(),
                            "Failed to send notifications",
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Updates the counts displayed in the fragment for all three entrant categories.
     * This method can be called after the fragment is created to refresh the displayed
     * numbers without recreating the entire fragment.
     * <p>
     * This is useful when entrant statuses change (e.g., someone moves from waiting list
     * to selected, or cancels their participation) and the UI needs to reflect the new counts.
     * </p>
     *
     * @param waitlist Number of users currently on the waiting list
     */
    public void updateCounts(int waitlist) {
        // Update the stored count values
        this.waitlistCount = waitlist;
        //this.selectedCount = selected;
        //this.cancelledCount = cancelled;

        // Update the UI only if the views have been created
        // This null check prevents crashes if called before onViewCreated()
        if (waitingCountText != null) {
            waitingCountText.setText(String.valueOf(waitlist));
            //selectedCountText.setText(String.valueOf(selected));
            //cancelledCountText.setText(String.valueOf(cancelled));
        }
    }
}



