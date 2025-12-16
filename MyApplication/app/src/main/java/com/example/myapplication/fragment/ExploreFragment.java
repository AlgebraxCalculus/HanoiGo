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
import android.widget.EditText;
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

import java.util.ArrayList;
import java.util.List;

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

    private EditText edtTravelDate, edtDurationDays, edtBudget;
    private CheckBox cbCuisine, cbCulture, cbEntertaining, cbIconic;
    private Button btnSuggestRoute;
    private ProgressBar progressBarAiRoute;
    private RecyclerView rvSuggestedRoutes;

    private AiRouteAdapter aiRouteAdapter;
    private AiRouteApi aiRouteApi;

    // ====== NEW: giữ popup route để quay lại vẫn còn ======
    private AlertDialog routeDialog;
    private AIRoute lastOpenedRoute;
    private boolean reopenRouteDialogOnResume = false;

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
        // bottomSheetExplore trong XML là NestedScrollView
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
        setupPlaceData(); // Gọi hàm load dữ liệu khi có vị trí
    }

    private void setupPlaceData() {
        listIconic = new ArrayList<>();
        listTopVisited = new ArrayList<>();
        listPopularNearU = new ArrayList<>();

        // 1. Get Iconic Places
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
                                // FIX: Thêm setId từ nhánh bookmark-fix
                                if (location.has("id")) {
                                    place.setId(location.getString("id"));
                                }
                                listIconic.add(place);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            adapterIconic =
                                    new PlaceAdapter(listIconic, place -> openPlaceDetail(place));
                            rvIconicPlaces.setAdapter(adapterIconic);
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(),
                                            "fetch iconic failed: " + errorMessage,
                                            Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                });

        // 2. Get Top Visited Places
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
                                // FIX: Thêm setId
                                if (location.has("id")) {
                                    place.setId(location.getString("id"));
                                }
                                listTopVisited.add(place);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            adapterTopVisited =
                                    new PlaceAdapter(listTopVisited, place -> openPlaceDetail(place));
                            rvTopVisited.setAdapter(adapterTopVisited);
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(),
                                            "fetch top visited failed: " + errorMessage,
                                            Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                });

        // 3. Get Popular Near You Places
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
                                // FIX: Thêm setId
                                if (location.has("id")) {
                                    place.setId(location.getString("id"));
                                }
                                listPopularNearU.add(place);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            adapterPopularNearU =
                                    new PlaceAdapter(listPopularNearU, place -> openPlaceDetail(place));
                            rvPopularNearYou.setAdapter(adapterPopularNearU);
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(),
                                            "fetch popular failed: " + errorMessage,
                                            Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                });
    }

    private void setupSuggestedRoutes(View view) {
        edtTravelDate = view.findViewById(R.id.edtTravelDate);
        edtDurationDays = view.findViewById(R.id.edtDurationDays);
        edtBudget = view.findViewById(R.id.edtBudget);

        cbCuisine = view.findViewById(R.id.cbCuisine);
        cbCulture = view.findViewById(R.id.cbCulture);
        cbEntertaining = view.findViewById(R.id.cbEntertaining);
        cbIconic = view.findViewById(R.id.cbIconic);

        btnSuggestRoute = view.findViewById(R.id.btnSuggestRoute);
        progressBarAiRoute = view.findViewById(R.id.progressBarAiRoute);
        rvSuggestedRoutes = view.findViewById(R.id.rvSuggestedRoutes);

        rvSuggestedRoutes.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));

        aiRouteAdapter = new AiRouteAdapter(route -> showRouteDetailDialog(route));
        rvSuggestedRoutes.setAdapter(aiRouteAdapter);

        aiRouteApi = new AiRouteApi();

        edtTravelDate.setText("2025-12-25");
        edtDurationDays.setText("1");
        cbCuisine.setChecked(true);
        cbCuisine.setChecked(true);
        cbCulture.setChecked(true);

        btnSuggestRoute.setOnClickListener(v -> requestAiRoutes());
    }

    private void requestAiRoutes() {
        String date = edtTravelDate.getText().toString().trim();
        String durationStr = edtDurationDays.getText().toString().trim();
        String budgetStr = edtBudget.getText().toString().trim();

        if (TextUtils.isEmpty(date) || TextUtils.isEmpty(durationStr)) {
            Toast.makeText(requireContext(),
                    "Please enter travel date and duration",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int durationDays;
        try {
            durationDays = Integer.parseInt(durationStr);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(),
                    "Duration is invalid",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> interests = new ArrayList<>();
        if (cbCuisine.isChecked()) interests.add("food");
        if (cbCulture.isChecked()) interests.add("culture");
        if (cbEntertaining.isChecked()) interests.add("entertainment");
        if (cbIconic.isChecked()) interests.add("iconic");

        if (interests.isEmpty()) {
            Toast.makeText(
                    requireContext(),
                    "Please select at least one interest",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        Long budget = null;
        if (!TextUtils.isEmpty(budgetStr)) {
            try {
                budget = Long.parseLong(budgetStr);
            } catch (NumberFormatException ignored) {
            }
        }

        TravelPlan plan = new TravelPlan(date, durationDays, interests, budget);

        String bearerToken = getAuthTokenOrNull(); // hiện đang trả null

        progressBarAiRoute.setVisibility(View.VISIBLE);

        aiRouteApi.getSuggestedRoutes(bearerToken, plan, new AiRouteApi.AiRouteCallback() {
            @Override
            public void onSuccess(List<AIRoute> routes) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    progressBarAiRoute.setVisibility(View.GONE);
                    aiRouteAdapter.setRoutes(routes);

                    if (routes.isEmpty()) {
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
                            "AI Route error: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private String getAuthTokenOrNull() {
        return null;
    }

    private void showRouteDetailDialog(AIRoute route) {
        if (!isAdded()) return;

        // Nếu đang có dialog khác -> đóng để tránh chồng
        if (routeDialog != null && routeDialog.isShowing()) {
            routeDialog.dismiss();
            routeDialog = null;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_route_detail, null, false);

        TextView tvRouteTitle = dialogView.findViewById(R.id.tvRouteTitle);
        TextView tvRouteSummary = dialogView.findViewById(R.id.tvRouteSummary);
        RecyclerView rvRouteStops = dialogView.findViewById(R.id.rvRouteStops);
        Button btnCloseRoute = dialogView.findViewById(R.id.btnCloseRoute);

        tvRouteTitle.setText(route.getTitle());

        String summary = String.format(
                java.util.Locale.getDefault(),
                "Total: %.2f km • %s",
                route.getDistanceKm(),
                route.getDuration()
        );
        tvRouteSummary.setText(summary);

        rvRouteStops.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        );

        // ✅ NEW: click stop -> hide dialog -> open place detail -> onResume show lại
        rvRouteStops.setAdapter(new RouteStopAdapter(route.getStops(), place -> {
            if (!isAdded()) return;

            if (place == null || place.getId() == null || place.getId().trim().isEmpty()) {
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

        // giữ instance dialog để show lại sau
        this.routeDialog = dialog;
        this.lastOpenedRoute = route;

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Close = đóng thật, không show lại nữa
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

        // Giữ lại logic reload khi resume từ nhánh bookmark-fix
        if (userLat != 0 && userLng != 0) {
            setupPlaceData();
        }

        // ✅ NEW: quay lại từ PlaceDetail thì show lại popup route
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
        // ✅ tránh leak window
        if (routeDialog != null) {
            routeDialog.dismiss();
            routeDialog = null;
        }
        super.onDestroyView();
    }
}
