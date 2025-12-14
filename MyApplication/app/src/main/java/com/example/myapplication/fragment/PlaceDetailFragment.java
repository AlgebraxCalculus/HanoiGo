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
import com.example.myapplication.adapter.BookmarkListSelectorAdapter;
import com.example.myapplication.adapter.ImageAdapter;
import com.example.myapplication.adapter.ReviewAdapter;
import com.example.myapplication.api.BookmarkApi;
import com.example.myapplication.api.CheckpointApi;
import com.example.myapplication.api.LocationApi;
import com.example.myapplication.model.Place;
import com.example.myapplication.model.Review;
import com.example.myapplication.model.SavedList;
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
    private MaterialButton btnDirections, btnSave, btnWriteReview, btnCheckin;
    private EditText searchBar;
    private ImageView actionButton, btnClose;
    private LinearLayout tagContainer;

    private ArrayList<JSONObject> availableCheckpoints;
    private Place placeData;
    private RecyclerView rvPlacePhotos;
    private RecyclerView rvReviews;

    private String username = "default";
    private String avatar = "";
    private String jwtToken = "";

    public PlaceDetailFragment() {}

    public static PlaceDetailFragment newInstance(Place place, ArrayList<JSONObject> checkpoints, String jwtToken, String username, String avatar) {
        PlaceDetailFragment fragment = new PlaceDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("placeData", place);
        args.putSerializable("availableCheckpoints", checkpoints);
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

        // --- Ánh xạ views ---
        rvPlacePhotos = view.findViewById(R.id.rvPlacePhotos);
        rvReviews = view.findViewById(R.id.rvReviews);
        placeDetailContainer = view.findViewById(R.id.placeDetailContainer);
        placeTitle = view.findViewById(R.id.placeTitle);
        ratingBar = view.findViewById(R.id.ratingBar);
        placeAddress = view.findViewById(R.id.placeAddress);
        overallDescription = view.findViewById(R.id.overallDescription);
        locationText = view.findViewById(R.id.locationText);
        btnCheckin = view.findViewById(R.id.btnCheckin);
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
            availableCheckpoints = (ArrayList<JSONObject>) getArguments().getSerializable("availableCheckpoints");
            jwtToken = getArguments().getString("jwtToken");
            username = getArguments().getString("username");
            avatar = getArguments().getString("avatar");
        }

        if (placeData != null && availableCheckpoints != null) {
            boolean isCheckpointAvailable = checkAddressInCheckpoints(placeData.getAddress(), availableCheckpoints);
            btnCheckin.setVisibility(isCheckpointAvailable ? View.VISIBLE : View.GONE);
        }

        // Check if location is saved and update button state
        checkIfLocationIsSaved();

        fetchPlaceDetail(placeData.getAddress());

        return view;
    }

    private boolean checkAddressInCheckpoints(String address, ArrayList<JSONObject> checkpointList) {
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
                                ((MapFragment) parent).showLocationMarker(lat, lng, result, true);
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
                        "2 days ago", (int) 4.5, 32,
                        new int[]{R.drawable.review_sample_img1, R.drawable.review_sample_img2, R.drawable.review_sample_img3}),
                new Review("Anh Phạm", "Traveler",
                        "Rất hài lòng, đồ ăn ngon, chỗ ngồi thoải mái. Giá hơi cao nhưng xứng đáng.",
                        "1 week ago", 5, 21,
                        new int[]{R.drawable.review_sample_img4}),
                new Review("Hà Lê", "Food Blogger",
                        "Không gian hơi ồn, nhưng đồ uống ngon, nhân viên thân thiện.",
                        "3 weeks ago", (int) 3.5, 10,
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

        btnSave.setOnClickListener(v -> showBookmarkListDialog()); //nút save

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

        btnCheckin.setOnClickListener(v -> {
            if (placeData == null || placeData.getAddress() == null) {
                Toast.makeText(getContext(), "No address available", Toast.LENGTH_SHORT).show();
                return;
            }
            if (jwtToken == null || jwtToken.isEmpty()) {
                Toast.makeText(getContext(), "You need to log in before checking in", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gọi API Check-in
            CheckpointApi.CheckIn(jwtToken, placeData.getAddress(), getContext(), new CheckpointApi.CheckpointApiCallback() {
                @Override
                public void onSuccess(ArrayList<JSONObject> resultList) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Check-in successful!", Toast.LENGTH_SHORT).show();
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
