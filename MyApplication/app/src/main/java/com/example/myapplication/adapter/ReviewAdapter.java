package com.example.myapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.colormoon.readmoretextview.ReadMoreTextView;
import com.example.myapplication.R;
import com.example.myapplication.model.Review;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private Context context;
    private List<Review> reviewList;

    public ReviewAdapter(Context context, List<Review> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
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

        // --- Gán dữ liệu cơ bản ---
        holder.tvUserName.setText(review.getName());
        holder.tvSubtitle.setText(review.getSubtitle());
        holder.tvTime.setText(review.getTime());
        holder.tvLikeCount.setText(String.valueOf(review.getLikeCount()));
        holder.ratingBar.setRating(review.getRating());
        holder.tvReviewContent.setText(review.getContent());
        holder.tvReviewContent.setCollapsedText("More");
        holder.tvReviewContent.setExpandedText("Less");
        holder.tvReviewContent.setCollapsedTextColor(R.color.blue);
        holder.tvReviewContent.setExpandedTextColor(R.color.blue);
        holder.tvReviewContent.setTrimLines(2);
        // --- Ẩn/hiện phần ảnh ---
        holder.imageGrid.removeAllViews();

        int[] imageResIds = review.getImageResIds();
        if (imageResIds != null && imageResIds.length > 0) {
            holder.imageGrid.setVisibility(View.VISIBLE);

            int maxImages = Math.min(imageResIds.length, 4);
            int imageSize = (int) (400 * context.getResources().getDisplayMetrics().density / 2.5); // khoảng 160dp

            for (int i = 0; i < maxImages; i++) {
                ImageView img = new ImageView(context);
                // --- Tỷ lệ hình chữ nhật ---
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                int imageWidth = (int) (150 * context.getResources().getDisplayMetrics().density);   // ~150dp
                int imageHeight = (int) (100 * context.getResources().getDisplayMetrics().density);  // ~100dp
                params.width = imageWidth;
                params.height = imageHeight;
                params.setMargins(8, 8, 8, 8);

                img.setLayoutParams(params);
                img.setScaleType(ImageView.ScaleType.CENTER_CROP);
                img.setImageResource(imageResIds[i]);

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

    // --- ViewHolder ---
    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvSubtitle, tvTime, tvLikeCount;
        ReadMoreTextView tvReviewContent;
        RatingBar ratingBar;
        GridLayout imageGrid;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvName);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            tvReviewContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            imageGrid = itemView.findViewById(R.id.imageGrid);
        }
    }
}
