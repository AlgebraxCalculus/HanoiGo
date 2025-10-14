package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout; // 🔹 Thêm import
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.adapter.AchievementAdapter;
import com.example.myapplication.adapter.LeaderboardAdapter;
import com.example.myapplication.api.LocationApi;
import com.example.myapplication.api.ResetPassApi;
import com.example.myapplication.api.UserApi;
import com.example.myapplication.model.Achievement;
import com.example.myapplication.model.LeaderboardItem;
import com.example.myapplication.model.Place;

import com.example.myapplication.adapter.PlaceAdapter;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewIconic;
    private RecyclerView recyclerViewTopVisited;
    private RecyclerView recyclerViewPopularNearU;

    private PlaceAdapter adapterIconic;
    private PlaceAdapter adapterTopVisited;
    private PlaceAdapter adapterPopularNearU;

    private List<Place> listIconic;
    private List<Place> listTopVisited;
    private List<Place> listPopularNearU;

    private RecyclerView recyclerAchievements;
    private AchievementAdapter achievementAdapter;
    private List<Achievement> achievementList;

    // 🔹 Thêm layout hiển thị khi không có thành tích
    private LinearLayout layoutNoAchievements;

    private RecyclerView recyclerLeaderboard;
    private LeaderboardAdapter leaderboardAdapter;
    private List<LeaderboardItem> leaderboardList;


    TextView tvUsername, tvPoints, tvRank, tvAchieveCount, tvTopAchievement, tvTop1Name, tvTop2Name, tvTop3Name, tvTop1Points, tvTop2Points, tvTop3Points;
    ImageView imgUserAvatar, imgTop1Avatar, imgTop2Avatar, imgTop3Avatar;
    String username = "default user";
    String avatar = "";
    String points = "0";
    String jwtToken = "";
    double userLat = 0, userLng = 0;



    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // --- lấy thông tin nhận được từ AuthAct

        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            jwtToken = getArguments().getString("jwtToken");
            String userJson = getArguments().getString("user");

            try {
                JSONObject userObj = new JSONObject(userJson);
                username = userObj.getString("username");
                avatar = userObj.getString("profilePicture");
                points = userObj.getString("points");

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //lat_lng nhà tao
        userLat = 21.005147582587608;
        userLng = 105.86326519584026;


        // --- mapper thành phần UI section ---
         tvUsername = view.findViewById(R.id.tvUserName);
         tvPoints = view.findViewById(R.id.tvPointsCount);
         imgUserAvatar = view.findViewById(R.id.imgUserAvatar);
         tvRank = view.findViewById(R.id.tvRankCount);
         tvAchieveCount = view.findViewById(R.id.tvAchievementCount);
         tvTopAchievement = view.findViewById(R.id.tvTopAchievement);
         tvTop1Name = view.findViewById(R.id.tvTop1Name);
         tvTop2Name = view.findViewById(R.id.tvTop2Name);
         tvTop3Name = view.findViewById(R.id.tvTop3Name);
         tvTop1Points = view.findViewById(R.id.tvTop1Points);
         tvTop2Points = view.findViewById(R.id.tvTop2Points);
         tvTop3Points = view.findViewById(R.id.tvTop3Points);
         imgTop1Avatar = view.findViewById(R.id.imgTop1Avatar);
         imgTop2Avatar = view.findViewById(R.id.imgTop2Avatar);
         imgTop3Avatar = view.findViewById(R.id.imgTop3Avatar);

        // --- CardStats section ---
        tvUsername.setText(username);
        tvPoints.setText(points);
        Glide.with(this)
                .load(avatar)
                .into(imgUserAvatar);



        // --- Place sections ---
        recyclerViewIconic = view.findViewById(R.id.recyclerViewIconic);
        recyclerViewTopVisited = view.findViewById(R.id.recyclerViewTop);
        recyclerViewPopularNearU = view.findViewById(R.id.recyclerViewPopularNearU);

        recyclerViewIconic.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewTopVisited.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewPopularNearU.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        setupPlaceData();

        // --- Achievements section ---
        recyclerAchievements = view.findViewById(R.id.recyclerAchievements);
        layoutNoAchievements = view.findViewById(R.id.layoutNoAchievements);
        recyclerAchievements.setLayoutManager(new LinearLayoutManager(requireContext()));
        setupAchievementData(jwtToken);

        // --- Full Leaderboard section ---
        recyclerLeaderboard = view.findViewById(R.id.recyclerViewLeaderboard);
        recyclerLeaderboard.setLayoutManager(new LinearLayoutManager(requireContext()));
        setupLeaderboardData();

        return view;
    }

    private void setupPlaceData() {
        listIconic = new ArrayList<>();
        listTopVisited = new ArrayList<>();
        listPopularNearU = new ArrayList<>();

        //setUp listIconic
        LocationApi.GetLocationList(userLat, userLng,"Iconic", false, false, getContext(), new LocationApi.LocationApiCallback() {
            @Override
            public void onSuccess(ArrayList<JSONObject> data) {
                for(JSONObject a : data){
                    try {
                        listIconic.add(new Place(a.getJSONObject("locationResponse").getString("name"), a.getJSONObject("locationResponse").getString("description"), a.getString("distanceText"), a.getJSONObject("locationResponse").getString("defaultPicture")));
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
                requireActivity().runOnUiThread(() -> {
                    adapterIconic = new PlaceAdapter(listIconic);
                    recyclerViewIconic.setAdapter(adapterIconic);
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                if (isAdded()) { // tránh crash nếu fragment đã bị remove
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "fetch location list failed: " + errorMessage, Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });

        //setUp listTopVisited
        LocationApi.GetLocationList(userLat, userLng,"", true, false, getContext(), new LocationApi.LocationApiCallback() {
            @Override
            public void onSuccess(ArrayList<JSONObject> data) {
                for(JSONObject a : data){
                    try {
                        listTopVisited.add(new Place(a.getJSONObject("locationResponse").getString("name"), a.getJSONObject("locationResponse").getString("description"), a.getString("distanceText"), a.getJSONObject("locationResponse").getString("defaultPicture")));
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
                requireActivity().runOnUiThread(() -> {
                    adapterTopVisited = new PlaceAdapter(listTopVisited);
                    recyclerViewTopVisited.setAdapter(adapterTopVisited);
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                if (isAdded()) { // tránh crash nếu fragment đã bị remove
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "fetch location list failed: " + errorMessage, Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });

        //setUp listPopularNearU
        LocationApi.GetLocationList(userLat, userLng,"", false, true, getContext(), new LocationApi.LocationApiCallback() {
            @Override
            public void onSuccess(ArrayList<JSONObject> data) {
                for(JSONObject a : data){
                    try {
                        listPopularNearU.add(new Place(a.getJSONObject("locationResponse").getString("name"), a.getJSONObject("locationResponse").getString("description"), a.getString("distanceText"), a.getJSONObject("locationResponse").getString("defaultPicture")));
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
                requireActivity().runOnUiThread(() -> {
                    adapterPopularNearU = new PlaceAdapter(listPopularNearU);
                    recyclerViewPopularNearU.setAdapter(adapterPopularNearU);
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                if (isAdded()) { // tránh crash nếu fragment đã bị remove
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "fetch location list failed: " + errorMessage, Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private void setupAchievementData(String jwt) {
        achievementList = new ArrayList<>();

        UserApi.GetMyAchievementList(jwt,getContext(), new UserApi.UserApiCallback() {
            @Override
            public void onSuccess(ArrayList<JSONObject> data) {
                int num = 1;
                String topAchievemnent = "";
                for(JSONObject a : data){
                    try {
                        if(num == 1) topAchievemnent = a.getString("name");
                        if(num>3) break;
                        achievementList.add(new Achievement(a.getString("name"), a.getString("description"), "Tier "+a.getString("tier"), R.drawable.ic_medal));
                        num++;
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
                String FinalTopAchievement = topAchievemnent;
                requireActivity().runOnUiThread(() -> {
                    if (achievementList == null || achievementList.isEmpty()) {
                        recyclerAchievements.setVisibility(View.GONE);
                        layoutNoAchievements.setVisibility(View.VISIBLE);
                    } else {
                        recyclerAchievements.setVisibility(View.VISIBLE);
                        layoutNoAchievements.setVisibility(View.GONE);
                    }
                    tvTopAchievement.setText(FinalTopAchievement);
                    tvAchieveCount.setText(String.valueOf(data.size()));
                    achievementAdapter = new AchievementAdapter(requireContext(), achievementList);
                    recyclerAchievements.setAdapter(achievementAdapter);
                });
            }

            @Override
            public void onSuccess(JSONObject userObj) {}

            @Override
            public void onFailure(String errorMessage) {
                requireActivity().runOnUiThread(() -> {
                    recyclerAchievements.setVisibility(View.GONE);
                    layoutNoAchievements.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "fetch achievement list failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setupLeaderboardData() {
        leaderboardList = new ArrayList<>();

        UserApi.GetUserListOrderByPoints(getContext(), new UserApi.UserApiCallback() {
            @Override
            public void onSuccess(ArrayList<JSONObject> data) {
                int num = 1;
                int my_rank = -1;

                for (JSONObject a : data) {
                    try {
                        if (a.getString("username").equals(username)) my_rank = num;
                        leaderboardList.add(new LeaderboardItem(
                                num++,
                                a.getString("username"),
                                Integer.parseInt(a.getString("points")),
                                a.getString("profilePicture")
                        ));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                int finalMyRank = my_rank;

                requireActivity().runOnUiThread(() -> {
                    // --- Hiển thị top 3 ---
                    if (leaderboardList.size() >= 3) {
                        tvTop1Name.setText(leaderboardList.get(0).getName());
                        tvTop1Points.setText(String.valueOf(leaderboardList.get(0).getScore()));
                        Glide.with(requireContext())
                                .load(leaderboardList.get(0).getAvatar())
                                .into(imgTop1Avatar);

                        tvTop2Name.setText(leaderboardList.get(1).getName());
                        tvTop2Points.setText(String.valueOf(leaderboardList.get(1).getScore()));
                        Glide.with(requireContext())
                                .load(leaderboardList.get(1).getAvatar())
                                .into(imgTop2Avatar);

                        tvTop3Name.setText(leaderboardList.get(2).getName());
                        tvTop3Points.setText(String.valueOf(leaderboardList.get(2).getScore()));
                        Glide.with(requireContext())
                                .load(leaderboardList.get(2).getAvatar())
                                .into(imgTop3Avatar);
                    }

                    // --- Chèn người dùng hiện tại vào vị trí 3 ---
                    leaderboardList.add(3, leaderboardList.get(finalMyRank - 1));

                    // --- Cắt list giữ từ index 3 trở đi ---.
                    if (leaderboardList.size() > 3) {
                        leaderboardList = new ArrayList<>(leaderboardList.subList(3, leaderboardList.size()));
                    } else {
                        LeaderboardItem li = leaderboardList.get(-1);
                        leaderboardList = new ArrayList<>();
                        leaderboardList.add(li);
                    }

                    // --- Cập nhật RecyclerView sau khi đã xử lý dữ liệu ---
                    leaderboardAdapter = new LeaderboardAdapter(leaderboardList);
                    recyclerLeaderboard.setAdapter(leaderboardAdapter);

                    String suffix;
                    switch (finalMyRank % 10) {
                        case 1: suffix = "st"; break;
                        case 2: suffix = "nd"; break;
                        case 3: suffix = "rd"; break;
                        default: suffix = "th"; break;
                    }
                    tvRank.setText(String.valueOf(finalMyRank) + suffix);
                });
            }

            @Override
            public void onSuccess(JSONObject userObj) {}

            @Override
            public void onFailure(String errorMessage) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "fetch leaderboard failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
