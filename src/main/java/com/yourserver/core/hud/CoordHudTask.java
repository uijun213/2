package com.yourserver.core.hud;

import com.yourserver.core.CorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * 서버 플러그인만으로 구현 가능한 "미니맵 대체" 기능.
 *
 * 실제 지형을 그려주는 미니맵(Xaero's Minimap, JourneyMap 등)은 클라이언트 모드가
 * 클라이언트에 이미 로드된 청크를 직접 렌더링하는 방식이라 서버 플러그인이
 * 별도의 "연동 패킷"을 보낼 필요가 없다 (바닐라 서버에서도 정상 작동한다).
 *
 * 이 태스크는 그 대신, 좌표(X/Y/Z)와 바라보는 방위를 액션바에 실시간으로
 * 표시해 주는 경량 HUD다. 하드코어 서버에서 좌표 노출 자체를 원치 않으면
 * config.yml의 coord-hud.enabled 를 false로 끄면 된다.
 */
public class CoordHudTask extends BukkitRunnable {

    private final CorePlugin plugin;

    public CoordHudTask(CorePlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (!plugin.getConfigManager().coordHudEnabled()) return;
        int interval = plugin.getConfigManager().coordHudInterval();
        this.runTaskTimer(plugin, 0L, interval);
    }

    @Override
    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Location loc = p.getLocation();
            String dir = directionOf(loc.getYaw());

            String text = ChatColor.GRAY + "X: " + ChatColor.WHITE + loc.getBlockX()
                    + ChatColor.GRAY + "  Y: " + ChatColor.WHITE + loc.getBlockY()
                    + ChatColor.GRAY + "  Z: " + ChatColor.WHITE + loc.getBlockZ()
                    + ChatColor.GRAY + "  방위: " + ChatColor.YELLOW + dir;

            p.sendActionBar(text);
        }
    }

    private String directionOf(float yaw) {
        // 마인크래프트 yaw 기준: 0=남(+Z), 90=서(-X), 180=북(-Z), 270=동(+X)
        String[] dirs = {"남", "남서", "서", "북서", "북", "북동", "동", "남동"};
        double normalized = (yaw + 360) % 360;
        int index = (int) Math.round(normalized / 45.0) % 8;
        return dirs[index];
    }
}
