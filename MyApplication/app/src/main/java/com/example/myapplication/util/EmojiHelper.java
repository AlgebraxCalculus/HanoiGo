package com.example.myapplication.util;

import java.util.ArrayList;
import java.util.List;

public class EmojiHelper {

    /**
     * Lấy tất cả emoji từ Unicode ranges - giống cách Google Maps làm
     * Không hardcode, load động từ hệ thống
     */
    public static String[] getAllEmojis() {
        List<String> emojiList = new ArrayList<>();

        // Unicode ranges cho emoji
        int[][] emojiRanges = {
                {0x1F600, 0x1F64F}, // Emoticons
                {0x1F300, 0x1F5FF}, // Misc Symbols and Pictographs
                {0x1F680, 0x1F6FF}, // Transport and Map
                {0x1F900, 0x1F9FF}, // Supplemental Symbols and Pictographs
                {0x2600, 0x26FF},   // Misc symbols
                {0x2700, 0x27BF},   // Dingbats
                {0x1F1E6, 0x1F1FF}, // Flags
        };

        for (int[] range : emojiRanges) {
            for (int codePoint = range[0]; codePoint <= range[1]; codePoint++) {
                // Kiểm tra xem có phải emoji hợp lệ không
                if (Character.isDefined(codePoint)) {
                    String emoji = new String(Character.toChars(codePoint));
                    // Chỉ thêm emoji có thể hiển thị được
                    if (emoji.length() > 0 && !Character.isISOControl(codePoint)) {
                        emojiList.add(emoji);
                    }
                }
            }
        }

        return emojiList.toArray(new String[0]);
    }

    /**
     * Emoji phổ biến cho quick access (optional)
     */
    public static String[] getPopularEmojis() {
        return new String[]{
            "😀", "😃", "😄", "😁", "😊", "😍", "🥰", "😘", "😎", "🤩",
            "❤️", "💛", "💚", "💙", "💜", "🖤", "🤍", "💕", "💖", "⭐",
            "🏠", "🏢", "🏛", "⛪", "🕌", "🏰", "🗼", "🗽", "⛩", "🎡",
            "🍕", "🍔", "🍟", "🌭", "🍿", "🍜", "🍱", "🍛", "🍲", "☕",
            "⚽", "🏀", "🏈", "⚾", "🎾", "🏐", "🎱", "🏓", "🎯", "🎮",
            "📱", "💻", "📷", "📚", "📖", "🎒", "✏️", "📝", "🎨", "🎭"
        };
    }
}
