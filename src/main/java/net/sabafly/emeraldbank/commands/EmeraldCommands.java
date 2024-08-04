package net.sabafly.emeraldbank.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.handler.LifecycleEventHandler;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.sabafly.emeraldbank.EmeraldBank;
import net.sabafly.emeraldbank.util.EmeraldUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

import static net.sabafly.emeraldbank.economy.EmeraldEconomy.formatCurrency;
import static net.sabafly.emeraldbank.util.EmeraldUtils.*;

public class EmeraldCommands implements LifecycleEventHandler<ReloadableRegistrarEvent<Commands>> {
    @Override
    public void run(@NotNull ReloadableRegistrarEvent<Commands> event) {
        final Commands commands = event.registrar();
        commands.register(
                Commands.literal("emeraldbank")
                        .then(
                                Commands.literal("reload")
                                        .requires(context -> context.getSender().hasPermission("emeraldbank.reload"))
                                        .executes(context -> {
                                            try {
                                                EmeraldBank.getInstance().reloadConfiguration();
                                            } catch (IOException e) {
                                                throw new RuntimeException(e);
                                            }
                                            context.getSource().getSender().sendMessage(MiniMessage.miniMessage().deserialize(EmeraldBank.getInstance().getMessages().reload));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                        .build()
                        )
                        .then(
                                BalanceCommand.command()
                        )
                        .then(
                                PayCommand.command()
                        )
                        .then(
                                BankCommand.command()
                        )
                        .build(),
                List.of("em", "embank", "emeraldbank")
        );
        commands.register(
                BalanceCommand.command()
        );
        commands.register(
                PayCommand.command()
        );
        commands.register(
                BankCommand.command()
        );
    }

    static int pay(CommandContext<CommandSourceStack> context, int amount, Player from, Player to) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        if (to.getUniqueId().equals( from.getUniqueId()))
            throw createCommandException(getMessages().errorPaySelf);
        final int cost = EmeraldBank.getInstance().getGlobalConfiguration().payCost.or(0);
        if (amount<=cost)
            throw createCommandException(getMessages().errorPayCost, tagResolver("value", formatCurrency(amount)), tagResolver("player", to.name()), tagResolver("cost", formatCurrency(cost)));
        if (!EmeraldUtils.payPlayer(from, to, amount, cost))
            throw createCommandException(getMessages().errorPay, tagResolver("value", formatCurrency(amount)), tagResolver("player", to.name()));
        context.getSource().getSender().sendMessage(deserializeMiniMessage(getMessages().paySuccess, tagResolver("value", formatCurrency(amount)), tagResolver("player", to.name())));
        return amount;
    }

    static int printBalance(CommandContext<CommandSourceStack> context, Player player) {
        final double balance = EmeraldBank.getInstance().getEconomy().getBalance(player);
        context.getSource().getSender().sendMessage(deserializeMiniMessage(getMessages().balance, tagResolver("player", player.name()), tagResolver("value", formatCurrency(balance))));
        return (int) balance;
    }

}
