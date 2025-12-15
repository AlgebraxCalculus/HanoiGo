package com.example.myapplication.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.PersAchievementAdapter;
import com.example.myapplication.api.UserApi;
import com.example.myapplication.model.Achievement;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AchievementsFragment extends Fragment {
    LinearLayout layoutNoAchievements;
    private boolean hasLoaded = false;   // 👈 khai báo ở đây
    TextView tvAchievementCount, filterHighestTier, filterNewest, filterLowestTier, filterOldest;
    EditText etSearchAchievement;
    String jwtToken = "";
    private RecyclerView rvAchievements;
    private PersAchievementAdapter persAchievementAdapter;
    private List<Achievement> achievementList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pers_achievements, container, false);
        tvAchievementCount = view.findViewById(R.id.tvAchievementCount);
        etSearchAchievement = view.findViewById(R.id.etSearchAchievement);
        filterHighestTier = view.findViewById(R.id.filterHighestTier);
        filterLowestTier = view.findViewById(R.id.filterLowestTier);
        filterNewest = view.findViewById(R.id.filterNewest);
        filterOldest = view.findViewById(R.id.filterOldest);
        rvAchievements = view.findViewById(R.id.rvAchievements);
        layoutNoAchievements = view.findViewById(R.id.layoutNoAchievements);

        //lấy ra jwt truyền từ PersonalFragment
        if (getArguments() != null) {
            jwtToken = getArguments().getString("jwtToken");
        }

        //hàm tìm kiếm trên thanh search
        etSearchAchievement.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = etSearchAchievement.getText().toString().trim();

                // Ẩn bàn phím
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(etSearchAchievement.getWindowToken(), 0);
                }

                filterAchievements(query);
                return true; // báo là đã xử lý hành động này
            }
            return false;
        });

        filterHighestTier.setOnClickListener(v -> {
            setupAchievementData(jwtToken, "tier","desc");
            filterHighestTier.setTextColor(Color.parseColor("#FFFFFF"));
            filterHighestTier.setBackground(getResources().getDrawable(R.drawable.bg_filter_selected));
            filterLowestTier.setTextColor(Color.parseColor("#000000"));
            filterLowestTier.setBackground(getResources().getDrawable(R.drawable.bg_filter_unselected));
            filterNewest.setTextColor(Color.parseColor("#000000"));
            filterNewest.setBackground(getResources().getDrawable(R.drawable.bg_filter_unselected));
            filterOldest.setTextColor(Color.parseColor("#000000"));
            filterOldest.setBackground(getResources().getDrawable(R.drawable.bg_filter_unselected));
        });
        filterLowestTier.setOnClickListener(v -> {
            setupAchievementData(jwtToken, "tier","asc");
            filterHighestTier.setTextColor(Color.parseColor("#000000"));
            filterHighestTier.setBackground(getResources().getDrawable(R.drawable.bg_filter_unselected));
            filterLowestTier.setTextColor(Color.parseColor("#FFFFFF"));
            filterLowestTier.setBackground(getResources().getDrawable(R.drawable.bg_filter_selected));
            filterNewest.setTextColor(Color.parseColor("#000000"));
            filterNewest.setBackground(getResources().getDrawable(R.drawable.bg_filter_unselected));
            filterOldest.setTextColor(Color.parseColor("#000000"));
            filterOldest.setBackground(getResources().getDrawable(R.drawable.bg_filter_unselected));
        });
        filterNewest.setOnClickListener(v -> {
            setupAchievementData(jwtToken, "earned_at","desc");
            filterHighestTier.setTextColor(Color.parseColor("#000000"));
            filterHighestTier.setBackground(getResources().getDrawable(R.drawable.bg_filter_unselected));
            filterLowestTier.setTextColor(Color.parseColor("#000000"));
            filterLowestTier.setBackground(getResources().getDrawable(R.drawable.bg_filter_unselected));
            filterNewest.setTextColor(Color.parseColor("#FFFFFF"));
            filterNewest.setBackground(getResources().getDrawable(R.drawable.bg_filter_selected));
            filterOldest.setTextColor(Color.parseColor("#000000"));
            filterOldest.setBackground(getResources().getDrawable(R.drawable.bg_filter_unselected));
        });
        filterOldest.setOnClickListener(v -> {
            setupAchievementData(jwtToken, "earned_at","asc");
            filterHighestTier.setTextColor(Color.parseColor("#000000"));
            filterHighestTier.setBackground(getResources().getDrawable(R.drawable.bg_filter_unselected));
            filterLowestTier.setTextColor(Color.parseColor("#000000"));
            filterLowestTier.setBackground(getResources().getDrawable(R.drawable.bg_filter_unselected));
            filterNewest.setTextColor(Color.parseColor("#000000"));
            filterNewest.setBackground(getResources().getDrawable(R.drawable.bg_filter_unselected));
            filterOldest.setTextColor(Color.parseColor("#FFFFFF"));
            filterOldest.setBackground(getResources().getDrawable(R.drawable.bg_filter_selected));
        });

        rvAchievements = view.findViewById(R.id.rvAchievements);
        layoutNoAchievements = view.findViewById(R.id.layoutNoAchievements);
        rvAchievements.setLayoutManager(new LinearLayoutManager(requireContext()) {
            @Override
            public boolean canScrollVertically() {
                return false; // ⛔ Không cho RecyclerView tự scroll
            }
        });
        return view;
    }

    public void setupAchievementData(String jwt, String type, String sort) {
        achievementList = new ArrayList<>();

        UserApi.GetMyAchievementList(jwt, type, sort, getContext(), new UserApi.UserApiCallback() {
            @Override
            public void onSuccess(ArrayList<JSONObject> data) {
                for(JSONObject a : data){
                    try {
                        achievementList.add(new Achievement(a.getString("name"), a.getString("description"), "Tier "+a.getString("tier"), R.drawable.ic_medal, LocalDateTime.parse(a.getString("earned_at")).toLocalDate()));
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
//                System.out.println("listAchievemnent: "+achievementList);
                requireActivity().runOnUiThread(() -> {
                    if (achievementList == null || achievementList.isEmpty()) {
                        rvAchievements.setVisibility(View.GONE);
                        layoutNoAchievements.setVisibility(View.VISIBLE);
                    } else {
                        rvAchievements.setVisibility(View.VISIBLE);
                        layoutNoAchievements.setVisibility(View.GONE);
                    }
                    tvAchievementCount.setText(data.size() + " Achievements");
                    persAchievementAdapter = new PersAchievementAdapter(requireContext(), achievementList);
                    rvAchievements.setAdapter(persAchievementAdapter);
                });
            }

            @Override
            public void onSuccess(JSONObject userObj) {}

            @Override
            public void onFailure(String errorMessage) {
                requireActivity().runOnUiThread(() -> {
                    rvAchievements.setVisibility(View.GONE);
                    layoutNoAchievements.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "fetch achievement list failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }



    private void filterAchievements(String query) {
        List<Achievement> filtered = new ArrayList<>();
        for (Achievement a : achievementList) {
            if (a.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(a);
            }
        }
        requireActivity().runOnUiThread(() -> {
            if (filtered.isEmpty()) {
                rvAchievements.setVisibility(View.GONE);
                layoutNoAchievements.setVisibility(View.VISIBLE);
            } else {
                rvAchievements.setVisibility(View.VISIBLE);
                layoutNoAchievements.setVisibility(View.GONE);
            }
            persAchievementAdapter = new PersAchievementAdapter(requireContext(), filtered);
            rvAchievements.setAdapter(persAchievementAdapter);
        });
    }

}