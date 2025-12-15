package com.example.myapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.model.AIRoute;
import com.example.myapplication.model.Place;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AiRouteAdapter extends RecyclerView.Adapter<AiRouteAdapter.AiRouteViewHolder> {

    public interface OnRouteClickListener {
        void onRouteClick(AIRoute route);
    }

    private List<AIRoute> routes = new ArrayList<>();
    private final OnRouteClickListener listener;

    public AiRouteAdapter(OnRouteClickListener listener) {
        this.listener = listener;
    }

    public void setRoutes(List<AIRoute> newRoutes) {
        if (newRoutes == null) {
            this.routes = new ArrayList<>();
        } else {
            this.routes = new ArrayList<>(newRoutes);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AiRouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ai_route, parent, false);
        return new AiRouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AiRouteViewHolder holder, int position) {
        AIRoute route = routes.get(position);
        holder.bind(route, listener);
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    static class AiRouteViewHolder extends RecyclerView.ViewHolder {

        ImageView ivThumbnail;
        TextView tvTitle;
        TextView tvDescription;
        TextView tvDetails;

        public AiRouteViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDetails = itemView.findViewById(R.id.tvDetails);
        }

        public void bind(AIRoute route, OnRouteClickListener listener) {
            Context context = itemView.getContext();

            tvTitle.setText(route.getTitle());
            tvDescription.setText(route.getDescription());

            String distanceText = String.format(
                    Locale.getDefault(),
                    "%.2f km - %s",
                    route.getDistanceKm(),
                    route.getDuration()
            );
            tvDetails.setText(distanceText);

            // ảnh: lấy ảnh của điểm dừng đầu tiên (nếu có)
            List<Place> stops = route.getStops();
            if (stops != null && !stops.isEmpty()) {
                String imageUrl = stops.get(0).getPictureURL();
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.bg_place_card)   // drawable bạn đã tạo
                        .error(R.drawable.bg_place_card)
                        .into(ivThumbnail);
            } else {
                ivThumbnail.setImageResource(R.drawable.bg_place_card );
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRouteClick(route);
                }
            });
        }
    }
}
