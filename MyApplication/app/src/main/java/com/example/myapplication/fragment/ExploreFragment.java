package com.example.myapplication.fragment;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.CheckBox;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.PlaceAdapter;
import com.example.myapplication.adapter.AiRouteAdapter;
import com.example.myapplication.adapter.RouteStopAdapter;
import com.example.myapplication.api.LocationApi;
import com.example.myapplication.api.AiRouteApi;
import com.example.myapplication.model.Place;
import com.example.myapplication.model.AIRoute;
import com.example.myapplication.model.TravelPlan;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ExploreFragment extends Fragment {

    private BottomSheetBehavior<View> bottomSheetBehavior;
    private NestedScrollView nestedScrollExplore;

    private RecyclerView rvIconicPlaces;
    private RecyclerView rvTopVisited;
    private RecyclerView rvPopularNearYou;

    private List<Place> listIconic;
    private List<Place> listTopVisited;
    private List<Place> listPopularNearU;

    private PlaceAdapter adapterIconic;
    private PlaceAdapter adapterTopVisited;
    private PlaceAdapter adapterPopularNearU;

    // ✅ ONLY INTERESTS
    private CheckBox cbCuisine, cbCulture, cbEntertaining, cbIconic;
    private Button btnSuggestRoute;
    private ProgressBar progressBarAiRoute;
    private RecyclerView rvSuggestedRoutes;

    private AiRouteAdapter aiRouteAdapter;
    private AiRouteApi aiRouteApi;

    // giữ popup route để quay lại vẫn còn
    private AlertDialog routeDialog;
    private AIRoute lastOpenedRoute;
    private boolean reopenRouteDialogOnResume = false;

    // ✅ user location from MapFragment (real-time)
    double userLat = 0, userLng = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        setupBottomSheet(view);
        setupSuggestedRoutes(view);

        return view;
    }

    private void setupBottomSheet(View view) {
        nestedScrollExplore = view.findViewById(R.id.bottomSheetExplore);
        bottomSheetBehavior = BottomSheetBehavior.from(nestedScrollExplore);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setPeekHeight(450);

        ImageView btnClose = view.findViewById(R.id.btnCloseExplore);
        btnClose.setOnClickListener(v ->
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED));

        rvIconicPlaces = view.findViewById(R.id.rvIconicPlaces);
        rvTopVisited = view.findViewById(R.id.rvTopVisited);
        rvPopularNearYou = view.findViewById(R.id.rvPopularNearYou);

        rvIconicPlaces.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvTopVisited.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvPopularNearYou.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    public void updateUserLocation(double lat, double lng) {
        this.userLat = lat;
        this.userLng = lng;
        setupPlaceData();
    }

    private void setupPlaceData() {
        listIconic = new ArrayList<>();
        listTopVisited = new ArrayList<>();
        listPopularNearU = new ArrayList<>();

        // Iconic
        LocationApi.GetLocationList(userLat, userLng, "Iconic", false, false, getContext(),
                new LocationApi.LocationApiCallback() {
                    @Override
                    public void onSuccess(ArrayList<JSONObject> data) {
                        for (JSONObject a : data) {
                            try {
                                JSONObject location = a.getJSONObject("locationResponse");
                                Place place = new Place(
                                        location.getString("name"),
                                        location.getString("description"),
                                        a.getString("distanceText"),
                                        location.getString("defaultPicture"),
                                        location.getString("address")
                                );
                                if (location.has("id")) place.setId(location.getString("id"));
                                listIconic.add(place);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            adapterIconic = new PlaceAdapter(listIconic, this::openPlaceDetail);
                            rvIconicPlaces.setAdapter(adapterIconic);
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(),
                                        "fetch iconic failed: " + errorMessage,
                                        Toast.LENGTH_SHORT).show()
                        );
                    }

                    private void openPlaceDetail(Place place) {
                        ExploreFragment.this.openPlaceDetail(place);
                    }
                });

        // Top visited
        LocationApi.GetLocationList(userLat, userLng, "", true, false, getContext(),
                new LocationApi.LocationApiCallback() {
                    @Override
                    public void onSuccess(ArrayList<JSONObject> data) {
                        for (JSONObject a : data) {
                            try {
                                JSONObject location = a.getJSONObject("locationResponse");
                                Place place = new Place(
                                        location.getString("name"),
                                        location.getString("description"),
                                        a.getString("distanceText"),
                                        location.getString("defaultPicture"),
                                        location.getString("address")
                                );
                                if (location.has("id")) place.setId(location.getString("id"));
                                listTopVisited.add(place);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            adapterTopVisited = new PlaceAdapter(listTopVisited, this::openPlaceDetail);
                            rvTopVisited.setAdapter(adapterTopVisited);
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(),
                                        "fetch top visited failed: " + errorMessage,
                                        Toast.LENGTH_SHORT).show()
                        );
                    }

                    private void openPlaceDetail(Place place) {
                        ExploreFragment.this.openPlaceDetail(place);
                    }
                });

        // Popular near you
        LocationApi.GetLocationList(userLat, userLng, "", false, true, getContext(),
                new LocationApi.LocationApiCallback() {
                    @Override
                    public void onSuccess(ArrayList<JSONObject> data) {
                        for (JSONObject a : data) {
                            try {
                                JSONObject location = a.getJSONObject("locationResponse");
                                Place place = new Place(
                                        location.getString("name"),
                                        location.getString("description"),
                                        a.getString("distanceText"),
                                        location.getString("defaultPicture"),
                                        location.getString("address")
                                );
                                if (location.has("id")) place.setId(location.getString("id"));
                                listPopularNearU.add(place);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            adapterPopularNearU = new PlaceAdapter(listPopularNearU, this::openPlaceDetail);
                            rvPopularNearYou.setAdapter(adapterPopularNearU);
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(),
                                        "fetch popular failed: " + errorMessage,
                                        Toast.LENGTH_SHORT).show()
                        );
                    }

                    private void openPlaceDetail(Place place) {
                        ExploreFragment.this.openPlaceDetail(place);
                    }
                });
    }

    private void setupSuggestedRoutes(View view) {
        cbCuisine = view.findViewById(R.id.cbCuisine);
        cbCulture = view.findViewById(R.id.cbCulture);
        cbEntertaining = view.findViewById(R.id.cbEntertaining);
        cbIconic = view.findViewById(R.id.cbIconic);

        btnSuggestRoute = view.findViewById(R.id.btnSuggestRoute);
        progressBarAiRoute = view.findViewById(R.id.progressBarAiRoute);
        rvSuggestedRoutes = view.findViewById(R.id.rvSuggestedRoutes);

        rvSuggestedRoutes.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));

        aiRouteAdapter = new AiRouteAdapter(this::showRouteDetailDialog);
        rvSuggestedRoutes.setAdapter(aiRouteAdapter);

        aiRouteApi = new AiRouteApi();

        // optional default tick for demo
        cbCuisine.setChecked(true);
        cbCulture.setChecked(true);

        btnSuggestRoute.setOnClickListener(v -> requestAiRoutes());
    }

    private void requestAiRoutes() {
        // (optional) nếu bạn muốn bắt buộc phải có location thật
        if (userLat == 0 || userLng == 0) {
            Toast.makeText(requireContext(),
                    "Waiting for your location...",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> interests = new ArrayList<>();
        if (cbCuisine.isChecked()) interests.add("food");
        if (cbCulture.isChecked()) interests.add("culture");
        if (cbEntertaining.isChecked()) interests.add("entertainment");
        if (cbIconic.isChecked()) interests.add("iconic");

        if (interests.isEmpty()) {
            Toast.makeText(requireContext(),
                    "Please select at least one interest",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ default values because you removed 3 inputs
        String date = getTomorrowDate();
        int durationDays = 1;
        Long budget = null;

        TravelPlan plan = new TravelPlan(date, durationDays, interests, budget);

        String bearerToken = getAuthTokenOrNull();
        progressBarAiRoute.setVisibility(View.VISIBLE);

        aiRouteApi.getSuggestedRoutes(bearerToken, plan, new AiRouteApi.AiRouteCallback() {
            @Override
            public void onSuccess(List<AIRoute> routes) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    progressBarAiRoute.setVisibility(View.GONE);
                    aiRouteAdapter.setRoutes(routes);

                    if (routes == null || routes.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "No suitable routes found",
                                Toast.LENGTH_SHORT).show();
                    } else if (nestedScrollExplore != null && rvSuggestedRoutes != null) {
                        nestedScrollExplore.post(() -> {
                            int y = rvSuggestedRoutes.getTop() - 32;
                            if (y < 0) y = 0;
                            nestedScrollExplore.smoothScrollTo(0, y);
                        });
                    }
                });
            }

            @Override
            public void onError(Throwable t) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    progressBarAiRoute.setVisibility(View.GONE);
                    Toast.makeText(requireContext(),
                            "AI Route error: " + (t != null ? t.getMessage() : "unknown"),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private String getTomorrowDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
    }

    private String getAuthTokenOrNull() {
        return null;
    }

    private void showRouteDetailDialog(AIRoute route) {
        if (!isAdded() || route == null) return;

        if (routeDialog != null && routeDialog.isShowing()) {
            routeDialog.dismiss();
            routeDialog = null;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_route_detail, null, false);

        TextView tvRouteTitle = dialogView.findViewById(R.id.tvRouteTitle);
        TextView tvRouteSummary = dialogView.findViewById(R.id.tvRouteSummary);
        RecyclerView rvRouteStops = dialogView.findViewById(R.id.rvRouteStops);
        Button btnCloseRoute = dialogView.findViewById(R.id.btnCloseRoute);

        tvRouteTitle.setText(route.getTitle());

        String summary = String.format(
                Locale.getDefault(),
                "Total: %.2f km • %s",
                route.getDistanceKm(),
                route.getDuration()
        );
        tvRouteSummary.setText(summary);

        rvRouteStops.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        );

        // ✅ click stop -> hide dialog -> open place detail -> onResume show lại
        rvRouteStops.setAdapter(new RouteStopAdapter(route.getStops(), place -> {
            if (!isAdded()) return;

            if (place == null || TextUtils.isEmpty(place.getId())) {
                Toast.makeText(requireContext(),
                        "This stop has no id, cannot open detail.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            reopenRouteDialogOnResume = true;
            lastOpenedRoute = route;

            if (routeDialog != null) routeDialog.hide();
            openPlaceDetail(place);
        }));

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        routeDialog = dialog;
        lastOpenedRoute = route;

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        btnCloseRoute.setOnClickListener(v -> {
            reopenRouteDialogOnResume = false;
            lastOpenedRoute = null;

            if (routeDialog != null) {
                routeDialog.dismiss();
                routeDialog = null;
            }
        });

        dialog.show();
    }

    private void openPlaceDetail(Place place) {
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof MapFragment) {
            ((MapFragment) parentFragment).openPlaceDetailFragment(place);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (userLat != 0 && userLng != 0) {
            setupPlaceData();
        }

        if (reopenRouteDialogOnResume) {
            reopenRouteDialogOnResume = false;

            if (routeDialog != null) {
                routeDialog.show();
            } else if (lastOpenedRoute != null) {
                showRouteDetailDialog(lastOpenedRoute);
            }
        }
    }

    @Override
    public void onDestroyView() {
        if (routeDialog != null) {
            routeDialog.dismiss();
            routeDialog = null;
        }
        super.onDestroyView();
    }
}
