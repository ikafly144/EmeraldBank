package net.sabafly.emeraldbank.configuration;

import net.sabafly.emeraldbank.configuration.type.IntOr;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "InnerClassMayBeStatic"})
@ConfigSerializable
public class GlobalConfiguration extends ConfigurationPart {
    static final int CURRENT_VERSION = 1;
    private static GlobalConfiguration instance;
    public static boolean isFirstStart = false;

    public static GlobalConfiguration get() {
        return instance;
    }

    static void set(GlobalConfiguration instance) {
        GlobalConfiguration.instance = instance;
    }

    @Setting(Configuration.VERSION_FIELD)
    @Comment("DO NOT CHANGE THIS VALUE!")
    public int version = CURRENT_VERSION;

    @Comment("The cost for creating a bank.")
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
    public static class Banking extends ConfigurationPart {

        public Banking() {}

        @Comment("Enable or disable banking.")
        public boolean enabled = true;

        @Comment("""
                ################################################################
                #                                                              #
                #  Banking Tax                                                 #
                #                                                              #
                ################################################################
                """)
        public BankingTax tax = new BankingTax();

        @ConfigSerializable
        public static class BankingTax extends ConfigurationPart {

            public BankingTax() {}

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

}
