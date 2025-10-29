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

import java.util.List;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder> {

    private Context context;
    private List<Achievement> achievementList;

    public AchievementAdapter(Context context, List<Achievement> achievementList) {
        this.context = context;
        this.achievementList = achievementList;
    }

    @Override
    public AchievementViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_achievement, parent, false);
        return new AchievementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AchievementViewHolder holder, int position) {
        Achievement item = achievementList.get(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvDescription.setText(item.getDescription());
        holder.tvBadge.setText(item.getBadgeLevel());
        holder.imgIcon.setImageResource(item.getResBadgeImage());

        String level = item.getBadgeLevel().trim().toUpperCase();

        if (level.equals("TIER SSS") || level.equals("SSS")) {
            holder.tvBadge.setBackgroundResource(R.drawable.bg_tier_sss);
        }else if (level.equals("TIER SS") || level.equals("SS")) {
            holder.tvBadge.setBackgroundResource(R.drawable.bg_tier_ss);
        } else if (level.equals("TIER S+") || level.equals("S+")) {
            holder.tvBadge.setBackgroundResource(R.drawable.bg_tier_splus);
        } else if (level.equals("TIER S") || level.equals("S")) {
            holder.tvBadge.setBackgroundResource(R.drawable.bg_tier_s);
        } else{
            holder.tvBadge.setBackgroundResource(R.drawable.bg_tier_a);
        }
    }

    @Override
    public int getItemCount() {
        return achievementList.size();
    }

    public static class AchievementViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        TextView tvTitle, tvDescription, tvBadge;

        public AchievementViewHolder(View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvBadge = itemView.findViewById(R.id.tvBadge);
        }
    }
}
