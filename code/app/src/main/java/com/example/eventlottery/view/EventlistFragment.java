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

public class EventlistFragment extends Fragment {
    /** The eventlist view */
    private ListView eventListView;
    /** The adapter. Stored for later (in case fragment method is called before it's expanded) */
    private EventAdapter adapter;
    /** The on click listener */
    private AdapterView.OnItemClickListener clickListener;

    /**
     * Sets the event adapter for the fragment.
     * @param adapter The adapter to set.
     */
    public void setAdapter(EventAdapter adapter) {
        this.adapter = adapter;

        // Only set it if the ListView exists already
        if (eventListView != null) {
            eventListView.setAdapter(adapter);
        }
    }

    /**
     * Sets the click listener for the fragment.
     * @param listener The listener to set.
     */
    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        this.clickListener = listener;

        // If fragment is already expanded, apply listener
        if (eventListView != null) {
            eventListView.setOnItemClickListener(listener);
        }
    }

    /**
     * Inflates the eventlist fragment & sets the adapter if the adapter is not null.
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return the eventlist fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // inflate fragment layout
        View view = inflater.inflate(R.layout.fragment_eventlist, container, false);

        eventListView = view.findViewById(R.id.eventList);

        // Apply adapter if it was set before fragment was actually created
        if (adapter != null) {
            eventListView.setAdapter(adapter);
        }

        // Apply clickListener if it was before fragment was actually created
        if (clickListener != null) {
            eventListView.setOnItemClickListener(clickListener);
        }
        return view;
    }
}
