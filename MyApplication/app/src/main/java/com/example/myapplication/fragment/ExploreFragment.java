package com.example.myapplication.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.adapter.PlaceAdapter;
import com.example.myapplication.adapter.RouteAdapter;
import com.example.myapplication.model.Place;
import com.example.myapplication.model.Route;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.List;

public class ExploreFragment extends Fragment {

    private BottomSheetBehavior<View> bottomSheetBehavior;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        // Setup Bottom Sheet
        setupBottomSheet(view);
        return view;
    }

    private void setupBottomSheet(View view) {
        View bottomSheet = view.findViewById(R.id.bottomSheetExplore);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setPeekHeight(450);

        ImageView btnClose = view.findViewById(R.id.btnCloseExplore);
        btnClose.setOnClickListener(v -> bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED));

        setupIconicPlaces(view);
        setupTopVisited(view);
        setupPopularNearYou(view);
        setupSuggestedRoutes(view);
    }

    // ======= RecyclerViews setup =======
    private void setupIconicPlaces(View view) {
        RecyclerView rv = view.findViewById(R.id.rvIconicPlaces);
        rv.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        List<Place> list = new ArrayList<>();

        // 1. Hoan Kiem Lake
        List<String> hoanKiemImages = new ArrayList<>();
        hoanKiemImages.add("https://res.cloudinary.com/dsm1uhecl/image/upload/v1759653241/ho_hoan_kiem_mgbnsy.jpg");
        hoanKiemImages.add("https://res.cloudinary.com/dsm1uhecl/image/upload/v1759654177/lang_bac_a5tcsy.jpg");
        hoanKiemImages.add("https://res.cloudinary.com/dsm1uhecl/image/upload/v1759654176/Nh%C3%A0_H%C3%A1t_L%E1%BB%9Bn_H%C3%A0_N%E1%BB%99i_njilod.jpg");
        list.add(new Place(
                "Hoan Kiem Lake",
                "Lake featuring a temple on a small island reached via a wooden bridge and a tower on another island.",
                "3.6 km",
                hoanKiemImages,
                21.028511, 105.854444
        ));

// 2. Temple of Literature
        List<String> vanMieuImages = new ArrayList<>();
        vanMieuImages.add("https://res.cloudinary.com/dsm1uhecl/image/upload/v1759653241/ho_hoan_kiem_mgbnsy.jpg"); // URL Cổng chính

        list.add(new Place(
                "Temple of Literature",
                "The first national university in Vietnam, known for its well-preserved traditional Vietnamese architecture.",
                "4.2 km",
                vanMieuImages,
                21.028511, 105.854444
        ));
        rv.setAdapter(new PlaceAdapter(list, place -> openPlaceDetail(place)));
    }

    private void setupTopVisited(View view) {
        RecyclerView rv = view.findViewById(R.id.rvTopVisited);
        rv.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        List<Place> list = new ArrayList<>();

        List<String> dongXuanImages = new ArrayList<>();
        dongXuanImages.add("https://res.cloudinary.com/dsm1uhecl/image/upload/v1759653241/ho_hoan_kiem_mgbnsy.jpg");

        list.add(new Place(
                "Dong Xuan Market",
                "A large indoor market in the heart of the Old Quarter.",
                "2.5 km",
                dongXuanImages,
                21.028511, 105.854444
        ));

        rv.setAdapter(new PlaceAdapter(list, place -> openPlaceDetail(place)));
    }

    private void setupPopularNearYou(View view) {
        RecyclerView rv = view.findViewById(R.id.rvPopularNearYou);
        rv.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        List<Place> list = new ArrayList<>();

        List<String> operaHouseImages = new ArrayList<>();
        operaHouseImages.add("https://res.cloudinary.com/dsm1uhecl/image/upload/v1759653241/ho_hoan_kiem_mgbnsy.jpg");

        list.add(new Place(
                "Opera House",
                "A beautiful French colonial architectural landmark built in 1911.",
                "2.3 km",
                operaHouseImages,
                21.028511, 105.854444
        ));

        rv.setAdapter(new PlaceAdapter(list, place -> openPlaceDetail(place)));
    }

    private void setupSuggestedRoutes(View view) {
        RecyclerView rv = view.findViewById(R.id.rvSuggestedRoutes);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        List<Route> routes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            routes.add(new Route("Route " + (i+1), "Sample description", "6.3 km", "20m 36s", R.drawable.hoguom));
        }
        rv.setAdapter(new RouteAdapter(routes));
    }

    private void openPlaceDetail(Place place) {
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof MapFragment) {
            ((MapFragment) parentFragment).openPlaceDetailFragment(place);
        }
    }
}
