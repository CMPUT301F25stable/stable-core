package com.example.eventlottery.view;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.eventlottery.R;
import com.example.eventlottery.users.User;

import java.util.ArrayList;

// References: https://stackoverflow.com/questions/36113134/understanding-the-fragment-newinstance-method?utm_source=chatgpt.com
public class WaitlistDialog extends DialogFragment {

    private ArrayList<User> waitlistedUsers;

    /**
     * Essentially a constructor for waitlist. You put the ArrayList of users here & its retrieved later
     * once the dialog is actually created
     * @param users The arraylist of users.
     * @return
     */
    public static WaitlistDialog newInstance(ArrayList<User> users) {
        WaitlistDialog dialog = new WaitlistDialog();
        Bundle args = new Bundle();
        args.putSerializable("waitlistedUsers", users);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Retrieve waitlist
        if (getArguments() != null) {
            waitlistedUsers = (ArrayList<User>) getArguments().getSerializable("waitlistedUsers");
        }

        // Inflate dialog layout
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_waitlist, null);
        ListView userWaitlist = dialogView.findViewById(R.id.userWaitlist);

        // Get each user's name
        ArrayList<String> userNames = new ArrayList<>();
        for (User user : waitlistedUsers) {
            userNames.add(user.getName());
        }

        // Set adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, userNames);
        userWaitlist.setAdapter(adapter);

        // Build the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Waitlisted Users");
        builder.setView(dialogView);
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());

        return builder.create();
    }
}