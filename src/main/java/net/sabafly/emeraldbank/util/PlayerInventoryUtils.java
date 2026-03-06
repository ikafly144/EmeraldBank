package net.sabafly.emeraldbank.util;

import com.google.common.base.Preconditions;
import net.sabafly.emeraldbank.configuration.Settings.Currency;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;

import java.util.Map;

public class PlayerInventoryUtils {

    private PlayerInventoryUtils() {
    }

    public static int getCurrencyAmount(Player player, Currency currency) {
        int amount = getCurrencyCount(player, currency);
        return (int) Math.floor(amount * currency.rate);
    }

    public static int getCurrencyCount(Player player, Currency currency) {
        int amount = 0;
        amount += player.getInventory().all(currency.getItemType().createItemStack().getType()).values().stream().mapToInt(ItemStack::getAmount).sum();
        for (var child : currency.getChildren().entrySet()) {
            amount += player.getInventory().all(child.getKey().createItemStack().getType()).values().stream().mapToInt(ItemStack::getAmount).map(v -> v * child.getValue()).sum();
        }
        return amount;
    }

    public static boolean convertToParentIfNeeded(Player player, Currency currency, int countOfParent) {
        if (getCurrencyCount(player, currency) < countOfParent)
            return false;
        int remain = countOfParent;
        remain -= player.getInventory().all(currency.getItemType().createItemStack().getType()).values().stream().mapToInt(ItemStack::getAmount).sum();
        if (remain <= 0) return true;
        for (var child : currency.getChildren().entrySet()) {
            var childAmount = player.getInventory().all(child.getKey().createItemStack().getType()).values().stream().mapToInt(ItemStack::getAmount).sum();
            if (childAmount > 0) {
                var removed = removeCurrency1(player, child.getKey(), (int) Math.ceil((double) remain / (double) child.getValue()));
                if (removed > 0) {
                    remain -= removed * child.getValue();
                    if (addCurrency1(player, currency.getItemType(), removed * child.getValue()) > 0) {
                        throw new IllegalStateException("Failed to add currency to inventory after removing child currency");
                    }
                }
            }
            if (remain <= 0) {
                return true;
            }
        }
        return true;
    }


    /**
     * @return count of currency removed
     */
    private static int removeCurrency0(Player player, Currency currency, final int count) {
        if (count <= 0) {
            return 0;
        }
        int remain = count;
        for (var child : currency.getChildren().entrySet()) {
            var childAmount = (int) Math.ceil((double) remain / (double) child.getValue());
            remain -= removeCurrency1(player, child.getKey(), childAmount) * child.getValue();
            if (remain <= 0) {
                return count + addCurrency(player, currency, -remain);
            }
        }
        remain -= removeCurrency1(player, currency.getItemType(), remain);
        if (remain < 0) {
            return count + addCurrency(player, currency, -remain);
        }
        return count - remain;
    }

    /**
     * @return count of currency removed
     */
    private static int removeCurrency1(Player player, ItemType type, final int count) {
        int remain = count;
        for (ItemStack item : player.getInventory().all(type.createItemStack().getType()).values()) {
            if (item.getAmount() > remain) {
                item.setAmount(item.getAmount() - remain);
                return count;
            } else {
                remain -= item.getAmount();
                item.setAmount(0);
            }
        }
        return count - remain;
    }

    public static boolean removeCurrency(Player player, Currency currency, final int count) {
        if (getCurrencyAmount(player, currency) < count) {
            return false;
        }
        int removed = removeCurrency0(player, currency, count);
        Preconditions.checkArgument(removed == count, "Removed " + removed + " items, but expected " + count);
        return true;
    }

    private static int addCurrency1(Player player, ItemType type, final int count) {
        int remain = count;
        for (ItemStack item : player.getInventory().all(type.createItemStack().getType()).values()) {
            if (item.getAmount() + remain <= item.getMaxStackSize()) {
                item.setAmount(item.getAmount() + remain);
                return 0;
            } else {
                remain -= item.getMaxStackSize() - item.getAmount();
                item.setAmount(item.getMaxStackSize());
            }
        }
        if (remain > 0) {
            Map<Integer, ItemStack> map = player.getInventory().addItem(type.createItemStack(remain));
            remain = map.values().stream().mapToInt(ItemStack::getAmount).sum();
        }
        return remain;
    }

    /**
     * @return count of currency remain
     * 0 if all items were added
     */
    private static int addCurrency0(Player player, Currency currency, int count) {
        int remain = count;
        for (var child : currency.getChildren().entrySet()) {
            var childAmount = remain / child.getValue();
            remain -= childAmount * child.getValue();
            if (childAmount > 0)
                remain += addCurrency1(player, child.getKey(), childAmount) * child.getValue();
            if (remain <= 0) {
                return 0;
            }
        }
        return addCurrency1(player, currency.getItemType(), remain);
    }

    /**
     * @return count of currency remain
     * 0 if all items were added
     */
    public static int addCurrency(Player player, Currency currency, int count) {
        if (count <= 0) {
            return 0;
        }
        return addCurrency0(player, currency, count);
    }

    public static void addCurrencyItem(Player player, Currency target, int count) {
        var remain = addCurrency(player, target, count);
        if (remain > 0) {
            ItemStack itemStack = target.getItemType().createItemStack(remain);
            Map<Integer, ItemStack> map = player.getInventory().addItem(itemStack);
            remain = map.values().stream().mapToInt(ItemStack::getAmount).sum();
            if (remain > 0) {
                player.getWorld().dropItem(player.getLocation(), itemStack);
            }
        }
    }
}
