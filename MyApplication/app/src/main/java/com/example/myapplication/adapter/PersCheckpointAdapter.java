package com.example.myapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.model.Checkpoint;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class PersCheckpointAdapter extends RecyclerView.Adapter<PersCheckpointAdapter.CheckpointViewHolder> {

    private Context context;
    private List<Checkpoint> checkpointList;
    private OnCheckpointClickListener listener;

    public interface OnCheckpointClickListener {
        void onCheckpointClick(Checkpoint checkpoint);
    }

    public PersCheckpointAdapter(Context context, List<Checkpoint> checkpointList, OnCheckpointClickListener listener) {
        this.context = context;
        this.checkpointList = checkpointList;
        this.listener = listener;
    }

    @Override
    public CheckpointViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pers_checkpoint, parent, false);
        return new CheckpointViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CheckpointViewHolder holder, int position) {
        Checkpoint checkpoint = checkpointList.get(position);

        // Set place info
        holder.tvPlaceName.setText(checkpoint.getPlace().getName());
        holder.tvPlaceDescription.setText(checkpoint.getPlace().getDescription());

        // Load place image using Glide
        Glide.with(context)
                .load(checkpoint.getPlace().getPictureURL())
//                .placeholder(R.drawable.placeholder_image)
//                .error(R.drawable.placeholder_image)
                .centerCrop()
                .into(holder.ivPlaceImage);

        // Format and set date and time
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        holder.tvCheckpointDate.setText(checkpoint.getDate().format(dateFormatter));
        holder.tvCheckpointTime.setText(checkpoint.getDate().format(timeFormatter));

        // Check if review exists
        if (checkpoint.getReview() != null) {
            // Show review section
            holder.layoutHasReview.setVisibility(View.VISIBLE);
            holder.layoutNoReview.setVisibility(View.GONE);

            // Set rating stars
            int rating = checkpoint.getReview().getRating();
            setStarRating(holder, rating);

            // Set review time
            holder.tvReviewTime.setText(checkpoint.getReview().getTime());

            // Set review content
            holder.tvReviewContent.setText(checkpoint.getReview().getContent());

            // Set click listener for view full review
            holder.btnViewFullReview.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCheckpointClick(checkpoint);
                }
            });
        } else {
            // Show no review section
            holder.layoutHasReview.setVisibility(View.GONE);
            holder.layoutNoReview.setVisibility(View.VISIBLE);

            // Set click listener for write review
            holder.btnWriteReview.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCheckpointClick(checkpoint);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return checkpointList.size();
    }

    private void setStarRating(CheckpointViewHolder holder, int rating) {
        ImageView[] stars = {holder.star1, holder.star2, holder.star3, holder.star4, holder.star5};

        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setImageResource(R.drawable.ic_star_filled);
            } else {
                stars[i].setImageResource(R.drawable.ic_star_empty);
            }
        }
    }

//    public void updateList(List<Checkpoint> newList) {
//        this.checkpointList = newList;
//        notifyDataSetChanged();
//    }

    public static class CheckpointViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPlaceImage;
        TextView tvPlaceName, tvPlaceDescription, tvCheckpointDate, tvCheckpointTime;
        LinearLayout layoutHasReview, layoutNoReview;

        // Has review views
        ImageView star1, star2, star3, star4, star5;
        TextView tvReviewTime, tvReviewContent, btnViewFullReview;

        // No review views
        TextView btnWriteReview;

        public CheckpointViewHolder(View itemView) {
            super(itemView);

            // Place info
            ivPlaceImage = itemView.findViewById(R.id.ivPlaceImage);
            tvPlaceName = itemView.findViewById(R.id.tvPlaceName);
            tvPlaceDescription = itemView.findViewById(R.id.tvPlaceDescription);
            tvCheckpointDate = itemView.findViewById(R.id.tvCheckpointDate);
            tvCheckpointTime = itemView.findViewById(R.id.tvCheckpointTime);

            // Review sections
            layoutHasReview = itemView.findViewById(R.id.layoutHasReview);
            layoutNoReview = itemView.findViewById(R.id.layoutNoReview);

            // Has review
            star1 = itemView.findViewById(R.id.star1);
            star2 = itemView.findViewById(R.id.star2);
            star3 = itemView.findViewById(R.id.star3);
            star4 = itemView.findViewById(R.id.star4);
            star5 = itemView.findViewById(R.id.star5);
            tvReviewTime = itemView.findViewById(R.id.tvReviewTime);
            tvReviewContent = itemView.findViewById(R.id.tvReviewContent);
            btnViewFullReview = itemView.findViewById(R.id.btnViewFullReview);

            // No review
            btnWriteReview = itemView.findViewById(R.id.btnWriteReview);
        }
    }
}