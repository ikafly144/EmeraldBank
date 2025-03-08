package net.sabafly.emeraldbank.database.impl;

import net.sabafly.emeraldbank.bank.Bank;
import net.sabafly.emeraldbank.bank.User;
import net.sabafly.emeraldbank.database.Database;
import org.apache.commons.dbutils.QueryRunner;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class Base implements Database {

    private QueryRunner runner;

    private final String CREATE_TABLE_USERS = """
            CREATE TABLE IF NOT EXISTS emeraldbank_users (
                uuid VARCHAR(36) PRIMARY KEY,
                balance DOUBLE NOT NULL,
                use_wallet_first BOOLEAN NOT NULL,
                offline_transaction DOUBLE
            )
            """;

    private final String CREATE_TABLE_BANKS = """
            CREATE TABLE IF NOT EXISTS emeraldbank_banks (
                name VARCHAR(36) PRIMARY KEY,
                balance DOUBLE NOT NULL
            )
            """;

    private final String CREATE_TABLE_BANK_MEMBERS = """
            CREATE TABLE IF NOT EXISTS emeraldbank_bank_members (
                bank VARCHAR(36) NOT NULL REFERENCES emeraldbank_banks(name) ON DELETE CASCADE,
                member VARCHAR(36) NOT NULL REFERENCES emeraldbank_users(uuid) ON DELETE CASCADE,
                is_owner BOOLEAN NOT NULL,
                PRIMARY KEY (bank, member)
            )
            """;

    //TODO: Implement bank invites
//    private final String CREATE_TABLE_BANK_INVITES = """
//            CREATE TABLE IF NOT EXISTS emeraldbank_bank_invites (
//                bank VARCHAR(36) NOT NULL REFERENCES emeraldbank_banks(name) ON DELETE CASCADE,
//                member VARCHAR(36) NOT NULL REFERENCES emeraldbank_users(uuid) ON DELETE CASCADE
//            )
//            """;

//    private final String CREATE_TABLE_TRANSACTIONS = """
//            CREATE TABLE IF NOT EXISTS emeraldbank_transactions (
//                id SERIAL PRIMARY KEY,
//                sender VARCHAR(36) NOT NULL REFERENCES emeraldbank_users(uuid) ON DELETE CASCADE,
//                receiver VARCHAR(36) NOT NULL REFERENCES emeraldbank_users(uuid) ON DELETE CASCADE,
//                amount DOUBLE NOT NULL,
//                date TIMESTAMP NOT NULL
//            )
//            """;

    public abstract Connection getConnection();

    @Override
    public void setup() {
        runner = new QueryRunner();

        try (Connection connection = getConnection()) {
            runner.execute(connection, CREATE_TABLE_USERS);
            runner.execute(connection, CREATE_TABLE_BANKS);
            runner.execute(connection, CREATE_TABLE_BANK_MEMBERS);
//            runner.execute(connection, CREATE_TABLE_TRANSACTIONS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public @NotNull User getUser(@NotNull UUID uuid) {
        try (Connection connection = getConnection()) {
            return runner.query(connection, "SELECT * FROM emeraldbank_users WHERE uuid = ?", rs -> {
                if (!rs.next()) {
                    User user = new User(uuid, 0, false, null);
                    saveUser(user);
                    return user;
                }
                return new User(uuid, rs.getDouble("balance"), rs.getBoolean("use_wallet_first"), rs.getDouble("offline_transaction"));
            }, uuid.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("Failed to get user");
    }

    @Override
    public void saveUser(@NotNull User user) {
        try (Connection connection = getConnection()) {
            user.onSave();
            runner.update(connection, "INSERT INTO emeraldbank_users (uuid, balance, use_wallet_first, offline_transaction) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE balance = ?, use_wallet_first = ?, offline_transaction = ?",
                    user.getUuid().toString(), user.getWallet(), user.isUseWalletFirst(), user.getOfflineTransaction(),
                    user.getWallet(), user.isUseWalletFirst(), user.getOfflineTransaction()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public @NotNull Optional<Bank> getBank(@NotNull String name) {
        try (Connection connection = getConnection()) {
            return Optional.ofNullable(runner.query(connection, "SELECT * FROM emeraldbank_banks WHERE name = ?", rs -> {
                if (!rs.next()) {
                    return null;
                }
                return new Bank(rs.getString("name"), rs.getDouble("balance"));
            }, name));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public @NotNull List<User> getUsers() {
        try (Connection connection = getConnection()) {
            return runner.query(connection, "SELECT * FROM emeraldbank_users", rs -> {
                List<User> users = new ArrayList<>();
                while (rs.next()) {
                    users.add(new User(UUID.fromString(rs.getString("uuid")), rs.getDouble("balance"), rs.getBoolean("use_wallet_first"), rs.getDouble("offline_transaction")));
                }
                return users;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return List.of();
    }

    @Override
    public void saveBank(@NotNull Bank bank) {
        try (Connection connection = getConnection()) {
            runner.update(connection, "INSERT INTO emeraldbank_banks (name, balance) VALUES (?, ?) ON DUPLICATE KEY UPDATE balance = ?",
                    bank.getName(), bank.getBalance(), bank.getBalance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public @NotNull List<Bank> getBanksByOwner(@NotNull UUID owner) {
        try (Connection connection = getConnection()) {
            return runner.query(connection, "SELECT * FROM emeraldbank_banks WHERE name IN (SELECT bank FROM emeraldbank_bank_members WHERE member = ? AND is_owner = TRUE)", rs -> {
                List<Bank> banks = new ArrayList<>();
                while (rs.next()) {
                    banks.add(new Bank(rs.getString("name"), rs.getDouble("balance")));
                }
                return banks;
            }, owner.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("Failed to get banks by owner");
    }

    @Override
    public @NotNull List<Bank> getBanksByMember(@NotNull UUID member) {
        try (Connection connection = getConnection()) {
            return runner.query(connection, "SELECT * FROM emeraldbank_banks WHERE name IN (SELECT bank FROM emeraldbank_bank_members WHERE member = ?)", rs -> {
                List<Bank> banks = new ArrayList<>();
                while (rs.next()) {
                    banks.add(new Bank(rs.getString("name"), rs.getDouble("balance")));
                }
                return banks;
            }, member.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("Failed to get banks by member");
    }

    @Override
    public void deleteBank(@NotNull String name) {
        try (Connection connection = getConnection()) {
            runner.update(connection, "DELETE FROM emeraldbank_banks WHERE name = ?", name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public @NotNull List<User> getMembers(@NotNull String bank) {
        try (Connection connection = getConnection()) {
            return runner.query(connection, "SELECT * FROM emeraldbank_users WHERE uuid IN (SELECT member FROM emeraldbank_bank_members WHERE bank = ?)", rs -> {
                List<User> users = new ArrayList<>();
                while (rs.next()) {
                    users.add(new User(UUID.fromString(rs.getString("uuid")), rs.getDouble("balance"), rs.getBoolean("use_wallet_first"), rs.getDouble("offline_transaction")));
                }
                return users;
            }, bank);
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("Failed to get members");
    }

    @Override
    public void addMember(@NotNull String bank, @NotNull UUID member) {
        getUser(member);
        try (Connection connection = getConnection()) {
            runner.update(connection, "INSERT IGNORE INTO emeraldbank_bank_members (bank, member, is_owner) VALUES (?, ?, FALSE)",
                    bank, member.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeMember(@NotNull String bank, @NotNull UUID member) {
        try (Connection connection = getConnection()) {
            runner.update(connection, "DELETE FROM emeraldbank_bank_members WHERE bank = ? AND member = ?", bank, member.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean existsUser(UUID uniqueId) {
        try (Connection connection = getConnection()) {
            return runner.query(connection, "SELECT EXISTS(SELECT 1 FROM emeraldbank_users WHERE uuid = ?)", rs -> {
                rs.next();
                return rs.getBoolean(1);
            }, uniqueId.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("Failed to check if user exists");
    }

    @Override
    public @NotNull List<User> getOwners(String name) {
        try (Connection connection = getConnection()) {
            return runner.query(connection, "SELECT * FROM emeraldbank_users WHERE uuid IN (SELECT member FROM emeraldbank_bank_members WHERE bank = ? AND is_owner = TRUE)", rs -> {
                List<User> users = new ArrayList<>();
                while (rs.next()) {
                    users.add(new User(UUID.fromString(rs.getString("uuid")), rs.getDouble("balance"), rs.getBoolean("use_wallet_first"), rs.getDouble("offline_transaction")));
                }
                return users;
            }, name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("Failed to get owners");
    }

    @Override
    public void addOwner(@NotNull String bank, @NotNull UUID owner) {
        getUser(owner);
        try (Connection connection = getConnection()) {
            runner.update(connection, "INSERT INTO emeraldbank_bank_members (bank, member, is_owner) VALUES (?, ?, TRUE) ON DUPLICATE KEY UPDATE is_owner = TRUE",
                    bank, owner.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Bank> getBanks() {
        try (Connection connection = getConnection()) {
            return runner.query(connection, "SELECT * FROM emeraldbank_banks", rs -> {
                List<Bank> banks = new ArrayList<>();
                while (rs.next()) {
                    banks.add(new Bank(rs.getString("name"), rs.getDouble("balance")));
                }
                return banks;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("Failed to get banks");
    }

    @Override
    public void removeOwner(String account, @NotNull UUID uniqueId) {
        // make member owner
        try (Connection connection = getConnection()) {
            runner.update(connection, "UPDATE emeraldbank_bank_members SET is_owner = FALSE WHERE bank = ? AND member = ?", account, uniqueId.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
