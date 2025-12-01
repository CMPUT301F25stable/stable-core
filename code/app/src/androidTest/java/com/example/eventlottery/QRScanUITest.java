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
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.Date;

/**
 * QR Scan UI Test (description to be completed)
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class QRScanUITest {
    /**
     * Creates an ArrayList of events.
     * @return The ArrayList of events.
     */
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
                    new Date(1735776000000L),
                    null));
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

    /**
     * This code simulates the original functionality of the QR scanner in the app.
     * @param context The context of the instrumentation's package.
     * @param resourceId The QR code resource to be decoded.
     * @return The decoded contents of the QR code.
     */
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

    /**
     * Creates an intent for launching EventJoinAndLeave with the provided event details.
     * @param appContext The app's context.
     * @param event The event which will be displayed in EventJoinAndLeave.
     * @return The created intent containing the event details.
     */
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
        intent.putExtra("geolocation", false);
        intent.putExtra("waitlistMax", event.getWaitlistMax());
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

    /**
     * Tests to make sure that an event from an ArrayList of events is displayed correctly in EventJoinAndLeave
     */
    @Test
    public void displayTest() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        ArrayList<Event> events = createTestEvents();
        Event testEvent = events.get(0);

        Context appContext = instrumentation.getTargetContext();
        Intent realIntent = createIntent(appContext, testEvent);
        ActivityScenario.launch(realIntent);

        String startDate = testEvent.getFormattedStartDate();
        String startTime = testEvent.getFormattedStartTime();
        String endDate = testEvent.getFormattedEndDate();
        String endTime = testEvent.getFormattedEndTime();
        String when = (endDate != null && endTime != null)
                ? String.format("📅 %s %s → %s %s", startDate, startTime, endDate, endTime)
                : String.format("📅 %s  🕒 %s", startDate, startTime);
        String eventDetails = when + "\n\n📍 Location: " + testEvent.getLocation() + "\n\n🎟️ Organizer: " + testEvent.getOrganizer();

        onView(withId(R.id.eventTitle)).check(matches(withText(testEvent.getName())));
        onView(withId(R.id.eventDescription)).check(matches(withText(testEvent.getDescription())));
        onView(withId(R.id.eventDetails)).check(matches(withText(eventDetails)));
    }

    /**
     * Tests decoding an event id from a QR code and ensuring that it displays correctly in EventJoinAndLeave
     * US 01.06.01
     */
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
        String eventDetails = when + "\n\n📍 Location: " + realEvent.getLocation() + "\n\n🎟️ Organizer: " + realEvent.getOrganizer();

        onView(withId(R.id.eventTitle)).check(matches(withText(realEvent.getName())));
        onView(withId(R.id.eventDescription)).check(matches(withText(realEvent.getDescription())));
        onView(withId(R.id.eventDetails)).check(matches(withText(eventDetails)));
    }

    /**
     * Tests decoding an event id from a QR code and ensuring that it displays correctly in EventJoinAndLeave
     * US 01.06.01
     */
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
        String eventDetails = when + "\n\n📍 Location: " + realEvent.getLocation() + "\n\n🎟️ Organizer: " + realEvent.getOrganizer();

        onView(withId(R.id.eventTitle)).check(matches(withText(realEvent.getName())));
        onView(withId(R.id.eventDescription)).check(matches(withText(realEvent.getDescription())));
        onView(withId(R.id.eventDetails)).check(matches(withText(eventDetails)));
    }
}