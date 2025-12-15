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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BookmarkListSelectorAdapter extends RecyclerView.Adapter<BookmarkListSelectorAdapter.ViewHolder> {

    private List<SavedList> bookmarkLists;
    private OnListSelectedListener listener;
    private Set<String> savedListIds; // IDs of lists that already contain this location

    public interface OnListSelectedListener {
        void onListSelected(SavedList savedList);
    }

    public BookmarkListSelectorAdapter(List<SavedList> bookmarkLists, OnListSelectedListener listener) {
        this.bookmarkLists = bookmarkLists;
        this.listener = listener;
        this.savedListIds = new HashSet<>();
    }

    public void setSavedListIds(Set<String> savedListIds) {
        this.savedListIds = savedListIds != null ? savedListIds : new HashSet<>();
        notifyDataSetChanged();
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

        // Check if it's an emoji or old-style icon
        if (list.isEmojiIcon()) {
            // Show emoji, hide icon
            holder.listIcon.setVisibility(View.GONE);
            holder.listEmojiIcon.setVisibility(View.VISIBLE);
            holder.listEmojiIcon.setText(list.getIconType());
        } else {
            // Show icon, hide emoji
            holder.listIcon.setVisibility(View.VISIBLE);
            holder.listEmojiIcon.setVisibility(View.GONE);
            holder.listIcon.setImageResource(list.getIconResId());

            // Set tint
            if (list.getIconResId() == R.drawable.ic_heart) {
                ImageViewCompat.setImageTintList(holder.listIcon, null);
            } else {
                ImageViewCompat.setImageTintList(holder.listIcon, ColorStateList.valueOf(Color.WHITE));
            }
        }

        holder.listTitle.setText(list.getTitle());

        // Hide description in save to dialog
        holder.listDescription.setVisibility(View.GONE);

        holder.listCount.setText(list.getPlaceCount() + " places");

        // Show "Saved" if this list already contains the location
        if (savedListIds.contains(list.getId())) {
            holder.tvSaved.setVisibility(View.VISIBLE);
        } else {
            holder.tvSaved.setVisibility(View.GONE);
        }

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
        TextView listEmojiIcon;
        TextView listTitle;
        TextView listDescription;
        TextView listCount;
        TextView tvSaved;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            listIcon = itemView.findViewById(R.id.listIcon);
            listEmojiIcon = itemView.findViewById(R.id.listEmojiIcon);
            listTitle = itemView.findViewById(R.id.listTitle);
            listDescription = itemView.findViewById(R.id.listDescription);
            listCount = itemView.findViewById(R.id.listCount);
            tvSaved = itemView.findViewById(R.id.tvSaved);
        }
    }
}
