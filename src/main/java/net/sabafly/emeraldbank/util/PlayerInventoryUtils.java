package net.sabafly.emeraldbank.util;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Map;

public class PlayerInventoryUtils {

    private PlayerInventoryUtils() {
    }

    public static int getEmeraldsAmount(Player player) {
        return player.getInventory().all(Material.EMERALD).values().stream().mapToInt(ItemStack::getAmount).sum()
                + player.getInventory().all(Material.EMERALD_BLOCK).values().stream().mapToInt(ItemStack::getAmount).sum() * 9;
    }

    public static boolean removeEmeralds(Player player, final int amount) {
        int remain = amount;

        for (ItemStack item : player.getInventory().all(Material.EMERALD_BLOCK).values()) {
            if (item.getAmount() >  (int) Math.ceil((double) remain / 9.0)) {
                item.setAmount(item.getAmount() -  (int) Math.ceil((double) remain / 9.0));
                int change = (remain % 9) > 0 ? 9 - (remain % 9) : 0;
                if (change > 0) {
                    addEmeralds(player, change);
                }
                return true;
            } else {
                remain -= item.getAmount() * 9;
                item.setAmount(0);
            }
        }

        for (ItemStack item : player.getInventory().all(Material.EMERALD).values()) {
            if (item.getAmount() > remain) {
                item.setAmount(item.getAmount() - remain);
                return true;
            } else {
                remain -= item.getAmount();
                item.setAmount(0);
            }
        }

        return remain <= 0;
    }

    public static int addEmeralds(Player player, final int amount) {
        int remain = amount;
        if (remain / 9 > 0) {
            for (ItemStack item : player.getInventory().all(Material.EMERALD_BLOCK).values()) {
                if (item.getAmount() + remain / 9 <= item.getMaxStackSize()) {
                    item.setAmount(item.getAmount() + remain / 9);
                    remain %= 9;
                } else {
                    remain -= (item.getMaxStackSize() - item.getAmount()) * 9;
                    item.setAmount(item.getMaxStackSize());
                }
            }
        }
        for (ItemStack item : player.getInventory().all(Material.EMERALD).values()) {
            if (item.getAmount() + remain <= item.getMaxStackSize()) {
                item.setAmount(item.getAmount() + remain);
                return 0;
            } else {
                remain -= item.getMaxStackSize() - item.getAmount();
                item.setAmount(item.getMaxStackSize());
            }
        }
        if (remain > 0) {
            var items = new ArrayList<ItemStack>();
            if (remain % 9 != 0) {
                items.add(new ItemStack(Material.EMERALD, remain % 9));
            }
            if (remain / 9 > 0) {
                items.add(new ItemStack(Material.EMERALD_BLOCK, remain / 9));
            }
            Map<Integer, ItemStack> map = player.getInventory().addItem(items.toArray(new ItemStack[0]));
            remain = map.values().stream().mapToInt(i -> i.getType() == Material.EMERALD ? i.getAmount() : i.getAmount() * 9).sum();
        }
        return remain;
    }

}
