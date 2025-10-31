package com.example.eventlottery.view;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventlottery.R;
import com.example.eventlottery.model.EventListData;
import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private  List<EventListData> eventListData;
    private  OnItemClickListener listener;

    private int lastAnimatedPosition = -1;

    public void setFilteredList(List<EventListData> filteredList) {
        this.eventListData = filteredList;
        notifyDataSetChanged();
        lastAnimatedPosition = -1;
    }

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

        if (position > lastAnimatedPosition) {
            holder.itemView.clearAnimation();
            holder.itemView.startAnimation(
                    AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.anim_one)
            );
            lastAnimatedPosition = position;
        }

    }

    @Override
    public int getItemCount() {
        return eventListData.size();
    }


}
