package com.example.myapplication.fragment;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Build;
import android.content.pm.PackageManager;
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
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.api.CloudinaryUploadHelper;
import com.example.myapplication.api.UserApi;
import com.yalantis.ucrop.UCrop;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


public class PersonalFragment extends Fragment {
    private Fragment progressFragment, achievementsFragment, checkpointsFragment, activeFragment;
    String jwtToken = "", targetTab = null;
    ImageView imgUserAvatar, imgProgress, imgAchievements, imgCheckpoints, icSettings;
    TextView tvUserName, tvProgress, tvAchievements, tvCheckpoints, tvPoints, tvRank;
    LinearLayout tabProgress, tabAchievements, tabCheckpoints;
    NestedScrollView nestedScrollView;
    private Uri selectedAvatarUri = null;

    // Avatar selection & cropping
    private static final int PERMISSION_REQUEST_CODE_AVATAR = 102;
    private static final int PICK_IMAGE_REQUEST_AVATAR = 101;
    private static final int UCROP_REQUEST_CODE = 103;




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
        nestedScrollView = view.findViewById(R.id.scrollView);
        icSettings = view.findViewById(R.id.icSettings);

        // Set click listener để chọn avatar
        imgUserAvatar.setOnClickListener(v -> checkPermissionAndPickImage());


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
            targetTab = getArguments().getString("targetTab");
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

        // --- Setting section ---
        icSettings.setOnClickListener(v -> {
            SettingFragment settingFragment = new SettingFragment();
            settingFragment.setArguments(bundle);   // PHẢI gọi trước
            settingFragment.setPreviousFragment(this);
            ((MainActivity) getActivity()).switchFragment(settingFragment);
        });

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

        //Xử lý chuyển tab nếu nhận được targetTab
        if(targetTab!=null){
            if(targetTab.equals("achievements")) tabAchievements.post(() -> tabAchievements.performClick());
            else if(targetTab.equals("checkpoints")) tabCheckpoints.post(() -> tabCheckpoints.performClick());
        }else{
            reloadData();
        }
        return view;
    }

    public void reloadData(){
        System.out.println("Reload Personal page");
        setupUserData(() -> {
            if(activeFragment == progressFragment) ((ProgressFragment) progressFragment).setupChartData(jwtToken);
            else if(activeFragment == achievementsFragment) ((AchievementsFragment) achievementsFragment).setupAchievementData(jwtToken, "tier", "desc");
            else ((CheckpointsFragment) checkpointsFragment).setupCheckpointData(jwtToken, "rating", "best", "all");
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

    public void resetScroll() {
        // Giả định bạn đã ánh xạ NestedScrollView:
        if (nestedScrollView != null) {
            // Cuộn về vị trí (0, 0)
            nestedScrollView.scrollTo(0, 0);
        }
    }

    //====================================================

    //-----------------AVATAR SELECTION SECTION----------

    //====================================================

    /**
     * Kiểm tra permission và mở image picker
     */
    private void checkPermissionAndPickImage() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            if (ContextCompat.checkSelfPermission(requireContext(),
                    android.Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                openImagePickerForAvatar();
            } else {
                requestPermissions(
                        new String[]{android.Manifest.permission.READ_MEDIA_IMAGES},
                        PERMISSION_REQUEST_CODE_AVATAR
                );
            }
        } else {
            // Android 12 trở xuống
            if (ContextCompat.checkSelfPermission(requireContext(),
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                openImagePickerForAvatar();
            } else {
                requestPermissions(
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE_AVATAR
                );
            }
        }
    }

    /**
     * Mở image picker từ library
     */
    private void openImagePickerForAvatar() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Avatar"), PICK_IMAGE_REQUEST_AVATAR);
    }

    /**
     * Xử lý kết quả từ image picker hoặc crop activity
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST_AVATAR && resultCode == getActivity().RESULT_OK && data != null) {
            // Người dùng chọn ảnh từ library
            Uri sourceUri = data.getData();
            if (sourceUri != null) {
                launchImageCrop(sourceUri);
            }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == getActivity().RESULT_OK) {
            // Người dùng đã cắt ảnh xong
            final Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                uploadAvatarAndUpdateProfile(resultUri);
            }
        } else if (requestCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            if (cropError != null) {
                Toast.makeText(getContext(), "Crop error: " + cropError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Mở UCrop activity để cắt ảnh theo hình vuông
     */
    private void launchImageCrop(Uri sourceUri) {
        // Tạo file để lưu ảnh sau khi cắt
        File destinationFile = new File(requireContext().getCacheDir(), "avatar_" + UUID.randomUUID() + ".jpg");
        Uri destinationUri = Uri.fromFile(destinationFile);

        UCrop.Options options = new UCrop.Options();
        options.setCompressionQuality(90);
        options.withAspectRatio(1f, 1f); // Hình vuông 1:1
        options.withMaxResultSize(500, 500); // Max 500x500px

        UCrop.of(sourceUri, destinationUri)
                .withOptions(options)
                .start(requireContext(), this, UCrop.REQUEST_CROP);
    }

    /**
     * Cập nhật avatar ImageView với ảnh đã chọn
     */

    private void uploadAvatarAndUpdateProfile(Uri imageUri) {
        Glide.with(requireContext())
                .load(imageUri)
                .circleCrop() // Nếu avatar là circular
                .into(imgUserAvatar);

        CloudinaryUploadHelper.uploadImages(getContext(),
                Arrays.asList(imageUri),
                new CloudinaryUploadHelper.UploadCallback() {
                    @Override
                    public void onSuccess(List<String> imageUrls) {
                        // Gọi API update avatar với imageUrls.get(0)
                        UserApi.updateMyAvatar(jwtToken, imageUrls.get(0), getContext(), new UserApi.UserApiCallback() {
                            @Override
                            public void onSuccess(ArrayList<JSONObject> dataList) {

                            }

                            @Override
                            public void onSuccess(JSONObject userObj) {
                                try {
                                    String avatar = userObj.getString("profilePicture");
                                    requireActivity().runOnUiThread(() -> {
                                        // Hiển thị toast thành công
                                        Toast.makeText(getContext(), "Your avatar is updated!", Toast.LENGTH_SHORT).show();
                                    });
                                }catch (JSONException e){
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                if (isAdded()) {
                                    requireActivity().runOnUiThread(() -> {
                                        Toast.makeText(requireContext(), "update avatar failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                                    });
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(getContext(),
                                "Upload image failed: " + errorMessage,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgress(int current, int total) {}
                });
    }

    /**
     * Xử lý kết quả request permission
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE_AVATAR) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePickerForAvatar();
            } else {
                Toast.makeText(getContext(), "Permission denied to access images", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
