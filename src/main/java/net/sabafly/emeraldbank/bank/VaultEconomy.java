package net.sabafly.emeraldbank.bank;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;

public abstract class VaultEconomy implements Economy {

    @Override
    public boolean hasAccount(String s) {
        return hasAccount(Bukkit.getOfflinePlayer(s));
    }

    @Override
    public boolean hasAccount(String s, String s1) {
        return hasAccount(Bukkit.getOfflinePlayer(s));
    }

    @Override
    public double getBalance(String s) {
        return getBalance(Bukkit.getOfflinePlayer(s));
    }

    @Override
    public double getBalance(String s, String s1) {
        return getBalance(Bukkit.getOfflinePlayer(s));
    }

    @Override
    public boolean has(String s, double v) {
        return has(Bukkit.getOfflinePlayer(s), v);
    }

    @Override
    public boolean has(String s, String s1, double v) {
        return has(Bukkit.getOfflinePlayer(s), v);
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, double v) {
        return withdrawPlayer(Bukkit.getOfflinePlayer(s), v);
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, String s1, double v) {
        return withdrawPlayer(Bukkit.getOfflinePlayer(s), v);
    }

    @Override
    public EconomyResponse depositPlayer(String s, double v) {
        return depositPlayer(Bukkit.getOfflinePlayer(s), v);
    }

    @Override
    public EconomyResponse depositPlayer(String s, String s1, double v) {
        return depositPlayer(Bukkit.getOfflinePlayer(s), v);
    }

    @Override
    public EconomyResponse createBank(String s, String s1) {
        return createBank(s, Bukkit.getOfflinePlayer(s1));
    }

    @Override
    public EconomyResponse isBankOwner(String s, String s1) {
        return isBankOwner(s, Bukkit.getOfflinePlayer(s1));
    }

    @Override
    public EconomyResponse isBankMember(String s, String s1) {
        return isBankMember(s, Bukkit.getOfflinePlayer(s1));
    }

    @Override
    public boolean createPlayerAccount(String s) {
        return createPlayerAccount(s, null);
    }

    @Override
    public boolean createPlayerAccount(String s, String s1) {
        return createPlayerAccount(Bukkit.getOfflinePlayer(s), s1);
    }
}
