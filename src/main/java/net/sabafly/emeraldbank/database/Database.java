package net.sabafly.emeraldbank.database;

import net.sabafly.emeraldbank.bank.Bank;
import net.sabafly.emeraldbank.bank.User;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Database {

    void setup();

    void close();

    @NotNull User getUser(@NotNull UUID uuid);

    void saveUser(@NotNull User user);

    @NotNull Optional<Bank> getBank(@NotNull String name);

    @NotNull List<User> getUsers();

    void saveBank(@NotNull Bank bank);

    @NotNull List<Bank> getBanksByOwner(@NotNull UUID owner);

    @NotNull List<Bank> getBanksByMember(@NotNull UUID member);

    void deleteBank(@NotNull String name);

    @NotNull List<User> getMembers(@NotNull String bank);

    @NotNull List<User> getOwners(String name);

    void addMember(@NotNull String bank, @NotNull UUID member);

    void addOwner(@NotNull String bank, @NotNull UUID owner);

    void removeMember(@NotNull String bank, @NotNull UUID member);

    List<Bank> getBanks();

    boolean existsUser(UUID uniqueId);

    void removeOwner(String account, @NotNull UUID uniqueId);
}
