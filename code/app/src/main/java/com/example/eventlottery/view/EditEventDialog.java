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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * A dialog fragment that allows organizers to edit existing {@link Event} details.
 */
public class EditEventDialog extends DialogFragment {
    private Event event;
    private OnEventUpdatedListener listener;

    /**
     * Essentially a constructor for EditEventDialog. Pass in the event here, & its retrieved
     * later once the dialog is actually created
     * @param event The event we want to edit.
     * @return A new instance of {@link EditEventDialog} with the event attached.
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
     * Creates and returns the dialog for editing an event.
     * <p>
     * The dialog retrieves the event passed via {@link #newInstance(Event)},
     * displays its current waitlist maximum, and allows the user to modify it.
     * Input is validated to ensure it is a non-negative integer. Once validated,
     * the event object is updated locally, and the listener is notified.
     * </p>
     *
     * @param savedInstanceState The saved instance state of the fragment, or {@code null}
     *                           if this is a newly created dialog.
     * @return The constructed {@link Dialog} for editing the event.
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
        EditText title = dialogView.findViewById(R.id.titleInput);
        EditText description = dialogView.findViewById(R.id.descriptionInput);
        EditText location = dialogView.findViewById(R.id.locationInput);
        EditText waitlistMax = dialogView.findViewById(R.id.waitlistMaxInput);
        EditText startDate = dialogView.findViewById(R.id.startDateInput);
        EditText endDate = dialogView.findViewById(R.id.endDateInput);
        Switch geolocationSwitch = dialogView.findViewById(R.id.geolocationSwitch);

        // Set click listeners for the TimePickerDialog
        startDate.setOnClickListener(v -> openDatePicker(startDate));
        endDate.setOnClickListener(v -> openDatePicker(endDate));

        /****************************************************
         * 1. Update text fields to be what it currently is *
         ****************************************************/
        // Display current text for title, description, & location
        String currentTitle = event.getName();
        String currentDescription = event.getDescription();
        String currentLocation = event.getLocation();
        title.setText(currentTitle);
        description.setText(currentDescription);
        location.setText(currentLocation);

        // Display current waitlistMax if not default (Integer.MAX_VALUE)
        // I did it this way because I don't think we want the integer max to show. - John
        String currentMax = String.valueOf(event.getWaitlistMax());
        if (!currentMax.equals(Integer.toString(Integer.MAX_VALUE))) {
            waitlistMax.setText(currentMax);
        }

        // Display current start & end date
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CANADA);
        Date currentStartDate = event.getStartTime();
        Date currentEndDate = event.getEndTime();
        String currentStartDateString = isoFormat.format(currentStartDate);
        String currentEndDateString = isoFormat.format(currentEndDate);
        startDate.setText(currentStartDateString);
        endDate.setText(currentEndDateString);

        // Display proper geolocation status
        boolean currentGeolocation = event.getGeolocation();
        geolocationSwitch.setChecked(currentGeolocation);

        /*********************************
         * 2. Build and show AlertDialog *
         *********************************/

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Event Parameters");
        builder.setView(dialogView);
        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton("Save", (dialog, which) -> {

            /**********************
             * 3. Get title input *
             **********************/
            String titleText  = title.getText().toString();
            if (titleText.isEmpty()) {
                Toast.makeText(requireContext(), "Title can't be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            /****************************
             * 4. Get description input *
             ****************************/
            String descriptionText = description.getText().toString();
            if (descriptionText.isEmpty()) {
                Toast.makeText(requireContext(), "Description can't be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            /*************************
             * 5. Get location input *
             *************************/
            String locationText = location.getText().toString();
            if (locationText.isEmpty()) {
                Toast.makeText(requireContext(), "Location can't be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            /*****************************
             * 6. Get waitlist max input *
             *****************************/
            String waitlistMaxText = waitlistMax.getText().toString();

            // Assume no waitlist limit unless there is valid text input
            int maxSize = Integer.MAX_VALUE;

            // Get valid integer if there's any input. Check if input was valid
            if (!waitlistMaxText.isEmpty()) {
                try {
                    maxSize = Integer.parseInt(waitlistMaxText);
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
             * 7. Get valid date input *
             ***************************/
            // Get date inputs
            String startDateText = startDate.getText().toString();
            String endDateText = endDate.getText().toString();

            // Initialize date formatter & variables for storing start & end date
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
             * 8. Get geolocation input *
             ****************************/
            boolean geolocation;
            if (geolocationSwitch.isChecked()) {
                geolocation = true;
            } else { geolocation = false; }

            /***********************************************************
             * 9. Update event locally & run OrganizerPanel's listener *
             ***********************************************************/
            // TODO: Can't update event image yet
            event.setName(titleText);
            event.setDescription(descriptionText);
            event.setLocation(locationText);
            event.setWaitlistMax(maxSize);
            event.setStartTime(start);
            event.setEndTime(end);
            event.setGeolocation(geolocation);
            if (listener != null) {
                listener.onEventUpdated(event);
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