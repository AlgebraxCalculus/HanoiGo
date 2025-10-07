package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Place;

import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {

    private List<Place> places;

    public PlaceAdapter(List<Place> places) {
        this.places = places;
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
        holder.ivPlaceImage.setImageResource(place.getImageResId());
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
