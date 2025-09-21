package com.example.myapplication;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

public class MapActivity extends AppCompatActivity {

    private MapView mapView;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo Mapbox với Goong API Key
        Mapbox.getInstance(this, getString(R.string.goong_api_key));

        setContentView(R.layout.activity_map);
        mapView = findViewById(R.id.mapView);
        statusText = findViewById(R.id.statusText);

        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                String apiKey = getString(R.string.goong_api_key);
                String styleUrl = "https://tiles.goong.io/assets/goong_map_web.json?api_key=" + apiKey;

                statusText.setText("Loading Goong map...");

                mapboxMap.setStyle(new Style.Builder().fromUri(styleUrl),
                        style -> statusText.setText("✅ Goong map loaded!"));
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
