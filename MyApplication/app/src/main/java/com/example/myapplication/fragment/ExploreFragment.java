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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.PlaceAdapter;
import com.example.myapplication.adapter.RouteAdapter;
import com.example.myapplication.api.LocationApi;
import com.example.myapplication.model.Place;
import com.example.myapplication.model.Route;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExploreFragment extends Fragment {

    private BottomSheetBehavior<View> bottomSheetBehavior;

    private RecyclerView rvIconicPlaces;
    private RecyclerView rvTopVisited;
    private RecyclerView rvPopularNearYou;

    private List<Place> listIconic;
    private List<Place> listTopVisited;
    private List<Place> listPopularNearU;

    private PlaceAdapter adapterIconic;
    private PlaceAdapter adapterTopVisited;
    private PlaceAdapter adapterPopularNearU;

    private double userLat = 21.005147582587608;
    private double userLng = 105.86326519584026;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        setupBottomSheet(view);
        setupPlaceData();
        setupSuggestedRoutes(view);

        return view;
    }

    private void setupBottomSheet(View view) {
        View bottomSheet = view.findViewById(R.id.bottomSheetExplore);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setPeekHeight(450);

        ImageView btnClose = view.findViewById(R.id.btnCloseExplore);
        btnClose.setOnClickListener(v -> bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED));

        rvIconicPlaces = view.findViewById(R.id.rvIconicPlaces);
        rvTopVisited = view.findViewById(R.id.rvTopVisited);
        rvPopularNearYou = view.findViewById(R.id.rvPopularNearYou);

        rvIconicPlaces.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvTopVisited.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvPopularNearYou.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    private void setupPlaceData() {
        listIconic = new ArrayList<>();
        listTopVisited = new ArrayList<>();
        listPopularNearU = new ArrayList<>();

        //setUp listIconic
        LocationApi.GetLocationList(userLat, userLng,"Iconic", false, false, getContext(), new LocationApi.LocationApiCallback() {
            @Override
            public void onSuccess(ArrayList<JSONObject> data) {
                Map<Place, String> placeToAddress = new HashMap<>();
                for (JSONObject a : data) {
                    try {
                        JSONObject location = a.getJSONObject("locationResponse");
                        Place place = new Place(
                                location.getString("name"),
                                location.getString("description"),
                                a.getString("distanceText"),
                                location.getString("defaultPicture")
                        );
                        listIconic.add(place);

                        // Lưu tạm address vào map
                        placeToAddress.put(place, location.getString("address"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                for (Place place : listIconic) {
                    String address = placeToAddress.get(place);
                    place.setAddress(address);
                }
                requireActivity().runOnUiThread(() -> {
                    adapterIconic = new PlaceAdapter(listIconic, place -> openPlaceDetail(place));
                    rvIconicPlaces.setAdapter(adapterIconic);
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                if (isAdded()) { // tránh crash nếu fragment đã bị remove
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "fetch location list failed: " + errorMessage, Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });

        //setUp listTopVisited
        LocationApi.GetLocationList(userLat, userLng,"", true, false, getContext(), new LocationApi.LocationApiCallback() {
            @Override
            public void onSuccess(ArrayList<JSONObject> data) {
                Map<Place, String> placeToAddress = new HashMap<>();
                for (JSONObject a : data) {
                    try {
                        JSONObject location = a.getJSONObject("locationResponse");
                        Place place = new Place(
                                location.getString("name"),
                                location.getString("description"),
                                a.getString("distanceText"),
                                location.getString("defaultPicture")
                        );
                        listTopVisited.add(place);

                        // Lưu tạm address vào map
                        placeToAddress.put(place, location.getString("address"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                for (Place place : listTopVisited) {
                    String address = placeToAddress.get(place);
                    place.setAddress(address);
                }
                requireActivity().runOnUiThread(() -> {
                    adapterTopVisited = new PlaceAdapter(listTopVisited, place -> openPlaceDetail(place));
                    rvTopVisited.setAdapter(adapterTopVisited);
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                if (isAdded()) { // tránh crash nếu fragment đã bị remove
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "fetch location list failed: " + errorMessage, Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });

        //setUp listPopularNearU
        LocationApi.GetLocationList(userLat, userLng,"", false, true, getContext(), new LocationApi.LocationApiCallback() {
            @Override
            public void onSuccess(ArrayList<JSONObject> data) {
                Map<Place, String> placeToAddress = new HashMap<>();
                for (JSONObject a : data) {
                    try {
                        JSONObject location = a.getJSONObject("locationResponse");
                        Place place = new Place(
                                location.getString("name"),
                                location.getString("description"),
                                a.getString("distanceText"),
                                location.getString("defaultPicture")
                        );
                        listPopularNearU.add(place);

                        // Lưu tạm address vào map
                        placeToAddress.put(place, location.getString("address"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                for (Place place : listPopularNearU) {
                    String address = placeToAddress.get(place);
                    place.setAddress(address);
                }
                requireActivity().runOnUiThread(() -> {
                    adapterPopularNearU = new PlaceAdapter(listPopularNearU, place -> openPlaceDetail(place));
                    rvPopularNearYou.setAdapter(adapterPopularNearU);
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "fetch location list failed: " + errorMessage, Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private void setupSuggestedRoutes(View view) {
        RecyclerView rv = view.findViewById(R.id.rvSuggestedRoutes);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        List<Route> routes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            routes.add(new Route("Route " + (i + 1), "Sample description", "6.3 km", "20m 36s", R.drawable.hoguom));
        }
        rv.setAdapter(new RouteAdapter(routes));
    }

    private void openPlaceDetail(Place place) {
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof MapFragment) {
            ((MapFragment) parentFragment).openPlaceDetailFragment(place);
        }
    }
}