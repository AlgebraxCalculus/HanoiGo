package com.example.myapplication;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.maps.MapView;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.MapInitOptions;

public class MapActivity extends AppCompatActivity {

    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mapView = findViewById(R.id.mapView);
        setContentView(mapView);

        // Load map và Goong style
        MapboxMap mapboxMap = mapView.getMapboxMap();
        mapboxMap.loadStyleUri(
                "https://tiles.goong.io/assets/goong_map_web.json?api_key=your_maptiles_key",
                style -> {
                    // Map đã load xong, có thể thêm marker hoặc các thao tác khác
                }
        );
    }
}
