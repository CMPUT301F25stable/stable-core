package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.eventlottery.events.Event;
import com.example.eventlottery.view.EventJoinAndLeave;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;

/**
 * QR Scan UI Test (description to be completed)
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class QRScanUITest {
    private ArrayList<Event> createTestEvents() {
        ArrayList<Event> events = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            events.add(new Event(
                    "Filler Event",
                    "Filler Description",
                    "Filler Location",
                    "Filler Organizer",
                    "https://storage.googleapis.com/cmput-301-stable-21008.firebasestorage.app/hockey.webp",
                    new Date(1735758000000L),
                    new Date(1735776000000L)));
        }

        Date startDate1 = new Date(System.currentTimeMillis());
        Date endDate1 = new Date(System.currentTimeMillis() + 7200000L);
        events.add(4, new Event(
                "93d9b36e-76e8-48d9-b506-e2dcade84e97",
                "Demon Slayer: Infinity Castle – The Final Battle Begins",
                "Enter the Infinity Castle — the ever-shifting fortress where Tanjiro Kamado and the Hashira face their greatest challenge yet.",
                "Edmonton Cineplex Westmount",
                "Anime Alberta",
                "https://storage.googleapis.com/cmput-301-stable-21008.firebasestorage.app/anime.webp", startDate1, endDate1));

        Date startDate2 = new Date(1767250800000L);
        Date endDate2 = new Date(1767337199000L);
        events.add(8, new Event(
                "5a43666c-5276-44bf-814a-0a5f99063286",
                "New Years Dance Party",
                "Dancing all day for New Years!",
                "Somewhere",
                "Canadian Dance Association",
                "https://storage.googleapis.com/cmput-301-stable-21008.firebasestorage.app/dance.jpg", startDate2, endDate2));

        return events;
    }

    private String decodeQRCode(Context context, int resourceId) {
        Bitmap qrCode = BitmapFactory.decodeResource(context.getResources(), resourceId);
        int[] pixels = new int[qrCode.getWidth() * qrCode.getHeight()];
        qrCode.getPixels(pixels, 0, qrCode.getWidth(), 0, 0, qrCode.getWidth(), qrCode.getHeight());

        RGBLuminanceSource luminanceSource = new RGBLuminanceSource(qrCode.getWidth(), qrCode.getHeight(), pixels);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(luminanceSource));

        try {
            Result result = new MultiFormatReader().decode(binaryBitmap);
            return result.getText();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Intent createIntent(Context appContext, Event event) {
        Intent intent = new Intent(appContext, EventJoinAndLeave.class);
        intent.putExtra("id", event.getId());
        intent.putExtra("name", event.getName());
        intent.putExtra("description", event.getDescription());
        intent.putExtra("dateStart", event.getFormattedStartDate());
        intent.putExtra("timeStart", event.getFormattedStartTime());
        intent.putExtra("dateEnd", event.getFormattedEndDate());
        intent.putExtra("timeEnd", event.getFormattedEndTime());
        intent.putExtra("location", event.getLocation());
        intent.putExtra("organizer", event.getOrganizer());
        intent.putExtra("image", event.getImage());
        return intent;
    }

    @Before
    public void initialize() {
        Intents.init();
    }

    @After
    public void cleanup() {
        Intents.release();
    }

    @Test
    public void firstQRTest() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Context context = instrumentation.getContext();
        ArrayList<Event> events = createTestEvents();

        String realQRContent = decodeQRCode(context, com.example.eventlottery.test.R.drawable.testqrcode1);
        int realQRIndex = Event.findEventById(events, realQRContent);
        assertTrue(realQRIndex > -1);

        String fakeQRContent = decodeQRCode(context, com.example.eventlottery.test.R.drawable.invalidqrcode1);
        int fakeQRIndex = Event.findEventById(events, fakeQRContent);
        assertEquals(-1, fakeQRIndex);

        Context appContext = instrumentation.getTargetContext();
        Event realEvent = events.get(realQRIndex);
        Intent realIntent = createIntent(appContext, realEvent);
        ActivityScenario.launch(realIntent);

        String startDate = realEvent.getFormattedStartDate();
        String startTime = realEvent.getFormattedStartTime();
        String endDate = realEvent.getFormattedEndDate();
        String endTime = realEvent.getFormattedEndTime();
        String when = (endDate != null && endTime != null)
                ? String.format("📅 %s %s → %s %s", startDate, startTime, endDate, endTime)
                : String.format("📅 %s  🕒 %s", startDate, startTime);
        String eventDetails = when + "\n📍 Location: " + realEvent.getLocation() + "\n🎟️ Organizer: " + realEvent.getOrganizer();

        onView(withId(R.id.eventTitle)).check(matches(withText(realEvent.getName())));
        onView(withId(R.id.eventDescription)).check(matches(withText(realEvent.getDescription())));
        onView(withId(R.id.eventDetails)).check(matches(withText(eventDetails)));
    }

    @Test
    public void secondQRTest() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Context context = instrumentation.getContext();
        ArrayList<Event> events = createTestEvents();

        String realQRContent = decodeQRCode(context, com.example.eventlottery.test.R.drawable.testqrcode2);
        int realQRIndex = Event.findEventById(events, realQRContent);
        assertTrue(realQRIndex > -1);

        String fakeQRContent = decodeQRCode(context, com.example.eventlottery.test.R.drawable.invalidqrcode2);
        int fakeQRIndex = Event.findEventById(events, fakeQRContent);
        assertEquals(-1, fakeQRIndex);

        Context appContext = instrumentation.getTargetContext();
        Event realEvent = events.get(realQRIndex);
        Intent realIntent = createIntent(appContext, realEvent);
        ActivityScenario.launch(realIntent);

        String startDate = realEvent.getFormattedStartDate();
        String startTime = realEvent.getFormattedStartTime();
        String endDate = realEvent.getFormattedEndDate();
        String endTime = realEvent.getFormattedEndTime();
        String when = (endDate != null && endTime != null)
                ? String.format("📅 %s %s → %s %s", startDate, startTime, endDate, endTime)
                : String.format("📅 %s  🕒 %s", startDate, startTime);
        String eventDetails = when + "\n📍 Location: " + realEvent.getLocation() + "\n🎟️ Organizer: " + realEvent.getOrganizer();

        onView(withId(R.id.eventTitle)).check(matches(withText(realEvent.getName())));
        onView(withId(R.id.eventDescription)).check(matches(withText(realEvent.getDescription())));
        onView(withId(R.id.eventDetails)).check(matches(withText(eventDetails)));
    }
}