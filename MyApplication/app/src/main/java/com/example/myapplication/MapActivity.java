package com.example.myapplication;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

public class MapActivity extends AppCompatActivity {

    private MapView mapView;
    private EditText searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init Mapbox with Goong API key
        Mapbox.getInstance(this, getString(R.string.goong_api_key));

        setContentView(R.layout.activity_map);
        mapView = findViewById(R.id.mapView);
//        searchBar = findViewById(R.id.searchBar);

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
