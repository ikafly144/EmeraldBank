package net.sabafly.emeraldbank.bank;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.sabafly.emeraldbank.configuration.Settings;
import net.sabafly.emeraldbank.economy.EmeraldEconomy;
import net.sabafly.emeraldbank.external.OpenInvAccess;
import net.sabafly.emeraldbank.util.PlayerInventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;
import static net.sabafly.emeraldbank.EmeraldBank.config;
import static net.sabafly.emeraldbank.util.EmeraldUtils.tagResolver;
import static net.sabafly.emeraldbank.util.PlayerInventoryUtils.getEmeraldsAmount;
import static net.sabafly.emeraldbank.util.PlayerInventoryUtils.removeEmeralds;

public class User {

    @Getter
    private final @NotNull UUID uuid;
    @Getter
    private double wallet;
    @Setter
    @Getter
    private boolean useWalletFirst;
    @Getter
    private @Nullable Double offlineTransaction;

    public User(@NotNull UUID uuid, double wallet, boolean useWalletFirst, @Nullable Double offlineTransaction) {
        this.uuid = uuid;
        this.wallet = wallet;
        this.useWalletFirst = useWalletFirst;
        this.offlineTransaction = offlineTransaction;
        migrate();
    }

    @Deprecated(since = "1.0.0", forRemoval = true)
    private void migrate() {
        player().ifPresent(player -> addWallet(new EmeraldEconomy().migratePlayer(player)));
    }

    public void onSave() {
        player().ifPresent(player -> {
            new EmeraldEconomy().archive(player);
        });
    }

    public void notifyOfflineTransaction() {
        if (offlineTransaction != null) {
            player().ifPresent(player -> player.sendMessage(miniMessage().deserialize(config().messages.offlineTransaction, tagResolver("value", Component.text(offlineTransaction)))));
            offlineTransaction = null;
        }
    }

    public Optional<Player> player() {
        return Optional.ofNullable(OpenInvAccess.getOpenInvPlayer(Bukkit.getOfflinePlayer(uuid)));
    }

    public int balance() {
        return player().map(PlayerInventoryUtils::getEmeraldsAmount).orElse(0) + (int) wallet;
    }

    /**
     * @return true if the transaction was successful
     */
    public boolean withdraw(final double amount) {
        if (amount <= 0) {
            return false;
        }
        if (balance() < Math.ceil(amount)) {
            return false;
        }
        if (useWalletFirst && wallet >= amount) {
            wallet -= amount;
            return true;
        }

        return player().map(p -> {
            double remain = amount;
            if (useWalletFirst && wallet > 0 && getEmeraldsAmount(p) + wallet >= amount) {
                wallet = 0;
                remain -= wallet;
            } else if (!useWalletFirst && getEmeraldsAmount(p) < amount) {
                removeWallet(getEmeraldsAmount(p));
                remain -= getEmeraldsAmount(p);
                wallet -= remain;
                return true;
            }
            return removeEmeralds(p, (int) Math.ceil(remain));
        }).orElseGet(()-> {
            if (offlineTransaction == null) {
                offlineTransaction = 0.0;
            }
            this.offlineTransaction += amount;
            return false;
        });
    }

    public void deposit(double amount) {
        if (config().defaultDestination == Settings.DefaultDestination.WALLET) {
            wallet += amount;
            return;
        }
        player().ifPresentOrElse(p -> {
            var items = PlayerInventoryUtils.addEmeralds(p, (int) Math.floor(amount));
            if (items == 0) {
                return;
            }
            wallet += items;
        }, () -> wallet += amount);
    }

    public @NotNull String getName() {
        var player = Bukkit.getOfflinePlayer(uuid);
        return player.getName() == null ? player.getUniqueId().toString() : player.getName();
    }

    public void addWallet(int amount) {
        wallet += amount;
    }

    public void removeWallet(int amount) {
        wallet -= amount;
    }
}
