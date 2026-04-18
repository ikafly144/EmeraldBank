package net.sabafly.emeraldbank.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.sabafly.emeraldbank.util.PayUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import static net.sabafly.emeraldbank.EmeraldBank.*;
import static net.sabafly.emeraldbank.util.EmeraldUtils.*;

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
                                                             if (!database().existsUser(Bukkit.getOfflinePlayer(target).getUniqueId()))
                                                                 throw createCommandException(getMessages().errorPlayerNotFound, tagResolver("player", Component.text(target)));
                                                             final int amount = context.getArgument("amount", Integer.class);
                                                             return PayUtils.payPlayer(context, amount, player, Bukkit.getOfflinePlayer(target));
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
                                                             if (!database().existsUser(Bukkit.getOfflinePlayer(target).getUniqueId()))
                                                                 throw createCommandException(getMessages().errorPlayerNotFound, tagResolver("player", Component.text(target)));
                                                             final int amount = (int) economy().getBalance(player);
                                                             return PayUtils.payPlayer(context, amount, player, Bukkit.getOfflinePlayer(target));
                                                         })
                                         )
                                         .build()
                         )
                         .build();
    }

}
