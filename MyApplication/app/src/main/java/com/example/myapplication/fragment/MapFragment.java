package com.example.myapplication.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.model.Place;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;

import org.json.JSONException;
import org.json.JSONObject;

public class MapFragment extends Fragment {

    private MapView mapView;
    private MapboxMap map;
    private LatLng userLocation;

    private View btnBookmark, btnCompass, btnNavigate;
    private String username = "default";
    private ImageView imgAvatar;
    private String avatar = "";
    private String jwtToken = "";
    private boolean hasZoomedToUser = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Mapbox.getInstance(requireContext(), getString(R.string.goong_map_key));
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Nhận dữ liệu user + jwt
        if (getArguments() != null) {
            jwtToken = getArguments().getString("jwtToken");
            String userJson = getArguments().getString("user");
            if (userJson != null) {
                try {
                    JSONObject userObj = new JSONObject(userJson);
                    username = userObj.optString("username", "");
                    avatar = userObj.optString("profilePicture", "");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        imgAvatar = view.findViewById(R.id.imgAvatar);

        // Load avatar user
        if (avatar != null && !avatar.isEmpty()) {
            Glide.with(this)
                    .load(avatar)
                    .placeholder(R.drawable.avatar)
                    .error(R.drawable.avatar)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .circleCrop()
                    .into(imgAvatar);
        } else {
            imgAvatar.setImageResource(R.drawable.avatar);
        }

        // --- MAP SETUP ---
        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapboxMap -> {
            map = mapboxMap;
            String styleUrl = "https://tiles.goong.io/assets/goong_map_web.json?api_key=" + getString(R.string.goong_map_key);
            map.setStyle(new Style.Builder().fromUri(styleUrl), style -> {
                // Khi style sẵn sàng thì hiển thị vị trí user nếu đã có
                if (userLocation != null) {
                    showMarker(userLocation.getLatitude(), userLocation.getLongitude(), "You are here");
                }
            });
        });

        // --- FRAGMENTS CHILD ---
        ExploreFragment exploreFragment = new ExploreFragment();
        Bundle childArgs = new Bundle();
        childArgs.putString("jwtToken", jwtToken);
        childArgs.putString("username", username);
        childArgs.putString("avatar", avatar);
        exploreFragment.setArguments(childArgs);
        loadChildFragment(exploreFragment);

        // --- BUTTONS ---
        btnBookmark = view.findViewById(R.id.btnBookmark);
        btnCompass = view.findViewById(R.id.btnCompass);
        btnNavigate = view.findViewById(R.id.btnNavigate);

        btnBookmark.setOnClickListener(v -> loadChildFragment(new BookmarkFragment()));
        btnCompass.setOnClickListener(v -> loadChildFragment(exploreFragment));

        btnNavigate.setOnClickListener(v -> {
            if (userLocation != null && map != null) {
                showMarker(userLocation.getLatitude(), userLocation.getLongitude(), "You are here");
            } else {
                Toast.makeText(requireContext(), "User location unavailable", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    // Load fragment con
    private void loadChildFragment(Fragment fragment) {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.childFragmentContainer, fragment)
                .commit();
    }

    // Mở chi tiết địa điểm
    public void openPlaceDetailFragment(Place place) {
        PlaceDetailFragment detailFragment = PlaceDetailFragment.newInstance(place);
        Bundle args = new Bundle();
        args.putSerializable("placeData", place);
        args.putString("jwtToken", jwtToken);
        args.putString("username", username);
        args.putString("avatar", avatar);
        detailFragment.setArguments(args);

        getChildFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_up,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.slide_out_down
                )
                .replace(R.id.childFragmentContainer, detailFragment)
                .addToBackStack(null)
                .commit();

        fadeOut(btnBookmark);
        fadeOut(btnCompass);
        fadeOut(btnNavigate);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setFooterVisibility(false);
        }
    }

    // Đóng chi tiết địa điểm và hiện lại nút
    public void onPlaceDetailClosed() {
        fadeIn(btnBookmark);
        fadeIn(btnCompass);
        fadeIn(btnNavigate);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setFooterVisibility(true);
        }
    }

    // Place marker trên map
    public void showMarker(double lat, double lng, String title) {
        if (map == null) return;
        map.clear();
        LatLng position = new LatLng(lat, lng);
        map.addMarker(new MarkerOptions().position(position).title(title));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
    }

    // Cập nhật Location người dùng từ MainActivity
    public void updateUserLocation(double lat, double lng) {
        if (lat == 0.0 && lng == 0.0) return;

        userLocation = new LatLng(lat, lng);
        System.out.println("updateUserLocation() → lat=" + lat + ", lng=" + lng);

        // Nếu map đã sẵn sàng thì hiển thị marker luôn
        if (map != null) {
            showMarker(lat, lng, "You are here");
        }
    }

    // --- Hiệu ứng ẩn/hiện nút ---
    private void fadeOut(View v) {
        v.animate().alpha(0f).setDuration(200).withEndAction(() -> v.setVisibility(View.GONE)).start();
    }

    private void fadeIn(View v) {
        v.setVisibility(View.VISIBLE);
        v.setAlpha(0f);
        v.animate().alpha(1f).setDuration(200).start();
    }

    // --- MAPVIEW LIFECYCLE ---
    @Override public void onStart() { super.onStart(); mapView.onStart(); }
    @Override public void onResume() { super.onResume(); mapView.onResume(); }
    @Override public void onPause() { super.onPause(); mapView.onPause(); }
    @Override public void onStop() { super.onStop(); mapView.onStop(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
    @Override public void onDestroyView() { super.onDestroyView(); mapView.onDestroy(); }
}
