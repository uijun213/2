package com.yourserver.core.region;

import com.yourserver.core.CorePlugin;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * 스폰 보호구역(원형)을 관리한다.
 * WorldGuard 같은 별도 플러그인 없이, 중심좌표 + 반경만으로 가볍게 판정한다.
 */
public class RegionManager {

    private final CorePlugin plugin;

    private boolean enabled;
    private String worldName;
    private double centerX, centerY, centerZ;
    private double radius;

    public RegionManager(CorePlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        var cm = plugin.getConfigManager();
        this.enabled = cm.spawnRegionEnabled();
        this.worldName = cm.spawnWorld();
        this.centerX = cm.spawnCenterX();
        this.centerY = cm.spawnCenterY();
        this.centerZ = cm.spawnCenterZ();
        this.radius = cm.spawnRadius();
    }

    /** 해당 위치가 스폰 보호구역 안에 있는지 판정 (Y축은 무시하고 수평거리만 계산) */
    public boolean isInsideSpawnRegion(Location loc) {
        if (!enabled || loc == null || loc.getWorld() == null) return false;
        if (!loc.getWorld().getName().equals(worldName)) return false;

        double dx = loc.getX() - centerX;
        double dz = loc.getZ() - centerZ;
        double distSq = dx * dx + dz * dz;
        return distSq <= radius * radius;
    }

    public void setCenter(Location loc) {
        this.worldName = loc.getWorld().getName();
        this.centerX = loc.getX();
        this.centerY = loc.getY();
        this.centerZ = loc.getZ();
        persist();
    }

    public void setRadius(double newRadius) {
        this.radius = newRadius;
        persist();
    }

    private void persist() {
        var cfg = plugin.getConfig();
        cfg.set("spawn-region.world", worldName);
        cfg.set("spawn-region.center-x", centerX);
        cfg.set("spawn-region.center-y", centerY);
        cfg.set("spawn-region.center-z", centerZ);
        cfg.set("spawn-region.radius", radius);
        plugin.saveConfig();
    }

    public boolean isEnabled() { return enabled; }
    public String getWorldName() { return worldName; }
    public double getCenterX() { return centerX; }
    public double getCenterY() { return centerY; }
    public double getCenterZ() { return centerZ; }
    public double getRadius() { return radius; }

    public String describe() {
        return String.format("world=%s center=(%.1f, %.1f, %.1f) radius=%.1f enabled=%s",
                worldName, centerX, centerY, centerZ, radius, enabled);
    }
}
