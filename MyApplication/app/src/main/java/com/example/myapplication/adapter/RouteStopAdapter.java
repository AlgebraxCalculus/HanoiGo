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
import com.example.myapplication.model.Place;

import java.util.List;

public class RouteStopAdapter extends RecyclerView.Adapter<RouteStopAdapter.StopViewHolder> {

    private final List<Place> stops;

    public RouteStopAdapter(List<Place> stops) {
        this.stops = stops;
    }

    @NonNull
    @Override
    public StopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route_stop, parent, false);
        return new StopViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull StopViewHolder holder, int position) {
        holder.bind(stops.get(position), position);
    }

    @Override
    public int getItemCount() {
        return stops == null ? 0 : stops.size();
    }

    static class StopViewHolder extends RecyclerView.ViewHolder {

        TextView tvOrder, tvName, tvAddress;
        ImageView ivImage;

        public StopViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrder = itemView.findViewById(R.id.tvStopOrder);
            tvName = itemView.findViewById(R.id.tvStopName);
            tvAddress = itemView.findViewById(R.id.tvStopAddress);
            ivImage = itemView.findViewById(R.id.ivStopImage);
        }

        public void bind(Place place, int position) {
            Context ctx = itemView.getContext();

            tvOrder.setText(String.valueOf(position + 1));
            tvName.setText(place.getName());
            tvAddress.setText(place.getAddress());

            Glide.with(ctx)
                    .load(place.getPictureURL())
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(ivImage);
        }
    }
}
