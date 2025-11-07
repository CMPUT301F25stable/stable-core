package com.example.eventlottery.view;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventlottery.R;
import com.example.eventlottery.events.Event;

import java.util.ArrayList;
import java.util.List;


/**
 * A custom {@link RecyclerView.Adapter} used to display a scrollable list of {@link Event} items.
 * <p>
 * This adapter binds event data (such as name, description, date, and image) to views defined
 * in the {@code event_list_data_view.xml} layout. It supports click listeners and basic
 * animations when new items appear.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * MyAdapter adapter = new MyAdapter(eventList, (event, position) -> {
 *     // Handle click event
 * });
 * recyclerView.setAdapter(adapter);
 * }
 * </pre>
 * </p>
 *
 * @see MyViewHolder
 * @see Event
 * @see RecyclerView.Adapter
 */
public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

    /**
     * Listener interface for handling item click events in the RecyclerView.
     */
    public interface OnItemClickListener {
        /**
         * Called when an event item is clicked.
         *
         * @param item     The clicked {@link Event} object.
         * @param position The position of the clicked item in the adapter.
         */
        void onItemClick(Event item, int position);
    }

    private List<Event> items;
    private final OnItemClickListener listener;
    private int lastAnimatedPosition = -1;

    /**
     * Constructs a {@link MyAdapter} with the given data and item click listener.
     *
     * @param data     The initial list of {@link Event} items to display.
     * @param listener The click listener to handle user interactions with items.
     */
    public MyAdapter(List<Event> data, OnItemClickListener listener) {
        this.items = (data == null) ? new ArrayList<>() : new ArrayList<>(data);
        this.listener = listener;
    }

    /**
     * Replaces the current list of events with a new one and refreshes the adapter.
     *
     * @param newItems The new list of {@link Event} objects to display.
     */
    public void setItems(List<Event> newItems) {
        if (this.items == null) this.items = new ArrayList<>();
        this.items.clear();
        if (newItems != null) this.items.addAll(newItems);
        notifyDataSetChanged();
        lastAnimatedPosition = -1;
    }

    /**
     * Replaces the current list with a filtered version (e.g., after a search) and refreshes the adapter.
     *
     * @param filteredList The filtered list of {@link Event} objects.
     */
    public void setFilteredList(List<Event> filteredList) {
        if (filteredList == null) filteredList = new ArrayList<>();
        this.items = new ArrayList<>(filteredList);
        notifyDataSetChanged();
        lastAnimatedPosition = -1;
    }

    /**
     * Inflates the layout for each individual list item.
     *
     * @param parent   The parent {@link ViewGroup} into which the new view will be added.
     * @param viewType The view type of the new view (not used here, as there is only one type).
     * @return A new {@link MyViewHolder} holding the inflated view.
     */
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.event_list_data_view, parent, false)
        );
    }

    /**
     * Binds event data to the {@link MyViewHolder} and handles animations and click events.
     * @param holder   The {@link MyViewHolder} that should be updated with event data.
     * @param position The position of the item within the adapter’s dataset.
     */
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Event item = items.get(position);

        holder.eventName.setText(item.getName());
        holder.eventDescription.setText(item.getDescription());
        holder.eventDate.setText(item.getFormattedStartDate());
        holder.eventTime.setText(item.getFormattedStartTime());
        holder.eventLocation.setText(item.getLocation());
        holder.eventOrganizer.setText(item.getOrganizer());

        Glide.with(holder.imageView)
                .load(item.getImage())
                .placeholder(R.drawable.placeholder)
                .into(holder.imageView);

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onItemClick(items.get(pos), pos);
            }
        });

        if (position > lastAnimatedPosition) {
            holder.itemView.clearAnimation();
            holder.itemView.startAnimation(
                    AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.anim_one)
            );
            lastAnimatedPosition = position;
        }
    }

    /**
     * Returns the total number of items managed by the adapter.
     * @return The number of {@link Event} items in the list.
     */
    @Override
    public int getItemCount() {
        return (items == null) ? 0 : items.size();
    }
}
