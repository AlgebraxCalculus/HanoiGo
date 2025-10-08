package com.example.myapplication;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.PlaceAdapter;
import com.example.myapplication.model.Place;
import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerViewPlaces;
    private PlaceAdapter placeAdapter;
    private List<Place> placeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerViewPlaces = findViewById(R.id.recyclerViewPlaces);

        recyclerViewPlaces.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        placeList = new ArrayList<>();
        placeList.add(new Place("Hoan Kiem Lake", "Beautiful lake in Hanoi center", "2.9 km", R.drawable.hoguom));
        placeList.add(new Place("Temple of Literature", "Ancient university in Hanoi", "4.2 km", R.drawable.hoguom));
        placeList.add(new Place("Old Quarter", "Historic area with narrow streets", "1.2 km", R.drawable.hoguom));

        placeAdapter = new PlaceAdapter(placeList);
        recyclerViewPlaces.setAdapter(placeAdapter);
    }
}
