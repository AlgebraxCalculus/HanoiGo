package com.example.myapplication.adapter;

import android.location.Location;
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
import java.util.Locale;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder> {

    private List<JSONObject> bookmarks;
    private OnBookmarkClickListener listener;
    private Location userLocation;

    public interface OnBookmarkClickListener {
        void onBookmarkClick(JSONObject bookmark);
        void onEditNoteClick(JSONObject bookmark);
        void onRemoveBookmark(JSONObject bookmark);
    }

    public BookmarkAdapter(List<JSONObject> bookmarks) {
        this.bookmarks = bookmarks;
    }

    public void setOnBookmarkClickListener(OnBookmarkClickListener listener) {
        this.listener = listener;
    }

    public void setUserLocation(Location location) {
        this.userLocation = location;
        notifyDataSetChanged();
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
            String description = bookmark.optString("description", "");
            if (description == null || description.isEmpty() || description.equals("null")) {
                holder.tvNote.setText("No note yet");
                holder.tvSeeMore.setVisibility(View.GONE);
            } else {
                holder.tvNote.setText(description);
                holder.tvNote.setMaxLines(Integer.MAX_VALUE); // Remove limit first to check actual line count

                // Check if text needs "See more" functionality
                holder.tvNote.post(() -> {
                    int lineCount = holder.tvNote.getLineCount();
                    if (lineCount > 2) {
                        // Text is long, show "See more" button and collapse to 2 lines
                        holder.tvNote.setMaxLines(2);
                        holder.tvSeeMore.setVisibility(View.VISIBLE);
                        holder.tvSeeMore.setText("See more");

                        // Handle See more/less click
                        holder.tvSeeMore.setOnClickListener(v -> {
                            if (holder.tvNote.getMaxLines() == 2) {
                                // Expand
                                holder.tvNote.setMaxLines(Integer.MAX_VALUE);
                                holder.tvSeeMore.setText("See less");
                            } else {
                                // Collapse
                                holder.tvNote.setMaxLines(2);
                                holder.tvSeeMore.setText("See more");
                            }
                        });
                    } else {
                        // Text is short, no need for "See more"
                        holder.tvSeeMore.setVisibility(View.GONE);
                    }
                });
            }

            // Show rating (always visible, 0 if no data)
            double avgRating = 0.0;
            long reviewCount = 0;

            if (bookmark.has("averageRating") && !bookmark.isNull("averageRating")) {
                try {
                    avgRating = bookmark.getDouble("averageRating");
                    reviewCount = bookmark.optLong("reviewCount", 0);
                } catch (JSONException e) {
                    // Keep default values (0.0 and 0)
                }
            }

            // Set rating on RatingBar and text
            holder.tvRating.setText(String.format(Locale.US, "%.1f", avgRating));
            holder.tvReviewCount.setText("(" + reviewCount + ")");

            // Always show RatingBar
            holder.ratingBar.setRating((float) avgRating);
            holder.ratingBar.setVisibility(View.VISIBLE);

            // Calculate distance if user location available
            if (userLocation != null && bookmark.has("latitude") && bookmark.has("longitude")) {
                try {
                    double lat = bookmark.getDouble("latitude");
                    double lng = bookmark.getDouble("longitude");

                    Location bookmarkLocation = new Location("");
                    bookmarkLocation.setLatitude(lat);
                    bookmarkLocation.setLongitude(lng);

                    float distanceInMeters = userLocation.distanceTo(bookmarkLocation);
                    float distanceInKm = distanceInMeters / 1000;

                    holder.tvDistance.setText(String.format(Locale.US, "%.1fkm", distanceInKm));
                    holder.distanceContainer.setVisibility(View.VISIBLE);
                } catch (JSONException e) {
                    holder.distanceContainer.setVisibility(View.GONE);
                }
            } else {
                holder.distanceContainer.setVisibility(View.GONE);
            }

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
                    showBookmarkMenu(v, bookmark);
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

    private void showBookmarkMenu(View anchor, JSONObject bookmark) {
        View dialogView = LayoutInflater.from(anchor.getContext()).inflate(R.layout.dialog_bookmark_options, null);

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(anchor.getContext())
                .setView(dialogView)
                .create();

        // Make dialog background transparent to show rounded corners
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Edit Note option
        dialogView.findViewById(R.id.optionEditNote).setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditNoteClick(bookmark);
            }
            dialog.dismiss();
        });

        // Remove from List option
        dialogView.findViewById(R.id.optionRemoveFromList).setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveBookmark(bookmark);
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    public static class BookmarkViewHolder extends RecyclerView.ViewHolder {
        ImageView imgLocation;
        TextView tvLocationName;
        android.widget.RatingBar ratingBar;
        TextView tvRating;
        TextView tvReviewCount;
        View distanceContainer;
        TextView tvDistance;
        TextView tvCategory;
        TextView tvNote;
        TextView tvSeeMore;
        ImageView btnEditBookmark;

        public BookmarkViewHolder(@NonNull View itemView) {
            super(itemView);
            imgLocation = itemView.findViewById(R.id.imgLocation);
            tvLocationName = itemView.findViewById(R.id.tvLocationName);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvReviewCount = itemView.findViewById(R.id.tvReviewCount);
            distanceContainer = itemView.findViewById(R.id.distanceContainer);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvNote = itemView.findViewById(R.id.tvNote);
            tvSeeMore = itemView.findViewById(R.id.tvSeeMore);
            btnEditBookmark = itemView.findViewById(R.id.btnEditBookmark);
        }
    }
}
