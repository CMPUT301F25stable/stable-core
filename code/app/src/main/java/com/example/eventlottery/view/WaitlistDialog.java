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
/**
 * A dialog fragment that displays a list of users currently waitlisted for an event.
 * <p>
 * This dialog retrieves a list of {@link User} objects, extracts their names, and
 * displays them in a simple list format using an {@link ArrayAdapter}.
 * It provides an interface to view the waitlist.
 * The layout for this dialog is created in {@code dialog_waitlist.xml}.
 * </p>
 * @see DialogFragment
 * @see User
 */
public class WaitlistDialog extends DialogFragment {
    /** A list of users who are currently on the event waitlist. */
    private ArrayList<User> waitlistedUsers;


    /**
     * Creates a new instance of {@link WaitlistDialog} with the given list of waitlisted users.
     * <p>
     * This method uses the fragment argument pattern to pass data to the dialog.
     * </p>
     * @param users The list of {@link User} objects representing the waitlisted users.
     * @return A new {@link WaitlistDialog} instance containing the given user list.
     */
    public static WaitlistDialog newInstance(ArrayList<User> users) {
        WaitlistDialog dialog = new WaitlistDialog();
        Bundle args = new Bundle();
        args.putSerializable("waitlistedUsers", users);
        dialog.setArguments(args);
        return dialog;
    }

    /**
     * Called when the dialog is being created.
     * <p>
     * This method retrieves the waitlisted users from the arguments bundle, inflates the dialog’s layout,
     * and populates a {@link ListView} with each user’s name using an {@link ArrayAdapter}.
     * </p>
     * @param savedInstanceState The last saved instance state of the fragment, or {@code null} if this is a new instance.
     * @return A {@link Dialog} displaying the waitlisted users and a "Close" button.
     */
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