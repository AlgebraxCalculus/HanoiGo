package com.example.myapplication.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.bumptech.glide.Glide;
import com.example.myapplication.AuthActivity;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.adapter.AchievementAdapter;
import com.example.myapplication.adapter.LeaderboardAdapter;
import com.example.myapplication.api.LocationApi;
import com.example.myapplication.api.UserApi;
import com.example.myapplication.model.Achievement;
import com.example.myapplication.model.LeaderboardItem;
import com.example.myapplication.model.Place;

import com.example.myapplication.adapter.PlaceAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class SettingFragment extends Fragment {

    TextView tvUsername, tvUserEmail;
    ImageView imgUserAvatar, btnBack;
    LinearLayout layoutAboutUs, layoutLogout;
    Fragment previousFragment;
    String username = "default user";
    String avatar = "";
    String email = "user@gmail.com";
    String jwtToken = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        super.onCreate(savedInstanceState);

        // --- lấy thông tin nhận được từ AuthAct
        if (getArguments() != null) {
            jwtToken = getArguments().getString("jwtToken");
        }

        // --- mapper thành phần UI section ---
        tvUsername = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        imgUserAvatar = view.findViewById(R.id.imgUserAvatar);
        btnBack = view.findViewById(R.id.btnBack);
        layoutAboutUs = view.findViewById(R.id.layoutAboutUs);
        layoutLogout = view.findViewById(R.id.layoutLogout);


        setupUserData();
        setupAction();
        return view;
    }
    public void setPreviousFragment(Fragment previousFragment) {
        this.previousFragment = previousFragment;
    }

    private void setupAction(){
        btnBack.setOnClickListener(v -> {
            ((MainActivity) getActivity()).switchFragment(previousFragment);
        });

        layoutAboutUs.setOnClickListener(v -> {
            AboutUsFragment aboutUsFragment = new AboutUsFragment();
            aboutUsFragment.setPreviousFragment(this);
            ((MainActivity) getActivity()).switchFragment(aboutUsFragment);
        });

        layoutLogout.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Sign out")
                    .setMessage("Are you sure you want to sign out?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Sign out", (dialog, which) -> {
                        performLogout();   // 👈 gọi ở đây
                    })
                    .show();
        });
    }

    private void performLogout() {
        Toast.makeText(getContext(), "Logout successfully!", Toast.LENGTH_SHORT).show();
        // Xóa JWT token
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        prefs.edit().remove("jwt_token").apply();

        // Chuyển về màn hình login
        Intent intent = new Intent(requireActivity(), AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        requireActivity().finish(); // Đóng MainActivity
    }


    private void setupUserData(){
        UserApi.getMe(jwtToken, getContext(), new UserApi.UserApiCallback() {
            @Override
            public void onSuccess(ArrayList<JSONObject> dataList) {

            }

            @Override
            public void onSuccess(JSONObject userObj) {
                try {
                    username = userObj.getString("username");
                    avatar = userObj.getString("profilePicture");
                    email = userObj.getString("email");
                    requireActivity().runOnUiThread(() -> {
                        tvUsername.setText(username);
                        tvUserEmail.setText(email);
                        Glide.with(requireContext())
                                .load(avatar)
                                .into(imgUserAvatar);
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
                    });
                }
            }
        });
    }
}

