package com.example.eventlottery.view;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventlottery.R;
import com.example.eventlottery.events.Event;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private final List<Event> eventListData;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Event item, int position);
    }

    public MyAdapter(List<Event> data, OnItemClickListener listener) {
        this.eventListData = data;
        this.listener = listener;
    }

    public void setItems(List<Event> items) {
        eventListData.clear();
        if (items != null) eventListData.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.event_list_data_view, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Event item = eventListData.get(position);
        holder.eventName.setText(item.getName());
        holder.eventDescription.setText(item.getDescription());
        holder.eventDate.setText(item.getFormattedStartDate());
        holder.eventTime.setText(item.getFormattedStartTime());
        holder.eventLocation.setText(item.getLocation());
        holder.eventOrganizer.setText(item.getOrganizer());
        holder.imageView.setImageResource(item.getImage());

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onItemClick(eventListData.get(pos), pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventListData.size();
    }
}