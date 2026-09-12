package com.yourserver.core.recall;

import com.yourserver.core.CorePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

/**
 * "귀환석" 아이템의 생성과 식별을 담당한다.
 * - 베이스 아이템: PAPER
 * - CustomModelData: 10001 (리소스팩에서 이 값에 맞는 커스텀 모델/텍스처를 매핑하면 됨)
 * - PersistentDataContainer 태그로 진짜 귀환석인지(다른 종이와 구분) 식별한다.
 */
public class RecallStone {

    public static final int CUSTOM_MODEL_DATA = 10001;

    private final CorePlugin plugin;
    private final NamespacedKey keyRecallStone;

    public RecallStone(CorePlugin plugin) {
        this.plugin = plugin;
        this.keyRecallStone = new NamespacedKey(plugin, "recall_stone");
    }

    public NamespacedKey getKey() {
        return keyRecallStone;
    }

    /** 귀환석 아이템 1개를 새로 생성한다. */
    public ItemStack create() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.AQUA + "[귀환석]");
        meta.setLore(List.of(ChatColor.GRAY + "우클릭 시 즉시 스폰 지역으로 순간이동합니다."));
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        meta.getPersistentDataContainer().set(keyRecallStone, PersistentDataType.BYTE, (byte) 1);

        item.setItemMeta(meta);
        return item;
    }

    /** 해당 아이템이 귀환석인지 판별한다. */
    public boolean isRecallStone(ItemStack stack) {
        if (stack == null || stack.getType() != Material.PAPER || !stack.hasItemMeta()) return false;
        ItemMeta meta = stack.getItemMeta();
        Byte tag = meta.getPersistentDataContainer().get(keyRecallStone, PersistentDataType.BYTE);
        return tag != null && tag == 1;
    }
}
