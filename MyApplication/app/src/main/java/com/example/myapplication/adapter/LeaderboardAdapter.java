package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.LeaderboardItem;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {

    private List<LeaderboardItem> leaderboardList;
    private int currentUserRank; // dùng để highlight người dùng hiện tại

    public LeaderboardAdapter(List<LeaderboardItem> leaderboardList, int currentUserRank) {
        this.leaderboardList = leaderboardList;
        this.currentUserRank = currentUserRank;
    }

    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rank_card, parent, false);
        return new LeaderboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
        LeaderboardItem item = leaderboardList.get(position);

        holder.tvRankPosition.setText("#" + item.getRank());
        holder.tvUsername.setText(item.getName());
        holder.tvPoints.setText(String.valueOf(item.getScore()));
        holder.imgAvatar.setImageResource(item.getAvatarRes());

        // 🔹 Nếu đây là người dùng hiện tại → tô nền xanh nhạt
        if (item.getRank() == currentUserRank) {
            holder.itemView.setBackgroundResource(R.drawable.bg_current_rank_item);
        } else {
            holder.itemView.setBackgroundResource(android.R.color.transparent);
        }
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
