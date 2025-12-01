package com.example.eventlottery.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.eventlottery.R;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** References:
 * https://stackoverflow.com/questions/60708558/android-wrap-buttons-within-view
 * https://stackoverflow.com/questions/48407340/use-flexboxlayout-programmatically
 */

public class FilterActivity extends AppCompatActivity {

    // --- Filters ---
    private final Set<String> selectedTags = new HashSet<>();
    private final Set<Long> selectedDates = new HashSet<>();

    // --- UI Components ---
    private TextView selectedDatesText;
    private CalendarView calendarView;
    private FlexboxLayout filterContainer;

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_event_filter);

        calendarView = findViewById(R.id.calendarView);
        selectedDatesText = findViewById(R.id.selectedDatesText);
        filterContainer = findViewById(R.id.filterContainer);
        Button btnClearAll = findViewById(R.id.btnClearAll);
        Button btnGoHome = findViewById(R.id.btnGoHome);


        ArrayList<String> tags = getIntent().getStringArrayListExtra("allTags");
        if (tags == null) tags = new ArrayList<>();

        ArrayList<String> preSelected = getIntent().getStringArrayListExtra("preSelectedTags");
        if (preSelected != null) selectedTags.addAll(preSelected);

        ArrayList<Long> preSelectedDates = (ArrayList<Long>) getIntent().getSerializableExtra("preSelectedDates");
        if (preSelectedDates != null) selectedDates.addAll(preSelectedDates);


        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            long dayMs = normalizeToMidnightMillis(year, month, dayOfMonth);
            if (selectedDates.contains(dayMs)) selectedDates.remove(dayMs);
            else selectedDates.add(dayMs);
            updateSelectedDatesText();
        });

        btnClearAll.setOnClickListener(v -> clearAllFilters());
        btnGoHome.setOnClickListener(v -> saveAndFinish());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                saveAndFinish();
            }
        });

        ArrayList<String> finalTags = tags;

        filterContainer.post(() -> {
            final int numberOfColumns = 3;
            float density = getResources().getDisplayMetrics().density; // Screen density for converting dp to px
            int buttonSideMarginPx = Math.round(density * 6); // Horizontal margin for each button
            int containerHorizontalPadding = filterContainer.getPaddingLeft() + filterContainer.getPaddingRight();
            int totalButtonMargins = buttonSideMarginPx * 2 * numberOfColumns; // Total horizontal margin used by all the buttons in that row
            int totalContainerWidth = filterContainer.getWidth(); // Actual width of the container after it's been laid
            int buttonWidth = (totalContainerWidth - containerHorizontalPadding - totalButtonMargins) / numberOfColumns; // Width of each button

            for (String tag : finalTags) {
                MaterialButton button = new MaterialButton(this);
                button.setText(tag);
                button.setAllCaps(false);

                // Style the button
                button.setStrokeWidth(2);
                button.setCornerRadius(12);
                button.setMinWidth(0);
                button.setMinimumWidth(0);
                button.setMaxLines(1);

                // Apply selected and unselected styles
                styleTagButton(button, selectedTags.contains(tag));

                // Layout params for flexbox: fixed width, wrap height, margins
                FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(buttonWidth, FlexboxLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(
                        buttonSideMarginPx,
                        Math.round(density * 8),
                        buttonSideMarginPx,
                        Math.round(density * 8)
                );
                button.setLayoutParams(lp);

                button.setOnClickListener(v -> {
                    boolean nowSelected = !button.isSelected();
                    if (nowSelected) {
                        selectedTags.add(tag);
                    } else {
                        selectedTags.remove(tag);
                    }
                    styleTagButton(button, nowSelected);
                });

                filterContainer.addView(button);
            }
        });

        updateSelectedDatesText();
    }

    /**
     * Styles a tag button based on whether it is selected or not.
     * @param button
     * @param selected
     */

    private void styleTagButton(MaterialButton button, boolean selected) {
        button.setSelected(selected);
        int bg = ContextCompat.getColor(this, selected ? R.color.black : R.color.white);
        int fg = ContextCompat.getColor(this, selected ? R.color.white : R.color.black);

        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(bg));button.setTextColor(fg);
        button.setStrokeColor(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
    }

    /**
     * Clears all filters and resets the UI.
     */
    private void clearAllFilters() {
        selectedTags.clear();
        selectedDates.clear();
        updateSelectedDatesText();

        // Reset the button styles to unselected
        for (int i = 0; i < filterContainer.getChildCount(); i++) {
            if (filterContainer.getChildAt(i) instanceof MaterialButton) {
                MaterialButton btn = (MaterialButton) filterContainer.getChildAt(i);
                styleTagButton(btn, false);
            }
        }

        Calendar today = Calendar.getInstance();
        long todayMs = normalizeToMidnightMillis(
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH));
        calendarView.setDate(todayMs, false, true);
    }

    /**
     * Saves the selected tags and dates and finishes the activity.
     */
    private void saveAndFinish() {
        Intent result = new Intent();
        result.putStringArrayListExtra("selectedTags", new ArrayList<>(selectedTags));
        result.putExtra("selectedDates", new ArrayList<>(selectedDates));
        setResult(RESULT_OK, result);
        finish();
    }

    /**
     * Updates the text displaying the selected dates.
     */
    private void updateSelectedDatesText() {
        if (selectedDates.isEmpty()) {
            selectedDatesText.setText("No dates selected");
            return;
        }

        SimpleDateFormat fmt = new SimpleDateFormat("• MMM d, yyyy", Locale.getDefault());
        String text = "";

        for (Long dayMs : selectedDates) {
            text += fmt.format(dayMs) + "\n";
        }

        selectedDatesText.setText(text.trim());
    }

    /**
     * Converts a given year, month, and day into a timestamp representing
     * midnight (00:00:00.000) of that day. This "normalization" removes the
     * time component so dates can be compared accurately — for example,
     * ensuring that the same calendar day always maps to the same millisecond
     * value, regardless of the time it was selected.
     */

    private long normalizeToMidnightMillis(int year, int monthZeroBased, int day) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, monthZeroBased);
        c.set(Calendar.DAY_OF_MONTH, day);
        return c.getTimeInMillis();
    }
}
