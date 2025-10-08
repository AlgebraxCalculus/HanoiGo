package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

        // Khởi tạo Mapbox với Goong API key
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

        // Thiết lập trạng thái ban đầu
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setPeekHeight(200);

        // Callback khi trạng thái BottomSheet thay đổi
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // Xử lý nếu cần
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // Xử lý khi slide nếu cần
            }
        });

        // Nút đóng BottomSheet
        ImageView btnClose = findViewById(R.id.btnCloseExplore);
        btnClose.setOnClickListener(v -> bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED));

        // Thiết lập RecyclerViews với dữ liệu mẫu
        setupIconicPlaces();
        setupTopVisited();
        setupPopularNearYou();
        setupSuggestedRoutes();
    }

    private void setupIconicPlaces() {
        RecyclerView rvIconicPlaces = findViewById(R.id.rvIconicPlaces);
        rvIconicPlaces.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<Place> places = new ArrayList<>();
        places.add(new Place("Hoan Kiem lake", "Description...", "3.6 km", R.drawable.hoguom));
        places.add(new Place("Temple of Literature", "Description...", "4.2 km", R.drawable.hoguom));
        places.add(new Place("Old Quarter", "Description...", "2.8 km", R.drawable.hoguom));
        places.add(new Place("Ho Chi Minh Mausoleum", "Description...", "5.1 km", R.drawable.hoguom));
        places.add(new Place("West Lake", "Description...", "6.3 km", R.drawable.hoguom));

        rvIconicPlaces.setAdapter(new PlaceAdapter(places));
    }

    private void setupTopVisited() {
        RecyclerView rvTopVisited = findViewById(R.id.rvTopVisited);
        rvTopVisited.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<Place> places = new ArrayList<>();
        places.add(new Place("Hoan Kiem lake", "Description...", "3.6 km", R.drawable.hoguom));
        places.add(new Place("Dong Xuan Market", "Description...", "2.5 km", R.drawable.hoguom));
        places.add(new Place("Long Bien Bridge", "Description...", "3.9 km", R.drawable.hoguom));
        places.add(new Place("Thang Long Imperial Citadel", "Description...", "4.7 km", R.drawable.hoguom));
        places.add(new Place("Vietnam Museum of Ethnology", "Description...", "7.2 km", R.drawable.hoguom));

        rvTopVisited.setAdapter(new PlaceAdapter(places));
    }

    private void setupPopularNearYou() {
        RecyclerView rvPopularNearYou = findViewById(R.id.rvPopularNearYou);
        rvPopularNearYou.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<Place> places = new ArrayList<>();
        places.add(new Place("Hoan Kiem lake", "Description...", "3.6 km", R.drawable.hoguom));
        places.add(new Place("St. Joseph's Cathedral", "Description...", "1.8 km", R.drawable.hoguom));
        places.add(new Place("Hanoi Opera House", "Description...", "2.3 km", R.drawable.hoguom));
        places.add(new Place("Train Street", "Description...", "1.5 km", R.drawable.hoguom));
        places.add(new Place("Tran Quoc Pagoda", "Description...", "5.8 km", R.drawable.hoguom));

        rvPopularNearYou.setAdapter(new PlaceAdapter(places));
    }

    private void setupSuggestedRoutes() {
        RecyclerView rvSuggestedRoutes = findViewById(R.id.rvSuggestedRoutes);
        rvSuggestedRoutes.setLayoutManager(new LinearLayoutManager(this));

        List<Route> routes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            routes.add(new Route(
                    "Hoan Kiem lake - Hanoi Old Q...",
                    "Description...",
                    "6.36 km",
                    "20m 36s",
                    R.drawable.hoguom
            ));
        }

        rvSuggestedRoutes.setAdapter(new RouteAdapter(routes));
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
