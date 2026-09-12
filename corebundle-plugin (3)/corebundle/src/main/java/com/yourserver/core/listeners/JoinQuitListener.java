package com.yourserver.core.listeners;

import com.yourserver.core.CorePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * 최초 접속 유저가 어떤 스타터 킷도 받지 않고
 * 완전히 빈 인벤토리(몸만)로 시작하도록 보장한다.
 *
 * 바닐라 서버는 원래 빈 인벤토리로 시작하지만,
 * 서버에 설치된 다른 플러그인/데이터팩이 첫 접속 시 아이템을 지급하는
 * 경우를 대비해 최초 접속 시점에 인벤토리를 강제로 비운다.
 */
public class JoinQuitListener implements Listener {

    private final CorePlugin plugin;

    public JoinQuitListener(CorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        var player = e.getPlayer();

        if (!player.hasPlayedBefore()) {
            // 1틱 뒤 실행: 다른 플러그인의 첫 접속 지급 로직 이후에 강제로 비워서
            // "어떠한 기본 아이템도 지급받지 않는다" 요구사항을 확실히 만족시킨다.
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                player.getInventory().clear();
                player.getInventory().setArmorContents(null);
                player.getEnderChest().clear();
                player.setExp(0);
                player.setLevel(0);
                player.setFoodLevel(20);
                player.setSaturation(5f);
            });
        }
    }
}
