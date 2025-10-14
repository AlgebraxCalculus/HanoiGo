package com.example.myapplication.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
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

import com.example.myapplication.R;
import com.example.myapplication.adapter.ImageAdapter;
import com.example.myapplication.adapter.ReviewAdapter;
import com.example.myapplication.model.Place;
import com.example.myapplication.model.Review;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class PlaceDetailFragment extends Fragment {

    private CoordinatorLayout placeDetailContainer;
    private BottomSheetBehavior<View> bottomSheetBehavior;

    private TextView placeTitle, tvRatingNumber, tvRatingMeta, placeAddress, placeTime;
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
        //Đóng gói dữ liệu place vào bundle và truyền vào fragment
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

        // Ánh xạ RecyclerView
        rvPlacePhotos = view.findViewById(R.id.rvPlacePhotos);

        // Lấy dữ liệu từ bundle
        if (getArguments() != null) {
            placeData = (Place) getArguments().getSerializable("placeData");
        }

        // Setup ảnh
        if (placeData != null && placeData.getImageUrls() != null && !placeData.getImageUrls().isEmpty()) {
            setupPlacePhotos(placeData.getImageUrls());
        }

        // Ánh xạ các view khác
        placeDetailContainer = view.findViewById(R.id.placeDetailContainer);
        placeTitle = view.findViewById(R.id.placeTitle);
        tvRatingNumber = view.findViewById(R.id.tvRatingNumber);
        tvRatingMeta = view.findViewById(R.id.tvRatingMeta);
        ratingBar = view.findViewById(R.id.ratingBar);
        placeAddress = view.findViewById(R.id.placeAddress);
        placeTime = view.findViewById(R.id.placeTime);
        btnDirections = view.findViewById(R.id.btnDirections);
        btnSave = view.findViewById(R.id.btnSave);
        btnWriteReview = view.findViewById(R.id.btnWriteReview);
        searchBar = view.findViewById(R.id.searchBar);
        actionButton = view.findViewById(R.id.actionButton);
        btnClose = view.findViewById(R.id.btnCloseExplore);
        tagContainer = view.findViewById(R.id.tagContainer);


        // Thiết lập bottom sheet
        View scrollView = view.findViewById(R.id.bottomSheetPlaceDetail);
        bottomSheetBehavior = BottomSheetBehavior.from(scrollView);
        bottomSheetBehavior.setPeekHeight(900);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        // Gắn sự kiện
        btnClose.setOnClickListener(v -> closeFragment());
        setupActions();

        rvReviews = view.findViewById(R.id.rvReviews);
        setupReviews();

        return view;
    }

    private void setupReviews() {
        List<Review> reviews = List.of(
                new Review(
                        "Minh Đỗ",
                        "Local Guide • 24 reviews",
                        "Quán có không gian đẹp, đồ uống ổn, phục vụ nhiệt tình. Mình thích nhất là phần trang trí và nhạc nhẹ nhàng.",
                        "2 days ago",
                        4.5f,
                        32,
                        new int[]{R.drawable.review_sample_img1, R.drawable.review_sample_img2, R.drawable.review_sample_img3}
                ),
                new Review(
                        "Anh Phạm",
                        "Traveler",
                        "Rất hài lòng, đồ ăn ngon, chỗ ngồi thoải mái. Giá hơi cao nhưng xứng đáng.",
                        "1 week ago",
                        5f,
                        21,
                        new int[]{R.drawable.review_sample_img4}
                ),
                new Review(
                        "Hà Lê",
                        "Food Blogger",
                        "Không gian hơi ồn, nhưng đồ uống ngon, nhân viên thân thiện.",
                        "3 weeks ago",
                        3.5f,
                        10,
                        new int[]{}
                )
        );

        rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        ReviewAdapter reviewAdapter = new ReviewAdapter(requireContext(), reviews);
        rvReviews.setAdapter(reviewAdapter);
    }

    private void setupPlacePhotos(List<String> imageUrls) {
        // 1. Tạo LayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        );
        rvPlacePhotos.setLayoutManager(layoutManager);

        // 2. Tạo Adapter và gán
        ImageAdapter adapter = new ImageAdapter(requireContext(), imageUrls);
        rvPlacePhotos.setAdapter(adapter);
    }

    private void closeFragment() {
        getParentFragmentManager().popBackStack();
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
