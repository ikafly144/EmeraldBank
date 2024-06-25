package net.sabafly.emeraldbank.configuration;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class Messages extends ConfigurationPart {
    private static final Logger LOGGER = LogUtils.getClassLogger();
    static final int CURRENT_VERSION = 1;

    @Setting(Configuration.VERSION_FIELD)
    public int version = CURRENT_VERSION;

    public String name = "EmeraldBank";

    public String reload = "<green>Reloaded configuration and messages";

    public String economyFormat = "<value> <green><currency></green>";
    public String currencyName = "Emerald";
    public String currencyNamePlural = "Emeralds";

    public String balance = "<green><player>'s Balance: <value>";
    public String balanceBank = "<green><bank>'s Balance: <value>";

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
    public String bankingTransfer = "<green>Transferred <bank> to <player>";
    public String bankingSend = "<green>Sent <value> from <bank_from> to <bank_to>";
    public String bankingPay = "<green>Paid <value> from bank <bank> to <player>";

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
    public String errorBankingTransfer = "<red>Failed to transfer <bank> to <player>!";
    public String errorBankingSend = "<red>Failed to send <value> from bank <bank_from> to bank <bank_to>!";
    public String errorBankingPay = "<red>Failed to pay <value> from bank <bank> to <player>!";
    public String errorBankingRemoveOwner = "<red><player> is the owner of bank <bank>!";
    public String errorBankingCreateCost = "<red>Failed to create bank due to insufficient funds <value>!";
    public String errorBankingAddMemberCost = "<red>Failed to add member <player> to bank <bank> due to insufficient funds <value>!";
    public String errorBankingTransferCost = "<red>Failed to transfer bank <bank> to <player> due to insufficient funds <value>!";
    public String errorBankingDepositCost = "<red>Failed to deposit <value> to bank <bank> due to insufficient funds <cost>!";
    public String errorBankingWithdrawCost = "<red>Failed to withdraw <value> from bank <bank> due to insufficient funds <cost>!";
    public String errorBankingPayCost = "<red>Failed to pay <value> from bank <bank> to <player> due to insufficient funds <cost>!";
    public String errorPayCost = "<red>Failed to pay <value> to <player> due to insufficient funds <cost>!";
}
