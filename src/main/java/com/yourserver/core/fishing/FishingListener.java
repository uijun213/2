package com.yourserver.core.fishing;

import com.yourserver.core.CorePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Random;

/**
 * 일반 낚시 시 일정 확률로 [일반/레어/전설] 등급의 커스텀 물고기가 낚이도록 처리한다.
 * 등급과 판매가는 아이템의 PersistentDataContainer에 저장해 두고,
 * /fish sell 명령어에서 해당 태그를 읽어 정산한다.
 */
public class FishingListener implements Listener {

    private final CorePlugin plugin;
    private final Random random = new Random();

    public final NamespacedKey KEY_TIER;
    public final NamespacedKey KEY_PRICE;
    public final NamespacedKey KEY_SIZE;

    public FishingListener(CorePlugin plugin) {
        this.plugin = plugin;
        this.KEY_TIER = new NamespacedKey(plugin, "fish_tier");
        this.KEY_PRICE = new NamespacedKey(plugin, "fish_price");
        this.KEY_SIZE = new NamespacedKey(plugin, "fish_size");
    }

    @EventHandler
    public void onFish(PlayerFishEvent e) {
        if (!plugin.getConfigManager().fishingEnabled()) return;
        if (e.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        if (!(e.getCaught() instanceof Item caughtItem)) return;

        ItemStack original = caughtItem.getItemStack();
        if (!isVanillaFish(original.getType())) return; // 낚시대/쓰레기 등은 그대로 둔다

        Player player = e.getPlayer();
        FishTier tier = rollTier();
        int size = rollSize();
        double price = priceFor(tier);

        ItemStack custom = buildCustomFish(original.getType(), tier, size, price);
        caughtItem.setItemStack(custom);

        player.sendMessage(tier.getColoredLabel() + ChatColor.RESET + " 물고기를 낚았습니다! ("
                + size + "cm, " + ChatColor.GREEN + plugin.getVaultHook().format(price) + ChatColor.RESET + ")");
    }

    private boolean isVanillaFish(Material mat) {
        return mat == Material.COD || mat == Material.SALMON
                || mat == Material.TROPICAL_FISH || mat == Material.PUFFERFISH;
    }

    private FishTier rollTier() {
        int roll = random.nextInt(100) + 1; // 1~100
        int common = plugin.getConfigManager().chanceCommon();
        int rare = plugin.getConfigManager().chanceRare();
        // legendary는 나머지 확률로 처리 (설정값 합이 100이 아니어도 안전)

        if (roll <= common) return FishTier.COMMON;
        if (roll <= common + rare) return FishTier.RARE;
        return FishTier.LEGENDARY;
    }

    private int rollSize() {
        int min = plugin.getConfigManager().sizeMin();
        int max = Math.max(min + 1, plugin.getConfigManager().sizeMax());
        return min + random.nextInt(max - min + 1);
    }

    private double priceFor(FishTier tier) {
        return switch (tier) {
            case COMMON -> plugin.getConfigManager().priceCommon();
            case RARE -> plugin.getConfigManager().priceRare();
            case LEGENDARY -> plugin.getConfigManager().priceLegendary();
        };
    }

    private ItemStack buildCustomFish(Material baseType, FishTier tier, int size, double price) {
        ItemStack item = new ItemStack(baseType);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(tier.getColoredLabel() + " " + ChatColor.RESET + niceName(baseType) + ChatColor.GRAY + " (" + size + "cm)");
        meta.setLore(List.of(
                ChatColor.GRAY + "크기: " + ChatColor.WHITE + size + "cm",
                ChatColor.GRAY + "판매가: " + ChatColor.GOLD + plugin.getVaultHook().format(price),
                ChatColor.DARK_GRAY + "/fish sell 로 판매할 수 있습니다."
        ));

        meta.getPersistentDataContainer().set(KEY_TIER, PersistentDataType.STRING, tier.name());
        meta.getPersistentDataContainer().set(KEY_PRICE, PersistentDataType.DOUBLE, price);
        meta.getPersistentDataContainer().set(KEY_SIZE, PersistentDataType.INTEGER, size);

        item.setItemMeta(meta);
        return item;
    }

    private String niceName(Material mat) {
        String raw = mat.name().replace('_', ' ').toLowerCase();
        return Character.toUpperCase(raw.charAt(0)) + raw.substring(1);
    }
}
