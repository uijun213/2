package com.yourserver.core.scoreboard;

import com.yourserver.core.CorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

/**
 * 우측 사이드바 스코어보드와 Tab 리스트(header/footer)를 표시한다.
 *
 * 성능 노트:
 *  - Scoreboard/Objective API는 Bukkit 메인 스레드에서만 안전하게 다룰 수 있으므로
 *    "async"로 직접 돌리지 않는다. 대신 매 tick이 아니라 config에 정의된
 *    interval(기본 20틱=1초)마다 한 번씩만 갱신하여 부하를 최소화한다.
 *  - 온라인 인원이 많을 때 비용이 큰 부분(문자열 포맷)은 미리 한 번만 계산해서 재사용한다.
 */
public class ScoreboardManager {

    private final CorePlugin plugin;
    private BukkitRunnable task;

    public ScoreboardManager(CorePlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (!plugin.getConfigManager().scoreboardEnabled()) return;
        stop();

        int interval = plugin.getConfigManager().scoreboardInterval();

        task = new BukkitRunnable() {
            @Override
            public void run() {
                updateTabListStatic();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    updateSidebar(p);
                }
            }
        };
        task.runTaskTimer(plugin, 0L, interval);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void updateTabListStatic() {
        String header = ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().tabHeader());
        String footer = ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().tabFooter());
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setPlayerListHeaderFooter(header, footer);
        }
    }

    private void updateSidebar(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().scoreboardTitle());
        Objective obj = board.registerNewObjective("core_side", "dummy", title);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        String serverName = plugin.getConfigManager().serverName();
        double balance = plugin.getVaultHook().isReady()
                ? plugin.getVaultHook().getBalance(player)
                : -1;

        int line = 8;

        obj.getScore(ChatColor.GRAY + "서버: " + ChatColor.WHITE + serverName).setScore(line--);
        obj.getScore(" ").setScore(line--);
        obj.getScore(ChatColor.GRAY + "닉네임: " + ChatColor.AQUA + player.getName()).setScore(line--);

        if (balance >= 0) {
            obj.getScore(ChatColor.GRAY + "소지금: " + ChatColor.GOLD + plugin.getVaultHook().format(balance)).setScore(line--);
        } else {
            obj.getScore(ChatColor.GRAY + "소지금: " + ChatColor.DARK_GRAY + "(Vault 미연동)").setScore(line--);
        }

        obj.getScore("  ").setScore(line--);
        obj.getScore(ChatColor.GRAY + "접속자: " + ChatColor.GREEN + Bukkit.getOnlinePlayers().size()
                + ChatColor.GRAY + "/" + Bukkit.getMaxPlayers()).setScore(line);

        player.setScoreboard(board);
    }
}
