package com.example.myapplication.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.SavedList;

import java.util.List;

public class BookmarkListSelectorAdapter extends RecyclerView.Adapter<BookmarkListSelectorAdapter.ViewHolder> {

    private List<SavedList> bookmarkLists;
    private OnListSelectedListener listener;

    public interface OnListSelectedListener {
        void onListSelected(SavedList savedList);
    }

    public BookmarkListSelectorAdapter(List<SavedList> bookmarkLists, OnListSelectedListener listener) {
        this.bookmarkLists = bookmarkLists;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bookmark_list_selector, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SavedList list = bookmarkLists.get(position);

        holder.listIcon.setImageResource(list.getIconResId());

        // Set tint
        if (list.getIconResId() == R.drawable.ic_heart) {
            ImageViewCompat.setImageTintList(holder.listIcon, null);
        } else {
            ImageViewCompat.setImageTintList(holder.listIcon, ColorStateList.valueOf(Color.BLACK));
        }

        holder.listTitle.setText(list.getTitle());
        holder.listCount.setText(list.getPlaceCount() + " places");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onListSelected(list);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookmarkLists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView listIcon;
        TextView listTitle;
        TextView listCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            listIcon = itemView.findViewById(R.id.listIcon);
            listTitle = itemView.findViewById(R.id.listTitle);
            listCount = itemView.findViewById(R.id.listCount);
        }
    }
}
