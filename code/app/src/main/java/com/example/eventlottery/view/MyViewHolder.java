package com.example.eventlottery.view;
import android.media.Image;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventlottery.R;


public class MyViewHolder extends RecyclerView.ViewHolder{

    ImageView imageView;
    TextView eventName;
    TextView eventDescription;
    TextView eventDate;
    TextView eventTime;
    TextView eventLocation;
    TextView eventOrganizer;

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
