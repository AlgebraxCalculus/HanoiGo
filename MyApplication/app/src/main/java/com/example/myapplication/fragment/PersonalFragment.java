package com.example.myapplication.fragment;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.api.UserApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class PersonalFragment extends Fragment {
    private Fragment progressFragment, achievementsFragment, checkpointsFragment, activeFragment;
    String jwtToken = "";
    ImageView imgUserAvatar, imgProgress, imgAchievements, imgCheckpoints;
    TextView tvUserName, tvProgress, tvAchievements, tvCheckpoints, tvPoints, tvRank;
    LinearLayout tabProgress, tabAchievements, tabCheckpoints;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_personal, container, false);
        super.onCreate(savedInstanceState);

        imgUserAvatar = view.findViewById(R.id.imgUserAvatar);
        imgProgress = view.findViewById(R.id.imgProgress);
        imgAchievements = view.findViewById(R.id.imgAchievements);
        imgCheckpoints = view.findViewById(R.id.imgCheckpoints);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvProgress = view.findViewById(R.id.tvProgress);
        tvAchievements = view.findViewById(R.id.tvAchievements);
        tvCheckpoints = view.findViewById(R.id.tvCheckpoints);
        tvPoints = view.findViewById(R.id.tvPoints);
        tvRank = view.findViewById(R.id.tvRank);
        tabProgress = view.findViewById(R.id.tabProgress);
        tabAchievements = view.findViewById(R.id.tabAchievements);
        tabCheckpoints = view.findViewById(R.id.tabCheckpoints);


        // --- swipe refresh section ---
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        NestedScrollView scrollView = view.findViewById(R.id.scrollView);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            System.out.println("User pulled to refresh");
            reloadData();
            new Handler().postDelayed(() -> {
                swipeRefreshLayout.setRefreshing(false);
            }, 1000);
        });

        // 👇 Chỉ cho phép refresh nếu đang ở đỉnh trang
        scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                swipeRefreshLayout.setEnabled(scrollY == 0);
            }
        });

        // --- lấy thông tin nhận được từ AuthAct
        if (getArguments() != null) {
            jwtToken = getArguments().getString("jwtToken");
        }

        // Ánh xạ các tab
        LinearLayout tabProgress = view.findViewById(R.id.tabProgress);
        LinearLayout tabAchievements = view.findViewById(R.id.tabAchievements);
        LinearLayout tabCheckpoints = view.findViewById(R.id.tabCheckpoints);

        // Khởi tạo các fragment con
        progressFragment = new ProgressFragment();
        achievementsFragment = new AchievementsFragment();
        checkpointsFragment = new CheckpointsFragment();

        //Truyền jwt vào các fragment con
        Bundle bundle = new Bundle();
        bundle.putString("jwtToken", jwtToken);

        progressFragment.setArguments(bundle);
        achievementsFragment.setArguments(bundle);
        checkpointsFragment.setArguments(bundle);

        // Gắn tất cả fragment nhưng ẩn đi, chỉ show cái đầu
        getChildFragmentManager().beginTransaction()
                .add(R.id.contentContainer, progressFragment, "progress")
                .add(R.id.contentContainer, achievementsFragment, "achievements")
                .add(R.id.contentContainer, checkpointsFragment, "checkpoints")
                .hide(achievementsFragment)
                .hide(checkpointsFragment)
                .commit();
        activeFragment = progressFragment;

        // Sự kiện bấm tab
        tabProgress.setOnClickListener(v -> {
            imgProgress.setImageTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
            tvProgress.setTextColor(Color.parseColor("#FFFFFF"));
            tabProgress.setBackgroundResource(R.drawable.bg_nav_left_active);
            imgAchievements.setImageTintList(ColorStateList.valueOf(Color.parseColor("#021526")));
            tvAchievements.setTextColor(Color.parseColor("#021526"));
            tabAchievements.setBackgroundResource(R.drawable.bg_nav_center_inactive);
            imgCheckpoints.setImageTintList(ColorStateList.valueOf(Color.parseColor("#021526")));
            tvCheckpoints.setTextColor(Color.parseColor("#021526"));
            tabCheckpoints.setBackgroundResource(R.drawable.bg_nav_right_inactive);

            switchTab(progressFragment);
        });
        tabAchievements.setOnClickListener(v -> {
            imgAchievements.setImageTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
            tvAchievements.setTextColor(Color.parseColor("#FFFFFF"));
            tabAchievements.setBackgroundResource(R.drawable.bg_nav_center_active);
            imgProgress.setImageTintList(ColorStateList.valueOf(Color.parseColor("#021526")));
            tvProgress.setTextColor(Color.parseColor("#021526"));
            tabProgress.setBackgroundResource(R.drawable.bg_nav_left_inactive);
            imgCheckpoints.setImageTintList(ColorStateList.valueOf(Color.parseColor("#021526")));
            tvCheckpoints.setTextColor(Color.parseColor("#021526"));
            tabCheckpoints.setBackgroundResource(R.drawable.bg_nav_right_inactive);

            switchTab(achievementsFragment);
        });
        tabCheckpoints.setOnClickListener(v -> {
            imgCheckpoints.setImageTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
            tvCheckpoints.setTextColor(Color.parseColor("#FFFFFF"));
            tabCheckpoints.setBackgroundResource(R.drawable.bg_nav_right_active);
            imgProgress.setImageTintList(ColorStateList.valueOf(Color.parseColor("#021526")));
            tvProgress.setTextColor(Color.parseColor("#021526"));
            tabProgress.setBackgroundResource(R.drawable.bg_nav_left_inactive);
            imgAchievements.setImageTintList(ColorStateList.valueOf(Color.parseColor("#021526")));
            tvAchievements.setTextColor(Color.parseColor("#021526"));
            tabAchievements.setBackgroundResource(R.drawable.bg_nav_center_inactive);

            switchTab(checkpointsFragment);
        });

        reloadData();
        return view;
    }

    private void reloadData(){
        System.out.println("Reload Personal page");
        setupUserData(() -> {
            if(activeFragment == progressFragment) ((ProgressFragment) progressFragment).setupChartData(jwtToken);
            else if(activeFragment == achievementsFragment) ((AchievementsFragment) achievementsFragment).setupAchievementData(jwtToken, "tier", "desc");
        });
    }

    private void setupUserData(Runnable onComplete){
        UserApi.getMe(jwtToken, getContext(), new UserApi.UserApiCallback() {
            @Override
            public void onSuccess(ArrayList<JSONObject> dataList) {

            }

            @Override
            public void onSuccess(JSONObject userObj) {
                try {
                    String username = userObj.getString("username");
                    String points = userObj.getString("points");
                    String avatar = userObj.getString("profilePicture");
                    requireActivity().runOnUiThread(() -> {
                        tvUserName.setText(username);
                        tvPoints.setText(points);
                        Glide.with(requireContext())
                                .load(avatar)
                                .into(imgUserAvatar);

                        if (onComplete != null) onComplete.run();
                    });
                }catch (JSONException e){
                    e.printStackTrace();
                }


            }

            @Override
            public void onFailure(String errorMessage) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "fetch user data failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                        if (onComplete != null) onComplete.run();
                    });
                }
            }
        });

        UserApi.getMyRank(jwtToken, getContext(), new UserApi.UserApiCallback() {
            @Override
            public void onSuccess(ArrayList<JSONObject> dataList) {

            }

            @Override
            public void onSuccess(JSONObject userObj) {
                try {
                    String rank = userObj.getString("result");
                    requireActivity().runOnUiThread(() -> {
                        String suffix;
                        switch (Integer.parseInt(rank) % 10) {
                            case 1: suffix = "st"; break;
                            case 2: suffix = "nd"; break;
                            case 3: suffix = "rd"; break;
                            default: suffix = "th"; break;
                        }
                        tvRank.setText(rank + suffix);

//                        if (onComplete != null) onComplete.run();
                    });
                }catch (JSONException e){
                    e.printStackTrace();
                }


            }

            @Override
            public void onFailure(String errorMessage) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "fetch user data failed: " + errorMessage, Toast.LENGTH_SHORT).show();
//                        if (onComplete != null) onComplete.run();
                    });
                }
            }
        });
    }

    private void switchTab(Fragment targetFragment) {
        if (targetFragment == activeFragment) return;

        getChildFragmentManager().beginTransaction()
                .hide(activeFragment)
                .show(targetFragment)
                .commit();

        activeFragment = targetFragment;
        reloadData();
    }
}
