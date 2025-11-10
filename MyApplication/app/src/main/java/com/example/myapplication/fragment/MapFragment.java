package com.example.myapplication.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.myapplication.api.LocationApi;

public class MapFragment extends Fragment {

    private MapView mapView;
    private MapboxMap map;
    private LatLng userLocation;
    private Marker userMarker;

    private View btnBookmark, btnCompass, btnNavigate;
    private String username = "default";
    private ImageView imgAvatar;
    private String avatar = "";
    private String jwtToken = "";
    private LinearLayout tagContainer;
    private String currentSelectedTag = null;
    private RecyclerView rvSearchSuggestions;

    private CardView searchSuggestionsCard;
    private TextWatcher textWatcher;
    private static final Map<String, Integer> iconMap = new HashMap<String, Integer>() {{
        put("Iconic", R.drawable.ic_tag_iconic);
        put("Cuisine", R.drawable.ic_tag_cuisine);
        put("Entertaining", R.drawable.ic_tag_entertaining);
        put("Culture", R.drawable.ic_tag_culture);
    }};

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

        // --- MAP SETUP ---
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
                        openPlaceDetailFragment(new Place(
                                locationResponse.optString("name"),
                                locationResponse.optString("description"),
                                placeData.optString("distanceText"),
                                locationResponse.optString("defaultPicture"),
                                locationResponse.optString("address")
                        ));
                        return true;
                    }
                    return false;
                });
                // Nếu userLocation có sẵn, show marker user
                if (userLocation != null) {
                    showMarker(userLocation.getLatitude(), userLocation.getLongitude(), "You are here", false);
                }
            });
        });

        // Child fragment
        ExploreFragment exploreFragment = new ExploreFragment();
        Bundle childArgs = new Bundle();
        childArgs.putString("jwtToken", jwtToken);
        childArgs.putString("username", username);
        childArgs.putString("avatar", avatar);
        loadChildFragment(exploreFragment);

        // Button
        btnBookmark = view.findViewById(R.id.btnBookmark);
        btnCompass = view.findViewById(R.id.btnCompass);
        btnNavigate = view.findViewById(R.id.btnNavigate);

        btnBookmark.setOnClickListener(v -> loadChildFragment(new BookmarkFragment()));
        btnCompass.setOnClickListener(v -> loadChildFragment(exploreFragment));

        btnNavigate.setOnClickListener(v -> {
            if (userLocation != null && map != null) {
                showMarker(userLocation.getLatitude(), userLocation.getLongitude(), "You are here", true);
            } else {
                Toast.makeText(requireContext(), "User location unavailable", Toast.LENGTH_SHORT).show();
            }
        });
        // Search autocomplete
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

    // Load fragment con
    private void loadChildFragment(Fragment fragment) {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.childFragmentContainer, fragment)
                .commit();
    }

    // Mở chi tiết địa điểm
    public void openPlaceDetailFragment(Place place) {
        PlaceDetailFragment detailFragment = PlaceDetailFragment.newInstance(place);
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

        fadeOut(btnBookmark);
        fadeOut(btnCompass);
        fadeOut(btnNavigate);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setFooterVisibility(false);
        }
    }

    // Đóng chi tiết địa điểm và hiện lại nút
    public void onPlaceDetailClosed() {
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

        // Nếu marker user đã tồn tại → chỉ cập nhật vị trí, không xóa/clear map
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

    // Cập nhật Location người dùng từ MainActivity
    public void updateUserLocation(double lat, double lng) {
        if (lat == 0.0 && lng == 0.0) return;

        userLocation = new LatLng(lat, lng);
        System.out.println("updateUserLocation() → lat=" + lat + ", lng=" + lng);

        // Nếu map đã sẵn sàng thì hiển thị marker luôn
        if (map != null) {
            showMarker(lat, lng, "You are here", false);
        }

        // Gửi vị trí xuống ExploreFragment
        Fragment child = getChildFragmentManager().findFragmentById(R.id.childFragmentContainer);
        if (child instanceof ExploreFragment) {
            ((ExploreFragment) child).updateUserLocation(lat, lng);
        }
    }

    private void showLocationMarkerWithEmoji(double lat, double lng, String type, JSONObject placeData) {
        if (map == null) return;
        LatLng position = new LatLng(lat, lng);
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View iconView = inflater.inflate(R.layout.item_location_marker, null);

        int emojiRes = iconMap.getOrDefault(type, R.drawable.ic_tag_iconic);
        ImageView iv = iconView.findViewById(R.id.ivEmoji);
        iv.setImageResource(emojiRes);

        // --- Đo layout và vẽ thành bitmap ---
        int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        iconView.measure(spec, spec);
        iconView.layout(0, 0, iconView.getMeasuredWidth(), iconView.getMeasuredHeight());

        Bitmap iconBitmap = Bitmap.createBitmap(iconView.getMeasuredWidth(), iconView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas iconCanvas = new Canvas(iconBitmap);
        iconView.draw(iconCanvas);

        JSONObject locationResponse = placeData.optJSONObject("locationResponse");
        String name = (locationResponse != null) ? locationResponse.optString("name", "") : "";

        // --- Thiết lập Paint để vẽ text ---
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#1E90FF"));
        textPaint.setTextSize(50f);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setShadowLayer(5f, 0f, 0f, Color.WHITE);

        // --- Xử lý text nhiều dòng ---
        int maxTextWidth = 350;
        List<String> lines = new ArrayList<>();
        String[] words = name.split(" ");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            String testLine = line.length() == 0 ? word : line + " " + word;
            if (textPaint.measureText(testLine) > maxTextWidth) {
                lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(testLine);
            }
        }
        if (line.length() > 0) lines.add(line.toString());

        // --- Tính chiều cao tổng thể ---
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float lineHeight = fontMetrics.bottom - fontMetrics.top + 10;
        int textHeight = (int) (lines.size() * lineHeight);
        int textPadding = 20;
        int iconTextSpacing = 20;
        int totalWidth = Math.max(iconBitmap.getWidth(), maxTextWidth + textPadding * 2);
        int totalHeight = iconBitmap.getHeight() + iconTextSpacing + textHeight + textPadding;

        Bitmap combined = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(combined);

        int iconX = (totalWidth - iconBitmap.getWidth()) / 2;
        canvas.drawBitmap(iconBitmap, iconX, 0, null);

        // Vẽ nền chữ
        float textX = totalWidth / 2f;
        float textYStart = iconBitmap.getHeight() + iconTextSpacing;
        float bgLeft = textX - maxTextWidth / 2f - textPadding;
        float bgTop = textYStart - textPadding;
        float bgRight = textX + maxTextWidth / 2f + textPadding;
        float bgBottom = textYStart + textHeight + textPadding;

        // Vẽ hình chữ nhật bo góc làm nền
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.WHITE);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setShadowLayer(8f, 0f, 0f, Color.GRAY);
        canvas.drawRoundRect(new RectF(bgLeft, bgTop, bgRight, bgBottom), 18f, 18f, bgPaint);

        float y = textYStart - fontMetrics.top;
        for (String l : lines) {
            canvas.drawText(l, textX, y, textPaint);
            y += lineHeight;
        }

        int scaledWidth = (int) (combined.getWidth() * 0.6);
        int scaledHeight = (int) (combined.getHeight() * 0.6);
        Bitmap finalBitmap = Bitmap.createScaledBitmap(combined, scaledWidth, scaledHeight, true);

        IconFactory iconFactory = IconFactory.getInstance(requireContext());
        Icon icon = iconFactory.fromBitmap(finalBitmap);

        Marker marker = map.addMarker(new MarkerOptions()
                .position(position)
                .icon(icon)
                .title(name));

        markerMap.put(marker, placeData);
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

                    // Nếu bấm lại tag đang chọn -> bỏ chọn
                    if (selectedTag.equals(currentSelectedTag)) {
                        currentSelectedTag = null;
                        resetAllTagBackgrounds();
                        resetMapToUserLocation();
                        return;
                    }

                    // Bỏ chọn tất cả tag trước đó
                    resetAllTagBackgrounds();

                    // Chọn tag mới
                    tagView.setBackgroundResource(R.drawable.bg_tag_selected);
                    currentSelectedTag = selectedTag;

                    Log.d("TAG_SELECTED", "Clicked tag: " + selectedTag);
                    loadLocationsByTag(selectedTag);
                });
            }
        }
    }

    // Reset toàn bộ background về mặc định
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

        // Zoom về vị trí user
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

        // Xóa marker cũ (không xóa userMarker)
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

                JSONArray tagsArray = locationResponse.optJSONArray("tags");
                String type = (tagsArray != null && tagsArray.length() > 0)
                        ? tagsArray.optString(0)
                        : "Iconic";

                showLocationMarkerWithEmoji(lat, lng, type, loc);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Zoom marker
        if (!allPoints.isEmpty()) {
            LatLngBounds bounds = LatLngBounds.from(
                    getMaxLat(allPoints), getMaxLng(allPoints),
                    getMinLat(allPoints), getMinLng(allPoints)
            );
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 80));
        }
    }

    // Hàm tiện ích tính min/max
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

            openPlaceDetailFragment(new Place(
                    item.optString("name"),
                    item.optString("description"),
                    "",
                    item.optString("defaultPicture"),
                    item.optString("address")
            ));
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

    // --- Hiệu ứng ẩn/hiện nút ---
    private void fadeOut(View v) {
        v.animate().alpha(0f).setDuration(200).withEndAction(() -> v.setVisibility(View.GONE)).start();
    }

    private void fadeIn(View v) {
        v.setVisibility(View.VISIBLE);
        v.setAlpha(0f);
        v.animate().alpha(1f).setDuration(200).start();
    }

    // --- MAPVIEW LIFECYCLE ---
    @Override public void onStart() { super.onStart(); mapView.onStart(); }
    @Override public void onResume() { super.onResume(); mapView.onResume(); }
    @Override public void onPause() { super.onPause(); mapView.onPause(); }
    @Override public void onStop() { super.onStop(); mapView.onStop(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
    @Override public void onDestroyView() { super.onDestroyView(); mapView.onDestroy(); }
}
