package com.example.eventlottery.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.example.eventlottery.R;
import com.example.eventlottery.users.User;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * A dialog fragment that displays a list of users accepted for an event with the option
 * to export the list as a .csv file.
 * reference: <a href="https://stackoverflow.com/questions/61279201/how-to-export-csv-file-android-studio">...</a>
 */
public class FinalListDialog extends DialogFragment {
    private static final String TAG = "FinalListDialog";
    private ArrayList<User> finalListUsers;

    /**
     * New instance of FinalListDialog with the list of finalized users
     * @param users The arraylist of users.
     * @return a new instance of FinalListDialog with the list of finalized users
     */
    public static FinalListDialog newInstance(ArrayList<User> users) {
        FinalListDialog dialog = new FinalListDialog();
        Bundle args = new Bundle();
        args.putSerializable("finalizedUsers", users);
        dialog.setArguments(args);
        return dialog;
    }

    /**
     * Gets the finalizedUsers from the bundle and lists them with the option to export
     * the list as a .csv file.
     * @param savedInstanceState saved instance of the fragment
     * @return a dialog listing the finalized users with a close button and a export button
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Retrieve finalized list
        if (getArguments() != null) {
            finalListUsers = (ArrayList<User>) getArguments().getSerializable("finalizedUsers");
        }

        // Inflate dialog layout
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_final_list, null);
        ListView userWaitlist = dialogView.findViewById(R.id.userFinalList);

        // Get each user's name
        ArrayList<String> userNames = new ArrayList<>();
        for (User user : finalListUsers) {
            userNames.add(user.getName());
        }

        // Set adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, userNames);
        userWaitlist.setAdapter(adapter);

        // Export Button
        Button exportBtn = dialogView.findViewById(R.id.export_btn);
        exportBtn.setOnClickListener( v -> {
            if (finalListUsers.isEmpty()) {
                Toast.makeText(requireContext(), "Empty List", Toast.LENGTH_SHORT).show();
            } else {
                export(finalListUsers);
            }
        });

        // Build the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Finalized Entrants");
        builder.setView(dialogView);
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());

        return builder.create();
    }

    /**
     * Saves the StringBuilder from getData() then exports it to different apps
     * @param finalListUsers The arraylist of users.
     */
    public void export(ArrayList<User> finalListUsers) {
        StringBuilder data = getData(finalListUsers);

        try {
            Context context = requireContext();
            String filename = "FinalListOfUsers.csv";

            // Save
            FileOutputStream outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write((data.toString()).getBytes());
            outputStream.close();

            // Export
            File fileLocation = new File(context.getFilesDir(), filename);
            Uri uri = FileProvider.getUriForFile(context, "com.example.eventlottery.fileprovider", fileLocation);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Final List of Users");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(intent, "Send Data"));

        } catch (Exception e) {
            Log.e(TAG, "Failed making .CSV File", e);
        }
    }

    /**
     * Builds the CSV file in a StringBuilder composed from each users info as
     * firstName,lastName,email,phoneNumber
     * @param finalListUsers The arraylist of users.
     * @return data StringBuilder in CSV format
     */
    @NonNull
    private static StringBuilder getData(ArrayList<User> finalListUsers) {
        StringBuilder data = new StringBuilder();
        String[] splitName;

        // Assumes that users put their full name, extracting their first and last name
        // and ignoring a middle name if it exists.
        data.append("FirstName,LastName,Email,PhoneNumber");
        for (User user : finalListUsers) {
            String fullName = user.getName();
            splitName = fullName.split(" ");
            data.append("\n").append(splitName[0]).append(",").append(splitName[splitName.length - 1])
                    .append(",").append(user.getEmailAddress()).append(",");

            String phoneNum = user.getPhoneNumber();
            if (phoneNum == null || phoneNum.isEmpty()) {
                phoneNum = "0000000000";
            }
            data.append(phoneNum);
        }
        return data;
    }
}
