package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.myapplication.fragment.MapFragment;
import com.example.myapplication.fragment.PlaceDetailFragment;
import com.example.myapplication.model.Place;

public class MainActivity extends AppCompatActivity {

    private Fragment mapFragment;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ẩn status bar & navigation bar để fullscreen
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        // ====== KHỞI TẠO FRAGMENT ======
        mapFragment = new MapFragment();

        // Thêm MapFragment vào container nhưng ẩn đi trước
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragmentContainer, mapFragment, "MAP").hide(mapFragment);
        transaction.commit();

        // Chưa có fragment nào active
        activeFragment = null;

        // Nút Map (nếu có)
        View btnMapFloat = findViewById(R.id.btnMapFloat);
        btnMapFloat.setOnClickListener(v -> switchFragment(mapFragment));
    }

    private void switchFragment(Fragment targetFragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out
        );

        if (activeFragment != null) {
            transaction.hide(activeFragment);
        }

        transaction.show(targetFragment);
        transaction.commit();

        activeFragment = targetFragment;
    }

    public void setFooterVisibility(boolean visible) {
        View footer = findViewById(R.id.footerContainer);
        View mapButton = findViewById(R.id.btnMapFloatContainer);

        int visibility = visible ? View.VISIBLE : View.GONE;

        if (footer != null) footer.setVisibility(visibility);
        if (mapButton != null) mapButton.setVisibility(visibility);
    }
}
