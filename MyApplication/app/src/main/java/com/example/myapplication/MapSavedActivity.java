package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.SavedListAdapter;
import com.example.myapplication.model.SavedList;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import java.util.ArrayList;
import java.util.List;

public class MapSavedActivity extends AppCompatActivity {

    private MapView mapView;
    private BottomSheetBehavior<LinearLayout> savedBottomSheetBehavior;
    private RecyclerView savedListsRecyclerView;
    private SavedListAdapter savedListAdapter;
    private List<SavedList> savedLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init Mapbox with Goong API key
        Mapbox.getInstance(this, getString(R.string.goong_map_key));

        setContentView(R.layout.activity_map_saved);
        mapView = findViewById(R.id.mapView);

        mapView.onCreate(savedInstanceState);

        // Load Goong map style
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                String apiKey = getString(R.string.goong_api_key);
                String styleUrl = "https://tiles.goong.io/assets/goong_map_web.json?api_key=" + apiKey;
                mapboxMap.setStyle(new Style.Builder().fromUri(styleUrl));
            }
        });

        // Setup Saved Bottom Sheet
        setupSavedBottomSheet();
    }

    private void setupSavedBottomSheet() {
        // Initialize bottom sheet
        LinearLayout bottomSheet = findViewById(R.id.bottomSheetSaved);
        savedBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        // Set peek height
        int peekHeight = (int) (200 * getResources().getDisplayMetrics().density);
        savedBottomSheetBehavior.setPeekHeight(peekHeight);
        savedBottomSheetBehavior.setHideable(false);
        savedBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        // Initialize RecyclerView
        savedListsRecyclerView = findViewById(R.id.savedListsRecyclerView);
        savedListsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize sample data
        savedLists = new ArrayList<>();
        savedLists.add(new SavedList(R.drawable.ic_bookmark, "Saved places", 0));
        savedLists.add(new SavedList(R.drawable.ic_heart, "Favorites", 0));
        savedLists.add(new SavedList(R.drawable.ic_flag, "Want to go", 0));
        savedLists.add(new SavedList(R.drawable.ic_heart, "Dating", 3));
        savedLists.add(new SavedList(R.drawable.ic_bookmark, "Hanging out", 6));

        // Set adapter
        savedListAdapter = new SavedListAdapter(savedLists);
        savedListsRecyclerView.setAdapter(savedListAdapter);

        // Close button
        findViewById(R.id.closeSavedButton).setOnClickListener(v -> finish());
    }

    // lifecycle
    @Override public void onStart() { super.onStart(); mapView.onStart(); }
    @Override public void onResume() { super.onResume(); mapView.onResume(); }
    @Override public void onPause() { super.onPause(); mapView.onPause(); }
    @Override public void onStop() { super.onStop(); mapView.onStop(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
    @Override protected void onDestroy() { super.onDestroy(); mapView.onDestroy(); }
    @Override protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
