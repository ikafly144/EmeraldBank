package net.sabafly.emeraldbank.bank;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.sabafly.emeraldbank.configuration.Settings;
import net.sabafly.emeraldbank.external.OpenInvAccess;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;
import static net.sabafly.emeraldbank.EmeraldBank.config;
import static net.sabafly.emeraldbank.util.EmeraldUtils.tagResolver;
import static net.sabafly.emeraldbank.util.PlayerInventoryUtils.*;

public class User {

    @Getter
    private final @NotNull UUID uuid;
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
    }

    public void notifyOfflineTransaction() {
        if (offlineTransaction != null) {
            player().ifPresent(player -> player.sendMessage(miniMessage().deserialize(config().messages.offlineTransaction, tagResolver("value", Component.text(Math.abs(offlineTransaction))))));
            offlineTransaction = null;
        }
    }

    public Optional<Player> player() {
        return Optional.ofNullable(OpenInvAccess.getOpenInvPlayer(Bukkit.getOfflinePlayer(uuid)));
    }

    public int balance() {
        return player().map(p -> getCurrencyCount(p, config().getDefaultCurrency())).orElse(0) + (int) wallet;
    }

    /**
     * @return true if the transaction was successful
     */
    public boolean withdraw(final double count) {
        if (count <= 0) {
            return false;
        }
        if (balance() < Math.ceil(count)) {
            return false;
        }
        if (isOffline()) {
            this.offlineTransaction = (this.offlineTransaction == null ? 0.0 : this.offlineTransaction) - count;
        }
        if (useWalletFirst && wallet >= count) {
            wallet -= count;
            return true;
        }

        return player().map(p -> {
            int remain = (int) Math.ceil(count);
            if (useWalletFirst && wallet > 0 && getCurrencyCount(p, config().getDefaultCurrency()) + wallet >= count) {
                remain -= (int) wallet;
                wallet = 0;
            } else if (!useWalletFirst && getCurrencyCount(p, config().getDefaultCurrency()) < count) {
                remain -= getCurrencyCount(p, config().getDefaultCurrency());
                removeCurrency(p, config().getDefaultCurrency(), getCurrencyCount(p, config().getDefaultCurrency()));
                removeWallet(remain);
                return true;
            }
            return removeCurrency(p, config().getDefaultCurrency(), remain);
        }).orElse(false);
    }

    public void deposit(double count) {
        if (isOffline()) {
            this.offlineTransaction = (this.offlineTransaction == null ? 0.0 : this.offlineTransaction) + count;
        }
        if (config().defaultDestination == Settings.DefaultDestination.WALLET) {
            wallet += count;
            return;
        }
        player().ifPresentOrElse(p -> {
            var items = addCurrency(p, config().getDefaultCurrency(), (int) Math.floor(count));
            if (items == 0) {
                return;
            }
            wallet += items;
        }, () -> wallet += count);
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

    public double wallet() {
        return wallet;
    }

    public boolean isOffline() {
        return !player().map(Player::isOnline).orElse(false);
    }

}
