package com.example.eventlottery.view;

import android.content.res.ColorStateList;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.eventlottery.R;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.button.MaterialButton;
import java.util.Arrays;
import java.util.List;

public class FilterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_event_filter);

        FlexboxLayout filterContainer = findViewById(R.id.filterContainer);

        List<String> tags = Arrays.asList("Anime", "Movie", "Music", "Sports", "Workshop", "Gaming", "2", "3", "4", "5", "6");

        filterContainer.post(() -> {

            final int numberOfColumns = 3;
            int buttonSideMargin = dp(6);
            int containerHorizontalPadding = filterContainer.getPaddingLeft() + filterContainer.getPaddingRight();
            int totalButtonMargins = buttonSideMargin * 2 * numberOfColumns;
            int totalContainerWidth = filterContainer.getWidth();
            int buttonWidth = (totalContainerWidth - containerHorizontalPadding - totalButtonMargins) / numberOfColumns;

            for (String tag : tags) {
                MaterialButton button = new MaterialButton(this);
                button.setText(tag);
                button.setAllCaps(false);

                button.setTextColor(ContextCompat.getColor(this, R.color.black ));

                button.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(this, R.color.white)
                ));
                button.setStrokeColor(ColorStateList.valueOf(
                        ContextCompat.getColor(this, R.color.black)
                ));
                button.setStrokeWidth(2);
                button.setCornerRadius(12);


                button.setMinWidth(0);
                button.setMinimumWidth(0);
                button.setMaxLines(1);

                FlexboxLayout.LayoutParams layoutParams = new FlexboxLayout.LayoutParams(
                        buttonWidth,
                        FlexboxLayout.LayoutParams.WRAP_CONTENT
                );

                layoutParams.setMargins(
                        buttonSideMargin,
                        dp(8),
                        buttonSideMargin,
                        dp(8)
                );

                button.setLayoutParams(layoutParams);

                button.setOnClickListener(v -> {
                    boolean sel = !button.isSelected();
                    button.setSelected(sel);
                    if (sel) {
                        button.setBackgroundTintList(ColorStateList.valueOf(
                                ContextCompat.getColor(this, R.color.Grey)
                        ));
                    } else {
                        button.setBackgroundTintList(ColorStateList.valueOf(
                                ContextCompat.getColor(this, R.color.white)
                        ));
                    }
                });

                filterContainer.addView(button);
            }
        });
    }

    private int dp(int v) {
        return Math.round(getResources().getDisplayMetrics().density * v);
    }
}
