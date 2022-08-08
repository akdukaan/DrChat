package org.acornmc.drchat;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;

public class VaultHook {

    public static void giveMoney(UUID uuid, double amount) {
        Economy econ = getEconomy();
        if (econ == null) return;
        econ.depositPlayer(Bukkit.getOfflinePlayer(uuid), amount);
    }

    public static Economy getEconomy() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return null;
        return rsp.getProvider();
    }
}
