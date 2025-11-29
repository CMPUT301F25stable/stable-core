package com.example.eventlottery.view;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.eventlottery.R;
import com.example.eventlottery.events.Event;
import com.example.eventlottery.events.NotificationSystem;
import com.example.eventlottery.users.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A dialog fragment that allows organizers to edit existing {@link Event} details
 * and run lottery drawings.
 */
public class EditEventDialog extends DialogFragment {
    private static final String TAG = "EditEventDialog";

    private Event event;
    private OnEventUpdatedListener listener;
    private FirebaseFirestore db;
    private LinearLayout lotteryInputLayout;
    private EditText lotterySizeInput;
    private TextView waitlistInfoText;
    private Button runLotteryButton;
    private LinearLayout lotteryStatusLayout;
    private TextView lotteryResultText;

    private int currentWaitlistCount = 0;

    private ImageView eventPosterPreview;
    private Button changePosterButton;
    private Button removePosterButton;
    private Uri newPosterUri = null;
    private static final int PICK_IMAGE_REQUEST = 1;
    private FirebaseStorage storage;

    /**
     * Essentially a constructor for EditEventDialog. Pass in the event here, & its retrieved
     * later once the dialog is actually created
     * @param event The event we want to edit.
     * @param isEditMode True if editing existing event, false if creating new event
     * @return A new instance of {@link EditEventDialog} with the event attached.
     */
    public static EditEventDialog newInstance(Event event, boolean isEditMode) {
        EditEventDialog dialog = new EditEventDialog();
        Bundle args = new Bundle();
        args.putSerializable("event", event);
        args.putBoolean("isEditMode", isEditMode);
        dialog.setArguments(args);
        return dialog;
    }

    public interface OnEventUpdatedListener {
        void onEventUpdated(Event updatedEvent);
    }

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

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Get event we want to edit and check mode
        boolean isEditMode = false;
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
            isEditMode = getArguments().getBoolean("isEditMode", false);
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

        // Set up lottery UI elements
        lotteryInputLayout = dialogView.findViewById(R.id.lotteryInputLayout);
        lotterySizeInput = dialogView.findViewById(R.id.lotterySizeInput);
        waitlistInfoText = dialogView.findViewById(R.id.waitlistInfoText);
        runLotteryButton = dialogView.findViewById(R.id.runLotteryButton);
        lotteryStatusLayout = dialogView.findViewById(R.id.lotteryStatusLayout);
        lotteryResultText = dialogView.findViewById(R.id.lotteryResultText);

        // Set up poster UI elements
        eventPosterPreview = dialogView.findViewById(R.id.eventPosterPreview);
        changePosterButton = dialogView.findViewById(R.id.changePosterButton);
        removePosterButton = dialogView.findViewById(R.id.removePosterButton);
        View posterSectionTitle = dialogView.findViewById(R.id.posterSectionTitle);
        View lotterySectionTitle = dialogView.findViewById(R.id.lotterySectionTitle);

        // Get initial upload buttons from XML
        View initialUploadSection = dialogView.findViewById(R.id.initialUploadSection);
        Button selectImageBtn = dialogView.findViewById(R.id.select_image_btn);
        Button uploadImageBtn = dialogView.findViewById(R.id.upload_image_btn);

        // Hide lottery and poster sections by default (this switches when in edit mode)
        if (lotteryInputLayout != null) lotteryInputLayout.setVisibility(View.GONE);
        if (lotteryStatusLayout != null) lotteryStatusLayout.setVisibility(View.GONE);
        if (eventPosterPreview != null) eventPosterPreview.setVisibility(View.GONE);
        if (changePosterButton != null) changePosterButton.setVisibility(View.GONE);
        if (removePosterButton != null) removePosterButton.setVisibility(View.GONE);
        if (posterSectionTitle != null) posterSectionTitle.setVisibility(View.GONE);
        if (lotterySectionTitle != null) lotterySectionTitle.setVisibility(View.GONE);
        if (initialUploadSection != null) initialUploadSection.setVisibility(View.GONE);

        // Show sections based on mode
        if (isEditMode) {
            // edit shows lottery and change remove poster
            if (lotteryInputLayout != null) lotteryInputLayout.setVisibility(View.VISIBLE);
            if (eventPosterPreview != null) eventPosterPreview.setVisibility(View.VISIBLE);
            if (changePosterButton != null) changePosterButton.setVisibility(View.VISIBLE);
            if (removePosterButton != null) removePosterButton.setVisibility(View.VISIBLE);
            if (posterSectionTitle != null) posterSectionTitle.setVisibility(View.VISIBLE);
            if (lotterySectionTitle != null) lotterySectionTitle.setVisibility(View.VISIBLE);
        } else {
            // Create mode shows only the initial upload and settings
            if (initialUploadSection != null) initialUploadSection.setVisibility(View.VISIBLE);
        }

        // Set click listeners for the TimePickerDialog
        startDate.setOnClickListener(v -> openDatePicker(startDate));
        endDate.setOnClickListener(v -> openDatePicker(endDate));

        runLotteryButton.setOnClickListener(v -> showLotteryConfirmationDialog());

        changePosterButton.setOnClickListener(v -> openImagePicker());

        removePosterButton.setOnClickListener(v -> removePoster());


        loadWaitlistCount();

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
        startDate.setText(isoFormat.format(event.getStartTime()));
        endDate.setText(isoFormat.format(event.getEndTime()));

        geolocationSwitch.setChecked(event.getGeolocation());

        // Display current poster if it exists
        loadCurrentPoster();

        /*********************************
         * 2. Build and show AlertDialog *
         *********************************/
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Event Parameters");
        builder.setView(dialogView);
        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());

        // Override setPositiveButton's listener so that it only
        // References: https://stackoverflow.com/questions/2620444/how-to-prevent-a-dialog-from-closing-when-a-button-is-clicked
        builder.setPositiveButton("Save", null);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(v -> {
                /**********************
                 * 3. Get title input *
                 **********************/
                String titleText = title.getText().toString();
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
                int maxSize = Integer.MAX_VALUE;
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
                Date start, end;

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
                boolean geolocation = geolocationSwitch.isChecked();

                /***********************************************************
                 * 9. Update event locally & run OrganizerPanel's listener *
                 ***********************************************************/
                event.setName(titleText);
                event.setDescription(descriptionText);
                event.setLocation(locationText);
                event.setWaitlistMax(maxSize);
                event.setStartTime(start);
                event.setEndTime(end);
                event.setGeolocation(geolocation);

                // Update poster if changed
                if (newPosterUri != null) {
                    uploadPosterToStorage(newPosterUri);
                }

                if (listener != null) {
                    listener.onEventUpdated(event);
                }

                dialog.dismiss();
            });
        });

        Log.d(TAG, "Dialog created successfully");
        return dialog;
    }

    /**
     * Loads the current waitlist count from Firestore
     */
    private void loadWaitlistCount() {
        if (event == null || event.getId() == null) return;

        db.collection("event-p4")
                .document(event.getId())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Map<String, Object> waitlist = (Map<String, Object>) doc.get("waitlist");
                        if (waitlist != null) {
                            List<Map<String, Object>> waitlistedUsers =
                                    (List<Map<String, Object>>) waitlist.get("waitlistedUsers");
                            currentWaitlistCount = waitlistedUsers != null ? waitlistedUsers.size() : 0;

                            waitlistInfoText.setText("Current waitlist: " + currentWaitlistCount + " entrants");

                            Boolean lotteryDrawn = doc.getBoolean("lotteryDrawn");
                            if (lotteryDrawn != null && lotteryDrawn) {
                                showLotteryCompletedStatus(doc);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load waitlist count", e));
    }

    private void showLotteryCompletedStatus(DocumentSnapshot doc) {
        List<String> selectedIds = (List<String>) doc.get("selectedIds");
        Long lotteryDrawnAt = doc.getLong("lotteryDrawnAt");

        // Makes sure some Ids were selected and the lottery worked properly
        if (selectedIds != null && lotteryDrawnAt != null) {
            int winnersCount = selectedIds.size();
            Date date = new Date(lotteryDrawnAt);
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

            lotteryResultText.setText(winnersCount + " entrants selected on " + dateFormat.format(date));
            lotteryStatusLayout.setVisibility(View.VISIBLE);
            runLotteryButton.setEnabled(false);
            runLotteryButton.setText("Lottery Already Run");
        }
    }

    /**
     * Shows confirmation dialog before running lottery to confirm that organizer input is correct
     */
    private void showLotteryConfirmationDialog() {
        String lotterySizeStr = lotterySizeInput.getText().toString().trim();

        // checks if nothing was entered
        if (lotterySizeStr.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter number of entrants to select", Toast.LENGTH_SHORT).show();
            return;
        }

        // checks if int was entered
        int lotterySize;
        try {
            lotterySize = Integer.parseInt(lotterySizeStr);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Please enter a valid number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Makes sure at least one lottery winner is selected
        if (lotterySize <= 0) {
            Toast.makeText(requireContext(), "Number must be greater than 0", Toast.LENGTH_SHORT).show();
            return;
        }

        // Checks that organizer didn't select a size bigger than the waitlist
        if (lotterySize > currentWaitlistCount) {
            Toast.makeText(requireContext(), "Cannot select " + lotterySize + " from " + currentWaitlistCount + " on waitlist", Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Run Lottery Drawing?")
                .setMessage("This will randomly select " + lotterySize + " entrants from " + currentWaitlistCount + " on the waitlist.\n\nSelected entrants will be notified and moved to the selected list.\n\nThis action cannot be undone. Continue?")
                .setPositiveButton("Run Lottery", (dialog, which) -> runLottery(lotterySize))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void runLottery(int numberOfWinners) {
        event.drawLotteryWinners(numberOfWinners);
        updateFirestoreAfterLottery(event.getSelectedIds());
    }

    private void updateFirestoreAfterLottery(List<String> winnerIds) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("selectedIds", FieldValue.arrayUnion(winnerIds.toArray()));
        updates.put("lotteryDrawn", true);
        updates.put("lotteryDrawnAt", System.currentTimeMillis());

        List<Map<String, Object>> newWaitlist = new ArrayList<>();
        for (User user : event.getWaitlist().getWaitlistedUsers()) {
            if (!winnerIds.contains(user.getId())) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", user.getId());
                newWaitlist.add(userData);
            }
        }
        updates.put("waitlist.waitlistedUsers", newWaitlist);

        db.collection("event-p4")
                .document(event.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Update each winner's user document
                    updateWinnerUserDocuments(winnerIds);

                    notifyWinners(winnerIds);
                    loadWaitlistCount();

                    if (listener != null) {
                        listener.onEventUpdated(event);
                    }

                    Log.d(TAG, "Lottery completed successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update after lottery", e);
                    Toast.makeText(requireContext(), "Failed to complete lottery: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Updates user documents to move winners from waitlistedEvents to registeredEvents
     * @param winnerIds List of user IDs who won the lottery
     */
    private void updateWinnerUserDocuments(List<String> winnerIds) {
        for (String userId : winnerIds) {
            Map<String, Object> userUpdates = new HashMap<>();

            // Add to registeredEvents with "Notified" status
            userUpdates.put("registeredEvents." + event.getId(), "Notified");

            // Remove from waitlistedEvents array
            userUpdates.put("waitlistedEvents", FieldValue.arrayRemove(event.getId()));

            // Also remove from waitlistedEventIds if it exists
            userUpdates.put("waitlistedEventIds", FieldValue.arrayRemove(event.getId()));

            db.collection("users-p4")
                    .document(userId)
                    .update(userUpdates)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Updated user document for winner: " + userId);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to update user document for: " + userId, e);
                    });
        }
    }


    private void notifyWinners(List<String> winnerIds) {
        if (winnerIds.isEmpty()) return;

        NotificationSystem notificationSystem = new NotificationSystem(requireContext());

        // notifies winners in batches so it does not notify too many users at once
        for (int i = 0; i < winnerIds.size(); i += 10) {
            int end = Math.min(i + 10, winnerIds.size());
            List<String> batch = winnerIds.subList(i, end);

            db.collection("users-p4")
                    .whereIn("userId", batch)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        for (DocumentSnapshot userDoc : querySnapshot.getDocuments()) {
                            User user = userDoc.toObject(User.class);
                            if (user != null) {
                                notificationSystem.notifyLotteryWinner(user, event.getName());
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                    });
        }
    }

    /**
     * Loads the current event poster from Firebase Storage
     */
    private void loadCurrentPoster() {
        if (event == null || event.getId() == null || eventPosterPreview == null) return;

        db.collection("event-p4")
                .document(event.getId())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.contains("image")) {
                        // gets the firebase url that contains the current poster
                        String imageUrl = doc.getString("image");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            loadImageFromUrl(imageUrl);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load poster", e);
                });
    }

    /**
     * Loads image from URL into ImageView
     * * Source: <a href="https://stackoverflow.com/questions/6407324/how-to-display-image-from-url-on-android">...</a>
     * * Source: <a href="https://developer.android.com/reference/android/os/NetworkOnMainThreadException">...</a>
     */
    private void loadImageFromUrl(String url) {
        if (url == null || url.isEmpty() || eventPosterPreview == null) {
            return;
        }

        // Needed because android does not allow network on main thread
        new Thread(() -> {
            try {
                java.net.URL imageUrl = new java.net.URL(url);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) imageUrl.openConnection();
                connection.setDoInput(true);
                connection.connect();
                java.io.InputStream input = connection.getInputStream();
                Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(input);
                input.close();

                if (bitmap != null && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (eventPosterPreview != null && isAdded()) {
                            eventPosterPreview.setImageBitmap(bitmap);
                        }
                    });
                }
            }
            // Makes sure program does not crash if url does not work
            catch (Exception e) {
                Log.e(TAG, "Error loading image from URL: " + e.getMessage(), e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (eventPosterPreview != null && isAdded()) {
                            eventPosterPreview.setImageResource(android.R.color.darker_gray);
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * Opens image picker to select a new poster
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    /**
     * Handles the result from image picker
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == android.app.Activity.RESULT_OK
                && data != null && data.getData() != null) {
            newPosterUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                        requireActivity().getContentResolver(), newPosterUri);

                eventPosterPreview.setImageBitmap(bitmap);

                Toast.makeText(requireContext(), "Poster updated (will save when you click Save)",
                        Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                Toast.makeText(requireContext(), "Failed to load image",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Removes the event poster (added just in case but could probably be removed)
     */
    private void removePoster() {
        if (!isAdded()) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Remove Poster?")
                .setMessage("Are you sure you want to remove the event poster?")
                .setPositiveButton("Remove", (dialog, which) -> {

                    db.collection("event-p4")
                            .document(event.getId())
                            .get()
                            .addOnSuccessListener(doc -> {
                                String storagePath = doc.getString("storagePath");

                                if (storagePath != null && !storagePath.isEmpty()) {
                                    storage.getReference(storagePath)
                                            .delete()
                                            .addOnSuccessListener(aVoid -> updateFirestoreAfterPosterRemoval())
                                            .addOnFailureListener(e -> updateFirestoreAfterPosterRemoval());
                                } else {
                                    updateFirestoreAfterPosterRemoval();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(requireContext(),
                                        "Failed to remove poster", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    /**
     * Updates Firestore after the poster is removed
     */
    private void updateFirestoreAfterPosterRemoval() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("image", "");
        updates.put("storagePath", "");

        db.collection("event-p4")
                .document(event.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (eventPosterPreview != null) {
                        eventPosterPreview.setImageResource(android.R.color.darker_gray);
                    }
                    Toast.makeText(requireContext(), "Poster removed", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),
                            "Failed to remove poster", Toast.LENGTH_SHORT).show();
                });
    }


    /**
     * Uploads poster to Firebase Storage and updates Firestore
     */
    private void uploadPosterToStorage(Uri imageUri) {
        if (event == null || event.getId() == null || !isAdded()) return;

        String storagePath = "images/" + java.util.UUID.randomUUID().toString();
        StorageReference storageRef = storage.getReference(storagePath);

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {

                            Map<String, Object> updates = new HashMap<>();
                            updates.put("image", downloadUri.toString());
                            updates.put("storagePath", storagePath);

                            db.collection("event-p4")
                                    .document(event.getId())
                                    .update(updates)
                                    .addOnSuccessListener(aVoid -> {
                                        if (isAdded() && getContext() != null) {
                                            Toast.makeText(getContext(),
                                                    "Poster updated successfully!",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                        Log.d(TAG, "Poster uploaded: " + downloadUri.toString());
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Failed to update Firestore", e);
                                        if (isAdded() && getContext() != null) {
                                            Toast.makeText(getContext(),
                                                    "Failed to save poster info",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        })
                )
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to upload to storage", e);
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(),
                                "Failed to upload poster: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
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

        DatePickerDialog dialog = new DatePickerDialog(getContext(), (view, year1, monthIndex, dayOfMonth) -> {
            targetText.setText(year1 + "-" + (monthIndex + 1) + "-" + dayOfMonth);
        }, year, month, day);
        dialog.show();
    }
}
