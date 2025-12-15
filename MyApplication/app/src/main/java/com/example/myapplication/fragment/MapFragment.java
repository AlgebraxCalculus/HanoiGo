package com.example.myapplication.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.adapter.SearchSuggestionAdapter;
import com.example.myapplication.api.CheckpointApi;
import com.example.myapplication.api.LocationApi;
import com.example.myapplication.model.Place;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment {

    private MapView mapView;
    private MapboxMap map;
    private LatLng userLocation;
    private Marker userMarker;

    private CardView btnCheckpoint, btnBookmark, btnCompass, btnNavigate;
    private String username = "default";
    private ImageView imgAvatar;
    private String avatar = "";
    private String jwtToken = "";
    private LinearLayout tagContainer;
    private String currentSelectedTag = null;
    private RecyclerView rvSearchSuggestions;

    private CardView searchSuggestionsCard;
    private TextWatcher textWatcher;
    private final Map<Marker, JSONObject> markerMap = new HashMap<>();

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

        // MAP SETUP
        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapboxMap -> {
            map = mapboxMap;
            String styleUrl = "https://tiles.goong.io/assets/goong_map_web.json?api_key=" + getString(R.string.goong_map_key);
            map.setStyle(new Style.Builder().fromUri(styleUrl), style -> {
                map.setOnMarkerClickListener(clickedMarker -> {
                    JSONObject placeData = markerMap.get(clickedMarker);
                    JSONObject locationResponse = placeData.optJSONObject("locationResponse");
                    if (placeData != null) {
                        Place place = new Place(
                                locationResponse.optString("name"),
                                locationResponse.optString("description"),
                                placeData.optString("distanceText"),
                                locationResponse.optString("defaultPicture"),
                                locationResponse.optString("address")
                        );
                        place.setId(locationResponse.optString("id"));
                        openPlaceDetailFragment(place);
                        return true;
                    }
                    return false;
                });
                // Show marker user
                if (userLocation != null) {
                    showMarker(userLocation.getLatitude(), userLocation.getLongitude(), "You are here", false);
                }
            });
        });

        ExploreFragment exploreFragment = new ExploreFragment();
        Bundle childArgs = new Bundle();
        childArgs.putString("jwtToken", jwtToken);
        childArgs.putString("username", username);
        childArgs.putString("avatar", avatar);
        loadChildFragment(exploreFragment);

        btnCheckpoint = view.findViewById(R.id.btnCheckpoint);
        btnBookmark = view.findViewById(R.id.btnBookmark);
        btnCompass = view.findViewById(R.id.btnCompass);
        btnNavigate = view.findViewById(R.id.btnNavigate);

        List<CardView> allButtons = Arrays.asList(btnCheckpoint, btnBookmark, btnCompass, btnNavigate);

        View.OnClickListener buttonClickListener = v -> {
            setActiveButton((CardView) v);
            // Xóa marker cũ nếu có
            for (Marker m : new ArrayList<>(markerMap.keySet())) {
                m.remove();
            }
            markerMap.clear();
            if (v == btnBookmark) {
                loadChildFragment(new BookmarkFragment());
            } else if (v == btnCompass) {
                loadChildFragment(exploreFragment);
            } else if (v == btnNavigate) {
                if (userLocation != null && map != null) {
                    showMarker(userLocation.getLatitude(), userLocation.getLongitude(), "You are here", true);
                } else {
                    Toast.makeText(requireContext(), "User location unavailable", Toast.LENGTH_SHORT).show();
                }
            } else if (v == btnCheckpoint) {
                fetchCheckpoints();
            }
        };

        for (CardView btn : allButtons) {
            btn.setOnClickListener(buttonClickListener);
        }

        EditText searchBar = view.findViewById(R.id.searchBar);
        ImageView ivClear = view.findViewById(R.id.ivClear);
        rvSearchSuggestions = view.findViewById(R.id.rvSearchSuggestions);
        searchSuggestionsCard = view.findViewById(R.id.searchSuggestionsCard);
        rvSearchSuggestions.setLayoutManager(new LinearLayoutManager(requireContext()));
        tagContainer = view.findViewById(R.id.tagContainer);

        searchAutocomplete(searchBar, rvSearchSuggestions, searchSuggestionsCard, tagContainer, ivClear);
        setupTagClicks();
        return view;
    }

    private void fetchCheckpoints() {
        CheckpointApi.GetEnableCheckIn(
                userLocation.getLatitude(),
                userLocation.getLongitude(),
                jwtToken,
                getContext(),
                new CheckpointApi.CheckpointApiCallback() {

                    @Override
                    public void onSuccess(ArrayList<JSONObject> list) {
                        requireActivity().runOnUiThread(() -> {

                            if (list == null || list.isEmpty()) {
                                Log.d("MapFragment", "No checkpoints found");
                                return;
                            }

                            List<LatLng> allPoints = new ArrayList<>();

                            for (JSONObject checkpoint : list) {
                                JSONObject location = checkpoint.optJSONObject("locationResponse");
                                if (location == null) continue;

                                double lat = location.optDouble("latitude", 0);
                                double lng = location.optDouble("longitude", 0);

                                LatLng pos = new LatLng(lat, lng);
                                allPoints.add(pos);

                                showLocationMarker(lat, lng, checkpoint, false);
                            }

                            // Zoom map
                            if (!allPoints.isEmpty()) {
                                LatLngBounds bounds = LatLngBounds.from(
                                        getMaxLat(allPoints),
                                        getMaxLng(allPoints),
                                        getMinLat(allPoints),
                                        getMinLng(allPoints)
                                );
                                map.animateCamera(
                                        CameraUpdateFactory.newLatLngBounds(bounds, 80)
                                );
                            }
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() ->
                                Log.e("MapFragment", "Checkpoint API error: " + errorMessage)
                        );
                    }
                }
        );
    }

    private void setActiveButton(CardView activeButton) {
        int activeColor = Color.parseColor("#007AFF");
        int inactiveColor = Color.parseColor("#001A2E");

        List<CardView> allButtons = Arrays.asList(btnCheckpoint, btnBookmark, btnCompass, btnNavigate);
        for (CardView btn : allButtons) {
            btn.setCardBackgroundColor(inactiveColor);
            ImageView icon = (ImageView) btn.getChildAt(0);
            if (icon != null) {
                icon.setColorFilter(Color.WHITE);
            }
        }
        activeButton.setCardBackgroundColor(activeColor);

        ImageView icon = (ImageView) activeButton.getChildAt(0);
        if (icon != null) {
            icon.setColorFilter(Color.parseColor("#FFFFFF"));
        }
    }

    // Load fragment con
    private void loadChildFragment(Fragment fragment) {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.childFragmentContainer, fragment)
                .commit();
    }

    // Mở chi tiết địa điểm
    public void openPlaceDetailFragment(Place place) {
        PlaceDetailFragment detailFragment =
                PlaceDetailFragment.newInstance(place, jwtToken, username, avatar);

        if (userLocation != null) {
            detailFragment.updateUserLocation(userLocation.getLatitude(), userLocation.getLongitude());
        }

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
        fadeOut(btnCheckpoint);
        fadeOut(btnBookmark);
        fadeOut(btnCompass);
        fadeOut(btnNavigate);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setFooterVisibility(false);
        }
    }

    // Đóng chi tiết địa điểm và hiện lại nút
    public void onPlaceDetailClosed() {
        fadeIn(btnCheckpoint);
        fadeIn(btnBookmark);
        fadeIn(btnCompass);
        fadeIn(btnNavigate);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setFooterVisibility(true);
        }
    }

    // Place marker trên map
    public void showMarker(double lat, double lng, String title, boolean zoomCamera) {
        if (map == null) return;
        LatLng position = new LatLng(lat, lng);

        if (userMarker != null) {
            userMarker.setPosition(position);
        } else {
            userMarker = map.addMarker(new MarkerOptions()
                    .position(position)
                    .title(title));
        }

        if (zoomCamera) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
        }
    }

    public void updateUserLocation(double lat, double lng) {
        if (lat == 0.0 && lng == 0.0) return;

        userLocation = new LatLng(lat, lng);
        System.out.println("updateUserLocation() → lat=" + lat + ", lng=" + lng);

        if (map != null) {
            showMarker(lat, lng, "You are here", false);
        }

        Fragment child = getChildFragmentManager().findFragmentById(R.id.childFragmentContainer);
        if (child instanceof ExploreFragment) {
            ((ExploreFragment) child).updateUserLocation(lat, lng);
        }
    }

    public void showLocationMarker(double lat, double lng, JSONObject placeData, boolean zoom) {
        if (map == null || !isAdded()) return;
        LatLng position = new LatLng(lat, lng);

        String name = "";
        if (placeData != null) {
            JSONObject locationResponse = placeData.optJSONObject("locationResponse");
            if (locationResponse != null) {
                name = locationResponse.optString("name", "");
            }
        }

        // Kiểm tra đã có marker tại vị trí này chưa
        final double EPS = 1e-5;
        Marker existing = null;
        for (Marker m : map.getMarkers()) {
            LatLng p = m.getPosition();
            if (Math.abs(p.getLatitude() - lat) < EPS && Math.abs(p.getLongitude() - lng) < EPS) {
                existing = m;
                break;
            }
        }

        if (existing != null) {
            // Cập nhật dữ liệu vào markerMap
            if (placeData != null) {
                markerMap.put(existing, placeData);
            }

            if (zoom) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
            }
            return;
        }

        // Tạo mới nếu chưa có marker tại vị trí này
        Bitmap iconBitmap = BitmapFactory.decodeResource(requireContext().getResources(), R.drawable.ic_tag_marker);

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#1E90FF"));
        textPaint.setTextSize(40f);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setShadowLayer(5f, 0f, 0f, Color.WHITE);

        int maxTextWidth = 300;
        List<String> lines = new ArrayList<>();
        if (name == null) name = "";
        String[] words = name.split(" ");
        StringBuilder lineBuilder = new StringBuilder();
        for (String w : words) {
            String testLine = lineBuilder.length() == 0 ? w : lineBuilder + " " + w;
            if (textPaint.measureText(testLine) > maxTextWidth) {
                lines.add(lineBuilder.toString());
                lineBuilder = new StringBuilder(w);
            } else {
                lineBuilder = new StringBuilder(testLine);
            }
        }
        if (lineBuilder.length() > 0) lines.add(lineBuilder.toString());

        int iconSizePx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 35, requireContext().getResources().getDisplayMetrics());
        Bitmap scaledIcon = Bitmap.createScaledBitmap(iconBitmap, iconSizePx, iconSizePx, true);

        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float lineHeight = fm.bottom - fm.top + 10;
        int textHeight = (int) (lines.size() * lineHeight);
        int textPadding = 15, iconTextSpacing = 35;

        int totalWidth = Math.max(iconSizePx, maxTextWidth + textPadding * 2);
        int totalHeight = iconSizePx + iconTextSpacing + textHeight + textPadding;

        Bitmap combined = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(combined);

        int iconX = (totalWidth - iconSizePx) / 2;
        canvas.drawBitmap(scaledIcon, iconX, 0, null);

        float textX = totalWidth / 2f;
        float textYStart = iconSizePx + iconTextSpacing;
        float currentTextWidth = 0;
        for (String l : lines) currentTextWidth = Math.max(currentTextWidth, textPaint.measureText(l));

        float bgLeft = textX - (currentTextWidth / 2f) - textPadding;
        float bgRight = textX + (currentTextWidth / 2f) + textPadding;
        float bgTop = textYStart - textPadding;
        float bgBottom = textYStart + textHeight + textPadding;

        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.WHITE);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setShadowLayer(6f, 0f, 0f, Color.GRAY);
        canvas.drawRoundRect(new RectF(bgLeft, bgTop, bgRight, bgBottom), 16f, 16f, bgPaint);

        float y = textYStart - fm.top;
        for (String l : lines) {
            canvas.drawText(l, textX, y, textPaint);
            y += lineHeight;
        }

        IconFactory iconFactory = IconFactory.getInstance(requireContext());
        Icon icon = iconFactory.fromBitmap(combined);

        Marker marker = map.addMarker(new MarkerOptions()
                .position(position)
                .icon(icon)
                .title(name));

        if (placeData != null) {
            markerMap.put(marker, placeData);
        }

        if (zoom) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
        }
    }

    private void setupTagClicks() {
        for (int i = 0; i < tagContainer.getChildCount(); i++) {
            View child = tagContainer.getChildAt(i);
            if (child instanceof TextView) {
                TextView tagView = (TextView) child;

                tagView.setOnClickListener(v -> {
                    String selectedTag = tagView.getText().toString()
                            .replaceAll("[^\\p{L}\\p{N}\\s]", "")
                            .trim();

                    if (selectedTag.equals(currentSelectedTag)) {
                        currentSelectedTag = null;
                        resetAllTagBackgrounds();
                        resetMapToUserLocation();
                        return;
                    }

                    resetAllTagBackgrounds();

                    tagView.setBackgroundResource(R.drawable.bg_tag_selected);
                    currentSelectedTag = selectedTag;

                    Log.d("TAG_SELECTED", "Clicked tag: " + selectedTag);
                    loadLocationsByTag(selectedTag);
                });
            }
        }
    }

    private void resetAllTagBackgrounds() {
        for (int j = 0; j < tagContainer.getChildCount(); j++) {
            View other = tagContainer.getChildAt(j);
            if (other instanceof TextView) {
                other.setBackgroundResource(R.drawable.bg_tag);
            }
        }
    }

    private void resetMapToUserLocation() {
        if (map == null) return;

        // Xóa toàn bộ marker trừ userMarker
        List<Marker> toRemove = new ArrayList<>();
        for (Marker m : map.getMarkers()) {
            if (!m.equals(userMarker)) toRemove.add(m);
        }
        for (Marker m : toRemove) {
            map.removeMarker(m);
            markerMap.remove(m);
        }

        if (userLocation != null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
        }
    }

    private void loadLocationsByTag(String tag) {
        if (userLocation == null) {
            Toast.makeText(requireContext(), "Chưa có vị trí người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        LocationApi.GetLocationList(
                userLocation.getLatitude(),
                userLocation.getLongitude(),
                tag,
                false,
                false,
                getContext(),
                new LocationApi.LocationApiCallback() {
                    @Override
                    public void onSuccess(ArrayList<JSONObject> data) {
                        requireActivity().runOnUiThread(() -> {
                            updateLocationResult(data);
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
                }
        );
    }

    private void updateLocationResult(List<JSONObject> listResult) {
        if (listResult == null || listResult.isEmpty()) {
            Toast.makeText(requireContext(), "Không có địa điểm nào cho tag này.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (map == null) return;

        List<Marker> toRemove = new ArrayList<>();
        for (Marker m : map.getMarkers()) {
            if (!m.equals(userMarker)) toRemove.add(m);
        }
        for (Marker m : toRemove) {
            map.removeMarker(m);
            markerMap.remove(m);
        }

        List<LatLng> allPoints = new ArrayList<>();
        if (userLocation != null) allPoints.add(userLocation);

        for (JSONObject loc : listResult) {
            try {
                JSONObject locationResponse = loc.optJSONObject("locationResponse");
                if (locationResponse == null) continue;

                double lat = locationResponse.optDouble("latitude", 0);
                double lng = locationResponse.optDouble("longitude", 0);

                LatLng pos = new LatLng(lat, lng);
                allPoints.add(pos);

                showLocationMarker(lat, lng, loc, false);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!allPoints.isEmpty()) {
            LatLngBounds bounds = LatLngBounds.from(
                    getMaxLat(allPoints), getMaxLng(allPoints),
                    getMinLat(allPoints), getMinLng(allPoints)
            );
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 80));
        }
    }

    private double getMinLat(List<LatLng> points) {
        double min = Double.MAX_VALUE;
        for (LatLng p : points) min = Math.min(min, p.getLatitude());
        return min;
    }
    private double getMaxLat(List<LatLng> points) {
        double max = -Double.MAX_VALUE;
        for (LatLng p : points) max = Math.max(max, p.getLatitude());
        return max;
    }
    private double getMinLng(List<LatLng> points) {
        double min = Double.MAX_VALUE;
        for (LatLng p : points) min = Math.min(min, p.getLongitude());
        return min;
    }
    private double getMaxLng(List<LatLng> points) {
        double max = -Double.MAX_VALUE;
        for (LatLng p : points) max = Math.max(max, p.getLongitude());
        return max;
    }

    private void searchAutocomplete(EditText searchBar, RecyclerView rvSearchSuggestions,
                                    CardView searchSuggestionsCard,
                                    LinearLayout tagContainer,
                                    ImageView ivClear) {

        ArrayList<JSONObject> suggestionList = new ArrayList<>();
        SearchSuggestionAdapter suggestionAdapter = new SearchSuggestionAdapter(requireContext(), suggestionList, item -> {

            searchSuggestionsCard.setVisibility(View.GONE);
            rvSearchSuggestions.setVisibility(View.GONE);
            tagContainer.setVisibility(View.GONE);

            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);

            searchBar.removeTextChangedListener(textWatcher);
            searchBar.setText(item.optString("name"));
            searchBar.addTextChangedListener(textWatcher);

            Place place = new Place(
                    item.optString("name"),
                    item.optString("description"),
                    "",
                    item.optString("defaultPicture"),
                    item.optString("address")
            );
            place.setId(item.optString("id"));
            openPlaceDetailFragment(place);
        });

        rvSearchSuggestions.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSearchSuggestions.setAdapter(suggestionAdapter);
        rvSearchSuggestions.setNestedScrollingEnabled(true);
        rvSearchSuggestions.setHasFixedSize(false);

        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                ivClear.setVisibility(keyword.isEmpty() ? View.GONE : View.VISIBLE);

                if (!keyword.isEmpty()) {
                    tagContainer.setVisibility(View.GONE);

                    LocationApi.SearchAutocomplete(keyword, getContext(), new LocationApi.LocationApiCallback() {
                        @Override
                        public void onSuccess(ArrayList<JSONObject> data) {
                            requireActivity().runOnUiThread(() -> {
                                if (data.isEmpty()) {
                                    searchSuggestionsCard.setVisibility(View.GONE);
                                    rvSearchSuggestions.setVisibility(View.GONE);
                                } else {
                                    searchSuggestionsCard.setVisibility(View.VISIBLE);
                                    rvSearchSuggestions.setVisibility(View.VISIBLE);
                                    suggestionAdapter.updateData(data);
                                }
                            });
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            requireActivity().runOnUiThread(() -> {
                                searchSuggestionsCard.setVisibility(View.GONE);
                                rvSearchSuggestions.setVisibility(View.GONE);
                            });
                        }
                    });
                } else {
                    searchSuggestionsCard.setVisibility(View.GONE);
                    rvSearchSuggestions.setVisibility(View.GONE);
                    tagContainer.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        searchBar.addTextChangedListener(textWatcher);

        // Clear text
        ivClear.setOnClickListener(v -> {
            searchBar.setText("");
            ivClear.setVisibility(View.GONE);
            searchSuggestionsCard.setVisibility(View.GONE);
            rvSearchSuggestions.setVisibility(View.GONE);
            tagContainer.setVisibility(View.VISIBLE);
        });
    }

    private void fadeOut(View v) {
        v.animate().alpha(0f).setDuration(200).withEndAction(() -> v.setVisibility(View.GONE)).start();
    }

    private void fadeIn(View v) {
        v.setVisibility(View.VISIBLE);
        v.setAlpha(0f);
        v.animate().alpha(1f).setDuration(200).start();
    }

    // MAPVIEW LIFECYCLE
    @Override public void onStart() { super.onStart(); mapView.onStart(); }
    @Override public void onResume() { super.onResume(); mapView.onResume(); }
    @Override public void onPause() { super.onPause(); mapView.onPause(); }
    @Override public void onStop() { super.onStop(); mapView.onStop(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
    @Override public void onDestroyView() { super.onDestroyView(); mapView.onDestroy(); }
}
