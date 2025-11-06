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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditEventDialog extends DialogFragment {
    private Event event;
    private OnEventUpdatedListener listener;

    /**
     * Essentially a constructor for EditEventDialog. Pass in the event here, & its retrieved
     * later once the dialog is actually created
     * @param event The event we want to edit.
     * @return The new instance of EditEventDialog.
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


    /**
     * Returns the dialog used for editing an event.
     * Changes event and tells the listener to run, given that all inputs are valid.
     * @param savedInstanceState The last saved instance state of the Fragment,
     * or null if this is a freshly created Fragment.
     *
     * @return The dialog for editing an event.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Get event we want to edit
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
        }

        // Inflate dialog view
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_event, null);

        // Set up variables for getting input
        EditText waitlistMax = dialogView.findViewById(R.id.waitlistMaxInput);
        EditText startDate = dialogView.findViewById(R.id.startDateInput);
        EditText endDate = dialogView.findViewById(R.id.endDateInput);
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CANADA);

        /****************************************************
         * 1. Update text to represent what it currently is *
         ****************************************************/

        // Display current waitlistMax if not default (Integer.MAX_VALUE)
        String currentMax = String.valueOf(event.getWaitlistMax());
        if (!currentMax.equals(Integer.toString(Integer.MAX_VALUE))) {
            waitlistMax.setText(currentMax);
        }
        // I'm not displaying the start time & end time because I think the implementation will probably be changed later - John

        // Build and show AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Event Parameters");
        builder.setView(dialogView);
        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton("Save", (dialog, which) -> {
            // Get user inputs
            String text = waitlistMax.getText().toString();
            String startDateText = startDate.getText().toString();
            String endDateText = endDate.getText().toString();

            /******************************
             * 2. Get valid integer input *
             ******************************/
            int maxSize = Integer.MAX_VALUE;  // Assume max for now. Changes if there is an input

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

            /***************************
             * 3. Get valid date input *
             ***************************/
            // Initialize relevant variables
            Date start;
            Date end;

            // Check if either start or end date is empty
            if (startDateText.isEmpty() || endDateText.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter both start and end dates", Toast.LENGTH_SHORT).show();
                return;
            }

            // Parse inputs into dates
            try {
                start = isoFormat.parse(startDateText);
                end = isoFormat.parse(endDateText);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Please enter valid dates (YYYY-MM-DD)", Toast.LENGTH_SHORT).show();
                return;
            }


            // Initialize calendar object
            Calendar cal = Calendar.getInstance();

            // Set start time to 11:59:59 of that day
            cal.setTime(start);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            start = cal.getTime();

            // Set end time to 11:59:59 as well
            cal.setTime(end);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            end = cal.getTime();

            /***********************************************************
             * 4. Update event locally & run OrganizerPanel's listener *
             ***********************************************************/
            event.setWaitlistMax(maxSize);
            event.setStartTime(start);
            event.setEndTime(end);
            if (listener != null) {
                listener.onEventUpdated(event);
            }
        });

        return builder.create();
    }

}