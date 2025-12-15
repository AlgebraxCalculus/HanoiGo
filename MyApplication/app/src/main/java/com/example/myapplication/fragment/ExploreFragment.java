package com.example.myapplication.fragment;

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
import androidx.appcompat.app.AlertDialog;
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
import java.util.Locale;

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

    // ====== PHẦN UI & DATA CHO AI ROUTE ======
    private EditText edtTravelDate, edtDurationDays, edtBudget;
    private CheckBox cbFood, cbCulture, cbEntertainment, cbIconic;
    private Button btnSuggestRoute;
    private ProgressBar progressBarAiRoute;
    private RecyclerView rvSuggestedRoutes;

    private AiRouteAdapter aiRouteAdapter;
    private AiRouteApi aiRouteApi;

    double userLat = 0, userLng = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        setupBottomSheet(view);
        // Nếu bạn muốn khi có location mới rồi mới load thì gọi setupPlaceData() ở updateUserLocation
        // setupPlaceData();
        setupSuggestedRoutes(view);

        return view;
    }

    private void setupBottomSheet(View view) {
        View bottomSheet = view.findViewById(R.id.bottomSheetExplore);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
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
        // Khi đã có location thật, bạn có thể mở comment dòng dưới để load list địa điểm:
        // setupPlaceData();
    }

    // ================== LOAD CÁC LIST PLACE ===============

    private void setupPlaceData() {
        listIconic = new ArrayList<>();
        listTopVisited = new ArrayList<>();
        listPopularNearU = new ArrayList<>();

        //setUp listIconic
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
                                            "fetch location list failed: " + errorMessage,
                                            Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                });

        //setUp listTopVisited
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
                                            "fetch location list failed: " + errorMessage,
                                            Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                });

        //setUp listPopularNearU
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
                                            "fetch location list failed: " + errorMessage,
                                            Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                });
    }

    // ================== PHẦN AI SUGGEST ROUTE ===============

    private void setupSuggestedRoutes(View view) {
        // bind view
        edtTravelDate = view.findViewById(R.id.edtTravelDate);
        edtDurationDays = view.findViewById(R.id.edtDurationDays);
        edtBudget = view.findViewById(R.id.edtBudget);

        cbFood = view.findViewById(R.id.cbFood);
        cbCulture = view.findViewById(R.id.cbCulture);
        cbEntertainment = view.findViewById(R.id.cbEntertainment);
        cbIconic = view.findViewById(R.id.cbIconic);

        btnSuggestRoute = view.findViewById(R.id.btnSuggestRoute);
        progressBarAiRoute = view.findViewById(R.id.progressBarAiRoute);
        rvSuggestedRoutes = view.findViewById(R.id.rvSuggestedRoutes);

        rvSuggestedRoutes.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));

        aiRouteAdapter = new AiRouteAdapter(route -> {
            // Khi user ấn vào card -> mở popup chi tiết route (có hình từng điểm dừng)
            showRouteDetailsDialog(route);
        });
        rvSuggestedRoutes.setAdapter(aiRouteAdapter);

        aiRouteApi = new AiRouteApi();

        // Optional: set sẵn default để test nhanh
        edtTravelDate.setText("2025-12-25");
        edtDurationDays.setText("1");
        cbFood.setChecked(true);
        cbCulture.setChecked(true);

        btnSuggestRoute.setOnClickListener(v -> requestAiRoutes());
    }

    private void requestAiRoutes() {
        String date = edtTravelDate.getText().toString().trim();
        String durationStr = edtDurationDays.getText().toString().trim();
        String budgetStr = edtBudget.getText().toString().trim();

        if (TextUtils.isEmpty(date) || TextUtils.isEmpty(durationStr)) {
            Toast.makeText(requireContext(),
                    "Vui lòng nhập ngày đi và số ngày",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int durationDays;
        try {
            durationDays = Integer.parseInt(durationStr);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(),
                    "Số ngày không hợp lệ",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> interests = new ArrayList<>();
        if (cbFood.isChecked()) interests.add("ẩm thực");
        if (cbCulture.isChecked()) interests.add("văn hóa");
        if (cbEntertainment.isChecked()) interests.add("giải trí");
        if (cbIconic.isChecked()) interests.add("iconic");

        Long budget = null;
        if (!TextUtils.isEmpty(budgetStr)) {
            try {
                budget = Long.parseLong(budgetStr);
            } catch (NumberFormatException ignored) {
            }
        }

        TravelPlan plan = new TravelPlan(date, durationDays, interests, budget);

        // Nếu /api/ai/routes đang permitAll thì cho null
        String bearerToken = getAuthTokenOrNull();

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
                                "Không tìm thấy lộ trình phù hợp",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(Throwable t) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    progressBarAiRoute.setVisibility(View.GONE);
                    Toast.makeText(requireContext(),
                            "Lỗi AI Route: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private String getAuthTokenOrNull() {
        // Hiện tại endpoint /api/ai/routes bạn đang để public (permitAll) -> return null
        return null;

        // Nếu sau này yêu cầu JWT thì đọc token từ SharedPreferences:
        /*
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("auth", Context.MODE_PRIVATE);
        String raw = prefs.getString("access_token", null);
        return raw != null ? "Bearer " + raw : null;
        */
    }

    private void showRouteDetailsDialog(AIRoute route) {
        if (!isAdded()) return;

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_route_details, null);

        TextView tvRouteTitle = dialogView.findViewById(R.id.tvRouteTitle);
        TextView tvRouteSummary = dialogView.findViewById(R.id.tvRouteSummary);
        RecyclerView rvRouteStops = dialogView.findViewById(R.id.rvRouteStops);
        Button btnClose = dialogView.findViewById(R.id.btnCloseRoute);

        tvRouteTitle.setText(route.getTitle());

        String summary = String.format(
                Locale.getDefault(),
                "Tổng: %.2f km · %s",
                route.getDistanceKm(),
                route.getDuration()
        );
        tvRouteSummary.setText(summary);

        rvRouteStops.setLayoutManager(new LinearLayoutManager(requireContext()));
        RouteStopAdapter stopAdapter = new RouteStopAdapter(route.getStops());
        rvRouteStops.setAdapter(stopAdapter);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());
    }

    private void openPlaceDetail(Place place) {
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof MapFragment) {
            ((MapFragment) parentFragment).openPlaceDetailFragment(place);
        }
    }
}
