package com.example.eventlottery;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.eventlottery.events.Event;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;

/**
 * QR Scan UI Test (description to be completed)
 */
@RunWith(AndroidJUnit4.class)
public class QRScanUITest {
    private ArrayList<Event> createTestEvents() {
        ArrayList<Event> events = new ArrayList<>();

        Date startDate1 = new Date(System.currentTimeMillis());
        Date endDate1 = new Date(System.currentTimeMillis() + 7200000L);
        events.add(new Event(
                "93d9b36e-76e8-48d9-b506-e2dcade84e97",
                "Demon Slayer: Infinity Castle – The Final Battle Begins",
                "Enter the Infinity Castle — the ever-shifting fortress where Tanjiro Kamado and the Hashira face their greatest challenge yet.",
                "Edmonton Cineplex Westmount",
                "Anime Alberta",
                "https://storage.googleapis.com/cmput-301-stable-21008.firebasestorage.app/anime.webp", startDate1, endDate1));

        Date startDate2 = new Date(1767250800000L);
        Date endDate2 = new Date(1767337199000L);
        events.add(new Event(
                "5a43666c-5276-44bf-814a-0a5f99063286",
                "New Years Dance Party",
                "Dancing all day for New Years!",
                "Somewhere",
                "Canadian Dance Association",
                "https://storage.googleapis.com/cmput-301-stable-21008.firebasestorage.app/dance.jpg", startDate2, endDate2));

        return events;
    }

    @Test
    public void qrScanningTest() {
        ArrayList<Event> events = createTestEvents();
    }
}