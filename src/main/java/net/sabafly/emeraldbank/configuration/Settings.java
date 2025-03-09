package net.sabafly.emeraldbank.configuration;

import net.sabafly.emeraldbank.configuration.type.IntOr;
import net.sabafly.emeraldbank.database.Database;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class Settings extends BaseConfig {
    public static final String HEADER = """
              ______                          _     _ ____              _   \s
             |  ____|                        | |   | |  _ \\            | |  \s
             | |__   _ __ ___   ___ _ __ __ _| | __| | |_) | __ _ _ __ | | __
             |  __| | '_ ` _ \\ / _ | '__/ _` | |/ _` |  _ < / _` | '_ \\| |/ /
             | |____| | | | | |  __| | | (_| | | (_| | |_) | (_| | | | |   <\s
             |______|_| |_| |_|\\___|_|  \\__,_|_|\\__,_|____/ \\__,_|_| |_|_|\\_\\
                                                                            \s
                                                                            \s
            """;
    public static final String VERSION_FIELD = "config_version";

    @Setting(VERSION_FIELD)
    @Comment("DO NOT CHANGE THIS VALUE!")
    public int version = ConfigurationLoader.CURRENT_VERSION;

    @Comment("The cost for paying a player.")
    public IntOr.Disabled payCost = IntOr.Disabled.DISABLED;

    @Comment("""
            ################################################################
            #                                                              #
            #  Banking                                                     #
            #                                                              #
            ################################################################
            """)
    public Banking banking = new Banking();

    @ConfigSerializable
    public static class Banking extends BaseConfig {

        public Banking() {
        }

        @Comment("Enable or disable banking.")
        public boolean enabled = true;

        public Banking.BankingTax tax = new Banking.BankingTax();

        @ConfigSerializable
        public static class BankingTax {

            public BankingTax() {
            }

            @Comment("The cost for creating a bank.")
            public IntOr.Disabled createCost = IntOr.Disabled.DISABLED;
            @Comment("The cost for creating a bank.")
            public IntOr.Disabled addMemberCost = IntOr.Disabled.DISABLED;
            @Comment("The cost for removing a member from a bank.")
            public IntOr.Disabled transferBankCost = IntOr.Disabled.DISABLED;
            @Comment("The cost for sending from a bank to another bank.")
            public IntOr.Disabled depositCost = IntOr.Disabled.DISABLED;
            @Comment("The cost for withdrawing from a bank.")
            public IntOr.Disabled withdrawCost = IntOr.Disabled.DISABLED;
            @Comment("The cost for paying from a bank to a player.")
            public IntOr.Disabled payCost = IntOr.Disabled.DISABLED;
        }
    }

    @Comment("""
            The default destination for payments.
            INVENTORY: The player's inventory.
            WALLET: The player's wallet.
            """)
    public DefaultDestination defaultDestination = DefaultDestination.INVENTORY;

    public enum DefaultDestination {
        INVENTORY,
        WALLET
    }

    @Comment("""
            Load offline players' inventories.
            This setting requires OpenInv (https://github.com/Jikoo/OpenInv).
            Change of this setting will require a restart of the server.
            """)
    public boolean loadOfflinePlayersInventories = false;

    @Comment("""
            ################################################################
            #                                                              #
            #  Database                                                     #
            #                                                              #
            ################################################################
            """)
    public DatabaseSettings database = new DatabaseSettings();

    @ConfigSerializable
    public static class DatabaseSettings extends BaseConfig {

        public DatabaseSettings() {
        }

        @Comment("""
                The type of database to use.
                Change of database type will require a restart of the server.
                H2: H2 Database
                MYSQL: MySQL Database
                """)
        public DatabaseType type = DatabaseType.H2;

        public enum DatabaseType {
            H2,
            MYSQL
        }

        @Comment("The host of the database.")
        public String host = "localhost";

        @Comment("The port of the database.")
        public int port = 3306;

        @Comment("The name of the database.")
        public String database = "emeraldbank";

        @Comment("The username of the database.")
        public String username = "root";

        @Comment("The password of the database.")
        public String password = "password";

        public @NotNull Database createDatabase() {
            return switch (type) {
                case H2 -> new net.sabafly.emeraldbank.database.impl.H2();
                case MYSQL -> new net.sabafly.emeraldbank.database.impl.MySQL();
            };
        }

    }

    @Comment("""
            ################################################################
            #                                                              #
            #  Messages                                                    #
            #                                                              #
            ################################################################
            """)
    public Messages messages = new Messages();

    @ConfigSerializable
    public static class Messages extends BaseConfig {
        public String name = "EmeraldBank";

        public String reload = "<green>Reloaded configuration and messages";

        public String economyFormat = "<value> <green><currency></green>";
        public String currencyName = "Emerald";
        public String currencyNamePlural = "Emeralds";

        public String balance = "<green><player>'s Balance: <value> (Wallet: <wallet>)";
        public String balanceBank = "<green><bank>'s Balance: <value>";

        public String leaderboard = "<player>: <balance>";

        public String paySuccess = "<green>Successfully paid <value> to <player>";
        public String errorPay = "<red>Failed to pay <value> to <player>";
        public String errorPaySelf = "<red>You cannot pay yourself!";

        public String bankingCreate = "<green>Created bank <bank>";
        public String bankingDelete = "<green>Deleted bank <bank>";
        public String bankingDeposit = "<green>Deposited <value> to <bank>";
        public String bankingWithdraw = "<green>Withdrew <value> from <bank>";
        public String bankingAddMember = "<green>Added <player> to bank <bank>";
        public String bankingRemoveMember = "<green>Removed <player> from bank <bank>";
        public String bankingList = "<green>Banks: <banks>";
        public String bankingMembers = "<green>Members of <bank>: <members>";
        public String bankingAddOwner = "<green>Added <player> as owner of bank <bank>!";
        public String bankingRemoveOwner = "<green>Removed <player> as owner of bank <bank>!";
        public String bankingSend = "<green>Sent <value> from <bank_from> to <bank_to>";
        public String bankingPay = "<green>Paid <value> from bank <bank> to <player>";

        public String offlineTransaction = "<green><value> has been moved to your balance while you were offline!";

        public String wallet = "<green><player>'s Wallet: <value>";
        public String addWallet = "<green>Added <value> to <player>'s wallet!";
        public String withdrawWallet = "<green>Withdrew <value> from <player>'s wallet!";

        public String received = "<green>Received <value> from <source>";
        public String receiveBank = "<green>Received <value> to bank <destination> from <source>";

        public String errorBankingDisabled = "<red>Banking is disabled!";
        public String errorBankingCreate = "<red>Failed to create bank <bank>!";
        public String errorBankingDelete = "<red>Failed to delete bank <bank>!";
        public String errorBankingExists = "<red>Bank <bank> already exists!";
        public String errorBankingNoBank = "<red>Bank <bank> does not exist!";
        public String errorBankingDeposit = "<red>Failed to deposit <value> to <bank>";
        public String errorBankingWithdraw = "<red>Failed to withdraw <value> from <bank>";
        public String errorBankingNotOwner = "<red><player> is not the owner of bank <bank>!";
        public String errorBankingNotMember = "<red><player> is not a member of bank <bank>!";
        public String errorBankingMemberExists = "<red><player> is already a member of bank <bank>!";
        public String errorBankingAddMember = "<red>Failed to add <player> to bank <bank>!";
        public String errorBankingRemoveMember = "<red>Failed to remove <player> from bank <bank>!";
        public String errorBankingDeleteRemaining = "<red>Bank <bank> has a remaining balance of <value>!";
        public String errorBankingSend = "<red>Failed to send <value> from bank <bank_from> to bank <bank_to>!";
        public String errorBankingPay = "<red>Failed to pay <value> from bank <bank> to <player>!";
        public String errorBankingRemoveOwner = "<red><player> is the owner of bank <bank>!";
        public String errorBankingCreateCost = "<red>Failed to create bank due to insufficient funds <cost>!";
        public String errorBankingAddMemberCost = "<red>Failed to add member <player> to bank <bank> due to insufficient funds <cost>!";
        public String errorBankingTransferCost = "<red>Failed to transfer bank <bank> to <player> due to insufficient funds <cost>!";
        public String errorBankingDepositCost = "<red>Failed to deposit <value> to bank <bank> due to insufficient funds <cost>!";
        public String errorBankingWithdrawCost = "<red>Failed to withdraw <value> from bank <bank> due to insufficient funds <cost>!";
        public String errorBankingPayCost = "<red>Failed to pay <value> from bank <bank> to <player> due to insufficient funds <cost>!";
        public String errorPayCost = "<red>Failed to pay <value> to <player> due to insufficient funds <cost>!";
        public String errorAddWallet = "<red>Failed to add <value> to <player>'s wallet!";
        public String errorWithdrawWallet = "<red>Failed to withdraw <value> from <player>'s wallet!";
        public String errorReload = "<red>Failed to reload configuration and messages!";
        public String errorBankingRemoveLastOwner = "<red>Failed to remove <player> as owner of bank <bank>!";
        public String errorBankingRemoveLastMember = "<red>Failed to remove <player> as member of bank <bank>!";
        public String errorPlayerNotFound = "<red>Player <player> not found!";
    }

}
