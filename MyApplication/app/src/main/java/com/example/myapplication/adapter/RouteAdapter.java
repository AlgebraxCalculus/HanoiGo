package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Route;

import java.util.List;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.RouteViewHolder> {

    private List<Route> routes;

    public RouteAdapter(List<Route> routes) {
        this.routes = routes;
    }

    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route, parent, false);
        return new RouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
        Route route = routes.get(position);
        holder.tvRouteName.setText(route.getName());
        holder.tvRouteDescription.setText(route.getDescription());
        holder.tvRouteDetails.setText(route.getDetails());
        holder.ivRouteImage.setImageResource(route.getImageResId());
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    static class RouteViewHolder extends RecyclerView.ViewHolder {
        ImageView ivRouteImage;
        TextView tvRouteName;
        TextView tvRouteDescription;
        TextView tvRouteDetails;

        public RouteViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRouteImage = itemView.findViewById(R.id.ivRouteImage);
            tvRouteName = itemView.findViewById(R.id.tvRouteName);
            tvRouteDescription = itemView.findViewById(R.id.tvRouteDescription);
            tvRouteDetails = itemView.findViewById(R.id.tvRouteDetails);
        }
    }
}
