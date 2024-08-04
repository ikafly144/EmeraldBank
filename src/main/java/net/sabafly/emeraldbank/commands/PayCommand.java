package net.sabafly.emeraldbank.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.sabafly.emeraldbank.EmeraldBank;
import org.bukkit.entity.Player;

public class PayCommand {
    public static LiteralCommandNode<CommandSourceStack> command() {
        return Commands.literal("pay")
                        .requires(context -> context.getSender().hasPermission("emeraldbank.pay"))
                        .then(
                                Commands.argument("target", ArgumentTypes.player())
                                        .requires(context -> context.getSender().hasPermission("emeraldbank.pay") && context.getSender() instanceof Player)
                                        .then(
                                                Commands.argument("amount", IntegerArgumentType.integer(1))
                                                        .requires(context -> context.getSender().hasPermission("emeraldbank.pay"))
                                                        .executes(context -> {
                                                            final Player target = context.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
                                                            if (!(context.getSource().getExecutor() instanceof Player player))
                                                                throw net.minecraft.commands.CommandSourceStack.ERROR_NOT_PLAYER.create();
                                                            final int amount = context.getArgument("amount", Integer.class);
                                                            return EmeraldCommands.pay(context, amount, player, target);
                                                        })
                                                        .build()
                                        )
                                        .then(
                                                Commands.literal("all")
                                                        .requires(context -> context.getSender().hasPermission("emeraldbank.pay"))
                                                        .executes(context -> {
                                                            final Player target = context.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
                                                            if (!(context.getSource().getExecutor() instanceof Player player))
                                                                throw net.minecraft.commands.CommandSourceStack.ERROR_NOT_PLAYER.create();
                                                            final int amount = (int) EmeraldBank.getInstance().getEconomy().getBalance(player);
                                                            return EmeraldCommands.pay(context, amount, player, target);
                                                        })
                                        )
                                        .build()
                        )
                        .build();
    }
}
