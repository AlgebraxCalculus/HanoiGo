package com.example.myapplication.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.model.Place;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;

public class MapFragment extends Fragment {

    private MapView mapView;
    private MapboxMap map;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

    private static boolean hasZoomedToUser = false;
    private LatLng userLocation;

    private View footerContainer, btnBookmark, btnCompass, btnNavigate;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Mapbox.getInstance(requireContext(), getString(R.string.goong_map_key));
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(mapboxMap -> {
            map = mapboxMap;
            String styleUrl = "https://tiles.goong.io/assets/goong_map_web.json?api_key=" + getString(R.string.goong_map_key);
            map.setStyle(new Style.Builder().fromUri(styleUrl), style -> {
                // Khi map load xong style → mới được phép hiển thị vị trí
                checkLocationPermissionAndGetLocation();
            });
        });

        // Load Explore mặc định
        loadChildFragment(new ExploreFragment());

        // Floating buttons
        btnBookmark = view.findViewById(R.id.btnBookmark);
        btnCompass = view.findViewById(R.id.btnCompass);
        btnNavigate = view.findViewById(R.id.btnNavigate);

        btnBookmark.setOnClickListener(v -> loadChildFragment(new BookmarkFragment()));
        btnCompass.setOnClickListener(v -> loadChildFragment(new ExploreFragment()));

        btnNavigate.setOnClickListener(v -> {
            if (userLocation != null && map != null) {
                showPlaceMarker(userLocation.getLatitude(), userLocation.getLongitude(), "You are here");
            } else {
                Toast.makeText(requireContext(), "User location unavailable", Toast.LENGTH_SHORT).show();
                checkLocationPermissionAndGetLocation();
            }
        });

        return view;
    }

    private void loadChildFragment(Fragment fragment) {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.childFragmentContainer, fragment)
                .commit();
    }

    public void openPlaceDetailFragment(Place place) {
        PlaceDetailFragment detailFragment = PlaceDetailFragment.newInstance(place);

        getChildFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_up,    // enter
                        R.anim.fade_out,       // exit (not used here for explore but ok)
                        R.anim.fade_in,        // popEnter
                        R.anim.slide_out_down  // popExit
                )
                .replace(R.id.childFragmentContainer, detailFragment)
                .addToBackStack(null)
                .commit();
        // Ẩn 3 button
        btnBookmark.setVisibility(View.GONE);
        btnCompass.setVisibility(View.GONE);
        btnNavigate.setVisibility(View.GONE);

        // Ẩn footer ở MainActivity
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setFooterVisibility(false);
        }

        // Hiển thị marker vị trí
        showPlaceMarker(place.getLatitude(), place.getLongitude(), place.getName());
    }

    private void checkLocationPermissionAndGetLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastKnownLocation();
        }
    }

    private void getLastKnownLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            if (map != null && !hasZoomedToUser) {
                                hasZoomedToUser = true;
                                // Hiển thị marker vị trí người dùng
                                showPlaceMarker(userLocation.getLatitude(), userLocation.getLongitude(), "You are here");
                            }
                        } else {
                            Toast.makeText(requireContext(), "Cannot detect your location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLastKnownLocation();
        } else {
            Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    // Hiển thị marker ở vị trí bất kỳ (cả user hoặc place)
    public void showPlaceMarker(double lat, double lng, String title) {
        if (map == null) return;

        map.clear(); // Xóa marker cũ
        LatLng position = new LatLng(lat, lng);

        map.addMarker(new MarkerOptions()
                .position(position)
                .title(title));

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
    }

    @Override public void onStart() { super.onStart(); mapView.onStart(); }
    @Override public void onResume() {
        super.onResume(); mapView.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setFooterVisibility(true);
        }
    }
    @Override public void onPause() { super.onPause(); mapView.onPause(); }
    @Override public void onStop() { super.onStop(); mapView.onStop(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
    @Override public void onDestroyView() { super.onDestroyView(); mapView.onDestroy(); }
}
