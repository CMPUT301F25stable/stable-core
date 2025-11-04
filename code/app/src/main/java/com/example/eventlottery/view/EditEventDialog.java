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
import com.example.eventlottery.model.EventDatabase;

public class EditEventDialog extends DialogFragment {
    private Event event;
    private EventDatabase eventDatabase;
    private OnEventUpdatedListener listener;

    /**
     * Essentially a constructor for EditEventDialog. Pass in the event here, & its retrieved
     * later once the dialog is actually created
     * @param event The event we want to edit.
     * @return
     */
    public static EditEventDialog newInstance(Event event) {
        EditEventDialog dialog = new EditEventDialog();
        Bundle args = new Bundle();
        args.putSerializable("event", event);
        dialog.setArguments(args);
        return dialog;
    }

    /**
     * Listener interface for notifying when an event is updated.
     */
    public interface OnEventUpdatedListener {
        void onEventUpdated(Event updatedEvent);
    }

    /**
     * Sets the listener for this dialog.
     * @param listener The listener.
     */
    public void setOnEventUpdatedListener(OnEventUpdatedListener listener) {
        this.listener = listener;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Get event we want to edit
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
        }

        // Inflate dialog view & get EditText input
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_event, null);
        EditText waitlistMax = dialogView.findViewById(R.id.waitlistMaxInput);

        // Display current waitlistMax if not default (Integer.MAX_VALUE)
        String currentMax = String.valueOf(event.getWaitlistMax());
        if (!currentMax.equals(Integer.toString(Integer.MAX_VALUE))) {
            waitlistMax.setText(currentMax);
        }

        // Build and show AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Event Parameters");
        builder.setView(dialogView);
        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton("Save", (dialog, which) -> {
            String text = waitlistMax.getText().toString();
            int maxSize = Integer.MAX_VALUE;

            // Get valid integer if any input
            if (!text.isEmpty()) {
                try {
                    maxSize = Integer.parseInt(text);
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Please enter a valid number", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Check if input is negative
            if (maxSize < 0) {
                Toast.makeText(getContext(), "Waitlist max can't be negative", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update event locally only
            event.setWaitlistMax(maxSize);

            // Notify OrganizerPanel that event was updated
            if (listener != null) {
                listener.onEventUpdated(event);
            }
        });

        return builder.create();
    }

}