package com.example.myapplication.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.PersCheckpointAdapter;
import com.example.myapplication.api.UserApi;
import com.example.myapplication.model.Checkpoint;
import com.example.myapplication.model.Place;
import com.example.myapplication.model.Review;
import com.example.myapplication.MainActivity;


import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CheckpointsFragment extends Fragment {
    LinearLayout layoutNoCheckpoints;
    TextView tvCheckpointCount, filterHighestRate, filterNewest, filterLowestRate, filterOldest;
    EditText etSearchCheckpoint;
    Spinner spinner;
    String jwtToken = "";
    private RecyclerView rvCheckpoints;
    private PersCheckpointAdapter persCheckpointAdapter;
    private List<Checkpoint> checkpointList;
    private String currentType = "date"; // "rating" hoặc "date"
    private String currentSort = "newest"; // "best", "worst", "newest", "oldest"

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pers_checkpoints, container, false);
        tvCheckpointCount = view.findViewById(R.id.tvCheckpointCount);
        etSearchCheckpoint = view.findViewById(R.id.etSearchCheckpoint);
        filterHighestRate = view.findViewById(R.id.filterHighestRate);
        filterLowestRate = view.findViewById(R.id.filterLowestRate);
        filterNewest = view.findViewById(R.id.filterNewest);
        filterOldest = view.findViewById(R.id.filterOldest);
        rvCheckpoints = view.findViewById(R.id.rvCheckpoints);
        layoutNoCheckpoints = view.findViewById(R.id.layoutNoCheckpoints);

        //lấy ra jwt truyền từ PersonalFragment
        if (getArguments() != null) {
            jwtToken = getArguments().getString("jwtToken");
        }

        //hàm tìm kiếm trên thanh search
        etSearchCheckpoint.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = etSearchCheckpoint.getText().toString().trim();

                // Ẩn bàn phím
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(etSearchCheckpoint.getWindowToken(), 0);
                }

                filterCheckpoints(query);
                return true; // báo là đã xử lý hành động này
            }
            return false;
        });

        // Setup spinner
        spinner = view.findViewById(R.id.spinnerView);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                getContext(), R.array.view_filter_options, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        //set event cho filter sort
        filterHighestRate.setOnClickListener(v -> {
            currentType = "rating";
            currentSort = "best";
            setupCheckpointData(jwtToken, currentType, currentSort, spinner.getSelectedItem().toString());
            filterHighestRate.setTextColor(Color.parseColor("#FFFFFF"));
            filterHighestRate.setBackground(getResources().getDrawable(R.drawable.bg_filter_selected));
            filterLowestRate.setTextColor(Color.parseColor("#000000"));
            filterLowestRate.setBackground(getResources().getDrawable(R.drawable.bg_filter_unselected));
            filterNewest.setTextColor(Color.parseColor("#000000"));
            filterNewest.setBackground(getResources().getDrawable(R.drawable.bg_filter_unselected));
            filterOldest.setTextColor(Color.parseColor("#000000"));
            filterOldest.setBackground(getResources().getDrawable(R.drawable.bg_filter_unselected));
        });
        filterLowestRate.setOnClickListener(v -> {
            currentType = "rating";
            currentSort = "worst";
            setupCheckpointData(jwtToken, currentType, currentSort, spinner.getSelectedItem().toString());
            filterHighestRate.setTextColor(Color.parseColor("#000000"));
            filterHighestRate.setBackground(getResources().getDrawable(R.drawable.bg_filter_unselected));
            filterLowestRate.setTextColor(Color.parseColor("#FFFFFF"));
            filterLowestRate.setBackground(getResources().getDrawable(R.drawable.bg_filter_selected));
            filterNewest.setTextColor(Color.parseColor("#000000"));
            filterNewest.setBackground(getResources().getDrawable(R.drawable.bg_filter_unselected));
            filterOldest.setTextColor(Color.parseColor("#000000"));
            filterOldest.setBackground(getResources().getDrawable(R.drawable.bg_filter_unselected));
        });
        filterNewest.setOnClickListener(v -> {
            currentType = "date";
            currentSort = "newest";
            setupCheckpointData(jwtToken, currentType, currentSort, spinner.getSelectedItem().toString());
            filterHighestRate.setTextColor(Color.parseColor("#000000"));
            filterHighestRate.setBackground(getResources().getDrawable(R.drawable.bg_filter_unselected));
            filterLowestRate.setTextColor(Color.parseColor("#000000"));
            filterLowestRate.setBackground(getResources().getDrawable(R.drawable.bg_filter_unselected));
            filterNewest.setTextColor(Color.parseColor("#FFFFFF"));
            filterNewest.setBackground(getResources().getDrawable(R.drawable.bg_filter_selected));
            filterOldest.setTextColor(Color.parseColor("#000000"));
            filterOldest.setBackground(getResources().getDrawable(R.drawable.bg_filter_unselected));
        });
        filterOldest.setOnClickListener(v -> {
            currentType = "date";
            currentSort = "oldest";
            setupCheckpointData(jwtToken, currentType, currentSort, spinner.getSelectedItem().toString());
            filterHighestRate.setTextColor(Color.parseColor("#000000"));
            filterHighestRate.setBackground(getResources().getDrawable(R.drawable.bg_filter_unselected));
            filterLowestRate.setTextColor(Color.parseColor("#000000"));
            filterLowestRate.setBackground(getResources().getDrawable(R.drawable.bg_filter_unselected));
            filterNewest.setTextColor(Color.parseColor("#000000"));
            filterNewest.setBackground(getResources().getDrawable(R.drawable.bg_filter_unselected));
            filterOldest.setTextColor(Color.parseColor("#FFFFFF"));
            filterOldest.setBackground(getResources().getDrawable(R.drawable.bg_filter_selected));
        });

        //set event cho filter view
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedView = parent.getItemAtPosition(position).toString();
                // Gọi lại hàm setupCheckpointData với filter hiện tại
                setupCheckpointData(jwtToken, currentType, currentSort, selectedView);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // không cần làm gì
            }
        });


        rvCheckpoints = view.findViewById(R.id.rvCheckpoints);
        layoutNoCheckpoints = view.findViewById(R.id.layoutNoCheckpoints);
        rvCheckpoints.setLayoutManager(new LinearLayoutManager(requireContext()) );
        rvCheckpoints.setNestedScrollingEnabled(false);
        return view;
    }

    public void setupCheckpointData(String jwt, String type, String sort, String view) {
        checkpointList = new ArrayList<>();

        UserApi.GetMyCheckpointList(jwt, type, sort, view, getContext(), new UserApi.UserApiCallback() {
            @Override
            public void onSuccess(ArrayList<JSONObject> data) {
                for(JSONObject a : data){
                    try {
                        Place p = new Place(a.getJSONObject("location").getString("name"), a.getJSONObject("location").getString("description"), null, a.getJSONObject("location").getString("defaultPicture"));
                        Review r = null;
                        if(!a.isNull("review")){
                            String time = getRelativeTime(a.getJSONObject("review").getString("createdAt")); //cần thêm hàm biến đổi về dạng khoảng time tới hiện tại (few seconds ago,...)
                            r = new Review(a.getJSONObject("review").getJSONObject("userResponse").getString("username"), a.getJSONObject("review").getString("content"), time, a.getJSONObject("review").getInt("rating"));
                        }
                        LocalDateTime checkinTime = LocalDateTime.parse(a.getString("checkedInTime"));
                        checkpointList.add(new Checkpoint(p, checkinTime, r));
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
//                System.out.println("listCheckpoint: "+ checkpointList);
                requireActivity().runOnUiThread(() -> {
                    if (checkpointList == null || checkpointList.isEmpty()) {
                        rvCheckpoints.setVisibility(View.GONE);
                        layoutNoCheckpoints.setVisibility(View.VISIBLE);
                    } else {
                        rvCheckpoints.setVisibility(View.VISIBLE);
                        layoutNoCheckpoints.setVisibility(View.GONE);
                    }
                    tvCheckpointCount.setText(data.size() + " Achievements");
                    persCheckpointAdapter = new PersCheckpointAdapter(requireContext(), checkpointList, checkpoint -> openPlaceDetail(checkpoint));
                    rvCheckpoints.setAdapter(persCheckpointAdapter);
                });
            }

            @Override
            public void onSuccess(JSONObject userObj) {}

            @Override
            public void onFailure(String errorMessage) {
                requireActivity().runOnUiThread(() -> {
                    rvCheckpoints.setVisibility(View.GONE);
                    layoutNoCheckpoints.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "fetch checkpoint list failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });

//        checkpointList = new ArrayList<>();
//        checkpointList.add(new Checkpoint(
//                new Place("Hồ Hoàn Kiếm", "Hồ Hoàn Kiếm là trái tim của Hà Nội, nổi bật với mặt nước xanh biếc, Tháp Rùa cổ kính và cầu Thê Húc đỏ rực dẫn vào đền Ngọc Sơn. Đây không chỉ là điểm tham quan nổi tiếng mà còn là nơi lưu giữ những giá trị lịch sử, văn hóa và không khí yên bình giữa lòng phố cổ", null, "https://res.cloudinary.com/dsm1uhecl/image/upload/v1759653241/ho_hoan_kiem_mgbnsy.jpg"),
//                LocalDateTime.now(),
//                new Review(null, null, "Nice view, simple lovely", "few seconds ago", 5, 0, null)
//        ));
//        checkpointList.add(new Checkpoint(
//                new Place("Hồ Hoàn Kiếm", "Hồ Hoàn Kiếm là trái tim của Hà Nội, nổi bật với mặt nước xanh biếc, Tháp Rùa cổ kính và cầu Thê Húc đỏ rực dẫn vào đền Ngọc Sơn. Đây không chỉ là điểm tham quan nổi tiếng mà còn là nơi lưu giữ những giá trị lịch sử, văn hóa và không khí yên bình giữa lòng phố cổ", null, "https://res.cloudinary.com/dsm1uhecl/image/upload/v1759653241/ho_hoan_kiem_mgbnsy.jpg"),
//                LocalDateTime.now(),
//                new Review(null, null, "Nice view, simple ", "few seconds ago", 5, 0, null)
//        ));
//        checkpointList.add(new Checkpoint(
//                new Place("Hồ Hoàn Kiếm", "Hồ Hoàn Kiếm là trái tim của Hà Nội, nổi bật với mặt nước xanh biếc, Tháp Rùa cổ kính và cầu Thê Húc đỏ rực dẫn vào đền Ngọc Sơn. Đây không chỉ là điểm tham quan nổi tiếng mà còn là nơi lưu giữ những giá trị lịch sử, văn hóa và không khí yên bình giữa lòng phố cổ", null, "https://res.cloudinary.com/dsm1uhecl/image/upload/v1759653241/ho_hoan_kiem_mgbnsy.jpg"),
//                LocalDateTime.now(),
//                new Review(null, null, "Nice view, lovely", "few seconds ago", 5, 0, null)
//        ));
//        checkpointList.add(new Checkpoint(
//                new Place("Hồ Hoàn Kiếm", "Hồ Hoàn Kiếm là trái tim của Hà Nội, nổi bật với mặt nước xanh biếc, Tháp Rùa cổ kính và cầu Thê Húc đỏ rực dẫn vào đền Ngọc Sơn. Đây không chỉ là điểm tham quan nổi tiếng mà còn là nơi lưu giữ những giá trị lịch sử, văn hóa và không khí yên bình giữa lòng phố cổ", null, "https://res.cloudinary.com/dsm1uhecl/image/upload/v1759653241/ho_hoan_kiem_mgbnsy.jpg"),
//                LocalDateTime.now(),
//                null
//        ));
//
//        if (checkpointList == null || checkpointList.isEmpty()) {
//            rvCheckpoints.setVisibility(View.GONE);
//            layoutNoCheckpoints.setVisibility(View.VISIBLE);
//        } else {
//            rvCheckpoints.setVisibility(View.VISIBLE);
//            layoutNoCheckpoints.setVisibility(View.GONE);
//        }
//        tvCheckpointCount.setText(checkpointList.size() + " Checkpoints");
//        persCheckpointAdapter = new PersCheckpointAdapter(requireContext(), checkpointList, checkpoint -> openPlaceDetail(checkpoint));
//        rvCheckpoints.setAdapter(persCheckpointAdapter);
    }



    private void filterCheckpoints(String query) {
        List<Checkpoint> filtered = new ArrayList<>();
        for (Checkpoint a : checkpointList) {
            if (a.getPlace().getName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(a);
            }
        }
        requireActivity().runOnUiThread(() -> {
            if (filtered.isEmpty()) {
                rvCheckpoints.setVisibility(View.GONE);
                layoutNoCheckpoints.setVisibility(View.VISIBLE);
            } else {
                rvCheckpoints.setVisibility(View.VISIBLE);
                layoutNoCheckpoints.setVisibility(View.GONE);
            }
            persCheckpointAdapter = new PersCheckpointAdapter(requireContext(), filtered, checkpoint -> openPlaceDetail(checkpoint));
            rvCheckpoints.setAdapter(persCheckpointAdapter);
        });
    }

    public static String getRelativeTime(String isoTime) {
        // Parse từ ISO string → LocalDateTime
        LocalDateTime time = LocalDateTime.parse(isoTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // Lấy thời gian hiện tại
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());

        // Tính khoảng cách giữa hai thời điểm
        Duration duration = Duration.between(time, now);

        long seconds = duration.getSeconds();

        if (seconds < 60) {
            return "few seconds ago";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else if (seconds < 2592000) { // dưới 30 ngày
            long days = seconds / 86400;
            return days + (days == 1 ? " day ago" : " days ago");
        } else if (seconds < 31536000) { // dưới 12 tháng
            long months = seconds / 2592000;
            return months + (months == 1 ? " month ago" : " months ago");
        } else {
            long years = seconds / 31536000;
            return years + (years == 1 ? " year ago" : " years ago");
        }
    }

    private void openPlaceDetail(Checkpoint checkpoint){
        // Gọi activity chứa fragment này
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).openPlaceDetailFromHome(checkpoint.getPlace());
        }
    }
}