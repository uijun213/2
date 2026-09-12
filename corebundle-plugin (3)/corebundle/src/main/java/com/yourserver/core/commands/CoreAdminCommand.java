package com.yourserver.core.commands;

import com.yourserver.core.CorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * /coreadmin setcenter          - 현재 위치를 스폰 보호구역 중심으로 지정
 * /coreadmin setradius <n>      - 스폰 보호구역 반경 설정
 * /coreadmin info               - 현재 설정 확인
 * /coreadmin reload             - config.yml 다시 불러오기
 * /coreadmin give recall <닉네임> - 귀환석 아이템 지급
 */
public class CoreAdminCommand implements CommandExecutor {

    private final CorePlugin plugin;

    public CoreAdminCommand(CorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "사용법: /coreadmin <setcenter|setradius|info|reload|give>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "setcenter" -> {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(ChatColor.RED + "플레이어만 사용할 수 있습니다.");
                    return true;
                }
                plugin.getRegionManager().setCenter(p.getLocation());
                sender.sendMessage(ChatColor.GREEN + "스폰 보호구역 중심이 현재 위치로 설정되었습니다.");
            }
            case "setradius" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "사용법: /coreadmin setradius <반경>");
                    return true;
                }
                try {
                    double radius = Double.parseDouble(args[1]);
                    plugin.getRegionManager().setRadius(radius);
                    sender.sendMessage(ChatColor.GREEN + "스폰 보호구역 반경이 " + radius + "로 설정되었습니다.");
                } catch (NumberFormatException ex) {
                    sender.sendMessage(ChatColor.RED + "숫자를 입력해 주세요.");
                }
            }
            case "info" -> sender.sendMessage(ChatColor.AQUA + "[스폰 보호구역] " + plugin.getRegionManager().describe());
            case "reload" -> {
                plugin.getConfigManager().reload();
                plugin.getRegionManager().load();
                sender.sendMessage(ChatColor.GREEN + "설정을 다시 불러왔습니다.");
            }
            case "give" -> handleGive(sender, args);
            default -> sender.sendMessage(ChatColor.YELLOW + "사용법: /coreadmin <setcenter|setradius|info|reload|give>");
        }
        return true;
    }

    /** /coreadmin give recall <닉네임> */
    private void handleGive(CommandSender sender, String[] args) {
        if (args.length < 3 || !args[1].equalsIgnoreCase("recall")) {
            sender.sendMessage(ChatColor.RED + "사용법: /coreadmin give recall <닉네임>");
            return;
        }

        String targetName = args[2];
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "'" + targetName + "' 님은 온라인 상태가 아닙니다.");
            return;
        }

        ItemStack recallItem = plugin.getRecallStone().create();
        var leftover = target.getInventory().addItem(recallItem);
        if (!leftover.isEmpty()) {
            target.getWorld().dropItem(target.getLocation(), recallItem);
            sender.sendMessage(ChatColor.YELLOW + "인벤토리가 가득 차 " + targetName + " 님의 발밑에 귀환석을 떨어뜨렸습니다.");
        }

        target.sendMessage(ChatColor.AQUA + "[귀환석] " + ChatColor.RESET + "귀환석 1개를 받았습니다.");
        sender.sendMessage(ChatColor.GREEN + targetName + " 님에게 귀환석을 지급했습니다.");
    }
}
