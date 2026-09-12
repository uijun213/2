package com.yourserver.core.fishing;

import org.bukkit.ChatColor;

public enum FishTier {

    COMMON(ChatColor.WHITE, "일반"),
    RARE(ChatColor.AQUA, "레어"),
    LEGENDARY(ChatColor.GOLD, "전설");

    private final ChatColor color;
    private final String displayName;

    FishTier(ChatColor color, String displayName) {
        this.color = color;
        this.displayName = displayName;
    }

    public ChatColor getColor() {
        return color;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** [등급] 접두어가 붙은 색상 코드 라벨 */
    public String getColoredLabel() {
        return color + "[" + displayName + "]" + ChatColor.RESET;
    }
}
