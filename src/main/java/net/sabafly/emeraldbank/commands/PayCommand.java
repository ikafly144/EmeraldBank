package net.sabafly.emeraldbank.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.sabafly.emeraldbank.EmeraldBank;
import net.sabafly.emeraldbank.util.EmeraldUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Optional;

import static net.sabafly.emeraldbank.util.EmeraldUtils.*;

@SuppressWarnings("UnstableApiUsage")
public class PayCommand {
    public static LiteralCommandNode<CommandSourceStack> command() {
        return Commands.literal("pay")
                        .requires(context -> context.getSender().hasPermission("emeraldbank.pay"))
                        .then(
                                Commands.argument("target", StringArgumentType.word())
                                        .requires(context -> context.getSender().hasPermission("emeraldbank.pay") && context.getSender() instanceof Player)
                                        .then(
                                                Commands.argument("amount", IntegerArgumentType.integer(1))
                                                        .requires(context -> context.getSender().hasPermission("emeraldbank.pay"))
                                                        .executes(context -> {
                                                            final String target = context.getArgument("target", String.class);
                                                            if (!(context.getSource().getExecutor() instanceof Player player))
                                                                throw net.minecraft.commands.CommandSourceStack.ERROR_NOT_PLAYER.create();
                                                            if (!Bukkit.getOfflinePlayer(target).hasPlayedBefore())
                                                                throw createCommandException(getMessages().errorPlayerNotFound, tagResolver("player", Component.text(target)));
                                                            final int amount = context.getArgument("amount", Integer.class);
                                                            return pay(context, amount, player, Bukkit.getOfflinePlayer(target));
                                                        })
                                                        .build()
                                        )
                                        .then(
                                                Commands.literal("all")
                                                        .requires(context -> context.getSender().hasPermission("emeraldbank.pay"))
                                                        .executes(context -> {
                                                            final String target = context.getArgument("target", String.class);
                                                            if (!(context.getSource().getExecutor() instanceof Player player))
                                                                throw net.minecraft.commands.CommandSourceStack.ERROR_NOT_PLAYER.create();
                                                            if (!Bukkit.getOfflinePlayer(target).hasPlayedBefore())
                                                                throw createCommandException(getMessages().errorPlayerNotFound, tagResolver("player", Component.text(target)));
                                                            final int amount = (int) EmeraldBank.getInstance().getEconomy().getBalance(player);
                                                            return pay(context, amount, player, Bukkit.getOfflinePlayer(target));
                                                        })
                                        )
                                        .build()
                        )
                        .build();
    }

    static int pay(CommandContext<CommandSourceStack> context, int amount, Player from, OfflinePlayer to) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        if (to.getUniqueId().equals(from.getUniqueId()))
            throw createCommandException(getMessages().errorPaySelf);
        int cost = EmeraldBank.getInstance().getSettings().payCost.or(0);
        if (context.getSource().getSender().hasPermission("emeraldbank.bypass.cost"))
            cost = 0;
        final var targetName = Component.text(Optional.ofNullable(to.getName()).orElse(to.getUniqueId().toString()));
        if (amount <= cost)
            throw createCommandException(getMessages().errorPayCost, tagResolver("value", formatCurrency(amount)), tagResolver("player", targetName), tagResolver("cost", formatCurrency(cost)));
        if (!EmeraldUtils.payPlayer(from, to, amount, cost))
            throw createCommandException(getMessages().errorPay, tagResolver("value", formatCurrency(amount)), tagResolver("player", targetName));
        context.getSource().getSender().sendMessage(deserializeMiniMessage(getMessages().paySuccess, tagResolver("value", formatCurrency(amount)), tagResolver("player", targetName), tagResolver("cost", formatCurrency(cost))));
        sendReceivedMessage(to, amount, from.name());
        return amount;
    }

    static void sendReceivedMessage(OfflinePlayer player, int amount, Component from) {
        Optional.ofNullable(player.getPlayer()).ifPresent(p -> p.sendMessage(deserializeMiniMessage(getMessages().received, tagResolver("value", formatCurrency(amount)), tagResolver("source", from))));
    }

}
