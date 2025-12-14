package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class EmojiAdapter extends RecyclerView.Adapter<EmojiAdapter.EmojiViewHolder> {

    private List<String> emojis;
    private List<String> allEmojis;
    private OnEmojiClickListener listener;

    public interface OnEmojiClickListener {
        void onEmojiClick(String emoji);
    }

    public EmojiAdapter(List<String> emojis) {
        this.emojis = new ArrayList<>(emojis);
        this.allEmojis = new ArrayList<>(emojis);
    }

    public void setOnEmojiClickListener(OnEmojiClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public EmojiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emoji, parent, false);
        return new EmojiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmojiViewHolder holder, int position) {
        String emoji = emojis.get(position);
        holder.emojiText.setText(emoji);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEmojiClick(emoji);
            }
        });
    }

    @Override
    public int getItemCount() {
        return emojis.size();
    }

    public void filter(String query) {
        emojis.clear();
        if (query.isEmpty()) {
            emojis.addAll(allEmojis);
        } else {
            // Simple filter - you can enhance this
            emojis.addAll(allEmojis);
        }
        notifyDataSetChanged();
    }

    public void updateEmojis(List<String> newEmojis) {
        this.emojis.clear();
        this.emojis.addAll(newEmojis);
        this.allEmojis.clear();
        this.allEmojis.addAll(newEmojis);
        notifyDataSetChanged();
    }

    static class EmojiViewHolder extends RecyclerView.ViewHolder {
        TextView emojiText;

        EmojiViewHolder(View itemView) {
            super(itemView);
            emojiText = itemView.findViewById(R.id.emojiText);
        }
    }
}
