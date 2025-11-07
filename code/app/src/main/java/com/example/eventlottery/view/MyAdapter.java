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

public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Event item, int position);
    }

    private List<Event> items;
    private final OnItemClickListener listener;
    private int lastAnimatedPosition = -1;

    public MyAdapter(List<Event> data, OnItemClickListener listener) {
        this.items = (data == null) ? new ArrayList<>() : new ArrayList<>(data);
        this.listener = listener;
    }

    public void setItems(List<Event> newItems) {
        if (this.items == null) this.items = new ArrayList<>();
        this.items.clear();
        if (newItems != null) this.items.addAll(newItems);
        notifyDataSetChanged();
        lastAnimatedPosition = -1;
    }

    public void setFilteredList(List<Event> filteredList) {
        if (filteredList == null) filteredList = new ArrayList<>();
        this.items = new ArrayList<>(filteredList);
        notifyDataSetChanged();
        lastAnimatedPosition = -1;
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

    @Override
    public int getItemCount() {
        return (items == null) ? 0 : items.size();
    }
}
