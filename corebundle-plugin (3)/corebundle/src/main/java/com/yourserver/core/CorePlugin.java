package com.yourserver.core;

import com.yourserver.core.commands.CoreAdminCommand;
import com.yourserver.core.config.ConfigManager;
import com.yourserver.core.economy.VaultHook;
import com.yourserver.core.fishing.FishCommand;
import com.yourserver.core.fishing.FishingListener;
import com.yourserver.core.hud.CoordHudTask;
import com.yourserver.core.listeners.JoinQuitListener;
import com.yourserver.core.listeners.SpawnProtectionListener;
import com.yourserver.core.mobs.CustomMobListener;
import com.yourserver.core.recall.RecallStone;
import com.yourserver.core.recall.RecallStoneListener;
import com.yourserver.core.region.RegionManager;
import com.yourserver.core.scoreboard.ScoreboardManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CorePlugin extends JavaPlugin {

    private ConfigManager configManager;
    private RegionManager regionManager;
    private VaultHook vaultHook;
    private ScoreboardManager scoreboardManager;
    private CoordHudTask coordHudTask;
    private FishingListener fishingListener;
    private RecallStone recallStone;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        this.regionManager = new RegionManager(this);
        this.vaultHook = new VaultHook();

        if (vaultHook.setup()) {
            getLogger().info("Vault 경제 플러그인과 연동되었습니다.");
        } else {
            getLogger().warning("Vault를 찾을 수 없습니다. 소지금 관련 기능(스코어보드 잔액, 낚시 판매, 몹 보상)이 비활성화됩니다.");
        }

        this.fishingListener = new FishingListener(this);
        this.recallStone = new RecallStone(this);

        // 이벤트 리스너 등록
        getServer().getPluginManager().registerEvents(new SpawnProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);
        getServer().getPluginManager().registerEvents(fishingListener, this);
        getServer().getPluginManager().registerEvents(new CustomMobListener(this), this);
        getServer().getPluginManager().registerEvents(new RecallStoneListener(this, recallStone), this);

        // 명령어 등록
        getCommand("fish").setExecutor(new FishCommand(this));
        getCommand("coreadmin").setExecutor(new CoreAdminCommand(this));

        // 스코어보드 / HUD 시작
        this.scoreboardManager = new ScoreboardManager(this);
        scoreboardManager.start();

        this.coordHudTask = new CoordHudTask(this);
        coordHudTask.start();

        getLogger().info("CoreBundle이 활성화되었습니다. " + regionManager.describe());
    }

    @Override
    public void onDisable() {
        if (scoreboardManager != null) scoreboardManager.stop();
        if (coordHudTask != null) coordHudTask.cancel();
        getLogger().info("CoreBundle이 비활성화되었습니다.");
    }

    public ConfigManager getConfigManager() { return configManager; }
    public RegionManager getRegionManager() { return regionManager; }
    public VaultHook getVaultHook() { return vaultHook; }
    public FishingListener getFishingListener() { return fishingListener; }
    public RecallStone getRecallStone() { return recallStone; }
}
