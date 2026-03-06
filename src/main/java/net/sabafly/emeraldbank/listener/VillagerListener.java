package net.sabafly.emeraldbank.listener;

import net.sabafly.emeraldbank.util.LogUtils;
import net.sabafly.emeraldbank.util.PlayerInventoryUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.TradeSelectEvent;

import static net.sabafly.emeraldbank.EmeraldBank.config;
import static net.sabafly.emeraldbank.EmeraldBank.economy;

public class VillagerListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onTradeSelect(TradeSelectEvent event) {
        if (!config().villagerIntegration) return;
        final var recipe = event.getMerchant().getRecipe(event.getIndex());
        final var ingredient = recipe.getAdjustedIngredient1();
        if (!(event.getWhoClicked() instanceof Player player))
            return;
        if (ingredient == null || ingredient.getType().asItemType() != config().getDefaultCurrency().getItemType()) {
            return;
        }
        final var slot1 = event.getInventory().getItem(0);
        if (slot1 != null && slot1.getAmount() > 0) return;
        if (PlayerInventoryUtils.getCurrencyCount(player, config().getDefaultCurrency()) < ingredient.getAmount()) {
            if (!economy().withdrawPlayer(player, ingredient.getAmount()).transactionSuccess())
                return;
            PlayerInventoryUtils.addCurrencyItem(player, config().getDefaultCurrency(), ingredient.getAmount());
        }
        if (!PlayerInventoryUtils.convertToParentIfNeeded(player, config().getDefaultCurrency(), ingredient.getAmount()))
            LogUtils.getLogger().debug("Failed to convert currency for player {} for villager trade", player.getName());
        player.updateInventory();
    }

}
