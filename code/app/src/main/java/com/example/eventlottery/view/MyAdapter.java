package com.example.eventlottery.view;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventlottery.R;
import com.example.eventlottery.model.EventListData;
import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private final List<EventListData> eventListData;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(EventListData item, int position);
    }

    public MyAdapter(List<EventListData> data, OnItemClickListener listener) {
        this.eventListData = data;
        this.listener = listener;
    }

    public void setItems(List<EventListData> items) {
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
        EventListData item = eventListData.get(position);
        holder.eventName.setText(item.getEventName());
        holder.eventDescription.setText(item.getEventDescription());
        holder.eventDate.setText(item.getEventDate());
        holder.eventTime.setText(item.getEventTime());
        holder.eventLocation.setText(item.getEventLocation());
        holder.eventOrganizer.setText(item.getEventOrganizer());
        holder.imageView.setImageResource(item.getEventImage());

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
