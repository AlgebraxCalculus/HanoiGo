// java
package com.example.myapplication;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.myapplication.fragment.HomeFragment;
import com.example.myapplication.fragment.MapFragment;
import com.example.myapplication.api.FirebaseMessagingApi;
import com.example.myapplication.fragment.PersonalFragment;
import com.example.myapplication.model.Place;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    private Fragment homeFragment;
    private Fragment mapFragment;
    private Fragment personalFragment;
    private Fragment activeFragment;

    private double userLat = 0.0;
    private double userLng = 0.0;
    private FusedLocationProviderClient fusedLocationClient;
    private static final float LOCATION_UPDATE_THRESHOLD_METERS = 15f; // chỉ update khi di chuyển > 15m

    private double lastLat = 0.0;
    private double lastLng = 0.0;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ====== FULLSCREEN MODE ======
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        // ====== POST NOTIFICATIONS PERMISSION ======
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        // ====== NHẬN DỮ LIỆU TỪ LOGIN ======
        String jwtToken = getIntent().getStringExtra("jwtToken");
        String userJson = getIntent().getStringExtra("user");

        SharedPreferences prefs = this.getSharedPreferences("user_prefs", MODE_PRIVATE);
        prefs.edit().putString("jwt_token", jwtToken).apply();

        // ====== LẤY TOKEN FCM THỦ CÔNG SAU KHI LOGIN ======
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Lấy token thủ công
                    String token = task.getResult();
                    Log.d("FCM", "Manual fetched token: " + token);

                    if (jwtToken != null && !jwtToken.isEmpty()) {
                        FirebaseMessagingApi service = new FirebaseMessagingApi();
                        service.sendTokenToServer(jwtToken, token);
                    }
                });

        // ====== KHỞI TẠO BUNDLE DÙNG CHUNG ======
        Bundle sharedBundle = new Bundle();
        sharedBundle.putString("jwtToken", jwtToken);
        sharedBundle.putString("user", userJson);

        // Khởi tạo Fragment
        homeFragment = new HomeFragment();
        mapFragment = new MapFragment();
        personalFragment = new PersonalFragment();

        // Truyền bundle vào cả 2 fragment
        homeFragment.setArguments(sharedBundle);
        mapFragment.setArguments(sharedBundle);
        personalFragment.setArguments(sharedBundle);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Setup location request and callback for real-time updates
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000) // set desired interval for active location updates
                .setFastestInterval(2000);  // set fastest interval for location updates

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                android.location.Location location = locationResult.getLastLocation();
                if (location != null) {
                    double newLat = location.getLatitude();
                    double newLng = location.getLongitude();

                    float[] results = new float[1];
                    android.location.Location.distanceBetween(lastLat, lastLng, newLat, newLng, results);
                    float distanceMoved = results[0];

                    // chỉ update khi user di chuyển hơn 15m
                    if (distanceMoved > LOCATION_UPDATE_THRESHOLD_METERS || lastLat == 0.0) {
                        lastLat = newLat;
                        lastLng = newLng;
                        userLat = newLat;
                        userLng = newLng;

                        if (mapFragment instanceof MapFragment) {
                            ((MapFragment) mapFragment).updateUserLocation(userLat, userLng);
                        }
                        if (homeFragment instanceof HomeFragment) {
                            ((HomeFragment) homeFragment).updateUserLocation(userLat, userLng);
                        }
                    }
                }
            }
        };

        checkLocationPermissionAndGetLocation();

        // Thêm Fragment vào Activity
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragment_container, homeFragment, "HOME");
            transaction.add(R.id.fragment_container, mapFragment, "MAP").hide(mapFragment);
            transaction.add(R.id.fragment_container, personalFragment, "PERSONAL").hide(personalFragment);
            transaction.show(homeFragment);

            transaction.commit();

            activeFragment = homeFragment;
        }

        // ====== XỬ LÝ NÚT MAP ======
        View btnMapFloat = findViewById(R.id.btnMapFloat);
        if (btnMapFloat != null) {
            btnMapFloat.setOnClickListener(v -> switchFragment(mapFragment));
        }

        // ====== XỬ LÝ NÚT HOME ======
        View btnHomepage = findViewById(R.id.btnHomepage);
        if (btnHomepage != null) {
            btnHomepage.setOnClickListener(v -> switchFragment(homeFragment));
        }

        // ====== XỬ LÝ NÚT PERSONAL ======
        View btnPersonal = findViewById(R.id.btnPersonal);
        if (btnPersonal != null) {
            btnPersonal.setOnClickListener(v -> switchFragment(personalFragment));
        }
    }

    // ====== HÀM XIN QUYỀN VỊ TRÍ ======
    private void checkLocationPermissionAndGetLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastKnownLocation();
        }
    }

    private void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                userLat = location.getLatitude();
                userLng = location.getLongitude();

                // Gửi toạ độ sang MapFragment
                if (mapFragment instanceof MapFragment) {
                    ((MapFragment) mapFragment).updateUserLocation(userLat, userLng);
                }

                // Gửi toạ độ sang HomeFragment
                if (homeFragment instanceof HomeFragment) {
                    ((HomeFragment) homeFragment).updateUserLocation(userLat, userLng);
                }
            } else {
                Toast.makeText(this, "Cannot detect your location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastKnownLocation();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ====== HÀM CHUYỂN FRAGMENT ======
    private void switchFragment(Fragment targetFragment) {
        if (targetFragment == activeFragment) return;

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

        if (activeFragment != null) {
            transaction.hide(activeFragment);
        }

        transaction.show(targetFragment);
        transaction.commit();

        activeFragment = targetFragment;
    }

    // ====== ẨN / HIỆN FOOTER ======
    public void setFooterVisibility(boolean visible) {
        View footer = findViewById(R.id.footerContainer);
        View mapButton = findViewById(R.id.btnMapFloatContainer);

        int visibility = visible ? View.VISIBLE : View.GONE;

        if (footer != null) footer.setVisibility(visibility);
        if (mapButton != null) mapButton.setVisibility(visibility);
    }

    public void openPlaceDetailFromHome(Place place) {
        if (mapFragment instanceof MapFragment) {
            switchFragment(mapFragment);
            ((MapFragment) mapFragment).openPlaceDetailFragment(place);
        }
    }
}
