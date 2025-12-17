package com.example.myapplication.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.colormoon.readmoretextview.ReadMoreTextView;
import com.example.myapplication.R;
import com.example.myapplication.adapter.BookmarkListSelectorAdapter;
import com.example.myapplication.adapter.ImageAdapter;
import com.example.myapplication.adapter.ImagePreviewAdapter;
import com.example.myapplication.adapter.ReviewAdapter;
import com.example.myapplication.api.BookmarkApi;
import com.example.myapplication.api.CheckpointApi;
import com.example.myapplication.api.CloudinaryUploadHelper;
import com.example.myapplication.api.LocationApi;
import com.example.myapplication.api.ReviewApi;
import com.example.myapplication.api.UserApi;
import com.example.myapplication.model.Place;
import com.example.myapplication.model.Review;
import com.example.myapplication.model.SavedList;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlaceDetailFragment extends Fragment implements ReviewAdapter.OnMyReviewLikedListener {
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private TextView placeTitle, placeAddress, overallDescription, locationText, tvYourName, tvYourTime, tvYourLikeCount, tvOverallRating, tvOverallRatingCount, tvRatingNumber, tvReviewCount;
    private TextView currentSelectedTag = null;
    private RatingBar ratingBar, yourRatingBar, rtOverallRatingBar;
    private MaterialButton btnDirections, btnSave, btnWriteReview, btnCheckin, btnUpdateReview, btnDeleteReview;
    private EditText searchBar;
    private ImageView btnClose, imgYourAvatar, btnYourLike;
    private LinearLayout tagContainer, yourReviewLayout;
    private View layoutNoReview, ratingBar1, ratingBar2, ratingBar3, ratingBar4, ratingBar5;
    private ReadMoreTextView tvYourContent;
    private GridLayout yourImageGrid;

    private ArrayList<JSONObject> availableCheckpoints = new ArrayList<>();
    private ArrayList<JSONObject> userCheckedInCheckpoints = new ArrayList<>();
    private List<Review> reviewList, LikedReviewList;
    private Place placeData;
    private RecyclerView rvPlacePhotos;
    private RecyclerView rvReviews;

    private String username = "default";
    private String avatar = "";
    private String jwtToken = "";

    double userLat = 0, userLng = 0;

    private double placeLat = 0;
    private double placeLng = 0;

    private String dialogType = "";

    // Khai báo constants và variables cho chức năng add review
    private static final int PICK_IMAGES_REQUEST = 100;
    private static final int MAX_IMAGES = 4;
    private List<Uri> selectedImageUris = new ArrayList<>();
    private android.app.AlertDialog currentReviewDialog;
    private String tempReviewContent = "";
    private float tempReviewRating = 0f;
    private float overallRating = 0f;
    private int maxRate = 0;

    public PlaceDetailFragment() {}

    public static PlaceDetailFragment newInstance(Place place, String jwtToken, String username, String avatar) {
        PlaceDetailFragment fragment = new PlaceDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("placeData", place);
        args.putString("jwtToken", jwtToken);
        args.putString("username", username);
        args.putString("avatar", avatar);
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

        rvPlacePhotos = view.findViewById(R.id.rvPlacePhotos);
        rvReviews = view.findViewById(R.id.rvReviews);
        placeTitle = view.findViewById(R.id.placeTitle);
        ratingBar = view.findViewById(R.id.ratingBar);
        tvRatingNumber = view.findViewById(R.id.tvRatingNumber);
        tvReviewCount = view.findViewById(R.id.tvReviewCount);
        placeAddress = view.findViewById(R.id.placeAddress);
        overallDescription = view.findViewById(R.id.overallDescription);
        locationText = view.findViewById(R.id.locationText);
        btnCheckin = view.findViewById(R.id.btnCheckin);
        btnDirections = view.findViewById(R.id.btnDirections);
        btnSave = view.findViewById(R.id.btnSave);
        btnWriteReview = view.findViewById(R.id.btnWriteReview);
        searchBar = view.findViewById(R.id.searchBar);
        btnClose = view.findViewById(R.id.btnCloseExplore);
        tagContainer = view.findViewById(R.id.tagContainer);
        layoutNoReview = view.findViewById(R.id.layoutNoReview);
        yourReviewLayout = view.findViewById(R.id.yourReview);
        btnUpdateReview = view.findViewById(R.id.btnUpdateReview);
        btnDeleteReview = view.findViewById(R.id.btnDeleteReview);

        tvOverallRating = view.findViewById(R.id.tvOverallRating);
        rtOverallRatingBar = view.findViewById(R.id.rtOverallRatingBar);
        tvOverallRatingCount = view.findViewById(R.id.tvOverallRatingCount);


        imgYourAvatar = view.findViewById(R.id.imgYourAvatar);
        tvYourName = view.findViewById(R.id.tvYourName);
        tvYourTime = view.findViewById(R.id.tvYourTime);
        tvYourLikeCount = view.findViewById(R.id.tvYourLikeCount);
        btnYourLike = view.findViewById(R.id.btnYourLike);
        tvYourContent = view.findViewById(R.id.tvYourContent);
        yourRatingBar = view.findViewById(R.id.yourRatingBar);
        yourImageGrid = view.findViewById(R.id.yourImageGrid);

        ratingBar5 = view.findViewById(R.id.ratingBar5);
        TextView star5 = ratingBar5.findViewById(R.id.starNumber);
        star5.setText("5");

        ratingBar4 = view.findViewById(R.id.ratingBar4);
        TextView star4 = ratingBar4.findViewById(R.id.starNumber);
        star4.setText("4");

        ratingBar3 = view.findViewById(R.id.ratingBar3);
        TextView star3 = ratingBar3.findViewById(R.id.starNumber);
        star3.setText("3");

        ratingBar2 = view.findViewById(R.id.ratingBar2);
        TextView star2 = ratingBar2.findViewById(R.id.starNumber);
        star2.setText("2");

        ratingBar1 = view.findViewById(R.id.ratingBar1);
        TextView star1 = ratingBar1.findViewById(R.id.starNumber);
        star1.setText("1");

        // AppBarLayout
        AppBarLayout header = view.findViewById(R.id.placeHeader);
        placeTitle = view.findViewById(R.id.placeTitle);
        btnClose = view.findViewById(R.id.btnCloseExplore);

        View bottomSheet = view.findViewById(R.id.bottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        // Setup bottom sheet kiểu Google Maps
        bottomSheetBehavior.setFitToContents(false);
        bottomSheetBehavior.setExpandedOffset(120);
        bottomSheetBehavior.setHalfExpandedRatio(0.45f);
        bottomSheetBehavior.setPeekHeight(550);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setHideable(false);

        btnClose.setOnClickListener(v -> closeFragment());
        setupActions();

        if (getArguments() != null) {
            placeData = (Place) getArguments().getSerializable("placeData");
            jwtToken = getArguments().getString("jwtToken");
            username = getArguments().getString("username");
            avatar = getArguments().getString("avatar");
        }

        updateSaveButtonState(false);
        checkIfLocationIsSaved();

        // Fetch available checkpoints for check-in
        CheckpointApi.GetEnableCheckIn(
                userLat,
                userLng,
                jwtToken,
                requireContext(),
                new CheckpointApi.CheckpointApiCallback() {
                    @Override
                    public void onSuccess(ArrayList<JSONObject> list) {
                        availableCheckpoints = list;
                        System.out.println("AvailableCheckpoint: " + availableCheckpoints);
                        requireActivity().runOnUiThread(() -> updateCheckinUI());
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        availableCheckpoints = new ArrayList<>();
                        System.out.println("AvailableCheckpoint: [] (API failed: " + errorMessage + ")");
                        requireActivity().runOnUiThread(() -> updateCheckinUI());
                    }
                }
        );

        // Fetch user's checked-in list
        UserApi.GetMyCheckpointList(
                jwtToken,
                "",
                "newest",
                "",
                requireContext(),
                new UserApi.UserApiCallback() {

                    @Override
                    public void onSuccess(ArrayList<JSONObject> dataList) {
                        userCheckedInCheckpoints = (dataList != null)
                                ? dataList
                                : new ArrayList<>();

                        System.out.println("UserCheckpoint: " + userCheckedInCheckpoints);
                        requireActivity().runOnUiThread(() -> updateCheckinUI());
                    }

                    @Override
                    public void onSuccess(JSONObject userObj) {
                        // Not used
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        userCheckedInCheckpoints = new ArrayList<>();
                        System.out.println("UserCheckpoint: [] (API failed: " + errorMessage + ")");
                        requireActivity().runOnUiThread(() -> updateCheckinUI());
                    }
                }
        );
        setupReviews(placeData.getAddress(), "most approved", this::updateReviewUI);
        fetchPlaceDetail(placeData.getAddress());

        return view;
    }

    private Review getYourReview() {
        if (reviewList == null) return null;
        for (Review a : reviewList) {
            if (a.getName().equalsIgnoreCase(username)) return a;
        }
        return null;
    }

    private void setupReviewsSummary() {
        overallRating = 0f;
        int[] starCounts = new int[5];
        maxRate = 0;
        int overallRatingCount = reviewList.size();
        for(Review r:reviewList){
            overallRating += r.getRating();
            starCounts[r.getRating() - 1]++;
            maxRate = Math.max(maxRate, starCounts[r.getRating() - 1]);
        }
        if(overallRatingCount > 0) overallRating /= overallRatingCount;

        requireActivity().runOnUiThread(() -> {
            tvOverallRating.setText(String.format(Locale.US, "%.1f", overallRating));
            tvRatingNumber.setText(String.format(Locale.US, "%.1f", overallRating));
            rtOverallRatingBar.setRating(overallRating);
            ratingBar.setRating(overallRating);
            NumberFormat nf = NumberFormat.getInstance(Locale.GERMANY);
            String formattedCount = nf.format(overallRatingCount); // Kết quả: "11.291"
            tvOverallRatingCount.setText("(" + formattedCount + ")");
            tvReviewCount.setText("(" + formattedCount + ")");

            if(maxRate > 0){
                ProgressBar progressBar;
                for(int i=0;i<starCounts.length;i++){
                    switch (i){
                        case 0:
                            progressBar = ratingBar1.findViewById(R.id.progressBar);
                            progressBar.setProgress((int) (starCounts[i]/(float)maxRate * 100f));
                            break;
                        case 1:
                            progressBar = ratingBar2.findViewById(R.id.progressBar);
                            progressBar.setProgress((int) (starCounts[i]/(float)maxRate * 100f));
                            break;
                        case 2:
                            progressBar = ratingBar3.findViewById(R.id.progressBar);
                            progressBar.setProgress((int) (starCounts[i]/(float)maxRate * 100f));
                            break;
                        case 3:
                            progressBar = ratingBar4.findViewById(R.id.progressBar);
                            progressBar.setProgress((int) (starCounts[i]/(float)maxRate * 100f));
                            break;
                        case 4:
                            progressBar = ratingBar5.findViewById(R.id.progressBar);
                            progressBar.setProgress((int) (starCounts[i]/(float)maxRate * 100f));
                            break;
                    }
                }
            }else{
                ProgressBar progressBar;
                progressBar = ratingBar1.findViewById(R.id.progressBar);
                progressBar.setProgress(0);
                progressBar = ratingBar2.findViewById(R.id.progressBar);
                progressBar.setProgress(0);
                progressBar = ratingBar3.findViewById(R.id.progressBar);
                progressBar.setProgress(0);
                progressBar = ratingBar4.findViewById(R.id.progressBar);
                progressBar.setProgress(0);
                progressBar = ratingBar5.findViewById(R.id.progressBar);
                progressBar.setProgress(0);
            }
        });
    }

    @Override
    public void bindMyReviewData(Review review) {
        Context context = getContext();
        if (context == null || review == null) return;

        tvYourName.setText(review.getName());
        tvYourTime.setText(review.getTime());
        tvYourLikeCount.setText(String.valueOf(review.getLikeCount()));
        yourRatingBar.setRating(review.getRating());
        tvYourContent.setText(review.getContent());
        tvYourContent.setCollapsedText("More");
        tvYourContent.setExpandedText("Less");
        tvYourContent.setCollapsedTextColor(context.getColor(R.color.blue));
        tvYourContent.setExpandedTextColor(context.getColor(R.color.blue));
        tvYourContent.setTrimLines(2);

        tempReviewContent = review.getContent();
        tempReviewRating = review.getRating();

        if(!review.getIsLiked()){
            btnYourLike.setImageResource(R.drawable.ic_thumb_up);
        }else{
            btnYourLike.setImageResource(R.drawable.ic_liked_thumb_up);
        }

        yourImageGrid.removeAllViews();
        List<String> imageUrls = review.getImageUrls();

        if (imageUrls != null && !imageUrls.isEmpty()) {
            yourImageGrid.setVisibility(View.VISIBLE);
            int maxImages = Math.min(imageUrls.size(), 4);
            float density = context.getResources().getDisplayMetrics().density;
            int imageWidth = (int) (150 * density);   // 150dp
            int imageHeight = (int) (100 * density);  // 100dp
            int margin = (int) (8 * density); // 8dp margin

            for (int i = 0; i < maxImages; i++) {
                ImageView img = new ImageView(context);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();

                params.width = imageWidth;
                params.height = imageHeight;
                params.setMargins(margin, margin, margin, margin);

                img.setLayoutParams(params);
                img.setScaleType(ImageView.ScaleType.CENTER_CROP);

                // Tải ảnh từ URL dùng Glide
                Glide.with(context)
                        .load(imageUrls.get(i))
                        .into(img);
                img.setBackgroundResource(R.drawable.bg_rounded_image);
                img.setClipToOutline(true);

                yourImageGrid.addView(img);
            }
        } else {
            yourImageGrid.setVisibility(View.GONE);
        }

        if (avatar != null && !avatar.isEmpty()) {
            Glide.with(context)
                    .load(avatar)
                    .into(imgYourAvatar);
        }
    }

    public void updateUserLocation(double lat, double lng) {
        this.userLat = lat;
        this.userLng = lng;
    }

    private boolean isPlaceAvailableForCheckin(String address, ArrayList<JSONObject> checkpointList) {
        if (address == null || checkpointList == null) return false;
        for (JSONObject checkpoint : checkpointList) {
            JSONObject location = checkpoint.optJSONObject("locationResponse");
            if (location != null) {
                String checkpointAddress = location.optString("address", "");
                if (checkpointAddress.equalsIgnoreCase(address)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isPlaceCheckedInByUser(String address, ArrayList<JSONObject> checkedInList) {
        if (address == null || checkedInList == null) return false;
        for (JSONObject checkpoint : checkedInList) {
            JSONObject location = checkpoint.optJSONObject("location");
            if (location != null) {
                String checkpointAddress = location.optString("address", "");
                if (checkpointAddress.equalsIgnoreCase(address)) {
                    return true;
                }
            }
        }
        return false;
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
                        try {
                            placeLat = Double.parseDouble(latStr);
                            placeLng = Double.parseDouble(lngStr);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }

                        if (placeLat != 0 && placeLng != 0) {
                            Fragment parent = getParentFragment();
                            if (parent instanceof MapFragment) {
                                ((MapFragment) parent).showLocationMarker(placeLat, placeLng, result, true);
                            }
                        }

                        placeTitle.setText(result.optString("name", "No name"));
                        placeAddress.setText(result.optString("address", "No address"));
                        List<String> imageUrls = new ArrayList<>();
                        String defaultPic = result.optString("defaultPicture", "");
                        if (!TextUtils.isEmpty(defaultPic)) imageUrls.add(defaultPic);

                        if (result.has("images")) {
                            JSONArray images = result.getJSONArray("images");
                            for (int i = 0; i < images.length(); i++) imageUrls.add(images.getString(i));
                        }

                        if (!imageUrls.isEmpty()) setupPlacePhotos(imageUrls);

                        overallDescription.setText(result.optString("description", "No description"));
                        locationText.setText(result.optString("address", "No location info"));

                    } catch (JSONException e) {
                        Toast.makeText(getContext(), "Error reading detail data", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error loading details: " + errorMessage, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
    private void updateReviewUI() {
        boolean isCheckedIn = isPlaceCheckedInByUser(placeData.getAddress(), userCheckedInCheckpoints);
        requireActivity().runOnUiThread(() -> {
            if (isCheckedIn) {
                Review myReview = getYourReview();
                if (myReview != null) {
                    yourReviewLayout.setVisibility(View.VISIBLE);
                    btnWriteReview.setVisibility(View.GONE);
                    bindMyReviewData(myReview);
                } else {
                    yourReviewLayout.setVisibility(View.GONE);
                    btnWriteReview.setVisibility(View.VISIBLE);
                    btnWriteReview.setEnabled(true);
                    btnWriteReview.setBackgroundTintList(
                            ColorStateList.valueOf(Color.parseColor("#01B8B3"))
                    );
                }
            } else {
                btnWriteReview.setEnabled(false);
                btnWriteReview.setBackgroundTintList(
                        ColorStateList.valueOf(Color.parseColor("#808080"))
                );
                yourReviewLayout.setVisibility(View.GONE);
            }
        });
    }
    private void updateCheckinUI() {
        boolean isCheckedIn = isPlaceCheckedInByUser(placeData.getAddress(), userCheckedInCheckpoints);
        boolean isAvailableForCheckin = isPlaceAvailableForCheckin(placeData.getAddress(), availableCheckpoints);

        if (isCheckedIn) {
            btnCheckin.setVisibility(View.GONE);
            setDisabledBackground();
        } else if (isAvailableForCheckin) {
            btnCheckin.setVisibility(View.VISIBLE);
            setNormalBackground();
        } else {
            btnCheckin.setVisibility(View.GONE);
            setNormalBackground();
        }

        updateReviewUI();
    }

    private void setDisabledBackground() {
        View content = getView().findViewById(R.id.bottomSheetContent);
        content.setBackgroundResource(R.drawable.bg_gradient_disabled);
        View header = getView().findViewById(R.id.placeHeader);
        header.setBackgroundResource(R.drawable.bg_gradient_disabled_header);
    }

    private void setNormalBackground() {
        View content = getView().findViewById(R.id.bottomSheetContent);
        content.setBackgroundResource(R.drawable.bg_bottom_sheet);
        View header = getView().findViewById(R.id.placeHeader);
        header.setBackgroundResource(R.drawable.bg_bottom_sheet);
    }

    // Setup scroll photo
    private void setupPlacePhotos(List<String> imageUrls) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getContext(), LinearLayoutManager.HORIZONTAL, false
        );
        rvPlacePhotos.setLayoutManager(layoutManager);
        rvPlacePhotos.setAdapter(new ImageAdapter(requireContext(), imageUrls));
    }


    private void setupReviews(String address, String sortType, Runnable onComplete) {
        reviewList = new ArrayList<>();
        LikedReviewList = new ArrayList<>();
        System.out.println("address: "+ address);
        ReviewApi.GetLikedReviews(address, jwtToken, getContext(), new ReviewApi.ReviewApiCallback() {
            @Override
            public void onSuccess(ArrayList<JSONObject> data) {
                for(JSONObject a : data){
                    try {
                        // Lấy ra List pictureUrls
                        JSONArray pictureUrlsArray = a.getJSONArray("pictureUrl");
                        List<String> pictureUrlsList = new ArrayList<>();
                        for (int i = 0; i < pictureUrlsArray.length(); i++) {
                            pictureUrlsList.add(pictureUrlsArray.getString(i));
                        }

                        // Lấy ra relativeTime của review
                        String time = CheckpointsFragment.getRelativeTime(a.getString("createdAt"));

                        Review r = new Review(a.getJSONObject("userResponse").getString("username"), a.getString("content"), time, a.getInt("rating"), a.getInt("likeCount"), a.getJSONObject("userResponse").getString("profilePicture"), pictureUrlsList);
                        LikedReviewList.add(r);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
                System.out.println("LikedReviewList: "+ data);

                ReviewApi.GetReviewListByAddress(address, sortType, getContext(), new ReviewApi.ReviewApiCallback() {
                    @Override
                    public void onSuccess(ArrayList<JSONObject> data) {
                        for(JSONObject a : data){
                            try {
                                // Lấy ra List pictureUrls
                                JSONArray pictureUrlsArray = a.getJSONArray("pictureUrl");
                                List<String> pictureUrlsList = new ArrayList<>();
                                for (int i = 0; i < pictureUrlsArray.length(); i++) {
                                    pictureUrlsList.add(pictureUrlsArray.getString(i));
                                }

                                // Lấy ra relativeTime của review
                                String time = CheckpointsFragment.getRelativeTime(a.getString("createdAt"));

                                Review r = new Review(a.getJSONObject("userResponse").getString("username"), a.getString("content"), time, a.getInt("rating"), a.getInt("likeCount"), a.getJSONObject("userResponse").getString("profilePicture"), pictureUrlsList);
                                reviewList.add(r);
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                        }
                        System.out.println("listReview: "+ data);

                        for (Review review : reviewList) {
                            // Đây là cách đơn giản nhất: Lặp qua LikedReviewList
                            for (Review likedReview : LikedReviewList) {
                                if (review.getName().equalsIgnoreCase(likedReview.getName())) {
                                    review.setIsLiked(true);
                                    break;
                                }
                            }
                        }

                        setupReviewsSummary();

                        requireActivity().runOnUiThread(() -> {
                            if (reviewList == null || reviewList.isEmpty()) {
                                rvReviews.setVisibility(View.GONE);
                                layoutNoReview.setVisibility(View.VISIBLE);
                            } else {
                                rvReviews.setVisibility(View.VISIBLE);
                                layoutNoReview.setVisibility(View.GONE);
                            }
                            rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));
                            rvReviews.setAdapter(new ReviewAdapter(requireContext(), reviewList, address, jwtToken, username, PlaceDetailFragment.this));
                        });
                        onComplete.run();
                    }

                    @Override
                    public void onSuccess(String msg) {}

                    @Override
                    public void onFailure(String errorMessage) {
                        requireActivity().runOnUiThread(() -> {
                            rvReviews.setVisibility(View.GONE);
                            layoutNoReview.setVisibility(View.VISIBLE);
                            Toast.makeText(getContext(), "fetch review list failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                            onComplete.run();
                        });
                    }
                });
            }

            @Override
            public void onSuccess(String msg) {}

            @Override
            public void onFailure(String errorMessage) {
                requireActivity().runOnUiThread(() -> {
                    rvReviews.setVisibility(View.GONE);
                    layoutNoReview.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "fetch liked review list failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                    onComplete.run();
                });
            }
        });

    }

    private void closeFragment() {
        Fragment parent = getParentFragment();
        if (parent instanceof MapFragment) {
            ((MapFragment) parent).onPlaceDetailClosed();
            parent.getChildFragmentManager().popBackStack();  // quay lại ExploreFragment
        }
    }

    private void setupActions() {
        btnDirections.setOnClickListener(v -> {
            if (userLat == 0 || userLng == 0) {
                Toast.makeText(requireContext(), "Waiting for location...", Toast.LENGTH_SHORT).show();
                return;
            }

            if (placeLat == 0 || placeLng == 0) {
                Toast.makeText(requireContext(), "Place location not loaded yet", Toast.LENGTH_SHORT).show();
                return;
            }

            DirectionFragment directionFragment = new DirectionFragment();

            Bundle args = new Bundle();
            args.putDouble("destLat", placeLat);
            args.putDouble("destLng", placeLng);
            args.putString("destName", placeData.getName());
            args.putDouble("userLat", userLat);
            args.putDouble("userLng", userLng);
            directionFragment.setArguments(args);

            android.util.Log.d("PlaceDetailFragment", "Opening DirectionFragment:");
            android.util.Log.d("PlaceDetailFragment", "  destLat=" + placeLat + ", destLng=" + placeLng);
            android.util.Log.d("PlaceDetailFragment", "  userLat=" + userLat + ", userLng=" + userLng);

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.childFragmentContainer, directionFragment)
                    .addToBackStack(null)
                    .commit();
        });



        btnSave.setOnClickListener(v -> showBookmarkListDialog()); //nút save

        btnWriteReview.setOnClickListener(v -> {
                dialogType = "add";
                showWriteReviewDialog();
            }
        );

        btnUpdateReview.setOnClickListener(v -> {
                dialogType = "update";
                showWriteReviewDialog();
            }
        );

        btnDeleteReview.setOnClickListener(v -> {
                deleteReview();
            }
        );

        btnYourLike.setOnClickListener(v -> {
                Review review = getYourReview();
                boolean currentlyLiked = review.getIsLiked();

                // 🌟 GỌI API LIKE/UNLIKE
                ReviewApi.LikeReview(jwtToken, review.getName(), placeData.getAddress(), getContext(), new ReviewApi.ReviewApiCallback() {
                    @Override
                    public void onSuccess(ArrayList<JSONObject> data) {

                    }

                    @Override
                    public void onSuccess(String msg) {
                        requireActivity().runOnUiThread(() -> {
                            // Cập nhật trạng thái
                            review.setIsLiked(!currentlyLiked);
                            review.setLikeCount(review.getIsLiked() ? review.getLikeCount() + 1 : review.getLikeCount() - 1); // Cập nhật LikeCount trong Model

                            // Cập nhật UI
                            if (!currentlyLiked) {
                                btnYourLike.setImageResource(R.drawable.ic_liked_thumb_up);
                            } else {
                                btnYourLike.setImageResource(R.drawable.ic_thumb_up);
                            }
                            tvYourLikeCount.setText(String.valueOf(review.getLikeCount()));
                            btnYourLike.setEnabled(true); // Kích hoạt lại nút

                            setupReviews(placeData.getAddress(), currentSelectedTag.getText().toString().toLowerCase(), () -> {});
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        requireActivity().runOnUiThread(() -> {
                            // Quay lại trạng thái ban đầu nếu thất bại
                            Toast.makeText(getContext(), "Like your review failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                            btnYourLike.setEnabled(true); // Kích hoạt lại nút
                        });
                    }
                });
            }
        );

        // Hàm tìm kiếm trên thanh search
        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = searchBar.getText().toString().trim();

                // Ẩn bàn phím
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
                }

                filterReviews(query);
                return true;
            }
            return false;
        });

        btnCheckin.setOnClickListener(v -> {
            if (placeData == null || placeData.getAddress() == null) {
                Toast.makeText(getContext(), "No address available", Toast.LENGTH_SHORT).show();
                return;
            }
            if (jwtToken == null || jwtToken.isEmpty()) {
                Toast.makeText(getContext(), "You need to log in before checking in", Toast.LENGTH_SHORT).show();
                return;
            }

            CheckpointApi.CheckIn(jwtToken, placeData.getAddress(), getContext(), new CheckpointApi.CheckpointApiCallback() {
                @Override
                public void onSuccess(ArrayList<JSONObject> resultList) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Check-in successful!", Toast.LENGTH_SHORT).show();

                        // if resultList is not null
                        if (resultList != null && !resultList.isEmpty()) {
                            userCheckedInCheckpoints.addAll(resultList);
                            updateCheckinUI();
                        } else {
                            UserApi.GetMyCheckpointList(
                                    jwtToken,
                                    "",
                                    "newest",
                                    "",
                                    requireContext(),
                                    new UserApi.UserApiCallback() {
                                        @Override
                                        public void onSuccess(ArrayList<JSONObject> dataList) {
                                            userCheckedInCheckpoints = (dataList != null)
                                                    ? dataList
                                                    : new ArrayList<>();
                                            requireActivity().runOnUiThread(() -> updateCheckinUI());
                                        }
                                        @Override public void onSuccess(JSONObject userObj) {}
                                        @Override public void onFailure(String errorMessage) {
                                            requireActivity().runOnUiThread(() -> updateCheckinUI());
                                        }
                                    }
                            );
                        }
                    });
                }

                @Override
                public void onFailure(String errorMessage) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Check-in failed: " + errorMessage, Toast.LENGTH_LONG).show();
                    });
                }
            });
        });

        // Reviews filter action
        for (int i = 0; i < tagContainer.getChildCount(); i++) {
            View child = tagContainer.getChildAt(i);
            if (child instanceof TextView) {
                TextView tagView = (TextView) child;

                // Đặt tag đầu tiên là mặc định được chọn (Tùy chọn)
                if (i == 0) {
                    tagView.setBackgroundResource(R.drawable.bg_review_tag_selected);
                    tagView.setTextColor(Color.parseColor("#1D2E3D"));
                    currentSelectedTag = tagView;
                }

                tagView.setOnClickListener(v -> {
                    TextView clickedTag = (TextView) v;

                    if (clickedTag != currentSelectedTag) {
                        if (currentSelectedTag != null) {
                            currentSelectedTag.setBackgroundResource(R.drawable.bg_tag);
                            currentSelectedTag.setTextColor(Color.parseColor("#FFFFFF"));
                        }
                        clickedTag.setBackgroundResource(R.drawable.bg_review_tag_selected);
                        clickedTag.setTextColor(Color.parseColor("#1D2E3D"));
                        setupReviews(placeData.getAddress(), clickedTag.getText().toString().toLowerCase(), () -> {});
                        currentSelectedTag = clickedTag;
                    }

                    String tagText = clickedTag.getText().toString();
                    Toast.makeText(getContext(), "Filter: " + tagText, Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    private void filterReviews(String query) {
        List<Review> filtered = new ArrayList<>();
        System.out.println("query: "+query);
        for (Review a : reviewList) {
            if (a.getContent().toLowerCase().contains(query.toLowerCase())) {
                System.out.println("review founded: "+a.getContent());
                filtered.add(a);
            }
        }
        requireActivity().runOnUiThread(() -> {
            if (filtered.isEmpty()) {
                rvReviews.setVisibility(View.GONE);
                layoutNoReview.setVisibility(View.VISIBLE);
            } else {
                rvReviews.setVisibility(View.VISIBLE);
                layoutNoReview.setVisibility(View.GONE);
            }
            rvReviews.setAdapter(new ReviewAdapter(requireContext(), filtered, placeData.getAddress(), jwtToken, username, PlaceDetailFragment.this));
        });
    }
    //=========================================================================

    //-----------------ADD/UPDATE/DELETE/LIKE REVIEW SECTION-------------------

    //=========================================================================

    // Method để hiển thị dialog viết review
    private void showWriteReviewDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_write_review, null);
        builder.setView(dialogView);

        // Ánh xạ views
        TextView tvDialogPlaceName = dialogView.findViewById(R.id.tvDialogPlaceName);
        ImageView ivDialogUserAvatar = dialogView.findViewById(R.id.ivDialogUserAvatar);
        TextView tvDialogUserName = dialogView.findViewById(R.id.tvDialogUserName);
        RatingBar dialogRatingBar = dialogView.findViewById(R.id.dialogRatingBar);
        EditText etDialogReviewContent = dialogView.findViewById(R.id.etDialogReviewContent);
        MaterialButton btnAddPhotos = dialogView.findViewById(R.id.btnAddPhotos);
        RecyclerView rvSelectedPhotos = dialogView.findViewById(R.id.rvSelectedPhotos);
        MaterialButton btnCancelReview = dialogView.findViewById(R.id.btnCancelReview);
        MaterialButton btnPostReview = dialogView.findViewById(R.id.btnPostReview);

        currentReviewDialog = builder.create();
        currentReviewDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        btnPostReview.setText(dialogType.equals("update") ? "Update" : "Post");

        // Set dữ liệu
        if (placeData != null) {
            tvDialogPlaceName.setText(placeData.getName());
        }
        tvDialogUserName.setText(username);

        // Load avatar
        if (avatar != null && !avatar.isEmpty()) {
            Glide.with(requireContext())
                    .load(avatar)
                    .into(ivDialogUserAvatar);
        }

        // Setup RecyclerView cho preview ảnh đã chọn
        if (!selectedImageUris.isEmpty()) {
            rvSelectedPhotos.setVisibility(View.VISIBLE);
            LinearLayoutManager layoutManager = new LinearLayoutManager(
                    requireContext(), LinearLayoutManager.HORIZONTAL, false
            );
            rvSelectedPhotos.setLayoutManager(layoutManager);

            // Adapter để hiển thị preview ảnh với nút xóa
            ImagePreviewAdapter adapter = new ImagePreviewAdapter(
                    requireContext(),
                    selectedImageUris,
                    position -> {
                        selectedImageUris.remove(position);
                        showWriteReviewDialog(); // Refresh dialog
                    }
            );
            rvSelectedPhotos.setAdapter(adapter);

            btnAddPhotos.setText("Add photos (" + selectedImageUris.size() + "/" + MAX_IMAGES + ")");
        } else {
            rvSelectedPhotos.setVisibility(View.GONE);
            btnAddPhotos.setText("Add photos");
        }

        if (tempReviewRating > 0) {
            dialogRatingBar.setRating(tempReviewRating);
        }
        if (!tempReviewContent.isEmpty()) {
            etDialogReviewContent.setText(tempReviewContent);
            // Di chuyển con trỏ về cuối (tùy chọn)
            etDialogReviewContent.setSelection(tempReviewContent.length());
        }

        // Button chọn ảnh
        btnAddPhotos.setOnClickListener(v -> {
            if (selectedImageUris.size() >= MAX_IMAGES) {
                Toast.makeText(requireContext(),
                        "Maximum " + MAX_IMAGES + " photos allowed",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            tempReviewRating = dialogRatingBar.getRating();
            tempReviewContent = etDialogReviewContent.getText().toString();
            checkAndRequestPermission(); // Kiểm tra permission trước
        });

        // Button Cancel
        btnCancelReview.setOnClickListener(v -> {
            selectedImageUris.clear();
//            tempReviewContent = "";  // Reset
//            tempReviewRating = 0f;   // Reset
            currentReviewDialog.dismiss();
        });

        // Button Post/Add Review
        btnPostReview.setOnClickListener(v -> {
            int rating = (int) dialogRatingBar.getRating();
            String content = etDialogReviewContent.getText().toString().trim();

            // Validation
            if (rating == 0) {
                Toast.makeText(requireContext(), "Please select a rating", Toast.LENGTH_SHORT).show();
                return;
            }

            if (content.isEmpty()) {
                Toast.makeText(requireContext(), "Please write your review", Toast.LENGTH_SHORT).show();
                return;
            }

//            tempReviewContent = "";  // Reset
//            tempReviewRating = 0f;   // Reset
            // Upload ảnh lên Cloudinary trước, sau đó gọi API
            if (!selectedImageUris.isEmpty()) {
                // Disable button để tránh spam
                String act = dialogType.equals("add") ? "Posting..." : "Updating...";
                btnPostReview.setEnabled(false);
                btnPostReview.setText(act);
                uploadImagesAndPostReview(rating, content);
            } else {
                // Không có ảnh thì gọi API luôn
                if(dialogType.equals("add")) addReviewToServer(rating, content, new ArrayList<>());
                else updateReviewToServer(rating, content, new ArrayList<>());
            }
        });

        currentReviewDialog.show();
    }


    // Method để xử lý kết quả chọn ảnh
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGES_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                // Xử lý nhiều ảnh
                if (data.getClipData() != null) {
                    int count = Math.min(data.getClipData().getItemCount(), MAX_IMAGES - selectedImageUris.size());
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        selectedImageUris.add(imageUri);
                    }
                }
                // Xử lý 1 ảnh
                else if (data.getData() != null && selectedImageUris.size() < MAX_IMAGES) {
                    Uri imageUri = data.getData();
                    selectedImageUris.add(imageUri);
                }

                // Giới hạn tối đa 4 ảnh
                if (selectedImageUris.size() > MAX_IMAGES) {
                    selectedImageUris = selectedImageUris.subList(0, MAX_IMAGES);
                }

                Toast.makeText(requireContext(),
                        selectedImageUris.size() + " photo(s) selected",
                        Toast.LENGTH_SHORT).show();

                // Hiển thị lại dialog với ảnh đã chọn
                showWriteReviewDialog();
            }
        }
    }

    // Method để upload ảnh lên Cloudinary và đăng review
    private void uploadImagesAndPostReview(int rating, String content) {
        // Hiển thị progress
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(requireContext());
        progressDialog.setTitle("Uploading images...");
        progressDialog.setMessage("Please wait");
        progressDialog.setProgressStyle(android.app.ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(selectedImageUris.size());
        progressDialog.setCancelable(false);
        progressDialog.show();

        CloudinaryUploadHelper.uploadImages(requireContext(), selectedImageUris,
                new CloudinaryUploadHelper.UploadCallback() {
                    @Override
                    public void onSuccess(List<String> imageUrls) {
                        requireActivity().runOnUiThread(() -> {
                            progressDialog.dismiss();
                            currentReviewDialog.dismiss();

                            // Gọi API với các URL ảnh đã upload
                            if(dialogType.equals("add")) addReviewToServer(rating, content, imageUrls);
                            else updateReviewToServer(rating, content, imageUrls);

                            // Clear selected images
                            selectedImageUris.clear();
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        requireActivity().runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(requireContext(),
                                    "Failed to upload images: " + errorMessage,
                                    Toast.LENGTH_LONG).show();

                            // Re-enable button
                            if (currentReviewDialog != null && currentReviewDialog.isShowing()) {
                                View dialogView = currentReviewDialog.findViewById(android.R.id.content);
                                MaterialButton btnPostReview = dialogView.findViewById(R.id.btnPostReview);
                                if (btnPostReview != null) {
                                    btnPostReview.setEnabled(true);
                                    btnPostReview.setText(dialogType.equals("Update") ? "Update" : "Post");
                                }
                            }
                        });
                    }

                    @Override
                    public void onProgress(int current, int total) {
                        requireActivity().runOnUiThread(() -> {
                            progressDialog.setProgress(current);
                            progressDialog.setMessage("Uploading " + current + " of " + total);
                        });
                    }
                });
    }

    // Method để gọi API thêm review
    private void addReviewToServer(int rating, String content, List<String> pictureUrls) {
        if (placeData == null || placeData.getAddress() == null) {
            Toast.makeText(requireContext(), "No place selected", Toast.LENGTH_SHORT).show();
            return;
        }

        if (jwtToken == null || jwtToken.isEmpty()) {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gọi API AddReview
        ReviewApi.AddReview(jwtToken, placeData.getAddress(), rating, content, pictureUrls,
                requireContext(), new ReviewApi.ReviewApiCallback() {
                    @Override
                    public void onSuccess(ArrayList<JSONObject> data) {
                        // Not used in this callback
                    }

                    @Override
                    public void onSuccess(String msg) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Review posted successfully!", Toast.LENGTH_SHORT).show();

                            currentReviewDialog.dismiss();
                            // Clear selected images
                            selectedImageUris.clear();

                            Runnable reviewCheckLogic = new Runnable() {
                                @Override
                                public void run() {
                                    // is checked-in
                                    requireActivity().runOnUiThread(() -> {
                                        if(isPlaceCheckedInByUser(placeData.getAddress(), userCheckedInCheckpoints)){
                                            Review myReview = getYourReview();
                                            System.out.println("ReviewList: "+reviewList);
                                            System.out.println("myReview: "+myReview);
                                            if(myReview != null) {
                                                // hiển thị phần review của user và 2 nút update, delete
                                                yourReviewLayout.setVisibility(View.VISIBLE);
                                                btnWriteReview.setVisibility(View.GONE);

                                                bindMyReviewData(myReview);
                                            }else{
                                                // hiển thị và active nút thêm review và visibility: gone cho review của user
                                                yourReviewLayout.setVisibility(View.GONE);
                                                btnWriteReview.setVisibility(View.VISIBLE);
                                                btnWriteReview.setBackgroundTintList(
                                                        ColorStateList.valueOf(Color.parseColor("#01B8B3"))
                                                );
                                                btnWriteReview.setEnabled(true);
                                            }
                                        }else{
                                            // vô hiệu hóa nút thêm review (đổi màu xám và khi bấm sẽ hiện toast "You have to check in before write a review")
                                            btnWriteReview.setEnabled(false);
                                            btnWriteReview.setBackgroundTintList(
                                                    ColorStateList.valueOf(Color.parseColor("#808080"))
                                            );
                                            yourReviewLayout.setVisibility(View.GONE);
                                        }
                                    });
                                }
                            };

                            setupReviews(placeData.getAddress(), "most approved", reviewCheckLogic);
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(),
                                    "Failed to post review: " + errorMessage,
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                });
    }

    private void updateReviewToServer(int rating, String content, List<String> pictureUrls) {
        if (placeData == null || placeData.getAddress() == null) {
            Toast.makeText(requireContext(), "No place selected", Toast.LENGTH_SHORT).show();
            return;
        }

        if (jwtToken == null || jwtToken.isEmpty()) {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gọi API AddReview
        ReviewApi.UpdateReview(jwtToken, placeData.getAddress(), rating, content, pictureUrls,
                requireContext(), new ReviewApi.ReviewApiCallback() {
                    @Override
                    public void onSuccess(ArrayList<JSONObject> data) {
                        // Not used in this callback
                    }

                    @Override
                    public void onSuccess(String msg) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Review updated successfully!", Toast.LENGTH_SHORT).show();

                            currentReviewDialog.dismiss();
                            // Clear selected images
                            selectedImageUris.clear();

                            Runnable reviewCheckLogic = new Runnable() {
                                @Override
                                public void run() {
                                    // is checked-in
                                    requireActivity().runOnUiThread(() -> {
                                        if(isPlaceCheckedInByUser(placeData.getAddress(), userCheckedInCheckpoints)){
                                            Review myReview = getYourReview();
                                            if(myReview != null) {
                                                // hiển thị phần review của user và 2 nút update, delete
                                                yourReviewLayout.setVisibility(View.VISIBLE);
                                                btnWriteReview.setVisibility(View.GONE);

                                                bindMyReviewData(myReview);
                                            }else{
                                                // hiển thị và active nút thêm review và visibility: gone cho review của user
                                                yourReviewLayout.setVisibility(View.GONE);
                                                btnWriteReview.setVisibility(View.VISIBLE);
                                                btnWriteReview.setBackgroundTintList(
                                                        ColorStateList.valueOf(Color.parseColor("#01B8B3"))
                                                );
                                                btnWriteReview.setEnabled(true);
                                            }
                                        }else{
                                            // vô hiệu hóa nút thêm review (đổi màu xám và khi bấm sẽ hiện toast "You have to check in before write a review")
                                            btnWriteReview.setEnabled(false);
                                            btnWriteReview.setBackgroundTintList(
                                                    ColorStateList.valueOf(Color.parseColor("#808080"))
                                            );
                                            yourReviewLayout.setVisibility(View.GONE);
                                        }
                                    });
                                }
                            };

                            setupReviews(placeData.getAddress(), "most approved", reviewCheckLogic);
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(),
                                    "Failed to update review: " + errorMessage,
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                });
    }

    private void deleteReview() {
        if (placeData == null || placeData.getAddress() == null) {
            Toast.makeText(requireContext(), "No place selected", Toast.LENGTH_SHORT).show();
            return;
        }

        if (jwtToken == null || jwtToken.isEmpty()) {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gọi API AddReview
        ReviewApi.DeleteReview(jwtToken, placeData.getAddress(),
                requireContext(), new ReviewApi.ReviewApiCallback() {
                    @Override
                    public void onSuccess(ArrayList<JSONObject> data) {
                        // Not used in this callback
                    }

                    @Override
                    public void onSuccess(String msg) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Review removed successfully!", Toast.LENGTH_SHORT).show();
                            tempReviewContent = "";  // Reset
                            tempReviewRating = 0f;   // Reset
                            Runnable reviewCheckLogic = new Runnable() {
                                @Override
                                public void run() {
                                    // is checked-in
                                    requireActivity().runOnUiThread(() -> {
                                        if(isPlaceCheckedInByUser(placeData.getAddress(), userCheckedInCheckpoints)){
                                            Review myReview = getYourReview();
                                            if(myReview != null) {
                                                // hiển thị phần review của user và 2 nút update, delete
                                                yourReviewLayout.setVisibility(View.VISIBLE);
                                                btnWriteReview.setVisibility(View.GONE);

                                                bindMyReviewData(myReview);
                                            }else{
                                                // hiển thị và active nút thêm review và visibility: gone cho review của user
                                                yourReviewLayout.setVisibility(View.GONE);
                                                btnWriteReview.setVisibility(View.VISIBLE);
                                                btnWriteReview.setBackgroundTintList(
                                                        ColorStateList.valueOf(Color.parseColor("#01B8B3"))
                                                );
                                                btnWriteReview.setEnabled(true);
                                            }
                                        }else{
                                            // vô hiệu hóa nút thêm review (đổi màu xám và khi bấm sẽ hiện toast "You have to check in before write a review")
                                            btnWriteReview.setEnabled(false);
                                            btnWriteReview.setBackgroundTintList(
                                                    ColorStateList.valueOf(Color.parseColor("#808080"))
                                            );
                                            yourReviewLayout.setVisibility(View.GONE);
                                        }
                                    });
                                }
                            };
                            setupReviews(placeData.getAddress(), "most approved", reviewCheckLogic);
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(),
                                    "Failed to delete review: " + errorMessage,
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                });
    }

    // Khai báo thêm constant
    private static final int PERMISSION_REQUEST_CODE = 101;

    // Method kiểm tra và request permission
    private void checkAndRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        PERMISSION_REQUEST_CODE);
            } else {
                openImagePicker();
            }
        } else {
            // Android 12 và thấp hơn
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            } else {
                openImagePicker();
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, PICK_IMAGES_REQUEST);
        if (currentReviewDialog != null) {
            currentReviewDialog.dismiss();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(requireContext(),
                        "Permission denied. Cannot access photos.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    //====================================================

    //-----------------BOOKMARK SECTION-------------------

    //====================================================
    private void showBookmarkListDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_select_bookmark_list, null);
        builder.setView(dialogView);

        RecyclerView recyclerBookmarkLists = dialogView.findViewById(R.id.recyclerBookmarkLists);
        MaterialButton btnCreateNewList = dialogView.findViewById(R.id.btnCreateNewList);

        recyclerBookmarkLists.setLayoutManager(new LinearLayoutManager(requireContext()));

        android.app.AlertDialog dialog = builder.create();

        // Make dialog background transparent to show rounded corners
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Load bookmark lists from backend
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", requireContext().MODE_PRIVATE);
        String jwtToken = prefs.getString("jwt_token", null);

        if (jwtToken == null) {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        BookmarkApi.getMyBookmarkLists(jwtToken, requireContext(), new BookmarkApi.BookmarkListCallback() {
            @Override
            public void onSuccess(ArrayList<JSONObject> bookmarkLists) {
                requireActivity().runOnUiThread(() -> {
                    List<SavedList> lists = new ArrayList<>();
                    for (JSONObject json : bookmarkLists) {
                        try {
                            SavedList list = new SavedList(
                                    json.getString("id"),
                                    json.optString("icon", "bookmark"),
                                    json.getString("name"),
                                    json.optString("description", ""),
                                    json.getLong("bookmarkCount")
                            );
                            lists.add(list);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    BookmarkListSelectorAdapter adapter = new BookmarkListSelectorAdapter(lists, selectedList -> {
                        // Save to selected list
                        saveToBookmarkList(selectedList.getId(), jwtToken);
                        dialog.dismiss();
                    });
                    recyclerBookmarkLists.setAdapter(adapter);

                    // Check which lists already contain this location
                    if (placeData != null && placeData.getId() != null) {
                        checkSavedLists(jwtToken, placeData.getId(), lists, adapter);
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Failed to load lists: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });

        // Create new list button
        btnCreateNewList.setOnClickListener(v -> {
            dialog.dismiss();
            showCreateNewListDialog(jwtToken);
        });

        dialog.show();
    }

    private void checkSavedLists(String jwtToken, String locationId, List<SavedList> lists, BookmarkListSelectorAdapter adapter) {
        java.util.Set<String> savedListIds = new java.util.HashSet<>();
        java.util.concurrent.atomic.AtomicInteger checkedCount = new java.util.concurrent.atomic.AtomicInteger(0);

        for (SavedList list : lists) {
            BookmarkApi.getBookmarksInList(jwtToken, list.getId(), requireContext(), new BookmarkApi.BookmarkCallback() {
                @Override
                public void onSuccess(ArrayList<JSONObject> bookmarks) {
                    // Check if this list contains the current location
                    for (JSONObject bookmark : bookmarks) {
                        try {
                            if (bookmark.getString("locationId").equals(locationId)) {
                                synchronized (savedListIds) {
                                    savedListIds.add(list.getId());
                                }
                                break;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    // Update adapter when all lists have been checked
                    if (checkedCount.incrementAndGet() == lists.size()) {
                        requireActivity().runOnUiThread(() -> {
                            adapter.setSavedListIds(savedListIds);
                        });
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    // Still count this as checked, even if it failed
                    if (checkedCount.incrementAndGet() == lists.size()) {
                        requireActivity().runOnUiThread(() -> {
                            adapter.setSavedListIds(savedListIds);
                        });
                    }
                }
            });
        }
    }

    private void checkIfLocationIsSaved() {
        if (placeData == null || placeData.getId() == null) {
            return;
        }

        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", requireContext().MODE_PRIVATE);
        String jwtToken = prefs.getString("jwt_token", null);

        if (jwtToken == null) {
            return;
        }

        BookmarkApi.getMyBookmarkLists(jwtToken, requireContext(), new BookmarkApi.BookmarkListCallback() {
            @Override
            public void onSuccess(ArrayList<JSONObject> bookmarkLists) {
                java.util.concurrent.atomic.AtomicBoolean isSaved = new java.util.concurrent.atomic.AtomicBoolean(false);
                java.util.concurrent.atomic.AtomicInteger checkedCount = new java.util.concurrent.atomic.AtomicInteger(0);
                int totalLists = bookmarkLists.size();

                if (totalLists == 0) {
                    requireActivity().runOnUiThread(() -> updateSaveButtonState(false));
                    return;
                }

                for (JSONObject listJson : bookmarkLists) {
                    try {
                        String listId = listJson.getString("id");
                        BookmarkApi.getBookmarksInList(jwtToken, listId, requireContext(), new BookmarkApi.BookmarkCallback() {
                            @Override
                            public void onSuccess(ArrayList<JSONObject> bookmarks) {
                                if (!isSaved.get()) {
                                    for (JSONObject bookmark : bookmarks) {
                                        try {
                                            if (bookmark.getString("locationId").equals(placeData.getId())) {
                                                isSaved.set(true);
                                                break;
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                if (checkedCount.incrementAndGet() == totalLists) {
                                    requireActivity().runOnUiThread(() -> updateSaveButtonState(isSaved.get()));
                                }
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                if (checkedCount.incrementAndGet() == totalLists) {
                                    requireActivity().runOnUiThread(() -> updateSaveButtonState(isSaved.get()));
                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (checkedCount.incrementAndGet() == totalLists) {
                            requireActivity().runOnUiThread(() -> updateSaveButtonState(isSaved.get()));
                        }
                    }
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                // Keep default state
            }
        });
    }

    private void updateSaveButtonState(boolean isSaved) {
        if (isSaved) {
            btnSave.setText("Saved");
            btnSave.setIconResource(R.drawable.ic_bookmark_filled);
        } else {
            btnSave.setText("Save");
            btnSave.setIconResource(R.drawable.ic_bookmark);
        }
    }

    private void saveToBookmarkList(String listId, String jwtToken) {
        if (placeData == null) {
            Toast.makeText(requireContext(), "No location selected", Toast.LENGTH_SHORT).show();
            return;
        }

        String locationId = placeData.getId();

        BookmarkApi.addBookmark(jwtToken, locationId, listId, null, requireContext(), new BookmarkApi.SingleBookmarkCallback() {
            @Override
            public void onSuccess(JSONObject bookmark) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Saved to bookmark list!", Toast.LENGTH_SHORT).show();
                    // Update save button state
                    checkIfLocationIsSaved();
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Failed to save: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showCreateNewListDialog(String jwtToken) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_new_bookmark_list, null);

        android.widget.EditText editListName = dialogView.findViewById(R.id.editListName);
        android.widget.EditText editListDescription = dialogView.findViewById(R.id.editListDescription);
        TextView selectedEmojiIcon = dialogView.findViewById(R.id.selectedEmojiIcon);
        View iconPickerButton = dialogView.findViewById(R.id.iconPickerButton);

        final String[] selectedIcon = {"😀"}; // Default emoji

        // Setup icon picker button
        iconPickerButton.setOnClickListener(v -> showEmojiPicker(selectedEmojiIcon, selectedIcon));

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        // Make dialog background transparent to show rounded corners
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnCreate).setOnClickListener(v -> {
            String listName = editListName.getText().toString().trim();
            String listDescription = editListDescription.getText().toString().trim();
            if (listName.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a list name", Toast.LENGTH_SHORT).show();
                return;
            }

            BookmarkApi.createBookmarkList(jwtToken, listName, selectedIcon[0], listDescription, requireContext(), new BookmarkApi.SingleBookmarkListCallback() {
                @Override
                public void onSuccess(JSONObject bookmarkList) {
                    requireActivity().runOnUiThread(() -> {
                        try {
                            String newListId = bookmarkList.getString("id");
                            Toast.makeText(requireContext(), "List created!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            // Now save to the newly created list
                            saveToBookmarkList(newListId, jwtToken);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                }

                @Override
                public void onFailure(String errorMessage) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Failed to create list: " + errorMessage, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });

        dialog.show();
    }

    private void showEmojiPicker(TextView selectedEmojiIcon, String[] selectedIcon) {
        com.example.myapplication.dialog.EmojiPickerDialog emojiPicker =
                com.example.myapplication.dialog.EmojiPickerDialog.newInstance(selectedIcon[0]);
        emojiPicker.setOnEmojiSelectedListener(emoji -> {
            selectedIcon[0] = emoji;
            selectedEmojiIcon.setText(emoji);
        });
        emojiPicker.show(getParentFragmentManager(), "emoji_picker");
    }
}
