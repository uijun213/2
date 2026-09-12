package com.yourserver.core.config;

import com.yourserver.core.CorePlugin;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * config.yml 값을 한 곳에서 읽고 쓰기 위한 헬퍼.
 * 매 tick마다 getConfig() 파싱 비용을 줄이기 위해 자주 쓰는 값은 캐싱한다.
 */
public class ConfigManager {

    private final CorePlugin plugin;

    public ConfigManager(CorePlugin plugin) {
        this.plugin = plugin;
    }

    public FileConfiguration cfg() {
        return plugin.getConfig();
    }

    public void reload() {
        plugin.reloadConfig();
    }

    public void save() {
        plugin.saveConfig();
    }

    // ---- 스폰 보호 ----
    public boolean spawnRegionEnabled() { return cfg().getBoolean("spawn-region.enabled", true); }
    public String spawnWorld() { return cfg().getString("spawn-region.world", "world"); }
    public double spawnCenterX() { return cfg().getDouble("spawn-region.center-x"); }
    public double spawnCenterY() { return cfg().getDouble("spawn-region.center-y"); }
    public double spawnCenterZ() { return cfg().getDouble("spawn-region.center-z"); }
    public double spawnRadius() { return cfg().getDouble("spawn-region.radius", 100); }

    // ---- 스코어보드 ----
    public boolean scoreboardEnabled() { return cfg().getBoolean("scoreboard.enabled", true); }
    public int scoreboardInterval() { return cfg().getInt("scoreboard.update-interval-ticks", 20); }
    public String serverName() { return cfg().getString("scoreboard.server-name", "MyServer"); }
    public String scoreboardTitle() { return cfg().getString("scoreboard.title", "&6서버 정보"); }
    public String tabHeader() { return cfg().getString("scoreboard.tab-header", ""); }
    public String tabFooter() { return cfg().getString("scoreboard.tab-footer", ""); }

    // ---- 좌표 HUD ----
    public boolean coordHudEnabled() { return cfg().getBoolean("coord-hud.enabled", true); }
    public int coordHudInterval() { return cfg().getInt("coord-hud.update-interval-ticks", 20); }

    // ---- 낚시 ----
    public boolean fishingEnabled() { return cfg().getBoolean("fishing.enabled", true); }
    public int chanceCommon() { return cfg().getInt("fishing.chance-common", 55); }
    public int chanceRare() { return cfg().getInt("fishing.chance-rare", 30); }
    public int chanceLegendary() { return cfg().getInt("fishing.chance-legendary", 15); }
    public double priceCommon() { return cfg().getDouble("fishing.price-common", 20); }
    public double priceRare() { return cfg().getDouble("fishing.price-rare", 80); }
    public double priceLegendary() { return cfg().getDouble("fishing.price-legendary", 300); }
    public int sizeMin() { return cfg().getInt("fishing.size-min-cm", 5); }
    public int sizeMax() { return cfg().getInt("fishing.size-max-cm", 120); }

    // ---- 커스텀 몹 ----
    public boolean customMobsEnabled() { return cfg().getBoolean("custom-mobs.enabled", true); }
    public boolean mobNightOnly() { return cfg().getBoolean("custom-mobs.night-only", true); }
    public int mobSpawnChance() { return cfg().getInt("custom-mobs.spawn-chance-percent", 8); }
    public double mobHealthMult() { return cfg().getDouble("custom-mobs.health-multiplier", 2.5); }
    public double mobDamageMult() { return cfg().getDouble("custom-mobs.damage-multiplier", 1.8); }
    public double mobRewardMoney() { return cfg().getDouble("custom-mobs.reward-money", 150); }
    public double mobRewardExp() { return cfg().getDouble("custom-mobs.reward-exp", 40); }

    // ---- 귀환석 ----
    public int recallCooldownSeconds() { return cfg().getInt("recall-stone.cooldown-seconds", 10); }
}
