package com.example.myapplication.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.SavedListAdapter;
import com.example.myapplication.model.SavedList;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.List;

public class BookmarkFragment extends Fragment {

    private BottomSheetBehavior<View> bottomSheetBehavior;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookmark, container, false);
        setupBottomSheet(view);
        return view;
    }

    private void setupBottomSheet(View view) {
        View bottomSheet = view.findViewById(R.id.bookmarkBottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setPeekHeight(450);

        ImageView btnClose = view.findViewById(R.id.closeSavedButton);
        btnClose.setOnClickListener(v -> bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED));

        setupSavedList(view);
    }

    private void setupSavedList(View view) {
        RecyclerView rv = view.findViewById(R.id.savedListsRecyclerView);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        List<SavedList> savedLists = new ArrayList<>();
        savedLists.add(new SavedList(R.drawable.ic_bookmark, "Saved places", 0));
        savedLists.add(new SavedList(R.drawable.ic_heart, "Favorites", 0));
        savedLists.add(new SavedList(R.drawable.ic_flag, "Want to go", 0));
        savedLists.add(new SavedList(R.drawable.ic_heart, "Dating", 3));
        savedLists.add(new SavedList(R.drawable.ic_bookmark, "Hanging out", 6));

        rv.setAdapter(new SavedListAdapter(savedLists));
    }
}
