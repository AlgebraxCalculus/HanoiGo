package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.PlaceAdapter;
import com.example.myapplication.adapter.RouteAdapter;
import com.example.myapplication.model.Place;
import com.example.myapplication.model.Route;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity {

    private MapView mapView;
    private BottomSheetBehavior<View> bottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init Mapbox with Goong API key
        Mapbox.getInstance(this, getString(R.string.goong_api_key));

        setContentView(R.layout.activity_map);
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

        // Setup Bottom Sheet
        setupBottomSheet();
    }

    private void setupBottomSheet() {
        View bottomSheet = findViewById(R.id.bottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        // Set initial state
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setPeekHeight(200);

        // Add callback for state changes
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // Handle state changes if needed
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // Handle slide changes if needed
            }
        });

        // Close button
        ImageView btnClose = findViewById(R.id.btnCloseExplore);
        btnClose.setOnClickListener(v -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });

        // Setup RecyclerViews with sample data
        setupIconicPlaces();
        setupTopVisited();
        setupPopularNearYou();
        setupSuggestedRoutes();
    }

    private void setupIconicPlaces() {
        RecyclerView rvIconicPlaces = findViewById(R.id.rvIconicPlaces);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvIconicPlaces.setLayoutManager(layoutManager);

        List<Place> places = new ArrayList<>();
        places.add(new Place("Hoan Kiem lake", "description bla bla bla bla bla bla", "3.6 km", R.drawable.hoguom));
        places.add(new Place("Temple of Literature", "description bla bla bla bla bla bla", "4.2 km", R.drawable.hoguom));
        places.add(new Place("Old Quarter", "description bla bla bla bla bla bla", "2.8 km", R.drawable.hoguom));
        places.add(new Place("Ho Chi Minh Mausoleum", "description bla bla bla bla bla bla", "5.1 km", R.drawable.hoguom));
        places.add(new Place("West Lake", "description bla bla bla bla bla bla", "6.3 km", R.drawable.hoguom));

        PlaceAdapter adapter = new PlaceAdapter(places);
        rvIconicPlaces.setAdapter(adapter);
    }

    private void setupTopVisited() {
        RecyclerView rvTopVisited = findViewById(R.id.rvTopVisited);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvTopVisited.setLayoutManager(layoutManager);

        List<Place> places = new ArrayList<>();
        places.add(new Place("Hoan Kiem lake", "description bla bla bla bla bla bla", "3.6 km", R.drawable.hoguom));
        places.add(new Place("Dong Xuan Market", "description bla bla bla bla bla bla", "2.5 km", R.drawable.hoguom));
        places.add(new Place("Long Bien Bridge", "description bla bla bla bla bla bla", "3.9 km", R.drawable.hoguom));
        places.add(new Place("Thang Long Imperial Citadel", "description bla bla bla bla bla bla", "4.7 km", R.drawable.hoguom));
        places.add(new Place("Vietnam Museum of Ethnology", "description bla bla bla bla bla bla", "7.2 km", R.drawable.hoguom));

        PlaceAdapter adapter = new PlaceAdapter(places);
        rvTopVisited.setAdapter(adapter);
    }

    private void setupPopularNearYou() {
        RecyclerView rvPopularNearYou = findViewById(R.id.rvPopularNearYou);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvPopularNearYou.setLayoutManager(layoutManager);

        List<Place> places = new ArrayList<>();
        places.add(new Place("Hoan Kiem lake", "description bla bla bla bla bla bla", "3.6 km", R.drawable.hoguom));
        places.add(new Place("St. Joseph's Cathedral", "description bla bla bla bla bla bla", "1.8 km", R.drawable.hoguom));
        places.add(new Place("Hanoi Opera House", "description bla bla bla bla bla bla", "2.3 km", R.drawable.hoguom));
        places.add(new Place("Train Street", "description bla bla bla bla bla bla", "1.5 km", R.drawable.hoguom));
        places.add(new Place("Tran Quoc Pagoda", "description bla bla bla bla bla bla", "5.8 km", R.drawable.hoguom));

        PlaceAdapter adapter = new PlaceAdapter(places);
        rvPopularNearYou.setAdapter(adapter);
    }

    private void setupSuggestedRoutes() {
        RecyclerView rvSuggestedRoutes = findViewById(R.id.rvSuggestedRoutes);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvSuggestedRoutes.setLayoutManager(layoutManager);

        List<Route> routes = new ArrayList<>();
        routes.add(new Route("Hoan Kiem lake - Hanoi Old Q...", "description bla bla bla bla bla bla", "6.36 km", "20m 36s", R.drawable.hoguom));
        routes.add(new Route("Hoan Kiem lake - Hanoi Old Q...", "description bla bla bla bla bla bla", "6.36 km", "20m 36s", R.drawable.hoguom));
        routes.add(new Route("Hoan Kiem lake - Hanoi Old Q...", "description bla bla bla bla bla bla", "6.36 km", "20m 36s", R.drawable.hoguom));
        routes.add(new Route("Hoan Kiem lake - Hanoi Old Q...", "description bla bla bla bla bla bla", "6.36 km", "20m 36s", R.drawable.hoguom));
        routes.add(new Route("Hoan Kiem lake - Hanoi Old Q...", "description bla bla bla bla bla bla", "6.36 km", "20m 36s", R.drawable.hoguom));

        RouteAdapter adapter = new RouteAdapter(routes);
        rvSuggestedRoutes.setAdapter(adapter);
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
