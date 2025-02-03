package net.sabafly.emeraldbank.configuration;

import net.sabafly.emeraldbank.configuration.type.IntOr;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
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
    public int version = CURRENT_VERSION;

    public IntOr.Disabled payCost = IntOr.Disabled.DISABLED;

    public Banking banking = new Banking();

    @ConfigSerializable
    public static class Banking extends ConfigurationPart {

        public Banking() {}

        public boolean enabled = true;

        public BankingTax tax = new BankingTax();

        @ConfigSerializable
        public static class BankingTax extends ConfigurationPart {

            public BankingTax() {}

            public IntOr.Disabled createCost = IntOr.Disabled.DISABLED;
            public IntOr.Disabled addMemberCost = IntOr.Disabled.DISABLED;
            public IntOr.Disabled transferBankCost = IntOr.Disabled.DISABLED;
            public IntOr.Disabled depositCost = IntOr.Disabled.DISABLED;
            public IntOr.Disabled withdrawCost = IntOr.Disabled.DISABLED;
            public IntOr.Disabled payCost = IntOr.Disabled.DISABLED;
        }
    }

    public DefaultDestination defaultDestination = DefaultDestination.INVENTORY;

    public enum DefaultDestination {
        INVENTORY,
        WALLET
    }

}
