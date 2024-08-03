package net.sabafly.emeraldbank.economy;

import com.lishid.openinv.OpenInv;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.sabafly.emeraldbank.EmeraldBank;
import net.sabafly.emeraldbank.util.EmeraldUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class EmeraldEconomy implements Economy {
    @Override
    public boolean isEnabled() {
        return EmeraldBank.getInstance().isEnabled();
    }

    @Override
    public String getName() {
        return EmeraldBank.getInstance().getMessages().name;
    }

    @Override
    public boolean hasBankSupport() {
        return EmeraldBank.getInstance().getGlobalConfiguration().banking.enabled && isEnabled();
    }

    @Override
    public int fractionalDigits() {
        return 0;
    }

    @Override
    public String format(double v) {
        return PlainTextComponentSerializer.plainText().serialize(MiniMessage.miniMessage().deserialize(EmeraldBank.getInstance().getMessages().economyFormat, TagResolver.resolver("currency", Tag.inserting(MiniMessage.miniMessage().deserialize(v == 1 ? EmeraldBank.getInstance().getMessages().currencyName : EmeraldBank.getInstance().getMessages().currencyNamePlural))), TagResolver.resolver("value", Tag.inserting(Component.text((int) v)))));
    }

    @Override
    public String currencyNamePlural() {
        return PlainTextComponentSerializer.plainText().serialize(MiniMessage.miniMessage().deserialize(EmeraldBank.getInstance().getMessages().currencyNamePlural));
    }

    @Override
    public String currencyNameSingular() {
        return PlainTextComponentSerializer.plainText().serialize(MiniMessage.miniMessage().deserialize(EmeraldBank.getInstance().getMessages().currencyName));
    }

    @Override
    public boolean hasAccount(String name) {
        return hasAccount(EmeraldBank.getInstance().getServer().getOfflinePlayer(name));
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer) {
        return offlinePlayer.hasPlayedBefore();
    }

    @Override
    public boolean hasAccount(String name, String world) {
        return hasAccount(EmeraldBank.getInstance().getServer().getOfflinePlayer(name), world);
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer, String world) {
        return hasAccount(offlinePlayer);
    }

    @Override
    public double getBalance(String s) {
        return getBalance(EmeraldBank.getInstance().getServer().getOfflinePlayer(s));
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer) {
        Player player = OpenInv.getPlugin(OpenInv.class).loadPlayer(offlinePlayer);
        return player == null ? 0 :
                Arrays.stream(player.getInventory().getContents()).filter(item -> item != null && item.getType() == Material.EMERALD).mapToDouble(ItemStack::getAmount).sum()
                        + Arrays.stream(player.getInventory().getContents()).filter(item -> item != null && item.getType() == Material.EMERALD_BLOCK).mapToDouble(item -> item.getAmount() * 9).sum();
    }

    @Override
    public double getBalance(String s, String s1) {
        return getBalance(EmeraldBank.getInstance().getServer().getOfflinePlayer(s), s1);
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer, String s) {
        return getBalance(offlinePlayer);
    }

    @Override
    public boolean has(String s, double v) {
        return has(EmeraldBank.getInstance().getServer().getOfflinePlayer(s), v);
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, double v) {
        return getBalance(offlinePlayer) >= v;
    }

    @Override
    public boolean has(String s, String s1, double v) {
        return has(EmeraldBank.getInstance().getServer().getOfflinePlayer(s), s1, v);
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, String s, double v) {
        return has(offlinePlayer, v);
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, double v) {
        return withdrawPlayer(EmeraldBank.getInstance().getServer().getOfflinePlayer(s), v);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, double v) {
        if (!has(offlinePlayer, v)) {
            return new EconomyResponse(0, getBalance(offlinePlayer), EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
        }
        Player player = OpenInv.getPlugin(OpenInv.class).loadPlayer(offlinePlayer);
        if (player == null) {
            return new EconomyResponse(0, getBalance(offlinePlayer), EconomyResponse.ResponseType.FAILURE, "Player not found");
        }
        int amount = (int) Math.ceil(v);
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.EMERALD) {
                int itemAmount = item.getAmount();
                if (itemAmount >= amount) {
                    item.setAmount(itemAmount - amount);
                    player.updateInventory();
                    return new EconomyResponse(v, getBalance(offlinePlayer), EconomyResponse.ResponseType.SUCCESS, null);
                } else {
                    amount -= itemAmount;
                    item.setAmount(0);
                }
            }
        }
        int emeraldBlocks = (int) Math.ceil((double) amount / 9.0);
        amount = (amount % 9) > 0 ? 9 - (amount % 9) : 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.EMERALD_BLOCK) {
                int itemAmount = item.getAmount();
                if (emeraldBlocks >= itemAmount) {
                    item.setAmount(itemAmount - emeraldBlocks);
                    if (amount > 0) player.getInventory().addItem(new ItemStack(Material.EMERALD, amount));
                    player.updateInventory();
                    return new EconomyResponse(v, getBalance(offlinePlayer), EconomyResponse.ResponseType.SUCCESS, null);
                } else {
                    emeraldBlocks -= itemAmount;
                    item.setAmount(0);
                }
            }
        }
        return new EconomyResponse(0, getBalance(offlinePlayer), EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, String s1, double v) {
        return withdrawPlayer(EmeraldBank.getInstance().getServer().getOfflinePlayer(s), s1, v);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, String s, double v) {
        return withdrawPlayer(offlinePlayer, v);
    }

    @Override
    public EconomyResponse depositPlayer(String s, double v) {
        return depositPlayer(EmeraldBank.getInstance().getServer().getOfflinePlayer(s), v);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, double v) {
        Player player = OpenInv.getPlugin(OpenInv.class).loadPlayer(offlinePlayer);
        if (player == null) {
            return new EconomyResponse(0, getBalance(offlinePlayer), EconomyResponse.ResponseType.FAILURE, "Player not found");
        }
        if (((int) v) % 9 > 0) player.getInventory().addItem(new ItemStack(Material.EMERALD, (int) v % 9));
        if (((int) v) / 9 > 0) player.getInventory().addItem(new ItemStack(Material.EMERALD_BLOCK, ((int) v) / 9));
        player.updateInventory();
        return new EconomyResponse(v, getBalance(offlinePlayer), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse depositPlayer(String s, String s1, double v) {
        return depositPlayer(EmeraldBank.getInstance().getServer().getOfflinePlayer(s), s1, v);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, String s, double v) {
        return depositPlayer(offlinePlayer, v);
    }

    @Override
    public EconomyResponse createBank(String name, String player) {
        return createBank(name, EmeraldBank.getInstance().getServer().getOfflinePlayer(player));
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer offlinePlayer) {
        if (!hasBankSupport()) return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banking is not enabled");
        final var account = createAccountData(name, offlinePlayer);
        return new EconomyResponse(0, account.balance(), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse deleteBank(String s) {
        if (!hasBankSupport()) return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banking is not enabled");
        var balance = bankBalance(s).balance;
        removeAccountData(s);
        return new EconomyResponse(0, balance, EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse bankBalance(String s) {
        if (!hasBankSupport()) return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banking is not enabled");
        var balance = getAccountData(s).balance();
        return new EconomyResponse(balance, balance, EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse bankHas(String s, double v) {
        if (!hasBankSupport()) return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banking is not enabled");
        final double balance = getAccountData(s).balance();
        return new EconomyResponse(balance, balance, balance >= v ? EconomyResponse.ResponseType.SUCCESS : EconomyResponse.ResponseType.FAILURE, null);
    }

    @Override
    public EconomyResponse bankWithdraw(String s, double v) {
        if (!hasBankSupport()) return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banking is not enabled");
        final BankAccount account = getAccountData(s);
        final double amount = account.withdraw(v);
        account.save();
        return new EconomyResponse(amount, account.balance(), amount == v ? EconomyResponse.ResponseType.SUCCESS : EconomyResponse.ResponseType.FAILURE, null);
    }

    @Override
    public EconomyResponse bankDeposit(String s, double v) {
        if (!hasBankSupport()) return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banking is not enabled");
        final BankAccount account = getAccountData(s);
        account.deposit(v);
        account.save();
        return new EconomyResponse(v, account.balance(), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse isBankOwner(String s, String s1) {
        return isBankOwner(s, EmeraldBank.getInstance().getServer().getOfflinePlayer(s1));
    }

    @Override
    public EconomyResponse isBankOwner(String s, OfflinePlayer offlinePlayer) {
        if (!hasBankSupport()) return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banking is not enabled");
        return new EconomyResponse(0, bankBalance(s).balance, getAccountData(s).isOwner(offlinePlayer) ? EconomyResponse.ResponseType.SUCCESS : EconomyResponse.ResponseType.FAILURE, null);
    }

    @Override
    public EconomyResponse isBankMember(String s, String s1) {
        return isBankMember(s, EmeraldBank.getInstance().getServer().getOfflinePlayer(s1));
    }

    @Override
    public EconomyResponse isBankMember(String s, OfflinePlayer offlinePlayer) {
        if (!hasBankSupport()) return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banking is not enabled");
        return new EconomyResponse(0, bankBalance(s).balance, getAccountData(s).isMember(offlinePlayer) ? EconomyResponse.ResponseType.SUCCESS : EconomyResponse.ResponseType.FAILURE, null);
    }

    @Override
    public List<String> getBanks() {
        if (!hasBankSupport()) return null;
        return getAccounts();
    }

    @Override
    public boolean createPlayerAccount(String s) {
        return createPlayerAccount(EmeraldBank.getInstance().getServer().getOfflinePlayer(s));
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer) {
        return OpenInv.getPlugin(OpenInv.class).loadPlayer(offlinePlayer) != null;
    }

    @Override
    public boolean createPlayerAccount(String s, String s1) {
        return createPlayerAccount(EmeraldBank.getInstance().getServer().getOfflinePlayer(s), s1);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer, String s) {
        return createPlayerAccount(offlinePlayer);
    }

    public boolean bankAddMember(String account, OfflinePlayer offlinePlayer) {
        var bank = getAccountData(account);
        if (bank.addMember(offlinePlayer)) {
            bank.save();
            return true;
        }
        return false;
    }

    public boolean bankRemoveMember(String account, OfflinePlayer player) {
        var bank = getAccountData(account);
        if (bank.removeMember(player)) {
            bank.save();
            return true;
        }
        return false;
    }

    public List<OfflinePlayer> getBankMembers(String account) {
        return getAccountData(account).members().stream().map(uuid -> EmeraldBank.getInstance().getServer().getOfflinePlayer(uuid)).toList();
    }

    public boolean bankTransfer(String account, OfflinePlayer target) {
        var bank = getAccountData(account);
        if (bank.isOwner(target)) return false;
        bank.removeMember(target);
        bank.addMember(bank.owner());
        bank.setOwner(target);
        bank.save();
        return true;
    }


    public static @NotNull TextComponent formatCurrency(double balance) {
        return Component.text(EmeraldBank.getInstance().getEconomy().format(balance));
    }

    private static @NotNull BankAccount getAccountData(String account) {
        if (!isAccountValid(account)) {
            throw new IllegalArgumentException("Account does not exist");
        }
        final PersistentDataContainer container = getPDC();
        final var bank = new BankAccount(container, Objects.requireNonNull(container.get(getBankKey(account), PersistentDataType.TAG_CONTAINER)), account);
        savePDC(container);
        return bank;
    }

    private static @NotNull BankAccount createAccountData(String account, OfflinePlayer owner) {
        if (isAccountValid(account)) {
            throw new IllegalArgumentException("Account already exists");
        }
        final PersistentDataContainer container = getPDC();
        container.set(getBankKey(account), PersistentDataType.TAG_CONTAINER, container.getAdapterContext().newPersistentDataContainer());
        final var bank = new BankAccount(container, Objects.requireNonNull(container.get(getBankKey(account), PersistentDataType.TAG_CONTAINER)), account);
        bank.setOwner(owner);
        bank.save();
        return bank;
    }

    private static void removeAccountData(String account) {
        if (!isAccountValid(account)) {
            throw new IllegalArgumentException("Account does not exist");
        }
        final PersistentDataContainer container = getPDC();
        container.remove(getBankKey(account));
        savePDC(container);
    }

    private static List<String> getAccounts() {
        return getPDC().getKeys().stream()
                .map(NamespacedKey::getKey)
                .toList();
    }

    static @NotNull PersistentDataContainer getPDC() {
        final PersistentDataContainer container = EmeraldUtils.getWorld().getPersistentDataContainer();
        var pdc = container.get(ACCOUNTS_KEY, PersistentDataType.TAG_CONTAINER);
        if (pdc == null) {
            container.set(ACCOUNTS_KEY, PersistentDataType.TAG_CONTAINER, container.getAdapterContext().newPersistentDataContainer());
        }
        return Objects.requireNonNull(container.get(ACCOUNTS_KEY, PersistentDataType.TAG_CONTAINER));
    }

    static void savePDC(@NotNull PersistentDataContainer container) {
        EmeraldUtils.getWorld().getPersistentDataContainer().set(ACCOUNTS_KEY, PersistentDataType.TAG_CONTAINER, container);
    }


    private static boolean isAccountValid(String account) {
        return getPDC().has(getBankKey(account), PersistentDataType.TAG_CONTAINER);
    }

    static NamespacedKey getBankKey(String account) {
        return new NamespacedKey(EmeraldBank.getInstance(), account);
    }

    public static NamespacedKey ACCOUNTS_KEY = new NamespacedKey(EmeraldBank.getInstance(), "accounts");


}
