package com.example.myapplication.adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
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
        String url = imageUrls.get(position);

        // --- Thiết lập kích thước ảnh (VD: hình chữ nhật 350dp x 180dp) ---
        int imageWidth = (int) (350 * context.getResources().getDisplayMetrics().density);
        int imageHeight = (int) (180 * context.getResources().getDisplayMetrics().density);

        ViewGroup.LayoutParams params = holder.imgMain.getLayoutParams();
        params.width = imageWidth;
        params.height = imageHeight;
        holder.imgMain.setLayoutParams(params);

        // --- Bo góc ảnh ---
        holder.imgMain.setBackgroundResource(R.drawable.bg_rounded_image);
        holder.imgMain.setClipToOutline(true);

        // --- Load ảnh với Glide
        Glide.with(context)
                .load(url)
                .centerCrop()
                .into(holder.imgMain);

        // --- Click ảnh → full screen ---
        holder.imgMain.setOnClickListener(v -> {
            Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            ImageView iv = new ImageView(context);
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            Glide.with(context).load(url).into(iv);
            dialog.setContentView(iv);
            iv.setOnClickListener(view -> dialog.dismiss());
            dialog.show();
        });
    }


    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imgMain;
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imgMain = itemView.findViewById(R.id.imgMain);
        }
    }
}
