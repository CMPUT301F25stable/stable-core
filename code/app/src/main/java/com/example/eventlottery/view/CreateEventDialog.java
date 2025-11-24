package com.example.eventlottery.view;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
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
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.eventlottery.R;
import com.example.eventlottery.events.Event;
import com.google.firebase.Firebase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageKt;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

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
    private static final String TAG = "CreateEventDialog";
    private OnEventCreatedListener listener;
    private String organizerName;
    private ActivityResultLauncher<Intent> imageResultLauncher;
    private Uri filePath;
    private StorageReference storageReference;
    private ProgressBar progressBar;
    private String eventImg;
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
     * Sets the organizer name for this dialog.
     * @param organizerName The organizer's name.
     */
    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
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
        EditText title = dialogView.findViewById(R.id.titleInput);
        EditText description = dialogView.findViewById(R.id.descriptionInput);
        EditText location = dialogView.findViewById(R.id.locationInput);
        EditText waitlistMax = dialogView.findViewById(R.id.waitlistMaxInput);
        EditText startDate = dialogView.findViewById(R.id.startDateInput);
        EditText endDate = dialogView.findViewById(R.id.endDateInput);
        Switch geolocationSwitch = dialogView.findViewById(R.id.geolocationSwitch);
        Button selectImgBtn = dialogView.findViewById(R.id.select_image_btn);
        Button uploadImgBtn = dialogView.findViewById(R.id.upload_image_btn);
        ImageView imgPreview = dialogView.findViewById(R.id.preview_select_img_view);
        progressBar = dialogView.findViewById(R.id.upload_img_progress);

        // Set click listeners for the TimePickerDialog
        startDate.setOnClickListener(v -> openDatePicker(startDate));
        endDate.setOnClickListener(v -> openDatePicker(endDate));

        // Firebase Storage
        FirebaseStorage storage = StorageKt.getStorage(Firebase.INSTANCE);
        storageReference = storage.getReference();

        // Image selection and upload
        pickImageLauncher(imgPreview);
        selectImgBtn.setOnClickListener(v -> pickImage());
        uploadImgBtn.setOnClickListener(v -> uploadImage());

        // Build & show AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("New Event Parameters");
        builder.setView(dialogView);
        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton("Save", (dialog, which) -> {

            /**********************
             * 1. Get title input *
             **********************/
            String titleText  = title.getText().toString();
            if (titleText.isEmpty()) {
                Toast.makeText(requireContext(), "Title can't be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            /****************************
             * 2. Get description input *
             ****************************/
            String descriptionText = description.getText().toString();
            if (descriptionText.isEmpty()) {
                Toast.makeText(requireContext(), "Description can't be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            /*************************
             * 3. Get location input *
             *************************/
            String locationText = location.getText().toString();
            if (locationText.isEmpty()) {
                Toast.makeText(requireContext(), "Location can't be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            /*****************************
             * 4. Get waitlist max input *
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
             * 5. Get valid date input *
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
             * 6. Get geolocation input *
             ****************************/
            boolean geolocation;
            if (geolocationSwitch.isChecked()) {
                geolocation = true;
            } else { geolocation = false; }

            /****************************
             * 7. Select and upload image
             ****************************/
            if (eventImg == null || eventImg.isEmpty()) {
                Toast.makeText(requireContext(), "Please select and upload an image", Toast.LENGTH_SHORT).show();
                return;
            }

            /*************************************
             * 8. Create event, given the inputs *
             *************************************/
            Event newEvent = new Event(titleText, descriptionText, locationText, organizerName, eventImg, start, end, new ArrayList<>(), geolocation);
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
            public void onDateSet(DatePicker view, int year, int monthIndex, int dayOfMonth) {
                targetText.setText(String.valueOf(year) + "-" + String.valueOf(monthIndex + 1) + "-" + String.valueOf(dayOfMonth));
            }
        }, year, month, day);
        dialog.show();
    }

    /**
     * Initialize image picker with activity result contract
     * @param imageView a ImageView to show the image the user selected
     */
    private void pickImageLauncher(ImageView imageView) {
        imageResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                activityResult -> {
                    try {
                        if (activityResult.getData() != null) {
                            Uri imageUri = activityResult.getData().getData();
                            filePath = imageUri;
                            imageView.setImageURI(imageUri);
                        }
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Failed to load Image", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Failed to load image", e);
                    }
                }
        );
    }

    /**
     * Allows the user to pick an image on their devices storage
     * Note: Select images that are more landscape
     */
    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imageResultLauncher.launch(intent);
    }

    /**
     * reference: https://www.geeksforgeeks.org/android/android-how-to-upload-an-image-on-firebase-storage/
     * Uploads the selected image from pickImage() to Firestore Storage and saves the image
     * to the new created event document.
     */
    private void uploadImage() {
        if (filePath != null) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);

            StorageReference reference = storageReference.child("images/" + UUID.randomUUID().toString());
            reference.putFile(filePath)
                    .addOnSuccessListener(task -> {
                        reference.getDownloadUrl().addOnSuccessListener(uri -> {
                            eventImg = uri.toString();
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(requireContext(), "Image Uploaded", Toast.LENGTH_SHORT).show();
                        })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(requireContext(), "Uploaded Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Uploaded Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnProgressListener(snapshot -> {
                        int progress = (int) (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        progressBar.setProgress(progress);
                    });
        } else {
            Toast.makeText(requireContext(), "No Image Selected", Toast.LENGTH_SHORT).show();
        }
    }

}
