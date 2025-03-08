package net.sabafly.emeraldbank.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.sabafly.emeraldbank.bank.User;
import net.sabafly.emeraldbank.util.PlayerInventoryUtils;
import org.bukkit.entity.Player;

import static net.sabafly.emeraldbank.EmeraldBank.database;
import static net.sabafly.emeraldbank.util.EmeraldUtils.*;

@SuppressWarnings("UnstableApiUsage")
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
                                    if (PlayerInventoryUtils.getEmeraldsAmount(player) < amount)
                                        throw createCommandException(getMessages().errorAddWallet, tagResolver("value", formatCurrency(amount)), tagResolver("player", player.name()));
                                    PlayerInventoryUtils.removeEmeralds(player, amount);
                                    User user = database().getUser(player.getUniqueId());
                                    user.addWallet(amount);
                                    database().saveUser(user);
                                    context.getSource().getSender().sendMessage(deserializeMiniMessage(getMessages().addWallet, tagResolver("value", formatCurrency(amount)), tagResolver("player", player.name())));
                                    return amount;
                                })
                        )
                        .then(Commands.literal("all")
                                .requires(context -> context.getSender().hasPermission("emeraldbank.wallet.add"))
                                .executes(context -> {
                                    if (!(context.getSource().getExecutor() instanceof Player player))
                                        throw net.minecraft.commands.CommandSourceStack.ERROR_NOT_PLAYER.create();
                                    final int amount = PlayerInventoryUtils.getEmeraldsAmount(player);
                                    PlayerInventoryUtils.removeEmeralds(player, amount);
                                    User user = database().getUser(player.getUniqueId());
                                    user.addWallet(amount);
                                    database().saveUser(user);
                                    context.getSource().getSender().sendMessage(deserializeMiniMessage(getMessages().addWallet, tagResolver("value", formatCurrency(amount)), tagResolver("player", player.name())));
                                    return amount;
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
                                    if (user.getWallet() < amount)
                                        throw createCommandException(getMessages().errorWithdrawWallet, tagResolver("value", formatCurrency(amount)), tagResolver("player", player.name()));
                                    user.removeWallet(amount);
                                    // 余り
                                    int remaining = PlayerInventoryUtils.addEmeralds(player, amount);
                                    user.addWallet(remaining);
                                    amount -= remaining;
                                    database().saveUser(user);
                                    context.getSource().getSender().sendMessage(deserializeMiniMessage(getMessages().withdrawWallet, tagResolver("value", formatCurrency(amount)), tagResolver("player", player.name())));
                                    return amount;
                                })
                        )
                        .build())
                .build();
    }

    static int printWallet(CommandContext<CommandSourceStack> context, Player player) {
        final double balance = database().getUser(player.getUniqueId()).getWallet();
        context.getSource().getSender().sendMessage(deserializeMiniMessage(getMessages().wallet, tagResolver("player", player.name()), tagResolver("value", formatCurrency(balance))));
        return (int) balance;
    }

}
