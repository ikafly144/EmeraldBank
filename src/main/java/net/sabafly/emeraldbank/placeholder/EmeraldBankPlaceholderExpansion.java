package net.sabafly.emeraldbank.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.sabafly.emeraldbank.EmeraldBank;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class EmeraldBankPlaceholderExpansion extends PlaceholderExpansion {

    private final EmeraldBank plugin;

    public EmeraldBankPlaceholderExpansion(EmeraldBank plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "emeraldbank";
    }

    @Override
    public @NotNull String getAuthor() {
        return "SabaFly";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.startsWith("balance_")) {
            OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(params.substring(8));
            return plugin.getEconomy().format(plugin.getEconomy().getBalance(offlinePlayer));
        }
        if (params.startsWith("bank_balance_")) {
            String bankName = params.substring(13);
            return plugin.getEconomy().format(plugin.getEconomy().bankBalance(bankName).balance);
        }
        if (params.startsWith("bank_owner_")) {
            String bankName = params.substring(11);
            List<OfflinePlayer> members = plugin.getEconomy().getBankMembers(bankName);
            return members.isEmpty() ? "" : members.stream().filter(p->plugin.getEconomy().isBankOwner(bankName, p).transactionSuccess()).findFirst().map(OfflinePlayer::getName).orElse("");
        }
        if (params.startsWith("bank_members_")) {
            String bankName = params.substring(13);
            List<OfflinePlayer> members = plugin.getEconomy().getBankMembers(bankName);
            return members.isEmpty() ? "" : members.stream().map(OfflinePlayer::getName).reduce((a, b) -> a + ", " + b).orElse(null);
        }
        if (params.startsWith("bank_list")) {
            List<String> banks = plugin.getEconomy().getBanks();
            return String.join(", ", banks);
        }
        if (player == null) {
            return null;
        }
        if (params.equals("balance")) {
            return plugin.getEconomy().format(plugin.getEconomy().getBalance(player));
        }
        return null;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        return onRequest(player, params);
    }
}
