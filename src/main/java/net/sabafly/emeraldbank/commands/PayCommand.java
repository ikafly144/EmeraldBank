package net.sabafly.emeraldbank.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.sabafly.emeraldbank.EmeraldBank;
import net.sabafly.emeraldbank.util.EmeraldUtils;
import org.bukkit.entity.Player;

import static net.sabafly.emeraldbank.economy.EmeraldEconomy.formatCurrency;
import static net.sabafly.emeraldbank.util.EmeraldUtils.*;

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
                                                            return pay(context, amount, player, target);
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
                                                            return pay(context, amount, player, target);
                                                        })
                                        )
                                        .build()
                        )
                        .build();
    }

    static int pay(CommandContext<CommandSourceStack> context, int amount, Player from, Player to) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        if (to.getUniqueId().equals(from.getUniqueId()))
            throw createCommandException(getMessages().errorPaySelf);
        int cost = EmeraldBank.getInstance().getGlobalConfiguration().payCost.or(0);
        if (context.getSource().getSender().hasPermission("emeraldbank.admin"))
            cost = 0;
        if (amount <= cost)
            throw createCommandException(getMessages().errorPayCost, tagResolver("value", formatCurrency(amount)), tagResolver("player", to.name()), tagResolver("cost", formatCurrency(cost)));
        if (!EmeraldUtils.payPlayer(from, to, amount, cost))
            throw createCommandException(getMessages().errorPay, tagResolver("value", formatCurrency(amount)), tagResolver("player", to.name()));
        context.getSource().getSender().sendMessage(deserializeMiniMessage(getMessages().paySuccess, tagResolver("value", formatCurrency(amount)), tagResolver("player", to.name()), tagResolver("cost", formatCurrency(cost))));
        sendReceivedMessage(to, amount, from.name());
        return amount;
    }

    static void sendReceivedMessage(Player player, int amount, Component from) {
        player.sendMessage(deserializeMiniMessage(getMessages().received, tagResolver("value", formatCurrency(amount)), tagResolver("source", from)));
    }

}
