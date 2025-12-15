package com.example.myapplication.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.DirectionAdapter;
import com.example.myapplication.adapter.SearchSuggestionAdapter;
import com.example.myapplication.api.DirectionApi;
import com.example.myapplication.api.LocationApi;
import com.example.myapplication.model.Direction;
import com.example.myapplication.utils.PolylineDecoder;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DirectionFragment extends Fragment {

    private MapView mapView;
    private MapboxMap map;
    private ImageView btnClose;

    private EditText etOriginLocation, etDestinationLocation;
    private TextView tvDestinationName, tvTravelInfo;
    private RecyclerView rvDirections;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private CardView btnNavigate;

    private DirectionAdapter directionAdapter;
    private List<Direction> directionList = new ArrayList<>();

    private double originLat = 0;
    private double originLng = 0;
    private double destLat = 0;
    private double destLng = 0;
    private double userLat = 0;
    private double userLng = 0;
    private String destName = "";

    private Marker userLocationMarker;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Mapbox.getInstance(requireContext(), getString(R.string.goong_map_key));
        View view = inflater.inflate(R.layout.fragment_direction, container, false);

        if (getArguments() != null) {
            destLat = getArguments().getDouble("destLat", 0);
            destLng = getArguments().getDouble("destLng", 0);
            destName = getArguments().getString("destName", "");
            userLat = getArguments().getDouble("userLat", 0);
            userLng = getArguments().getDouble("userLng", 0);

            originLat = userLat;
            originLng = userLng;

            Log.d("DirectionFragment", "Received coordinates:");
            Log.d("DirectionFragment", "  Destination: " + destLat + ", " + destLng);
            Log.d("DirectionFragment", "  User: " + userLat + ", " + userLng);
        } else {
            Log.e("DirectionFragment", "Arguments is NULL!");
        }

        initializeViews(view);
        setupMap(savedInstanceState);
        setupBottomSheet(view);
        setupRecyclerView();
        setupListeners();
        setLocationData();

        return view;
    }

    private void initializeViews(View view) {
        etOriginLocation = view.findViewById(R.id.etOriginLocation);
        etDestinationLocation = view.findViewById(R.id.etDestinationLocation);
        mapView = view.findViewById(R.id.mapView);
        tvDestinationName = view.findViewById(R.id.tvDestinationName);
        tvTravelInfo = view.findViewById(R.id.tvTravelInfo);
        btnClose = view.findViewById(R.id.btnCloseDirection);
        btnNavigate = view.findViewById(R.id.btnNavigateDirection);
        rvDirections = view.findViewById(R.id.rvDirections);

    }

    private void setupMap(Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapboxMap -> {
            map = mapboxMap;
            String styleUrl = "https://tiles.goong.io/assets/goong_map_web.json?api_key=" + getString(R.string.goong_map_key);
            map.setStyle(new Style.Builder().fromUri(styleUrl), style -> {
                Log.d("DirectionFragment", "Map ready, fetching directions...");
                fetchDirections();
            });
        });
    }

    private void setupBottomSheet(View view) {
        View bottomSheet = view.findViewById(R.id.bottomSheetDirection);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setPeekHeight(550);

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        // Thêm callback để ẩn/hiện btnNavigate
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    btnNavigate.setVisibility(View.GONE);
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    btnNavigate.setVisibility(View.VISIBLE);
                }
            }


            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // Optional: Fade out/in button khi swipe
                btnNavigate.setAlpha(1 - slideOffset);
            }
        });
    }


    private void setupRecyclerView() {
        directionAdapter = new DirectionAdapter(requireContext(), directionList);
        rvDirections.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvDirections.setAdapter(directionAdapter);
    }

    private void setupListeners() {
        btnClose.setOnClickListener(v -> closeFragment());

        etOriginLocation.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                searchAutocomplete(etOriginLocation, true);
            }
        });

        etDestinationLocation.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                searchAutocomplete(etDestinationLocation, false);
            }
        });

        btnNavigate.setOnClickListener(v -> {
            if (map != null && userLat != 0 && userLng != 0) {
                LatLng userPosition = new LatLng(userLat, userLng);
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(userPosition, 16));
            } else {
                Toast.makeText(requireContext(), "Location not available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLocationData() {
        etOriginLocation.setText("Your location");
        etDestinationLocation.setText(destName);
        tvDestinationName.setText(destName);
    }

    private void addUserLocationMarker() {
        if (map == null || userLat == 0 || userLng == 0) return;

        IconFactory iconFactory = IconFactory.getInstance(requireContext());
        Icon customIcon = iconFactory.fromResource(R.drawable.ic_current_location);

        if (userLocationMarker != null) {
            map.removeMarker(userLocationMarker);
        }

        userLocationMarker = map.addMarker(new MarkerOptions()
                .position(new LatLng(userLat, userLng))
                .icon(customIcon)
                .title("Your Location"));
    }

    private void searchAutocomplete(EditText searchBar, boolean isOrigin) {
        CardView searchSuggestionsCard = getView().findViewById(R.id.searchSuggestionsCard);
        RecyclerView rvSearchSuggestions = getView().findViewById(R.id.rvSearchSuggestions);

        searchSuggestionsCard.setVisibility(View.VISIBLE);

        ArrayList<JSONObject> suggestionList = new ArrayList<>();

        // Thêm mục cố định vào đầu danh sách
        try {
            JSONObject fixedLocation = new JSONObject();
            if (isOrigin) {
                fixedLocation.put("name", "Your location");
                fixedLocation.put("latitude", userLat);
                fixedLocation.put("longitude", userLng);
                fixedLocation.put("isYourLocation", true);
            } else {
                fixedLocation.put("name", destName);
                fixedLocation.put("latitude", destLat);
                fixedLocation.put("longitude", destLng);
                fixedLocation.put("isDestination", true);
            }
            suggestionList.add(fixedLocation);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SearchSuggestionAdapter adapter =
                new SearchSuggestionAdapter(requireContext(), suggestionList, item -> {
                    try {
                        double lat = item.getDouble("latitude");
                        double lng = item.getDouble("longitude");
                        String name = item.optString("name");

                        if (isOrigin) {
                            originLat = lat;
                            originLng = lng;
                            etOriginLocation.setText(name);
                            etOriginLocation.clearFocus();
                        } else {
                            destLat = lat;
                            destLng = lng;
                            destName = name;
                            etDestinationLocation.setText(name);
                            tvDestinationName.setText(name);
                            etDestinationLocation.clearFocus();
                        }

                        fetchDirections();
                        searchSuggestionsCard.setVisibility(View.GONE);

                        // Ẩn bàn phím
                        InputMethodManager imm = (InputMethodManager) requireContext()
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        rvSearchSuggestions.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSearchSuggestions.setAdapter(adapter);

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}

            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                String keyword = s.toString().trim();
                String defaultText = isOrigin ? "Your location" : destName;

                if (keyword.isEmpty() || keyword.equalsIgnoreCase(defaultText)) {
                    adapter.updateData(suggestionList);
                    return;
                }

                LocationApi.SearchAutocomplete(keyword, getContext(),
                        new LocationApi.LocationApiCallback() {
                            @Override
                            public void onSuccess(ArrayList<JSONObject> data) {
                                requireActivity().runOnUiThread(() -> {
                                    ArrayList<JSONObject> fullList = new ArrayList<>(suggestionList);
                                    fullList.addAll(data);
                                    adapter.updateData(fullList);
                                });
                            }

                            @Override
                            public void onFailure(String error) {
                                adapter.updateData(suggestionList);
                            }
                        });
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchDirections() {
        if (map == null) {
            Log.e("DirectionFragment", "Map is null, cannot fetch directions");
            return;
        }

        if (originLat == 0 || originLng == 0 || destLat == 0 || destLng == 0) {
            Toast.makeText(requireContext(), "Invalid coordinates", Toast.LENGTH_SHORT).show();
            return;
        }

        DirectionApi.GetDirection(originLat, originLng, destLat, destLng, requireContext(),
                new DirectionApi.DirectionApiCallback() {
                    @Override
                    public void onSuccess(JSONObject result) {
                        requireActivity().runOnUiThread(() -> {
                            try {
                                parseDirectionData(result);
                            } catch (Exception e) {
                                Log.e("DirectionFragment", "Error parsing direction data", e);
                                Toast.makeText(requireContext(), "Error parsing directions", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Error: " + errorMessage, Toast.LENGTH_SHORT).show()
                        );
                    }
                });
    }

    private void parseDirectionData(JSONObject result) throws Exception {
        directionList.clear();

        if (result.has("durationText") && result.has("distanceText")) {
            String duration = result.getString("durationText");
            String distance = result.getString("distanceText");
            String routeInfo = duration + " - " + distance;
            tvTravelInfo.setText(routeInfo);
        }

        if (result.has("instructions")) {
            JSONArray instructions = result.getJSONArray("instructions");
            for (int i = 0; i < instructions.length(); i++) {
                JSONObject step = instructions.getJSONObject(i);
                String instruction = step.optString("htmlInstructions", "Continue");
                String distance = step.optString("distanceText", "");
                String maneuver = step.optString("maneuver", "");

                directionList.add(new Direction(instruction, distance, maneuver));
            }
        }

        if (result.has("overviewPolyline")) {
            String encodedPolyline = result.getString("overviewPolyline");
            drawPolyline(encodedPolyline);
        }

        directionAdapter.notifyDataSetChanged();
    }

    private void drawPolyline(String encodedPolyline) {
        if (map == null) {
            Log.e("DirectionFragment", "Map is null, cannot draw polyline");
            return;
        }

        List<LatLng> decodedPath = PolylineDecoder.decode(encodedPolyline);

        map.clear();

        map.addPolyline(new PolylineOptions()
                .addAll(decodedPath)
                .color(Color.parseColor("#4DA3FF"))
                .width(8f));

        map.addMarker(new MarkerOptions()
                .position(new LatLng(destLat, destLng))
                .title(destName));

        // Zoom vào toàn bộ tuyến đường
        if (!decodedPath.isEmpty()) {
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            for (LatLng point : decodedPath) {
                boundsBuilder.include(point);
            }
            LatLngBounds bounds = boundsBuilder.build();
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        }
        addUserLocationMarker();
    }

    private void closeFragment() {
        getParentFragmentManager().popBackStack();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}