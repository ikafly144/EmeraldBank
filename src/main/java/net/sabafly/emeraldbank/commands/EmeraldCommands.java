package net.sabafly.emeraldbank.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.handler.LifecycleEventHandler;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.sabafly.emeraldbank.EmeraldBank;
import net.sabafly.emeraldbank.bank.User;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;
import static net.sabafly.emeraldbank.EmeraldBank.config;
import static net.sabafly.emeraldbank.EmeraldBank.database;
import static net.sabafly.emeraldbank.util.EmeraldUtils.*;

public class EmeraldCommands implements LifecycleEventHandler<@NotNull ReloadableRegistrarEvent<@NotNull Commands>> {
    @Override
    public void run(@NotNull ReloadableRegistrarEvent<@NotNull Commands> event) {
        final Commands commands = event.registrar();
        commands.register(
                Commands.literal("emeraldbank")
                        .then(
                                Commands.literal("reload")
                                        .requires(context -> context.getSender().hasPermission("emeraldbank.reload"))
                                        .executes(context -> {
                                            if (EmeraldBank.getInstance().loadConfiguration())
                                                context.getSource().getSender().sendMessage(miniMessage().deserialize(config().messages.reload));
                                            else
                                                context.getSource().getSender().sendMessage(miniMessage().deserialize(config().messages.errorReload));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                        .build()
                        )
                        .then(
                                Commands.literal("leaderboard")
                                        .requires(context -> context.getSender().hasPermission("emeraldbank.leaderboard"))
                                        .then(Commands.argument("page", IntegerArgumentType.integer(1, 100))
                                                .executes(context -> printLeaderboard(context, IntegerArgumentType.getInteger(context, "page")))
                                        )
                                        .executes(context -> printLeaderboard(context, 1))
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
                        .then(
                                WalletCommand.command()
                        )
                        .then(
                                ExchangeCommand.command()
                        )
                        .then(
                                AdminCommand.command()
                        )
                        .build(),
                List.of("em", "embank", "emeraldbank", "emerald")
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
        commands.register(
                WalletCommand.command()
        );
        commands.register(
                ExchangeCommand.command()
        );
    }

    private static final int ENTRIES_PER_PAGE = 10;

    static int printLeaderboard(CommandContext<CommandSourceStack> context, int page) {
        var result = Component.text();
        result.append(miniMessage().deserialize(getMessages().leaderboardHeader,
                Placeholder.unparsed("page", String.valueOf(page))
        ));
        int i = 0;
        int after = (page - 1) * ENTRIES_PER_PAGE;
        var users = new ArrayList<>(database().getTopUsers(after, ENTRIES_PER_PAGE));
        result.appendNewline();
        if (users.isEmpty()) {
            result.append(miniMessage().deserialize(config().messages.leaderboardEmpty));
        }
        users.sort(Comparator.comparingDouble(User::balance));
        for (@NotNull User user : users) {
            i++;
            var name = user.getName();
            result.append(miniMessage().deserialize(getMessages().leaderboard, tagResolver("player", Component.text(name)), tagResolver("balance", formatCurrency(user.balance()))));
            if (i == users.size()) {
                break;
            }
            result.appendNewline();
        }
        context.getSource().getSender().sendMessage(result);
        return i;
    }

}
