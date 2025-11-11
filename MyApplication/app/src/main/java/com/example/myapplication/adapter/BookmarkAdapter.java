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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder> {

    private List<JSONObject> bookmarks;
    private OnBookmarkClickListener listener;

    public interface OnBookmarkClickListener {
        void onBookmarkClick(JSONObject bookmark);
        void onEditNoteClick(JSONObject bookmark);
    }

    public BookmarkAdapter(List<JSONObject> bookmarks) {
        this.bookmarks = bookmarks;
    }

    public void setOnBookmarkClickListener(OnBookmarkClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookmarkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bookmark, parent, false);
        return new BookmarkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarkViewHolder holder, int position) {
        JSONObject bookmark = bookmarks.get(position);

        try {
            // Location name
            String locationName = bookmark.optString("locationName", "Unknown");
            holder.tvLocationName.setText(locationName);

            // Location address
            String locationAddress = bookmark.optString("locationAddress", "");
            holder.tvCategory.setText(locationAddress);

            // Description (note)
            String description = bookmark.optString("description", "No note yet");
            holder.tvNote.setText(description);

            // Rating (not in response, using placeholder)
            holder.tvRating.setText("4.5");
            holder.tvReviewCount.setText("(10)");
            holder.tvDistance.setText("1.5km");

            // Image
            String imageUrl = bookmark.optString("defaultPicture", "");
            if (!imageUrl.isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(imageUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(holder.imgLocation);
            } else {
                holder.imgLocation.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            // Click listeners
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookmarkClick(bookmark);
                }
            });

            holder.btnEditBookmark.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditNoteClick(bookmark);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return bookmarks.size();
    }

    public static class BookmarkViewHolder extends RecyclerView.ViewHolder {
        ImageView imgLocation;
        TextView tvLocationName;
        TextView tvRating;
        TextView tvReviewCount;
        TextView tvDistance;
        TextView tvCategory;
        TextView tvNote;
        ImageView btnEditBookmark;

        public BookmarkViewHolder(@NonNull View itemView) {
            super(itemView);
            imgLocation = itemView.findViewById(R.id.imgLocation);
            tvLocationName = itemView.findViewById(R.id.tvLocationName);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvReviewCount = itemView.findViewById(R.id.tvReviewCount);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvNote = itemView.findViewById(R.id.tvNote);
            btnEditBookmark = itemView.findViewById(R.id.btnEditBookmark);
        }
    }
}
