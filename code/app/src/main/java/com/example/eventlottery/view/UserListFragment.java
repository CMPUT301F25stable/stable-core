package com.example.eventlottery.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventlottery.R;

public class UserListFragment extends Fragment {
    /** The userlist view */
    private ListView userListView;
    /** The adapter. Stored for later (in case fragment method is called before it's expanded) */
    private UserAdapter adapter;
    /** The on click listener */
    private AdapterView.OnItemClickListener clickListener;

    /**
     * Sets the user adapter for the fragment.
     * @param adapter The adapter to set.
     */
    public void setAdapter(UserAdapter adapter) {
        this.adapter = adapter;

        // Only set it if the ListView exists already
        if (userListView != null) {
            userListView.setAdapter(adapter);
        }
    }

    /**
     * Sets the click listener for the fragment.
     * @param listener The listener to set.
     */
    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        this.clickListener = listener;

        // If fragment is already expanded, apply listener
        if (userListView != null) {
            userListView.setOnItemClickListener(listener);
        }
    }

    /**
     * Inflates the userlist fragment & sets the adapter if the adapter is not null.
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return the userlist fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // inflate fragment layout
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);

        userListView = view.findViewById(R.id.userList);

        // Apply adapter if it was set before fragment was actually created
        if (adapter != null) {
            userListView.setAdapter(adapter);
        }

        // Apply clickListener if it was before fragment was actually created
        if (clickListener != null) {
            userListView.setOnItemClickListener(clickListener);
        }
        return view;
    }
}
