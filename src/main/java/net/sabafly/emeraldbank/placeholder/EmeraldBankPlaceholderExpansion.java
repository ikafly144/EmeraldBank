package net.sabafly.emeraldbank.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.sabafly.emeraldbank.EmeraldBank;
import net.sabafly.emeraldbank.bank.User;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

import static net.sabafly.emeraldbank.EmeraldBank.database;
import static net.sabafly.emeraldbank.EmeraldBank.economy;

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
        return "ikafly144";
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
    public @NotNull List<String> getPlaceholders() {
        return Stream.of(
                "balance",
                "balance_<player>",
                "wallet",
                "wallet_<player>",
                "bank_balance_<bank>",
                "bank_owner_<bank>",
                "bank_members_<bank>",
                "bank_list"
        ).map(s -> "%" + getIdentifier() + "_" + s + "%").toList();
    }



    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        try {
            if (params.startsWith("balance_")) {
                OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(params.substring(8));
                return economy().format(economy().getBalance(offlinePlayer));
            }
            if (params.startsWith("wallet_")) {
                OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(params.substring(7));
                return economy().format(database().getUser(offlinePlayer.getUniqueId()).getWallet());
            }
            try {
                if (params.startsWith("bank_balance_")) {
                    String bankName = params.substring(13);
                    return economy().format(economy().bankBalance(bankName).balance);
                }
                if (params.startsWith("bank_owner_")) {
                    String bankName = params.substring(11);
                    List<User> members = database().getOwners(bankName);
                    return members.isEmpty() ? "" : String.join(", ", members.stream().map(User::getName).toList());
                }
                if (params.startsWith("bank_members_")) {
                    String bankName = params.substring(13);
                    List<User> members = database().getMembers(bankName);
                    return members.isEmpty() ? "" : String.join(", ", members.stream().map(User::getName).toList());
                }
                if (params.startsWith("bank_list")) {
                    List<String> banks = economy().getBanks();
                    return String.join(", ", banks);
                }
            } catch (Exception e) {
                return "";
            }
            if (player == null) {
                return null;
            }
            if (params.equals("balance")) {
                return economy().format(economy().getBalance(player));
            }
            if (params.equals("wallet")) {
                return economy().format(database().getUser(player.getUniqueId()).getWallet());
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        return onRequest(player, params);
    }
}
