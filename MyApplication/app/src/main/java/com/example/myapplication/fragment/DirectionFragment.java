package com.example.myapplication.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;

import java.net.URLEncoder;

public class DirectionFragment extends Fragment {

    private ImageView imgRouteMap, btnClose;
    private TextView tvDestinationName, tvTravelInfo;
    private CardView infoCard;
    private static final String ORIGIN = "20.981971,105.864323";
    private static final String DESTINATION = "21.03876,105.79810";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_direction, container, false);

        // Ánh xạ view
        imgRouteMap = view.findViewById(R.id.imgRouteMap);
        tvDestinationName = view.findViewById(R.id.tvDestinationName);
        tvTravelInfo = view.findViewById(R.id.tvTravelInfo);
        btnClose = view.findViewById(R.id.btnClose);
        infoCard = view.findViewById(R.id.infoCard);

        // Set dữ liệu mô phỏng
        tvDestinationName.setText("Keangnam Landmark");
        tvTravelInfo.setText("25 phút - 11.2 km");
        infoCard.setVisibility(View.VISIBLE);

        // Đóng infoCard
        btnClose.setOnClickListener(v -> infoCard.setVisibility(View.GONE));

        // Load ảnh tuyến đường
        loadStaticRoute();

        return view;
    }

    private void loadStaticRoute() {
        try {
            // Đặt kích thước mong muốn (nên để cố định)
            int width = 900;
            int height = 1200;

            String url = "https://rsapi.goong.io/staticmap/route?"
                    + "origin=" + ORIGIN
                    + "&destination=" + DESTINATION
                    + "&vehicle=bike"
                    + "&width=900"
                    + "&height=1200"
                    + "&api_key=" + getString(R.string.goong_api_key);

            Log.d("GoongURL", url);

            // Load bằng Glide
            Glide.with(requireContext())
                    .load(url)
                    .override(width, height)   // scale ảnh đúng kích thước
                    .fitCenter()               // giữ tỉ lệ đẹp
                    .into(imgRouteMap);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Lỗi tạo URL", Toast.LENGTH_SHORT).show();
        }
    }
}
