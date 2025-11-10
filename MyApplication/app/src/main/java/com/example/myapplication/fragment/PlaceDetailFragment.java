package com.example.myapplication.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.adapter.ImageAdapter;
import com.example.myapplication.adapter.ReviewAdapter;
import com.example.myapplication.api.LocationApi;
import com.example.myapplication.model.Place;
import com.example.myapplication.model.Review;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PlaceDetailFragment extends Fragment {

    private CoordinatorLayout placeDetailContainer;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private TextView placeTitle, placeAddress, overallDescription, locationText;
    private RatingBar ratingBar;
    private MaterialButton btnDirections, btnSave, btnWriteReview;
    private EditText searchBar;
    private ImageView actionButton, btnClose;
    private LinearLayout tagContainer;

    private Place placeData;
    private RecyclerView rvPlacePhotos;
    private RecyclerView rvReviews;

    public PlaceDetailFragment() {}

    public static PlaceDetailFragment newInstance(Place place) {
        PlaceDetailFragment fragment = new PlaceDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("placeData", place);
        fragment.setArguments(args);
        return fragment;
    }
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_place_detail, container, false);

        // --- Ánh xạ views ---
        rvPlacePhotos = view.findViewById(R.id.rvPlacePhotos);
        rvReviews = view.findViewById(R.id.rvReviews);
        placeDetailContainer = view.findViewById(R.id.placeDetailContainer);
        placeTitle = view.findViewById(R.id.placeTitle);
        ratingBar = view.findViewById(R.id.ratingBar);
        placeAddress = view.findViewById(R.id.placeAddress);
        overallDescription = view.findViewById(R.id.overallDescription);
        locationText = view.findViewById(R.id.locationText);
        btnDirections = view.findViewById(R.id.btnDirections);
        btnSave = view.findViewById(R.id.btnSave);
        btnWriteReview = view.findViewById(R.id.btnWriteReview);
        searchBar = view.findViewById(R.id.searchBar);
        actionButton = view.findViewById(R.id.actionButton);
        btnClose = view.findViewById(R.id.btnCloseExplore);
        tagContainer = view.findViewById(R.id.tagContainer);

        // --- Thiết lập dữ liệu tĩnh rating
        View ratingBar5 = view.findViewById(R.id.ratingBar5);
        TextView star5 = ratingBar5.findViewById(R.id.starNumber);
        star5.setText("5");

        View ratingBar4 = view.findViewById(R.id.ratingBar4);
        TextView star4 = ratingBar4.findViewById(R.id.starNumber);
        star4.setText("4");

        View ratingBar3 = view.findViewById(R.id.ratingBar3);
        TextView star3 = ratingBar3.findViewById(R.id.starNumber);
        star3.setText("3");

        View ratingBar2 = view.findViewById(R.id.ratingBar2);
        TextView star2 = ratingBar2.findViewById(R.id.starNumber);
        star2.setText("2");

        View ratingBar1 = view.findViewById(R.id.ratingBar1);
        TextView star1 = ratingBar1.findViewById(R.id.starNumber);
        star1.setText("1");

        // --- Bottom sheet setup ---
        View scrollView = view.findViewById(R.id.bottomSheetPlaceDetail);
        bottomSheetBehavior = BottomSheetBehavior.from(scrollView);
        bottomSheetBehavior.setPeekHeight(900);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        btnClose.setOnClickListener(v -> closeFragment());
        setupActions();
        setupReviews();

        // --- Lấy dữ liệu place từ bundle ---
        if (getArguments() != null) {
            placeData = (Place) getArguments().getSerializable("placeData");
        }

        fetchPlaceDetail(placeData.getAddress());

        return view;
    }
    // Gọi API chi tiết địa điểm
    private void fetchPlaceDetail(String address) {
        LocationApi.GetLocationByDetail(address, requireContext(), new LocationApi.LocationDetailCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                requireActivity().runOnUiThread(() -> {
                    try {
                        // Đọc và log tọa độ
                        String latStr = result.optString("latitude", "0");
                        String lngStr = result.optString("longitude", "0");
                        System.out.println("fetchPlaceDetail() → lat=" + latStr + ", lng=" + lngStr);
                        double lat = 0, lng = 0;
                        try {
                            lat = Double.parseDouble(latStr);
                            lng = Double.parseDouble(lngStr);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }

                        if (lat != 0 && lng != 0) {
                            Fragment parent = getParentFragment();
                            if (parent instanceof MapFragment) {
                                ((MapFragment) parent).showMarker(lat, lng, result.optString("name", "Unknown Place"), true);
                            }
                        }
                        placeTitle.setText(result.optString("name", "Không có tên"));
                        placeAddress.setText(result.optString("address", "Không có địa chỉ"));
                        List<String> imageUrls = new ArrayList<>();
                        String defaultPic = result.optString("defaultPicture", "");
                        if (!TextUtils.isEmpty(defaultPic)) imageUrls.add(defaultPic);

                        if (result.has("images")) {
                            JSONArray images = result.getJSONArray("images");
                            for (int i = 0; i < images.length(); i++) imageUrls.add(images.getString(i));
                        }

                        if (!imageUrls.isEmpty()) setupPlacePhotos(imageUrls);

                        // Overall description và location text
                        overallDescription.setText(result.optString("description", "Chưa có mô tả"));
                        locationText.setText(result.optString("address", "Chưa có thông tin vị trí"));

                    } catch (JSONException e) {
                        Toast.makeText(getContext(), "Lỗi đọc dữ liệu chi tiết", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Lỗi khi tải chi tiết: " + errorMessage, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    // Setup scroll photo
    private void setupPlacePhotos(List<String> imageUrls) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getContext(), LinearLayoutManager.HORIZONTAL, false
        );
        rvPlacePhotos.setLayoutManager(layoutManager);
        rvPlacePhotos.setAdapter(new ImageAdapter(requireContext(), imageUrls));
    }
    private void setupReviews() {
        List<Review> reviews = List.of(
                new Review("Minh Đỗ", "Local Guide • 24 reviews",
                        "Quán có không gian đẹp, đồ uống ổn, phục vụ nhiệt tình. Mình thích nhất là phần trang trí và nhạc nhẹ nhàng.",
                        "2 days ago", 4, 32,
                        new int[]{R.drawable.review_sample_img1, R.drawable.review_sample_img2, R.drawable.review_sample_img3}),
                new Review("Anh Phạm", "Traveler",
                        "Rất hài lòng, đồ ăn ngon, chỗ ngồi thoải mái. Giá hơi cao nhưng xứng đáng.",
                        "1 week ago", 5, 21,
                        new int[]{R.drawable.review_sample_img4}),
                new Review("Hà Lê", "Food Blogger",
                        "Không gian hơi ồn, nhưng đồ uống ngon, nhân viên thân thiện.",
                        "3 weeks ago", 3, 10,
                        new int[]{})
        );

        rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        rvReviews.setAdapter(new ReviewAdapter(requireContext(), reviews));
    }

    private void closeFragment() {
        Fragment parent = getParentFragment();
        if (parent instanceof MapFragment) {
            ((MapFragment) parent).onPlaceDetailClosed();
            parent.getChildFragmentManager().popBackStack();  // quay lại ExploreFragment
        }
    }

    private void setupActions() {
        btnDirections.setOnClickListener(v ->
                Toast.makeText(getContext(), "Opening directions...", Toast.LENGTH_SHORT).show());

        btnSave.setOnClickListener(v ->
                Toast.makeText(getContext(), "Saved place", Toast.LENGTH_SHORT).show());

        btnWriteReview.setOnClickListener(v ->
                Toast.makeText(getContext(), "Write your review", Toast.LENGTH_SHORT).show());

        actionButton.setOnClickListener(v -> {
            String query = searchBar.getText().toString().trim();
            if (TextUtils.isEmpty(query)) {
                Toast.makeText(getContext(), "Enter text to search", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Searching reviews for: " + query, Toast.LENGTH_SHORT).show();
            }
        });

        // Gắn sự kiện cho tag filter
        for (int i = 0; i < tagContainer.getChildCount(); i++) {
            View child = tagContainer.getChildAt(i);
            if (child instanceof TextView) {
                child.setOnClickListener(v -> {
                    String tagText = ((TextView) v).getText().toString();
                    Toast.makeText(getContext(), "Filter: " + tagText, Toast.LENGTH_SHORT).show();
                });
            }
        }
    }
}
