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
import com.example.myapplication.model.Place;

import java.util.ArrayList;
import java.util.List;

public class RouteStopAdapter extends RecyclerView.Adapter<RouteStopAdapter.StopViewHolder> {

    public interface OnStopClickListener {
        void onStopClick(Place place);
    }

    private final List<Place> stops;
    private final OnStopClickListener listener;

    public RouteStopAdapter(List<Place> stops, OnStopClickListener listener) {
        this.stops = (stops != null) ? stops : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public StopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route_stop, parent, false);
        return new StopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StopViewHolder holder, int position) {
        Place place = stops.get(position);
        holder.bind(place);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onStopClick(place);
        });
    }

    @Override
    public int getItemCount() {
        return stops.size();
    }

    static class StopViewHolder extends RecyclerView.ViewHolder {

        ImageView ivStopThumbnail;
        TextView tvStopName;
        TextView tvStopAddress;
        TextView tvStopDistance;

        public StopViewHolder(@NonNull View itemView) {
            super(itemView);
            ivStopThumbnail = itemView.findViewById(R.id.ivStopThumbnail);
            tvStopName = itemView.findViewById(R.id.tvStopName);
            tvStopAddress = itemView.findViewById(R.id.tvStopAddress);
            tvStopDistance = itemView.findViewById(R.id.tvStopDistance);
        }

        public void bind(Place place) {
            tvStopName.setText(place.getName() != null ? place.getName() : "");

            if (place.getAddress() != null && !place.getAddress().isEmpty()) {
                tvStopAddress.setText(place.getAddress());
            } else if (place.getDescription() != null && !place.getDescription().isEmpty()) {
                tvStopAddress.setText(place.getDescription());
            } else {
                tvStopAddress.setText("");
            }

            if (place.getDistance() != null && !place.getDistance().isEmpty()) {
                tvStopDistance.setVisibility(View.VISIBLE);
                tvStopDistance.setText(place.getDistance());
            } else {
                tvStopDistance.setVisibility(View.GONE);
            }

            if (place.getPictureURL() != null && !place.getPictureURL().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(place.getPictureURL())
                        .placeholder(R.drawable.ic_placeholder)
                        .into(ivStopThumbnail);
            } else {
                ivStopThumbnail.setImageResource(R.drawable.ic_placeholder);
            }
        }
    }
}
