package com.yourserver.core.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Vault의 Economy 서비스를 감싸는 얇은 래퍼.
 * Vault 또는 경제 플러그인이 없을 경우 안전하게 비활성화된다.
 */
public class VaultHook {

    private Economy economy;
    private boolean ready = false;

    public boolean setup() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        this.economy = rsp.getProvider();
        this.ready = (economy != null);
        return ready;
    }

    public boolean isReady() {
        return ready;
    }

    public double getBalance(OfflinePlayer player) {
        if (!ready) return 0;
        return economy.getBalance(player);
    }

    public void deposit(OfflinePlayer player, double amount) {
        if (!ready) return;
        economy.depositPlayer(player, amount);
    }

    public boolean withdraw(OfflinePlayer player, double amount) {
        if (!ready) return false;
        if (economy.getBalance(player) < amount) return false;
        economy.withdrawPlayer(player, amount);
        return true;
    }

    public String format(double amount) {
        if (!ready) return String.valueOf(amount);
        return economy.format(amount);
    }
}
