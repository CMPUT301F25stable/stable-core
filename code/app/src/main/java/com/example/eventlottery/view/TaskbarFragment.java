package com.example.eventlottery.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventlottery.R;

public class TaskbarFragment extends Fragment {
    /**
     * Inflates the home taskbar by default. TODO: Make it dynamically display taskbars later
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_taskbar, container, false);
    }

    /**
     * Right now, it just adds an event listener to the 'person' icon to start OrganizerPanel.
     * TODO: Add event listeners to all icons, and have the 'person' icon go to the user profile.
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /**
         * Sets event listener for starting OrganizerPanel
         */
        View organizerIcon = view.findViewById(R.id.OrganizerIcon);
        organizerIcon.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), OrganizerPanel.class);
            // If exists in stack, retrieve it instead of making a new one
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });

        /**
         * Sets event listener for starting user profile
         */
        View personIcon = view.findViewById(R.id.personIcon);
        personIcon.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), UserPanel.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });

        /**
         * Sets event listener for home page
         */
        View homeIcon = view.findViewById(R.id.homeIcon);
        homeIcon.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });
    }
}
