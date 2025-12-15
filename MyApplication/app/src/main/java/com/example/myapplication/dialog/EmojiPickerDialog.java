package com.example.myapplication.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.EmojiAdapter;
import com.example.myapplication.model.EmojiCategory;

import java.util.ArrayList;
import java.util.List;

public class EmojiPickerDialog extends DialogFragment {

    private EmojiAdapter emojiAdapter;
    private List<EmojiCategory> categories;
    private OnEmojiSelectedListener listener;
    private TextView categoryTitle;

    public interface OnEmojiSelectedListener {
        void onEmojiSelected(String emoji);
    }

    public static EmojiPickerDialog newInstance(String currentEmoji) {
        return new EmojiPickerDialog();
    }

    public void setOnEmojiSelectedListener(OnEmojiSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_emoji_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        categories = EmojiCategory.getAllCategories();

        // Setup category title
        categoryTitle = view.findViewById(R.id.categoryTitle);

        // Setup RecyclerView with 7 columns
        RecyclerView recyclerView = view.findViewById(R.id.emojiRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 7));

        // Get all emojis from first category as default
        List<String> allEmojis = new ArrayList<>();
        if (!categories.isEmpty()) {
            allEmojis.addAll(categories.get(0).getEmojis());
            categoryTitle.setText(getCategoryDisplayName(categories.get(0).getName()));
        }

        emojiAdapter = new EmojiAdapter(allEmojis);
        emojiAdapter.setOnEmojiClickListener(emoji -> {
            if (listener != null) {
                listener.onEmojiSelected(emoji);
            }
            dismiss();
        });
        recyclerView.setAdapter(emojiAdapter);

        // Close button
        ImageView btnClose = view.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> dismiss());

        // Setup category tabs
        setupCategories(view);
    }

    private View selectedCategoryTab = null;

    private void setupCategories(View view) {
        LinearLayout categoryContainer = view.findViewById(R.id.categoryContainer);

        for (int i = 0; i < categories.size(); i++) {
            EmojiCategory category = categories.get(i);

            // Create a vertical layout for icon + indicator
            LinearLayout tabLayout = new LinearLayout(requireContext());
            tabLayout.setOrientation(LinearLayout.VERTICAL);
            tabLayout.setGravity(android.view.Gravity.CENTER);
            tabLayout.setPadding(16, 8, 16, 8);
            tabLayout.setClickable(true);
            tabLayout.setFocusable(true);

            // Create emoji icon
            TextView iconView = new TextView(requireContext());
            iconView.setText(category.getIcon());
            iconView.setTextSize(32);
            iconView.setTextColor(0xFFFFFFFF); // White color for emoji visibility
            iconView.setGravity(android.view.Gravity.CENTER);

            // Create highlight indicator (underline)
            View indicator = new View(requireContext());
            LinearLayout.LayoutParams indicatorParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 4);
            indicatorParams.topMargin = 4;
            indicator.setLayoutParams(indicatorParams);
            indicator.setBackgroundColor(0x00000000); // Transparent by default

            tabLayout.addView(iconView);
            tabLayout.addView(indicator);

            int finalI = i;
            tabLayout.setOnClickListener(v -> {
                // Update selected category highlight
                if (selectedCategoryTab != null) {
                    View prevIndicator = ((LinearLayout) selectedCategoryTab).getChildAt(1);
                    prevIndicator.setBackgroundColor(0x00000000); // Transparent
                }
                indicator.setBackgroundColor(0xFF00BCD4); // Cyan highlight
                selectedCategoryTab = tabLayout;

                // Update emoji list and title
                emojiAdapter.updateEmojis(category.getEmojis());
                categoryTitle.setText(getCategoryDisplayName(category.getName()));
            });

            // Highlight first category by default
            if (i == 0) {
                indicator.setBackgroundColor(0xFF00BCD4); // Cyan
                selectedCategoryTab = tabLayout;
            }

            categoryContainer.addView(tabLayout);
        }
    }

    private String getCategoryDisplayName(String categoryName) {
        switch (categoryName) {
            case "SMILEYS": return "SMILEYS AND EMOTIONS";
            case "HEARTS": return "HEARTS AND SYMBOLS";
            case "TRAVEL": return "TRAVEL AND PLACES";
            case "FOOD": return "FOOD AND DRINK";
            case "ACTIVITIES": return "ACTIVITIES AND SPORTS";
            case "OBJECTS": return "OBJECTS";
            case "ANIMALS": return "ANIMALS AND NATURE";
            case "SYMBOLS": return "SYMBOLS";
            default: return categoryName;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return dialog;
    }
}
