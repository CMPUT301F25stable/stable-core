package com.example.eventlottery.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.eventlottery.R;
import com.example.eventlottery.events.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * References: https://medium.com/mindorks/custom-array-adapters-made-easy-b6c4930560dd
 */

/**
 * A custom {@link ArrayAdapter} for displaying a list of {@link Event} objects in a ListView or Spinner.
 * This adapter is designed to work with the {@link Event} class and its associated layout file
 * {@code event_list_item.xml}.
 * @see Event
 * @see android.widget.ArrayAdapter
 */
public class EventAdapter extends ArrayAdapter<Event> {
    private List<Event> eventList = new ArrayList<>();
    private Context context;


    /**
     * Constructs a new {@link EventAdapter}
     *
     * @param context The current context, typically the Activity or Fragment where this adapter is used.
     * @param events  The list of {@link Event} objects to display.
     */
    public EventAdapter(Context context, ArrayList<Event> events) {
        super(context, 0, events);
        this.context = context;
        this.eventList = events;
    }

    /**
     * Sets event_list_item.xml's text fields
     * @param position The position of the item within the adapter's data set of the item whose view
     *        we want.
     * @param convertView The old view to reuse, if possible.
     * @param parent The parent that this view will eventually be attached to
     * @return The fully populated {@link View} corresponding to the given position.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.event_list_item, parent, false);
        }

        Event event = eventList.get(position);
        TextView eventNameText = convertView.findViewById(R.id.eventNameText);
        TextView eventStartEnd = convertView.findViewById(R.id.eventStartEnd);

        eventNameText.setText(event.getName());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CANADA);
        eventStartEnd.setText(sdf.format(event.getStartTime()) + " - " + sdf.format(event.getEndTime()));

        return convertView;
    }
}
