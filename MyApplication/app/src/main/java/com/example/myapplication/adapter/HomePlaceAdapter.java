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

public class HomePlaceAdapter extends RecyclerView.Adapter<HomePlaceAdapter.HomePlaceViewHolder> {

    private List<Place> places;

    public HomePlaceAdapter(List<Place> places) {
        this.places = places;
    }

    @NonNull
    @Override
    public HomePlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_place_card, parent, false);
        return new HomePlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomePlaceViewHolder holder, int position) {
        Place place = places.get(position);

        holder.txtPlaceName.setText(place.getName());
        holder.txtPlaceDescription.setText(place.getDescription());
        holder.txtPlaceDistance.setText(place.getDistance());
        holder.imgPlace.setImageResource(place.getImageResId());
    }

    @Override
    public int getItemCount() {
        return places != null ? places.size() : 0;
    }

    static class HomePlaceViewHolder extends RecyclerView.ViewHolder {

        ImageView imgPlace;
        TextView txtPlaceName;
        TextView txtPlaceDescription;
        TextView txtPlaceDistance;

        public HomePlaceViewHolder(@NonNull View itemView) {
            super(itemView);

            imgPlace = itemView.findViewById(R.id.imgPlace);
            txtPlaceName = itemView.findViewById(R.id.txtPlaceName);
            txtPlaceDescription = itemView.findViewById(R.id.txtPlaceDescription);
            txtPlaceDistance = itemView.findViewById(R.id.txtPlaceDistance);
        }
    }
}
