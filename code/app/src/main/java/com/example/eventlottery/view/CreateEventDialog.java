package com.example.eventlottery.view;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.eventlottery.R;
import com.example.eventlottery.events.Event;

import java.util.ArrayList;
import java.util.Date;

/**
 * A dialog fragment that allows organizers to create new {@link Event} objects.
 * <p>
 * The dialog currently supports setting only the waitlist maximum size but can
 * be extended in the future to support more event parameters such as name,
 * description, and date. Once an event is created, the dialog notifies a
 * listener via the {@link OnEventCreatedListener} interface.
 * </p>
 */
public class CreateEventDialog extends DialogFragment {
    private OnEventCreatedListener listener;
    /**
     * Listener interface for receiving event creation callbacks.
     * Implementations of this interface are notified when a new {@link Event}
     * is created through this dialog.
     */
    public interface OnEventCreatedListener {
        /**
         * Called when a new event is created by the user.
         *
         * @param event The newly created {@link Event} object.
         */
        void onEventCreated(Event event);
    }

    /**
     * Sets the event listener for this dialog
     * @param listener The listener.
     */
    public void setOnEventCreatedListener(OnEventCreatedListener listener) {
        this.listener = listener;
    }

    /**
     * Returns the dialog for creating an event.
     * @param savedInstanceState The last saved instance state of the Fragment,
     * or null if this is a freshly created Fragment.
     *
     * @return The dialog.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Inflate dialog view
        LayoutInflater inflater = getLayoutInflater();
        // Note: This uses the same layout as editing the event
        View dialogView = inflater.inflate(R.layout.dialog_edit_event, null);

        // Set up variables for getting input
        EditText waitlistMax = dialogView.findViewById(R.id.waitlistMaxInput);

        // Build & show AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("New Event Parameters");
        builder.setView(dialogView);
        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton("Save", (dialog, which) -> {
            String text = waitlistMax.getText().toString();
            // Assume no waitlist limit unless there ise valid text input
            int maxSize = Integer.MAX_VALUE;

            // Get valid integer if there's any input
            if (!text.isEmpty()) {
                try {
                    maxSize = Integer.parseInt(text);
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), "Please enter a valid number", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Check if input is negative
            if (maxSize < 0) {
                Toast.makeText(requireContext(), "Waitlist max can't be negative", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create event & notify OrganizerPanel that it was created
            // TODO: This can only set waiting list max right now. Implement more later
            Date date = new Date();
            Event newEvent = new Event("Filler Title", "Event Description", "Event Location", "Organizer ID", "", date, date);
            newEvent.setWaitlistMax(maxSize);

            // Notify OrganizerPanel that something was created
            if (listener != null) {
                listener.onEventCreated(newEvent);
            }
        });

        return builder.create();
    }
}
