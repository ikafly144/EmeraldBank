package net.sabafly.emeraldbank.listener;

import com.google.common.base.Preconditions;
import io.papermc.paper.event.player.PlayerTradeEvent;
import net.sabafly.emeraldbank.EmeraldBank;
import net.sabafly.emeraldbank.util.LogUtils;
import net.sabafly.emeraldbank.util.PlayerInventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.TradeSelectEvent;

import java.util.Objects;
import java.util.Optional;

import static net.sabafly.emeraldbank.EmeraldBank.config;
import static net.sabafly.emeraldbank.EmeraldBank.economy;

public class VillagerListener implements Listener {

    private static final NamespacedKey TRADE_AMOUNT = new NamespacedKey(EmeraldBank.getInstance(), "trade_amount");
    private static final NamespacedKey TRADE_CURRENCY = new NamespacedKey(EmeraldBank.getInstance(), "trade_currency");

    @EventHandler(ignoreCancelled = true)
    public void onTradeSelect(TradeSelectEvent event) {
        if (!config().villagerIntegration) return;

        if (!(event.getMerchant() instanceof AbstractVillager villager) || !config().villagerTypes.contains(villager.getType().getKey().asMinimalString())) {
            return;
        }

        final var recipe = event.getMerchant().getRecipe(event.getIndex());
        final var ingredient = recipe.getAdjustedIngredient1();
        if (!(event.getWhoClicked() instanceof Player player))
            return;
        if (ingredient == null || ingredient.getType().asItemType() != config().getDefaultCurrency().getItemType()) {
            return;
        }
        final var slot1 = event.getInventory().getItem(0);
        var amount = 0;
        final var currencyKey = config().getDefaultCurrencyKey().asMinimalString();
        if (slot1 != null && slot1.getAmount() > 0) return;
        if (PlayerInventoryUtils.getCurrencyCount(player, config().getDefaultCurrency()) < ingredient.getAmount()) {
            if (!economy().withdrawPlayer(player, ingredient.getAmount()).transactionSuccess())
                return;
            PlayerInventoryUtils.addCurrencyItem(player, config().getDefaultCurrency(), ingredient.getAmount());
            amount = ingredient.getAmount();
        }

        if (!PlayerInventoryUtils.convertToParentIfNeeded(player, config().getDefaultCurrency(), ingredient.getAmount()))
            LogUtils.getLogger().debug("Failed to convert currency for player {} for villager trade", player.getName());
        player.updateInventory();

        if (amount <= 0) return;

        if (event.getWhoClicked().getPersistentDataContainer().has(TRADE_AMOUNT, org.bukkit.persistence.PersistentDataType.INTEGER) &&
            event.getWhoClicked().getPersistentDataContainer().has(TRADE_CURRENCY, org.bukkit.persistence.PersistentDataType.STRING))
        {
            if (Objects.equals(event.getWhoClicked().getPersistentDataContainer().get(TRADE_CURRENCY, org.bukkit.persistence.PersistentDataType.STRING), currencyKey)) {
                amount += Optional.ofNullable(event.getWhoClicked().getPersistentDataContainer().get(TRADE_AMOUNT, org.bukkit.persistence.PersistentDataType.INTEGER)).orElse(0);
            }
        }

        event.getWhoClicked().getPersistentDataContainer().set(TRADE_AMOUNT, org.bukkit.persistence.PersistentDataType.INTEGER, amount);
        event.getWhoClicked().getPersistentDataContainer().set(TRADE_CURRENCY, org.bukkit.persistence.PersistentDataType.STRING, currencyKey);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTrade(PlayerTradeEvent event) {
        if (!config().villagerIntegration) return;
        if (!config().villagerTypes.contains(event.getVillager().getType().key().asMinimalString())) return;
        final var item = event.getTrade().getAdjustedIngredient1();
        if (item == null) return;

        if (!(event.getPlayer().getPersistentDataContainer().has(TRADE_AMOUNT) && event.getPlayer().getPersistentDataContainer().has(TRADE_CURRENCY)))
            return;
        final var amount = event.getPlayer().getPersistentDataContainer().get(TRADE_AMOUNT, org.bukkit.persistence.PersistentDataType.INTEGER);
        Preconditions.checkNotNull(amount);
        final var currencyKey = event.getPlayer().getPersistentDataContainer().get(TRADE_CURRENCY, org.bukkit.persistence.PersistentDataType.STRING);
        Preconditions.checkNotNull(currencyKey);
        final var currency = config().getCurrency(Objects.requireNonNull(NamespacedKey.fromString(currencyKey)));

        if (item.getType().asItemType() != currency.getItemType()) return;

        event.getPlayer().getPersistentDataContainer().set(TRADE_AMOUNT, org.bukkit.persistence.PersistentDataType.INTEGER, amount - item.getAmount());
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!config().villagerIntegration) return;
        if (event.getInventory().getType() != org.bukkit.event.inventory.InventoryType.MERCHANT) return;

        if (event.getPlayer().getPersistentDataContainer().has(TRADE_AMOUNT, org.bukkit.persistence.PersistentDataType.INTEGER) &&
                event.getPlayer().getPersistentDataContainer().has(TRADE_CURRENCY, org.bukkit.persistence.PersistentDataType.STRING)) {
            final var amount = event.getPlayer().getPersistentDataContainer().get(TRADE_AMOUNT, org.bukkit.persistence.PersistentDataType.INTEGER);
            final var currencyKey = event.getPlayer().getPersistentDataContainer().get(TRADE_CURRENCY, org.bukkit.persistence.PersistentDataType.STRING);
            if (amount != null && currencyKey != null) {
                Bukkit.getScheduler().runTask(EmeraldBank.getInstance(), () -> {
                    final var currency = config().getCurrency(Objects.requireNonNull(NamespacedKey.fromString(currencyKey)));
                    if (PlayerInventoryUtils.removeCurrency((Player) event.getPlayer(), currency, amount)) {
                        if (!economy().depositPlayer((Player) event.getPlayer(), amount).transactionSuccess())
                            LogUtils.getLogger().debug("Failed to deposit {} to player {} for villager trade", amount, event.getPlayer().getName());
                    } else {
                        LogUtils.getLogger().debug("Failed to remove {} of currency {} from player {} for villager trade", amount, currencyKey, event.getPlayer().getName());
                    }
                    ((Player) event.getPlayer()).updateInventory();
                });
            }
        }

        event.getPlayer().getPersistentDataContainer().remove(TRADE_AMOUNT);
        event.getPlayer().getPersistentDataContainer().remove(TRADE_CURRENCY);
    }


}
