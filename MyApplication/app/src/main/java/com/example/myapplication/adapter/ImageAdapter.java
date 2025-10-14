package com.example.myapplication.adapter;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private final List<String> imageUrls;
    private final Context context;

    public ImageAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_place_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        // 3 ảnh: imgMain, imgSub1, imgSub2
        if (imageUrls.size() < 3) return;

        loadImage(holder.imgMain, imageUrls.get(0));
        loadImage(holder.imgSub1, imageUrls.get(1));
        loadImage(holder.imgSub2, imageUrls.get(2));

        // Click listener mở ảnh full-screen
        holder.imgMain.setOnClickListener(v -> showFullScreenImage(imageUrls.get(0)));
        holder.imgSub1.setOnClickListener(v -> showFullScreenImage(imageUrls.get(1)));
        holder.imgSub2.setOnClickListener(v -> showFullScreenImage(imageUrls.get(2)));
    }

    private void loadImage(ImageView iv, String url) {
        Glide.with(context)
                .load(url)
                .into(iv);
    }

    private void showFullScreenImage(String url) {
        Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        ImageView iv = new ImageView(context);
        Glide.with(context).load(url).into(iv);
        dialog.setContentView(iv);
        iv.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return 1; // Chỉ 1 item chứa 3 ảnh
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imgMain, imgSub1, imgSub2;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imgMain = itemView.findViewById(R.id.imgMain);
            imgSub1 = itemView.findViewById(R.id.imgSub1);
            imgSub2 = itemView.findViewById(R.id.imgSub2);
        }
    }
}
