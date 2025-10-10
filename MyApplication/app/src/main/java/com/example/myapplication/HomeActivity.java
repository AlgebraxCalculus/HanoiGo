package com.example.myapplication;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.AchievementAdapter;
import com.example.myapplication.adapter.HomePlaceAdapter;
import com.example.myapplication.adapter.LeaderboardTopAdapter;
import com.example.myapplication.model.Achievement;
import com.example.myapplication.model.LeaderboardItem;
import com.example.myapplication.model.Place;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerViewIconic;
    private RecyclerView recyclerViewTopVisited;
    private RecyclerView recyclerViewPopular;

    private HomePlaceAdapter adapterIconic;
    private HomePlaceAdapter adapterTopVisited;
    private HomePlaceAdapter adapterPopular;

    private List<Place> listIconic;
    private List<Place> listTopVisited;
    private List<Place> listPopular;

    private RecyclerView recyclerAchievements;
    private AchievementAdapter achievementAdapter;
    private List<Achievement> achievementList;

    private RecyclerView recyclerTop3;
    private LeaderboardTopAdapter topAdapter;
    private List<LeaderboardItem> top3List;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // --- Place sections ---
        recyclerViewIconic = findViewById(R.id.recyclerViewIconic);
        recyclerViewTopVisited = findViewById(R.id.recyclerViewTop);
        recyclerViewPopular = findViewById(R.id.recyclerViewPopular);

        recyclerViewIconic.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewTopVisited.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewPopular.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        setupDummyData();

        adapterIconic = new HomePlaceAdapter(listIconic);
        adapterTopVisited = new HomePlaceAdapter(listTopVisited);
        adapterPopular = new HomePlaceAdapter(listPopular);

        recyclerViewIconic.setAdapter(adapterIconic);
        recyclerViewTopVisited.setAdapter(adapterTopVisited);
        recyclerViewPopular.setAdapter(adapterPopular);

        // --- Achievements section ---
        recyclerAchievements = findViewById(R.id.recyclerAchievements);
        recyclerAchievements.setLayoutManager(new LinearLayoutManager(this));

        setupAchievementData();
        achievementAdapter = new AchievementAdapter(this, achievementList);
        recyclerAchievements.setAdapter(achievementAdapter);

        // --- Leaderboard Top 3 section ---
        recyclerTop3 = findViewById(R.id.recyclerTop3);
        recyclerTop3.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        setupTop3Data();

        topAdapter = new LeaderboardTopAdapter(this, top3List);
        recyclerTop3.setAdapter(topAdapter);
    }

    private void setupDummyData() {
        listIconic = new ArrayList<>();
        listTopVisited = new ArrayList<>();
        listPopular = new ArrayList<>();

        listIconic.add(new Place("Hoan Kiem Lake", "Beautiful lake in Hanoi", "2.9 km", R.drawable.hoguom));
        listIconic.add(new Place("Temple of Literature", "Ancient university", "4.2 km", R.drawable.hoguom));
        listIconic.add(new Place("Old Quarter", "Historic area", "1.5 km", R.drawable.hoguom));

        listTopVisited.add(new Place("West Lake", "Scenic lake area", "5.3 km", R.drawable.hoguom));
        listTopVisited.add(new Place("Ho Chi Minh Mausoleum", "Historical landmark", "3.8 km", R.drawable.hoguom));
        listTopVisited.add(new Place("Vietnam Museum of Ethnology", "Cultural experience", "6.1 km", R.drawable.hoguom));

        listPopular.add(new Place("Dong Xuan Market", "Traditional shopping area", "1.8 km", R.drawable.hoguom));
        listPopular.add(new Place("Hanoi Opera House", "French colonial architecture", "2.3 km", R.drawable.hoguom));
        listPopular.add(new Place("Long Bien Bridge", "Historic bridge over Red River", "3.1 km", R.drawable.hoguom));
    }

    private void setupAchievementData() {
        achievementList = new ArrayList<>();
        achievementList.add(new Achievement("Chiến thần", "Lọt vào top 10 bảng xếp hạng", "RANK SS", R.drawable.ic_medal));
        achievementList.add(new Achievement("Aura farmer", "Bài review đạt 50 lượt like", "RANK S+", R.drawable.ic_medal));
    }

    private void setupTop3Data() {
        top3List = new ArrayList<>();
        top3List.add(new LeaderboardItem(2, "User2", 1136, R.drawable.ic_user));
        top3List.add(new LeaderboardItem(1, "User1", 1234, R.drawable.ic_user));
        top3List.add(new LeaderboardItem(3, "User3", 1080, R.drawable.ic_user));
    }
}
