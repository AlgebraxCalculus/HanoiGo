package com.example.myapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Achievement;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class PersAchievementAdapter extends RecyclerView.Adapter<PersAchievementAdapter.AchievementViewHolder> {

    private Context context;
    private List<Achievement> achievementList;

    public PersAchievementAdapter(Context context, List<Achievement> achievementList) {
        this.context = context;
        this.achievementList = achievementList;
    }

    @Override
    public AchievementViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pers_achievement, parent, false);
        return new AchievementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AchievementViewHolder holder, int position) {
        Achievement item = achievementList.get(position);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        holder.tvAchievementTitle.setText(item.getTitle());
        holder.tvAchievementDescription.setText(item.getDescription());
        holder.tvTierBadge.setText(item.getBadgeLevel());
        holder.ivBadgeIcon.setImageResource(item.getResBadgeImage());
        holder.tvAchievementDate.setText(item.getDate().format(formatter));

        String level = item.getBadgeLevel().trim().toUpperCase();
        // Determine background resource based on tier level
        int backgroundRes;
        if (level.equals("TIER SSS") || level.equals("SSS")) {
            backgroundRes = R.drawable.bg_tier_sss;
        } else if (level.equals("TIER SS") || level.equals("SS")) {
            backgroundRes = R.drawable.bg_tier_ss;
        } else if (level.equals("TIER S+") || level.equals("S+")) {
            backgroundRes = R.drawable.bg_tier_splus;
        } else if (level.equals("TIER S") || level.equals("S")) {
            backgroundRes = R.drawable.bg_tier_s;
        } else {
            backgroundRes = R.drawable.bg_tier_a;
        }

        // Apply background to both badge icon background and tier badge
        holder.badgeBackground.setBackgroundResource(backgroundRes);
        holder.tvTierBadge.setBackgroundResource(backgroundRes);
    }

    @Override
    public int getItemCount() {
        return achievementList.size();
    }

    public static class AchievementViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBadgeIcon;
        TextView tvAchievementTitle, tvAchievementDescription, tvAchievementDate, tvTierBadge;
        View badgeBackground;

        public AchievementViewHolder(View itemView) {
            super(itemView);
            ivBadgeIcon = itemView.findViewById(R.id.ivBadgeIcon);
            tvAchievementTitle = itemView.findViewById(R.id.tvAchievementTitle);
            tvAchievementDescription = itemView.findViewById(R.id.tvAchievementDescription);
            tvAchievementDate = itemView.findViewById(R.id.tvAchievementDate);
            tvTierBadge = itemView.findViewById(R.id.tvTierBadge);
            badgeBackground = itemView.findViewById(R.id.badgeBackground);
        }
    }
}