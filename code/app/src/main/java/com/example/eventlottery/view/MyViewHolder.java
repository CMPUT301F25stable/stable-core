package com.example.eventlottery.view;
import android.media.Image;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventlottery.R;



/**
 * A {@link RecyclerView.ViewHolder} that holds references to the views used to display
 * an {@link com.example.eventlottery.events.Event} item in the RecyclerView.
 * <p>
 * This class provides access to individual UI elements such as the event name, description,
 * date, time, location, and organizer, as well as an image representing the event.
 * It is used by {@link MyAdapter} to bind data to views efficiently.
 * </p>
 * <p>
 * The layout associated with this ViewHolder is typically defined in
 * {@code event_list_data_view.xml}.
 * </p>
 *
 * @see MyAdapter
 * @see androidx.recyclerview.widget.RecyclerView.ViewHolder
 */
public class MyViewHolder extends RecyclerView.ViewHolder{
    /** Displays the event's image or poster. */
    ImageView imageView;
    /** Displays the event's name. */
    TextView eventName;
    /** Displays the event's description. */
    TextView eventDescription;
    /** Displays the event's date. */
    TextView eventDate;
    /** Displays the event's time. */
    TextView eventTime;
    /** Displays the event's location. */
    TextView eventLocation;
    /** Displays the event's organizer. */
    TextView eventOrganizer;

    /**
     * Constructs a new {@link MyViewHolder} and initializes its view components.
     *
     * @param itemView The inflated layout view for a single event item.
     */
    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.imageView);
        eventName = itemView.findViewById(R.id.eventName);
        eventDescription = itemView.findViewById(R.id.eventDescription);
        eventDate = itemView.findViewById(R.id.eventDate);
        eventTime = itemView.findViewById(R.id.eventTime);
        eventLocation = itemView.findViewById(R.id.eventLocation);
        eventOrganizer = itemView.findViewById(R.id.eventOrganizer);

    }
}
