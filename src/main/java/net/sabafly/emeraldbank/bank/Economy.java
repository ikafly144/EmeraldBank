package net.sabafly.emeraldbank.bank;

import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.EconomyResponse;
import net.sabafly.emeraldbank.EmeraldBank;
import org.bukkit.OfflinePlayer;

import java.util.List;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;
import static net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText;
import static net.sabafly.emeraldbank.EmeraldBank.config;
import static net.sabafly.emeraldbank.EmeraldBank.database;
import static net.sabafly.emeraldbank.util.EmeraldUtils.tagResolver;

public class Economy extends VaultEconomy {

    private EconomyResponse createResponse(double amount, double balance, boolean success) {
        return new EconomyResponse(success ? amount : 0, balance, success ? EconomyResponse.ResponseType.SUCCESS : EconomyResponse.ResponseType.FAILURE, "");
    }

    @Override
    public boolean isEnabled() {
        return EmeraldBank.getInstance().isEnabled();
    }

    @Override
    public String getName() {
        return "EmeraldBank";
    }

    @Override
    public boolean hasBankSupport() {
        return config().banking.enabled;
    }

    @Override
    public int fractionalDigits() {
        return 0;
    }

    @Override
    public String format(double amount) {
        return plainText().serialize(
                miniMessage().deserialize(config().messages.economyFormat, tagResolver("value", Component.text((int) amount)), tagResolver("currency", miniMessage().deserialize((amount == 1 ? currencyNameSingular() : currencyNamePlural()))))
        );
    }

    @Override
    public String currencyNamePlural() {
        return config().messages.currencyNamePlural;
    }

    @Override
    public String currencyNameSingular() {
        return config().messages.currencyName;
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return database().existsUser(player.getUniqueId());
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return database().existsUser(player.getUniqueId());
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return database().getUser(player.getUniqueId()).balance();
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return database().getUser(player.getUniqueId()).balance() >= amount;
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        User user = database().getUser(player.getUniqueId());
        boolean succeed = user.withdraw(amount);
        database().saveUser(user);
        return createResponse(amount, user.balance(), succeed);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        User user = database().getUser(player.getUniqueId());
        user.deposit(amount);
        database().saveUser(user);
        return createResponse(amount, user.balance(), true);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        if (database().getBank(name).isPresent()) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Bank already exists");
        }
        Bank bank = new Bank(name, 0);
        database().saveBank(bank);
        database().addOwner(name, player.getUniqueId());
        return createResponse(0, 0, true);
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        if (database().getBank(name).isEmpty()) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Bank does not exist");
        }
        database().deleteBank(name);
        return createResponse(0, 0, true);
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return database().getBank(name)
                .map(bank -> createResponse(bank.balance(), bank.balance(), true))
                .orElseGet(() -> new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Bank does not exist"));
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        final double balance = bankBalance(name).balance;
        return balance >= amount ? createResponse(amount, balance, true) : createResponse(amount, balance, false);
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        final var bank = database().getBank(name);
        if (bank.isEmpty()) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Bank does not exist");
        }
        final var bankAccount = bank.get();
        amount = bankAccount.withdraw(amount);
        database().saveBank(bankAccount);
        return createResponse(amount, bankAccount.balance(), true);
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        final var bank = database().getBank(name);
        if (bank.isEmpty()) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Bank does not exist");
        }
        final var bankAccount = bank.get();
        bankAccount.deposit(amount);
        database().saveBank(bankAccount);
        return createResponse(amount, bankAccount.balance(), true);
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return createResponse(-1, -1, database().getOwners(name).stream().anyMatch(owner -> owner.getUuid().equals(player.getUniqueId())));
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return createResponse(-1, -1, database().getMembers(name).stream().anyMatch(member -> member.getUuid().equals(player.getUniqueId())));
    }

    @Override
    public List<String> getBanks() {
        return database().getBanks().stream().map(Bank::name).toList();
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        if (database().existsUser(player.getUniqueId())) {
            return false;
        }
        database().saveUser(new User(player.getUniqueId(), 0, false, null));
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return createPlayerAccount(player);
    }
}
