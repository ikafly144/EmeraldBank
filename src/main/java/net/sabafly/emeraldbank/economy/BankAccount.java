package net.sabafly.emeraldbank.economy;

import net.sabafly.emeraldbank.EmeraldBank;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@Deprecated
public class BankAccount {
    public static final NamespacedKey BALANCE_KEY = new NamespacedKey(EmeraldBank.getInstance(), "balance");
    public static final NamespacedKey MEMBERS_KEY = new NamespacedKey(EmeraldBank.getInstance(), "members");
    public static final NamespacedKey OWNER_KEY = new NamespacedKey(EmeraldBank.getInstance(), "owner");

    private final PersistentDataContainer container;
    private final PersistentDataContainer pdc;
    private final String account;

    protected BankAccount(@NotNull PersistentDataContainer pdc, @NotNull PersistentDataContainer container, @NotNull String account) {
        this.container = container;
        this.account = account;
        this.pdc = pdc;
    }

    public double balance() {
        return container.getOrDefault(BALANCE_KEY, PersistentDataType.DOUBLE, 0.0);
    }

    public void balance(double balance) {
        container.set(BALANCE_KEY, PersistentDataType.DOUBLE, balance);
    }

    public void deposit(double amount) {
        balance(balance() + amount);
    }

    public double withdraw(double amount) {
        var balance = balance();
        if (balance < amount) {
            return 0;
        }
        balance(balance - amount);
        return amount;
    }

    public OfflinePlayer owner() {
        return EmeraldBank.getInstance().getServer().getOfflinePlayer(UUID.fromString(Objects.requireNonNull(container.get(OWNER_KEY, PersistentDataType.STRING))));
    }

    public void setOwner(@NotNull OfflinePlayer player) {
        Objects.requireNonNull(player);
        container.set(OWNER_KEY, PersistentDataType.STRING, player.getUniqueId().toString());
    }

    public Collection<UUID> members() {
        final Set<UUID> collect = container.getOrDefault(MEMBERS_KEY, PersistentDataType.LIST.strings(), new ArrayList<>()).stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());
        collect.add(owner().getUniqueId());
        return collect;
    }

    public boolean isMember(OfflinePlayer player) {
        return members().contains(player.getUniqueId()) || isOwner(player);
    }

    public boolean isOwner(OfflinePlayer player) {
        return Objects.equals(owner().getUniqueId(), player.getUniqueId());
    }

    public boolean addMember(OfflinePlayer player) {
        var members = container.getOrDefault(MEMBERS_KEY, PersistentDataType.LIST.strings(), new ArrayList<>());
        if (members.contains(player.getUniqueId().toString())) {
            return false;
        }
        members.add(player.getUniqueId().toString());
        container.set(MEMBERS_KEY, PersistentDataType.LIST.strings(), members);
        return true;
    }

    public boolean removeMember(OfflinePlayer player) {
        var members = container.getOrDefault(MEMBERS_KEY, PersistentDataType.LIST.strings(), new ArrayList<>());
        if (!members.contains(player.getUniqueId().toString())) {
            return false;
        }
        members.remove(player.getUniqueId().toString());
        container.set(MEMBERS_KEY, PersistentDataType.LIST.strings(), members);
        return true;
    }

    public void save() {
        pdc.set(EmeraldEconomy.getBankKey(account), PersistentDataType.TAG_CONTAINER, container);
        EmeraldEconomy.savePDC(pdc);
    }
}
