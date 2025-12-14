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

public class SavedListAdapter extends RecyclerView.Adapter<SavedListAdapter.SavedListViewHolder> {

    private List<SavedList> savedLists;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(SavedList savedList);
        void onMenuClick(SavedList savedList);
        void onIconClick(SavedList savedList);
    }

    public SavedListAdapter(List<SavedList> savedLists) {
        this.savedLists = savedLists;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SavedListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saved_list, parent, false);
        return new SavedListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SavedListViewHolder holder, int position) {
        SavedList savedList = savedLists.get(position);

        // Check if it's an emoji or old-style icon
        if (savedList.isEmojiIcon()) {
            // Show emoji, hide icon
            holder.listIcon.setVisibility(View.GONE);
            holder.listEmojiIcon.setVisibility(View.VISIBLE);
            holder.listEmojiIcon.setText(savedList.getIconType());
        } else {
            // Show icon, hide emoji
            holder.listIcon.setVisibility(View.VISIBLE);
            holder.listEmojiIcon.setVisibility(View.GONE);
            holder.listIcon.setImageResource(savedList.getIconResId());

            // Set tint white for non-heart icons, no tint for heart to keep red color
            if (savedList.getIconResId() == R.drawable.ic_heart) {
                ImageViewCompat.setImageTintList(holder.listIcon, null);
            } else {
                ImageViewCompat.setImageTintList(holder.listIcon, ColorStateList.valueOf(Color.WHITE));
            }
        }

        holder.listTitle.setText(savedList.getTitle());
        holder.placeCount.setText(savedList.getPlaceCount() + " places");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(savedList);
            }
        });

        holder.iconContainer.setOnClickListener(v -> {
            if (listener != null) {
                listener.onIconClick(savedList);
            }
        });

        holder.menuIcon.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMenuClick(savedList);
            }
        });
    }

    @Override
    public int getItemCount() {
        return savedLists.size();
    }

    public static class SavedListViewHolder extends RecyclerView.ViewHolder {
        View iconContainer;
        ImageView listIcon;
        TextView listEmojiIcon;
        TextView listTitle;
        TextView placeCount;
        ImageView menuIcon;

        public SavedListViewHolder(@NonNull View itemView) {
            super(itemView);
            iconContainer = itemView.findViewById(R.id.iconContainer);
            listIcon = itemView.findViewById(R.id.listIcon);
            listEmojiIcon = itemView.findViewById(R.id.listEmojiIcon);
            listTitle = itemView.findViewById(R.id.listTitle);
            placeCount = itemView.findViewById(R.id.placeCount);
            menuIcon = itemView.findViewById(R.id.menuIcon);
        }
    }
}
