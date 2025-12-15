package com.example.myapplication.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EmojiCategory {
    private String name;
    private String icon;
    private List<String> emojis;

    public EmojiCategory(String name, String icon, List<String> emojis) {
        this.name = name;
        this.icon = icon;
        this.emojis = emojis;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public List<String> getEmojis() {
        return emojis;
    }

    public static List<EmojiCategory> getAllCategories() {
        List<EmojiCategory> categories = new ArrayList<>();

        // Smileys & Emotions
        categories.add(new EmojiCategory(
                "SMILEYS",
                "😀",
                Arrays.asList(
                        "😀", "😃", "😄", "😁", "😆", "😅", "🤣", "😂", "🙂", "🙃",
                        "😉", "😊", "😇", "🥰", "😍", "🤩", "😘", "😗", "😚", "😙",
                        "😋", "😛", "😜", "🤪", "😝", "🤑", "🤗", "🤭", "🤫", "🤔",
                        "🤐", "🤨", "😐", "😑", "😶", "😏", "😒", "🙄", "😬", "🤥",
                        "😌", "😔", "😪", "🤤", "😴", "😷", "🤒", "🤕", "🤢", "🤮"
                )
        ));

        // Hearts & Symbols
        categories.add(new EmojiCategory(
                "HEARTS",
                "❤️",
                Arrays.asList(
                        "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔",
                        "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝", "💟", "☮️",
                        "✝️", "☪️", "🕉️", "☸️", "✡️", "🔯", "🕎", "☯️", "☦️", "🛐",
                        "⭐", "🌟", "✨", "⚡", "🔥", "💫", "⛅", "☀️", "🌈", "☁️"
                )
        ));

        // Travel & Places
        categories.add(new EmojiCategory(
                "TRAVEL",
                "🏠",
                Arrays.asList(
                        "🏠", "🏡", "🏢", "🏣", "🏤", "🏥", "🏦", "🏨", "🏩", "🏪",
                        "🏫", "🏬", "🏭", "🏯", "🏰", "💒", "🗼", "🗽", "⛪", "🕌",
                        "🛕", "🕍", "⛩️", "🕋", "⛲", "⛺", "🌁", "🌃", "🏙️", "🌄",
                        "🌅", "🌆", "🌇", "🌉", "♨️", "🎠", "🎡", "🎢", "💈", "🎪"
                )
        ));

        // Food & Drink
        categories.add(new EmojiCategory(
                "FOOD",
                "🍕",
                Arrays.asList(
                        "🍇", "🍈", "🍉", "🍊", "🍋", "🍌", "🍍", "🥭", "🍎", "🍏",
                        "🍐", "🍑", "🍒", "🍓", "🥝", "🍅", "🥥", "🥑", "🍆", "🥔",
                        "🥕", "🌽", "🌶️", "🥒", "🥬", "🥦", "🧄", "🧅", "🍄", "🥜",
                        "🍞", "🥐", "🥖", "🥨", "🥯", "🥞", "🧇", "🧀", "🍖", "🍗",
                        "🥩", "🥓", "🍔", "🍟", "🍕", "🌭", "🥪", "🌮", "🌯", "🥙",
                        "🧆", "🥚", "🍳", "🥘", "🍲", "🥣", "🥗", "🍿", "🧈", "🧂",
                        "🥫", "🍱", "🍘", "🍙", "🍚", "🍛", "🍜", "🍝", "🍠", "🍢",
                        "🍣", "🍤", "🍥", "🥮", "🍡", "🥟", "🥠", "🥡", "🦀", "🦞",
                        "☕", "🍵", "🍶", "🍾", "🍷", "🍸", "🍹", "🍺", "🍻", "🥂"
                )
        ));

        // Activities & Sports
        categories.add(new EmojiCategory(
                "ACTIVITIES",
                "⚽",
                Arrays.asList(
                        "⚽", "🏀", "🏈", "⚾", "🥎", "🎾", "🏐", "🏉", "🥏", "🎱",
                        "🪀", "🏓", "🏸", "🏒", "🏑", "🥍", "🏏", "🥅", "⛳", "🪁",
                        "🏹", "🎣", "🤿", "🥊", "🥋", "🎽", "🛹", "🛷", "⛸️", "🥌",
                        "🎿", "⛷️", "🏂", "🪂", "🏋️", "🤼", "🤸", "🤺", "⛹️", "🤾",
                        "🏌️", "🏇", "🧘", "🏊", "🤽", "🚣", "🧗", "🚴", "🚵", "🎯"
                )
        ));

        // Objects
        categories.add(new EmojiCategory(
                "OBJECTS",
                "💻",
                Arrays.asList(
                        "⌚", "📱", "📲", "💻", "⌨️", "🖥️", "🖨️", "🖱️", "🖲️", "🕹️",
                        "🗜️", "💽", "💾", "💿", "📀", "📼", "📷", "📸", "📹", "🎥",
                        "📽️", "🎞️", "📞", "☎️", "📟", "📠", "📺", "📻", "🎙️", "🎚️",
                        "🎛️", "🧭", "⏱️", "⏲️", "⏰", "🕰️", "⌛", "⏳", "📡", "🔋",
                        "🔌", "💡", "🔦", "🕯️", "🪔", "🧯", "🛢️", "💸", "💵", "💴",
                        "📚", "📖", "📝", "✏️", "✒️", "🖊️", "🖋️", "🖍️", "📎", "🎒",
                        "🎨", "🖌️", "🖍️", "📏", "📐", "🎭", "🎪", "🎬", "🎤", "🎧"
                )
        ));

        // Animals & Nature
        categories.add(new EmojiCategory(
                "ANIMALS",
                "🐶",
                Arrays.asList(
                        "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯",
                        "🦁", "🐮", "🐷", "🐽", "🐸", "🐵", "🙈", "🙉", "🙊", "🐒",
                        "🐔", "🐧", "🐦", "🐤", "🐣", "🐥", "🦆", "🦅", "🦉", "🦇",
                        "🐺", "🐗", "🐴", "🦄", "🐝", "🐛", "🦋", "🐌", "🐞", "🐜",
                        "🌸", "💮", "🏵️", "🌹", "🥀", "🌺", "🌻", "🌼", "🌷", "🌱",
                        "🌲", "🌳", "🌴", "🌵", "🌾", "🌿", "☘️", "🍀", "🍁", "🍂"
                )
        ));

        // Symbols
        categories.add(new EmojiCategory(
                "SYMBOLS",
                "🔢",
                Arrays.asList(
                        "🔴", "🟠", "🟡", "🟢", "🔵", "🟣", "🟤", "⚫", "⚪", "🟥",
                        "🟧", "🟨", "🟩", "🟦", "🟪", "🟫", "⬛", "⬜", "◼️", "◻️",
                        "◾", "◽", "▪️", "▫️", "🔶", "🔷", "🔸", "🔹", "🔺", "🔻",
                        "💠", "🔘", "🔳", "🔲", "✅", "☑️", "✔️", "❌", "❎", "➕",
                        "➖", "➗", "✖️", "♾️", "💯", "🔢", "🔣", "🔤", "🔡", "🔠"
                )
        ));

        return categories;
    }
}
