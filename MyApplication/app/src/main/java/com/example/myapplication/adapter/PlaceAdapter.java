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

import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Place place);
    }

    private List<Place> places;
    private OnItemClickListener listener;

    public PlaceAdapter(List<Place> places, OnItemClickListener listener) {
        this.places = places;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_place_card, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Place place = places.get(position);
        holder.tvPlaceName.setText(place.getName());
        holder.tvPlaceDescription.setText(place.getDescription());
        holder.tvPlaceDistance.setText(place.getDistance());
        Glide.with(holder.itemView.getContext())
                .load(place.getPictureURL())
                .into(holder.ivPlaceImage);

        // Set click listener cho từng item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(place);
            }
        });
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    static class PlaceViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPlaceImage;
        TextView tvPlaceName;
        TextView tvPlaceDescription;
        TextView tvPlaceDistance;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPlaceImage = itemView.findViewById(R.id.ivPlaceImage);
            tvPlaceName = itemView.findViewById(R.id.tvPlaceName);
            tvPlaceDescription = itemView.findViewById(R.id.tvPlaceDescription);
            tvPlaceDistance = itemView.findViewById(R.id.tvPlaceDistance);
        }
    }
}