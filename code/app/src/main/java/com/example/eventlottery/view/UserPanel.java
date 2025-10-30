package com.example.eventlottery.view;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventlottery.R;

/**
 * USER STORY 01.02.03
 * Create the user panel where the user can accept, decline invitation, and see all there events
 * @author Jensen Lee
 * */
public class UserPanel extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set to the userPanel xml
        setContentView(R.layout.user_event_list);
    }
}
