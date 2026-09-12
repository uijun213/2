package com.yourserver.core.fishing;

import com.yourserver.core.CorePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class FishCommand implements CommandExecutor {

    private final CorePlugin plugin;

    public FishCommand(CorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("플레이어만 사용할 수 있는 명령어입니다.");
            return true;
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("sell")) {
            player.sendMessage(ChatColor.YELLOW + "사용법: /fish sell");
            return true;
        }

        if (!plugin.getVaultHook().isReady()) {
            player.sendMessage(ChatColor.RED + "경제 플러그인(Vault)이 연동되어 있지 않아 판매할 수 없습니다.");
            return true;
        }

        FishingListener fl = plugin.getFishingListener();
        Inventory inv = player.getInventory();

        double total = 0;
        int count = 0;

        ItemStack[] contents = inv.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack stack = contents[i];
            if (stack == null || !stack.hasItemMeta()) continue;

            ItemMeta meta = stack.getItemMeta();
            Double price = meta.getPersistentDataContainer().get(fl.KEY_PRICE, PersistentDataType.DOUBLE);
            if (price == null) continue;

            total += price * stack.getAmount();
            count += stack.getAmount();
            inv.setItem(i, null);
        }

        if (count == 0) {
            player.sendMessage(ChatColor.GRAY + "판매할 수 있는 커스텀 물고기가 없습니다.");
            return true;
        }

        plugin.getVaultHook().deposit(player, total);
        player.sendMessage(ChatColor.GREEN + "물고기 " + count + "마리를 판매하여 "
                + plugin.getVaultHook().format(total) + ChatColor.GREEN + " 을(를) 받았습니다.");
        return true;
    }
}
