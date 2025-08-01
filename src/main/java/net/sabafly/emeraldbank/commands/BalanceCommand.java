package net.sabafly.emeraldbank.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.entity.Player;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;
import static net.sabafly.emeraldbank.EmeraldBank.database;
import static net.sabafly.emeraldbank.EmeraldBank.economy;
import static net.sabafly.emeraldbank.util.EmeraldUtils.formatCurrency;
import static net.sabafly.emeraldbank.util.EmeraldUtils.*;
import static net.sabafly.emeraldbank.util.EmeraldUtils.tagResolver;

public class BalanceCommand {
    public static LiteralCommandNode<CommandSourceStack> command() {
        return Commands.literal("balance")
                .requires(context -> context.getSender().hasPermission("emeraldbank.balance"))
                .executes(context -> {
                    if (!(context.getSource().getExecutor() instanceof Player player))
                        throw net.minecraft.commands.CommandSourceStack.ERROR_NOT_PLAYER.create();
                    return printBalance(context, player);
                })
                .then(
                        Commands.argument("player", ArgumentTypes.player())
                                .requires(context -> context.getSender().hasPermission("emeraldbank.balance.all"))
                                .executes(context -> {
                                    final Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
                                    return printBalance(context, target);
                                })
                                .build()
                )
                .build();
    }

    static int printBalance(CommandContext<CommandSourceStack> context, Player player) {
        final double balance = economy().getBalance(player);
        context.getSource().getSender().sendMessage(miniMessage().deserialize(getMessages().balance, tagResolver("player", player.name()), tagResolver("value", formatCurrency(balance)), tagResolver("wallet", formatCurrency(database().getUser(player.getUniqueId()).wallet()))));
        return (int) balance;
    }
}
