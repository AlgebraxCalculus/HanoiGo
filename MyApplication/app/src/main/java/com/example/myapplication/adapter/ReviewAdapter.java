package com.example.myapplication.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.colormoon.readmoretextview.ReadMoreTextView;
import com.example.myapplication.R;
import com.example.myapplication.api.ReviewApi;
import com.example.myapplication.fragment.CheckpointsFragment;
import com.example.myapplication.model.Review;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private Context context;
    private List<Review> reviewList;
    private int newLikeCount;

    private String placeAddress;
    private String jwtToken;
    private String username;
    private OnMyReviewLikedListener myReviewLikedListener;

    public interface OnMyReviewLikedListener {
        void bindMyReviewData(Review updatedReview);
    }

    public ReviewAdapter(Context context, List<Review> reviewList, String placeAddress, String jwtToken, String username, OnMyReviewLikedListener listener) {
        this.context = context;
        this.reviewList = reviewList;
        this.placeAddress = placeAddress;
        this.jwtToken = jwtToken;
        this.username = username; // Lưu username
        this.myReviewLikedListener = listener; // Lưu listener
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);

        Glide.with(context)
                .load(review.getAvatar())
                .into(holder.imgAvatar);
        holder.tvUserName.setText(review.getName());
        holder.tvTime.setText(review.getTime());
        holder.tvLikeCount.setText(String.valueOf(review.getLikeCount()));
        holder.ratingBar.setRating(review.getRating());
        holder.tvReviewContent.setText(review.getContent());
        holder.tvReviewContent.setCollapsedText("More");
        holder.tvReviewContent.setExpandedText("Less");
        holder.tvReviewContent.setCollapsedTextColor(R.color.blue);
        holder.tvReviewContent.setExpandedTextColor(R.color.blue);
        holder.tvReviewContent.setTrimLines(2);
        holder.imageGrid.removeAllViews();

        List<String> imageUrls = review.getImageUrls();

        if (review.getIsLiked()) {
            holder.btnLike.setImageResource(R.drawable.ic_liked_thumb_up);
        } else {
            holder.btnLike.setImageResource(R.drawable.ic_thumb_up);
        }

        // 🌟 BƯỚC 2: ĐẶT ONCLICK LISTENER CHO btnLike
        holder.btnLike.setOnClickListener(v -> {
            boolean currentlyLiked = review.getIsLiked();
            newLikeCount = review.getLikeCount();

            // Tạm thời vô hiệu hóa nút để tránh double-click
            holder.btnLike.setEnabled(false);

            // Xác định hành động (LIKE/UNLIKE)
            if (!currentlyLiked) {
                newLikeCount += 1;
            } else {
                newLikeCount -= 1;
            }

            // 🌟 GỌI API LIKE/UNLIKE
            ReviewApi.LikeReview(jwtToken, review.getName(), placeAddress, this.context, new ReviewApi.ReviewApiCallback() {
                @Override
                public void onSuccess(ArrayList<JSONObject> data) {

                }

                @Override
                public void onSuccess(String msg) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        // Cập nhật trạng thái
                        review.setIsLiked(!currentlyLiked);
                        review.setLikeCount(newLikeCount); // Cập nhật LikeCount trong Model

                        // Cập nhật UI
                        if (!currentlyLiked) {
                            holder.btnLike.setImageResource(R.drawable.ic_liked_thumb_up);
                        } else {
                            holder.btnLike.setImageResource(R.drawable.ic_thumb_up);
                        }
                        holder.tvLikeCount.setText(String.valueOf(newLikeCount));
                        holder.btnLike.setEnabled(true); // Kích hoạt lại nút

                        if (myReviewLikedListener != null && review.getName().equalsIgnoreCase(username)) {
                            myReviewLikedListener.bindMyReviewData(review);
                        }
                    });
                }

                @Override
                public void onFailure(String errorMessage) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        // Quay lại trạng thái ban đầu nếu thất bại
                        Toast.makeText(context, "Like review failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                        holder.btnLike.setEnabled(true); // Kích hoạt lại nút
                    });
                }
            });
        });

        if (imageUrls != null && !imageUrls.isEmpty()) {
            holder.imageGrid.setVisibility(View.VISIBLE);

            int maxImages = Math.min(imageUrls.size(), 4);
            int imageSize = (int) (400 * context.getResources().getDisplayMetrics().density / 2.5); // khoảng 160dp

            for (int i = 0; i < maxImages; i++) {
                ImageView img = new ImageView(context);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                int imageWidth = (int) (150 * context.getResources().getDisplayMetrics().density);   //150dp
                int imageHeight = (int) (100 * context.getResources().getDisplayMetrics().density);  //100dp
                params.width = imageWidth;
                params.height = imageHeight;
                params.setMargins(8, 8, 8, 8);

                img.setLayoutParams(params);
                img.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Glide.with(context)
                        .load(imageUrls.get(i)) // Tải ảnh từ URL
                        .into(img);

                // --- Bo góc mềm mại hơn ---
                img.setBackgroundResource(R.drawable.bg_rounded_image);
                img.setClipToOutline(true);

                holder.imageGrid.addView(img);
            }
        } else {
            holder.imageGrid.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }
    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvSubtitle, tvTime, tvLikeCount;
        ReadMoreTextView tvReviewContent;
        RatingBar ratingBar;
        GridLayout imageGrid;
        ImageView btnLike, imgAvatar;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvUserName = itemView.findViewById(R.id.tvName);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            tvReviewContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            imageGrid = itemView.findViewById(R.id.imageGrid);
            btnLike = itemView.findViewById(R.id.btnLike);
        }
    }
}
