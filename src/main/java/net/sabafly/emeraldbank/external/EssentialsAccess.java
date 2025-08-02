package net.sabafly.emeraldbank.external;

import com.earth2me.essentials.economy.EconomyLayer;
import com.earth2me.essentials.economy.EconomyLayers;
import net.sabafly.emeraldbank.EmeraldBank;
import net.sabafly.emeraldbank.bank.Economy;
import net.sabafly.emeraldbank.util.LogUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import java.math.BigDecimal;

import static net.sabafly.emeraldbank.EmeraldBank.economy;

public class EssentialsAccess {

    public EssentialsAccess() {
    }

    public void load() {
        EconomyLayers.registerLayer(new EssentialsEconomy());
    }

    public void enable(Plugin plugin) {
        EconomyLayer layer = EconomyLayers.onPluginEnable(plugin);
        if (layer instanceof EssentialsEconomy) {
            LogUtils.getLogger().info("Successfully hooked as Essentials payment resolution method for {} ({})", layer.getName(), layer.getPluginVersion());
        }
    }

    public static class EssentialsEconomy implements EconomyLayer {

        private Plugin plugin;
        private Economy economy;

        @Override
        public String getName() {
            return "EmeraldBank Essentials Support";
        }

        @Override
        public String getBackendName() {
            return economy().getName();
        }

        @Override
        public void enable(Plugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean onServerLoad() {
            this.economy = EmeraldBank.economy();
            return this.economy != null;
        }

        @Override
        public void disable() {
            this.plugin = null;
            this.economy = null;
        }

        @Override
        public String getPluginName() {
            return "EmeraldBank";
        }

        @Override
        public String getPluginVersion() {
            return plugin.getPluginMeta().getVersion();
        }

        @Override
        public boolean hasAccount(OfflinePlayer player) {
            return economy.hasAccount(player);
        }

        @Override
        public boolean createPlayerAccount(OfflinePlayer player) {
            return economy.createPlayerAccount(player);
        }

        @Override
        public BigDecimal getBalance(OfflinePlayer player) {
            return BigDecimal.valueOf(economy.getBalance(player));
        }

        @Override
        public boolean deposit(OfflinePlayer player, BigDecimal amount) {
            if (amount.signum() < 0) {
                return withdraw(player, amount.negate());
            }
            return economy.depositPlayer(player, amount.doubleValue()).transactionSuccess();
        }

        @Override
        public boolean withdraw(OfflinePlayer player, BigDecimal amount) {
            if (amount.signum() < 0) {
                return deposit(player, amount.negate());
            }
            return economy.withdrawPlayer(player, amount.doubleValue()).transactionSuccess();
        }

        @Override
        public boolean set(OfflinePlayer player, BigDecimal amount) {
            if (amount.signum() < 0) {
                throw new IllegalArgumentException("Cannot set a negative balance");
            }
            amount = getBalance(player).subtract(amount);
            if (amount.signum() < 0) {
                return deposit(player, amount.negate());
            } else if (amount.signum() == 0) {
                return true; // No change needed
            } else {
                return withdraw(player, amount);
            }
        }
    }

}
