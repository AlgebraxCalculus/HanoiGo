package com.example.myapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import org.json.JSONObject;

import java.util.List;

public class SearchSuggestionAdapter extends RecyclerView.Adapter<SearchSuggestionAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(JSONObject item);
    }

    private List<JSONObject> suggestionList;
    private final Context context;
    private final OnItemClickListener listener;

    public SearchSuggestionAdapter(Context context, List<JSONObject> suggestions, OnItemClickListener listener) {
        this.context = context;
        this.suggestionList = suggestions;
        this.listener = listener;
    }

    public void updateData(List<JSONObject> newList) {
        this.suggestionList = newList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_autocomplete, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        JSONObject obj = suggestionList.get(position);
        String name = obj.optString("name", "");
        String address = obj.optString("address", "");

        boolean isFixedLocation = obj.optBoolean("isYourLocation", false)
                || obj.optBoolean("isDestination", false);

        if (isFixedLocation || address.isEmpty()) {
            holder.tvSuggestion.setText(name);
        } else {
            holder.tvSuggestion.setText(name + " - " + address);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(obj));
    }

    @Override
    public int getItemCount() {
        return suggestionList != null ? suggestionList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSuggestion;
        ViewHolder(View itemView) {
            super(itemView);
            tvSuggestion = itemView.findViewById(R.id.tvResult);
        }
    }
}