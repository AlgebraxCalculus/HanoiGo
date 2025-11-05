package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.model.LeaderboardItem;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {

    private List<LeaderboardItem> leaderboardList;

    public LeaderboardAdapter(List<LeaderboardItem> leaderboardList) {
        this.leaderboardList = leaderboardList;
    }

    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new LeaderboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
        LeaderboardItem item = leaderboardList.get(position);

        holder.tvRankPosition.setText("#" + item.getRank());
        holder.tvUsername.setText(item.getName());
        holder.tvPoints.setText(String.valueOf(item.getScore()));
//        holder.imgAvatar.setImageResource(item.getAvatarRes());
        Glide.with(holder.itemView.getContext())
                .load(item.getAvatar())
                .into(holder.imgAvatar);

        holder.itemView.setBackgroundResource(android.R.color.transparent);
    }

    @Override
    public int getItemCount() {
        return leaderboardList != null ? leaderboardList.size() : 0;
    }

    static class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        TextView tvRankPosition, tvUsername, tvPoints;
        ImageView imgAvatar, imgPointIcon;

        public LeaderboardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRankPosition = itemView.findViewById(R.id.tvRankPosition);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvPoints = itemView.findViewById(R.id.tvPoints);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            imgPointIcon = itemView.findViewById(R.id.imgPointIcon);
        }
    }
}
