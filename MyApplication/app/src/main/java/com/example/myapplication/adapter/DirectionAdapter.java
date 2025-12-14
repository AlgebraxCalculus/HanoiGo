package com.example.myapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Direction;

import java.util.List;

public class DirectionAdapter extends RecyclerView.Adapter<DirectionAdapter.ViewHolder> {
    private List<Direction> directions;
    private Context context;

    public DirectionAdapter(Context context, List<Direction> directions) {
        this.context = context;
        this.directions = directions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_direction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Direction direction = directions.get(position);

        holder.tvInstruction.setText(direction.getInstruction());
        holder.tvDistance.setText(direction.getDistance());
        int iconRes = getManeuverIcon(direction.getManeuver());
        holder.ivIcon.setImageResource(iconRes);

        // Hide divider for last item
        if (position == directions.size() - 1) {
            holder.divider.setVisibility(View.GONE);
        } else {
            holder.divider.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return directions.size();
    }

    private int getManeuverIcon(String maneuver) {
        switch (maneuver) {
            case "left": return R.drawable.ic_turn_left;
            case "right": return R.drawable.ic_turn_right;
            case "": return R.drawable.ic_destination;
            default: return R.drawable.ic_go_straight;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvInstruction, tvDistance;
        View divider;

        ViewHolder(View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivDirectionIcon);
            tvInstruction = itemView.findViewById(R.id.tvDirectionInstruction);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            divider = itemView.findViewById(R.id.divider);
        }
    }
}
