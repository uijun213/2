package com.yourserver.core.recall;

import com.yourserver.core.CorePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 귀환석을 손에 들고 우클릭하면 즉시 스폰 지역(RegionManager에 설정된 스폰 보호구역 중심)으로
 * 순간이동시킨다. 이동 시 보라색 파티클 + 엔더맨 텔레포트 사운드를 재생하고,
 * 아이템 1개를 소모하며, 설정된 쿨타임(기본 10초) 동안 재사용을 막는다.
 */
public class RecallStoneListener implements Listener {

    private final CorePlugin plugin;
    private final RecallStone recallStone;

    /** 플레이어별 마지막 사용 시각(ms) */
    private final Map<UUID, Long> lastUse = new HashMap<>();

    public RecallStoneListener(CorePlugin plugin, RecallStone recallStone) {
        this.plugin = plugin;
        this.recallStone = recallStone;
    }

    @EventHandler(ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent e) {
        // 메인 핸드 기준으로만 처리 (오프핸드에서 한 번 더 발생하는 것을 방지)
        if (e.getHand() != EquipmentSlot.HAND) return;

        Action action = e.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = e.getItem();
        if (!recallStone.isRecallStone(item)) return;

        e.setCancelled(true); // 블록 상호작용(문 열기 등) 방지

        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        int cooldownSeconds = plugin.getConfigManager().recallCooldownSeconds();
        long now = System.currentTimeMillis();
        Long last = lastUse.get(uuid);

        if (last != null) {
            long elapsedMs = now - last;
            long remainingMs = (cooldownSeconds * 1000L) - elapsedMs;
            if (remainingMs > 0) {
                long remainingSec = (remainingMs / 1000) + 1;
                player.sendActionBar(ChatColor.RED + "귀환석 재사용 대기 중... (" + remainingSec + "초)");
                return;
            }
        }

        teleportToSpawn(player);
        lastUse.put(uuid, now);
        consumeOne(player, item);
    }

    private void teleportToSpawn(Player player) {
        var regionManager = plugin.getRegionManager();
        World world = plugin.getServer().getWorld(regionManager.getWorldName());
        if (world == null) {
            player.sendMessage(ChatColor.RED + "스폰 월드를 찾을 수 없어 이동할 수 없습니다. 관리자에게 문의하세요.");
            return;
        }

        Location from = player.getLocation();
        Location to = new Location(
                world,
                regionManager.getCenterX() + 0.5,
                regionManager.getCenterY(),
                regionManager.getCenterZ() + 0.5,
                player.getLocation().getYaw(),
                player.getLocation().getPitch()
        );

        playTeleportEffect(from);
        player.teleport(to);
        playTeleportEffect(to);

        player.sendMessage(ChatColor.AQUA + "[귀환석] " + ChatColor.RESET + "스폰 지역으로 귀환했습니다.");
    }

    private void playTeleportEffect(Location loc) {
        if (loc.getWorld() == null) return;
        loc.getWorld().spawnParticle(Particle.PORTAL, loc.clone().add(0, 1, 0), 40, 0.4, 0.6, 0.4, 0.1);
        loc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, loc.clone().add(0, 1, 0), 20, 0.4, 0.6, 0.4, 0.05);
        loc.getWorld().playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
    }

    private void consumeOne(Player player, ItemStack item) {
        int amount = item.getAmount();
        if (amount <= 1) {
            player.getInventory().setItemInMainHand(null);
        } else {
            item.setAmount(amount - 1);
        }
    }
}
