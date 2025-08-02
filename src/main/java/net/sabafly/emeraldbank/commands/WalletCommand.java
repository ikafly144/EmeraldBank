package net.sabafly.emeraldbank.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.sabafly.emeraldbank.bank.User;
import org.bukkit.entity.Player;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;
import static net.sabafly.emeraldbank.EmeraldBank.config;
import static net.sabafly.emeraldbank.EmeraldBank.database;
import static net.sabafly.emeraldbank.util.EmeraldUtils.*;
import static net.sabafly.emeraldbank.util.PlayerInventoryUtils.*;

public class WalletCommand {
    public static LiteralCommandNode<CommandSourceStack> command() {
        return Commands.literal("wallet")
                .then(Commands.literal("balance")
                        .requires(context -> context.getSender().hasPermission("emeraldbank.wallet.balance"))
                        .executes(context -> {
                            if (!(context.getSource().getExecutor() instanceof Player player))
                                throw net.minecraft.commands.CommandSourceStack.ERROR_NOT_PLAYER.create();
                            return printWallet(context, player);
                        })
                        .then(Commands.argument("player", ArgumentTypes.player())
                                .requires(context -> context.getSender().hasPermission("emeraldbank.wallet.balance.all"))
                                .executes(context -> {
                                    final Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
                                    return printWallet(context, target);
                                })
                                .build())
                        .build())
                .then(Commands.literal("add")
                        .requires(context -> context.getSender().hasPermission("emeraldbank.wallet.add"))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .requires(context -> context.getSender().hasPermission("emeraldbank.wallet.add"))
                                .executes(context -> {
                                    if (!(context.getSource().getExecutor() instanceof Player player))
                                        throw net.minecraft.commands.CommandSourceStack.ERROR_NOT_PLAYER.create();
                                    final int amount = context.getArgument("amount", Integer.class);
                                    if (getCurrencyCount(player, config().getDefaultCurrency()) < amount)
                                        throw createCommandException(getMessages().errorAddWallet, tagResolver("value", formatCurrency(amount)), tagResolver("player", player.name()));
                                    removeCurrency(player, config().getDefaultCurrency(), amount);
                                    User user = database().getUser(player.getUniqueId());
                                    user.addWallet(amount);
                                    database().saveUser(user);
                                    context.getSource().getSender().sendMessage(miniMessage().deserialize(getMessages().addWallet, tagResolver("value", formatCurrency(amount)), tagResolver("player", player.name())));
                                    return amount;
                                })
                        )
                        .then(Commands.literal("all")
                                .requires(context -> context.getSender().hasPermission("emeraldbank.wallet.add"))
                                .executes(context -> {
                                    if (!(context.getSource().getExecutor() instanceof Player player))
                                        throw net.minecraft.commands.CommandSourceStack.ERROR_NOT_PLAYER.create();
                                    final int count = getCurrencyCount(player, config().getDefaultCurrency());
                                    removeCurrency(player, config().getDefaultCurrency(), count);
                                    User user = database().getUser(player.getUniqueId());
                                    user.addWallet(count);
                                    database().saveUser(user);
                                    context.getSource().getSender().sendMessage(miniMessage().deserialize(getMessages().addWallet, tagResolver("value", formatCurrency(count)), tagResolver("player", player.name())));
                                    return count;
                                })
                                .build())
                        .build())
                .then(Commands.literal("withdraw")
                        .requires(context -> context.getSender().hasPermission("emeraldbank.wallet.withdraw"))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .requires(context -> context.getSender().hasPermission("emeraldbank.wallet.withdraw"))
                                .executes(context -> {
                                    if (!(context.getSource().getExecutor() instanceof Player player))
                                        throw net.minecraft.commands.CommandSourceStack.ERROR_NOT_PLAYER.create();
                                    int amount = context.getArgument("amount", Integer.class);
                                    User user = database().getUser(player.getUniqueId());
                                    if (user.wallet() < amount)
                                        throw createCommandException(getMessages().errorWithdrawWallet, tagResolver("value", formatCurrency(amount)), tagResolver("player", player.name()));
                                    user.removeWallet(amount);
                                    // 余り
                                    int remaining = addCurrency(player, config().getDefaultCurrency(), amount);
                                    user.addWallet(remaining);
                                    amount -= remaining;
                                    database().saveUser(user);
                                    context.getSource().getSender().sendMessage(miniMessage().deserialize(getMessages().withdrawWallet, tagResolver("value", formatCurrency(amount)), tagResolver("player", player.name())));
                                    return amount;
                                })
                        )
                        .build())
                .build();
    }

    static int printWallet(CommandContext<CommandSourceStack> context, Player player) {
        final double balance = database().getUser(player.getUniqueId()).wallet();
        context.getSource().getSender().sendMessage(miniMessage().deserialize(getMessages().wallet, tagResolver("player", player.name()), tagResolver("value", formatCurrency(balance))));
        return (int) balance;
    }

}
