package com.yourserver.core.listeners;

import com.yourserver.core.CorePlugin;
import com.yourserver.core.region.RegionManager;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.BlockInventoryHolder;

/**
 * 스폰/마을 보호구역 내에서 OP 권한이 없는 유저의
 * 파괴, 설치, 상자/화로 열기, 액자/갑옷대 조작을 차단한다.
 * 구역 밖에서는 아무 제한도 가하지 않는다 (자유 약탈/건축 허용).
 */
public class SpawnProtectionListener implements Listener {

    private final CorePlugin plugin;
    private final RegionManager regionManager;

    public SpawnProtectionListener(CorePlugin plugin) {
        this.plugin = plugin;
        this.regionManager = plugin.getRegionManager();
    }

    private boolean hasBypass(Player p) {
        return p.isOp() || p.hasPermission("corebundle.bypass");
    }

    private void deny(Player p) {
        p.sendActionBar(ChatColor.RED + "이 구역은 보호되어 있습니다.");
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (hasBypass(p)) return;
        if (regionManager.isInsideSpawnRegion(e.getBlock().getLocation())) {
            e.setCancelled(true);
            deny(p);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (hasBypass(p)) return;
        if (regionManager.isInsideSpawnRegion(e.getBlock().getLocation())) {
            e.setCancelled(true);
            deny(p);
        }
    }

    /** 상자, 화로, 배럴, 드로퍼 등 컨테이너 열기 차단 */
    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (hasBypass(p)) return;

        Block block = e.getClickedBlock();
        if (block == null) return;

        BlockState state = block.getState();
        if (!(state instanceof BlockInventoryHolder)) return; // 컨테이너가 아니면 통과

        if (regionManager.isInsideSpawnRegion(block.getLocation())) {
            e.setCancelled(true);
            deny(p);
        }
    }

    /** 액자, 그림 등 hanging 엔티티 파괴 차단 */
    @EventHandler(ignoreCancelled = true)
    public void onHangingBreak(HangingBreakByEntityEvent e) {
        if (!(e.getRemover() instanceof Player p)) return;
        if (hasBypass(p)) return;

        if (e.getEntity() instanceof ItemFrame && regionManager.isInsideSpawnRegion(e.getEntity().getLocation())) {
            e.setCancelled(true);
            deny(p);
        }
    }

    /** 갑옷대 조작(아이템 탈부착 등) 차단 */
    @EventHandler(ignoreCancelled = true)
    public void onArmorStand(PlayerArmorStandManipulateEvent e) {
        Player p = e.getPlayer();
        if (hasBypass(p)) return;

        ArmorStand stand = e.getRightClicked();
        if (regionManager.isInsideSpawnRegion(stand.getLocation())) {
            e.setCancelled(true);
            deny(p);
        }
    }
}
