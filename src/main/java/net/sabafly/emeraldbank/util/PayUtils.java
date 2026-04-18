package net.sabafly.emeraldbank.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Optional;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;
import static net.sabafly.emeraldbank.EmeraldBank.config;
import static net.sabafly.emeraldbank.EmeraldBank.economy;
import static net.sabafly.emeraldbank.util.EmeraldUtils.*;

public final class PayUtils {
    private PayUtils() {
    }

    public static int payPlayer(CommandContext<CommandSourceStack> context, int amount, Player from, OfflinePlayer to) throws CommandSyntaxException {
        if (to.getUniqueId().equals(from.getUniqueId())) {
            throw createCommandException(getMessages().errorPaySelf);
        }
        int cost = config().payCost.or(0);
        if (context.getSource().getSender().hasPermission("emeraldbank.bypass.cost")) {
            cost = 0;
        }
        final var targetName = Component.text(Optional.ofNullable(to.getName()).orElse(to.getUniqueId().toString()));
        if (amount <= cost) {
            throw createCommandException(getMessages().errorPayCost, tagResolver("value", formatCurrency(amount)), tagResolver("player", targetName), tagResolver("cost", formatCurrency(cost)));
        }
        if (!EmeraldUtils.payPlayer(from, to, amount, cost)) {
            throw createCommandException(getMessages().errorPay, tagResolver("value", formatCurrency(amount)), tagResolver("player", targetName));
        }
        context.getSource().getSender().sendMessage(miniMessage().deserialize(getMessages().paySuccess, tagResolver("value", formatCurrency(amount)), tagResolver("player", targetName), tagResolver("cost", formatCurrency(cost))));
        sendReceivedMessage(to, amount, from.name());
        return amount;
    }

    public static int payFromBank(CommandContext<CommandSourceStack> context, int amount, String bankFrom, Player to) throws CommandSyntaxException {
        int cost = config().banking.tax.payCost.or(config().banking.tax.withdrawCost.or(0));
        if (context.getSource().getSender().hasPermission("emeraldbank.bypass.cost")) {
            cost = 0;
        }
        if (amount <= cost) {
            throw createCommandException(getMessages().errorBankingPayCost, tagResolver("player", to.name()), tagResolver("value", formatCurrency(amount)), tagResolver("bank", Component.text(bankFrom)), tagResolver("cost", formatCurrency(cost)));
        }
        if (!economy().hasBankSupport()) {
            throw createCommandException(getMessages().errorBankingDisabled);
        }
        EconomyResponse response = economy().bankWithdraw(bankFrom, amount);
        if (!response.transactionSuccess()) {
            throw createCommandException(getMessages().errorBankingPay, tagResolver("value", formatCurrency(amount)), tagResolver("bank", Component.text(bankFrom)), tagResolver("player", to.name()));
        }
        response = economy().depositPlayer(to, response.amount - cost);
        if (!response.transactionSuccess()) {
            throw createCommandException(getMessages().errorBankingPay, tagResolver("value", formatCurrency(amount)), tagResolver("bank", Component.text(bankFrom)), tagResolver("player", to.name()));
        }
        context.getSource().getSender().sendMessage(miniMessage().deserialize(getMessages().bankingPay, tagResolver("value", formatCurrency(amount)), tagResolver("bank", Component.text(bankFrom)), tagResolver("player", to.name()), tagResolver("cost", formatCurrency(cost))));
        var src = context.getSource();
        sendReceivedMessage(to, amount, src != null ? src.getExecutor() != null ? src.getExecutor().name() : Component.text("SERVER") : Component.text("SERVER"));
        return amount;
    }

    public static void sendReceivedMessage(OfflinePlayer player, int amount, Component from) {
        Optional.ofNullable(player.getPlayer()).ifPresent(p -> p.sendMessage(miniMessage().deserialize(getMessages().received, tagResolver("value", formatCurrency(amount)), tagResolver("source", from))));
    }
}
