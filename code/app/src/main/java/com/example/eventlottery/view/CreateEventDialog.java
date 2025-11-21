package com.example.eventlottery.view;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.eventlottery.R;
import com.example.eventlottery.events.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
        EditText startDate = dialogView.findViewById(R.id.startDateInput);
        EditText endDate = dialogView.findViewById(R.id.endDateInput);
        Switch geolocationSwitch = dialogView.findViewById(R.id.geolocationSwitch);

        // Set click listeners for the TimePickerDialog
        startDate.setOnClickListener(v -> openDatePicker(startDate));
        endDate.setOnClickListener(v -> openDatePicker(endDate));

        // Build & show AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("New Event Parameters");
        builder.setView(dialogView);
        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton("Save", (dialog, which) -> {
            // Get user inputs
            String text = waitlistMax.getText().toString();
            String startDateText = startDate.getText().toString();
            String endDateText = endDate.getText().toString();

            /******************************
             * 1. Get valid integer input *
             ******************************/

            // Assume no waitlist limit unless there is valid text input
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

            /***************************
             * 2. Get valid date input *
             ***************************/
            // Initialize stuff for getting date
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CANADA);
            Date start;
            Date end;

            // Check if either start or end date is empty
            if (startDateText.isEmpty() || endDateText.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter both start and end dates", Toast.LENGTH_SHORT).show();
                return;
            }

            // Parse inputs into dates
            try {
                start = dateFormat.parse(startDateText);
                end = dateFormat.parse(endDateText);
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

            /****************************
             * 3. Get geolocation input *
             ****************************/
            boolean geolocation;
            if (geolocationSwitch.isChecked()) {
                geolocation = true;
            } else { geolocation = false; }

            /*************************************
             * 4. Create event, given the inputs *
             *************************************/
            // TODO: This can only set waiting list max, start and end date, and geolocation right now. Implement more later
            Event newEvent = new Event("Filler Title", "Event Description", "Event Location", "Organizer ID", "", start, end, new ArrayList<>(), geolocation);
            newEvent.setWaitlistMax(maxSize);

            // Run organizer panel's listener if something was created
            if (listener != null) {
                listener.onEventCreated(newEvent);
            }
        });

        return builder.create();
    }

    /**
     * Opens the built-in DatePickerDialog from Android Studio, given a target text field.
     * References: https://www.youtube.com/watch?v=TCUfcNzS6Xk
     * @param targetText the text field we want to set a listener on
     */
    private void openDatePicker(EditText targetText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                targetText.setText(String.valueOf(year) + "-" + String.valueOf(month) + "-" + String.valueOf(dayOfMonth));
            }
        }, year, month, day);
        dialog.show();
    }
}
