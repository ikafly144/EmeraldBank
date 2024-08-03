package net.sabafly.emeraldbank.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.entity.Player;

public class BalanceCommand {
    public static LiteralCommandNode<CommandSourceStack> command() {
        return Commands.literal("balance")
                .requires(context -> context.getSender().hasPermission("emeraldbank.balance"))
                .executes(context -> {
                    if (!(context.getSource().getSender() instanceof Player self)) {
                        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().create();
                    }
                    return EmeraldCommands.printBalance(context, self);
                })
                .then(
                        Commands.argument("player", ArgumentTypes.player())
                                .requires(context -> context.getSender().hasPermission("emeraldbank.balance.all"))
                                .executes(context -> {
                                    final Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
                                    return EmeraldCommands.printBalance(context, target);
                                })
                                .build()
                )
                .build();
    }
}
