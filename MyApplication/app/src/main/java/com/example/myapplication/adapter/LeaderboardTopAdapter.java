package com.example.myapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.LeaderboardItem;

import java.util.List;

public class LeaderboardTopAdapter extends RecyclerView.Adapter<LeaderboardTopAdapter.ViewHolder> {

    private Context context;
    private List<LeaderboardItem> top3List;

    public LeaderboardTopAdapter(Context context, List<LeaderboardItem> top3List) {
        this.context = context;
        this.top3List = top3List;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_leaderboard_top, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderboardItem item = top3List.get(position);

        holder.tvNameTop.setText(item.getName());
        holder.tvScoreTop.setText(String.valueOf(item.getScore()));
        holder.tvRankTop.setText(String.valueOf(item.getRank()));
        holder.imgAvatarTop.setImageResource(item.getAvatarRes());

        switch (item.getRank()) {
            case 1:
                holder.frameAvatarContainer.setBackgroundResource(R.drawable.bg_circle_rank_1);
                holder.tvRankTop.setBackgroundResource(R.drawable.bg_circle_number_1);
                break;
            case 2:
                holder.frameAvatarContainer.setBackgroundResource(R.drawable.bg_circle_rank_2);
                holder.tvRankTop.setBackgroundResource(R.drawable.bg_circle_number_2);
                break;
            case 3:
                holder.frameAvatarContainer.setBackgroundResource(R.drawable.bg_circle_rank_3);
                holder.tvRankTop.setBackgroundResource(R.drawable.bg_circle_number_3);
                break;
            default:
                holder.frameAvatarContainer.setBackgroundResource(0);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return top3List.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        FrameLayout frameAvatarContainer;
        ImageView imgAvatarTop, imgIconScore;
        TextView tvNameTop, tvScoreTop, tvRankTop;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            frameAvatarContainer = itemView.findViewById(R.id.frameAvatarContainer);
            imgAvatarTop = itemView.findViewById(R.id.imgAvatarTop);
            imgIconScore = itemView.findViewById(R.id.imgIconScore);
            tvNameTop = itemView.findViewById(R.id.tvNameTop);
            tvScoreTop = itemView.findViewById(R.id.tvScoreTop);
            tvRankTop = itemView.findViewById(R.id.tvRankTop);
        }
    }
}
