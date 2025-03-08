package net.sabafly.emeraldbank.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.handler.LifecycleEventHandler;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.sabafly.emeraldbank.EmeraldBank;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static net.sabafly.emeraldbank.util.EmeraldUtils.*;

@SuppressWarnings("UnstableApiUsage")
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
                                                context.getSource().getSender().sendMessage(MiniMessage.miniMessage().deserialize(EmeraldBank.getInstance().getSettings().messages.reload));
                                            else
                                                context.getSource().getSender().sendMessage(MiniMessage.miniMessage().deserialize(EmeraldBank.getInstance().getSettings().messages.errorReload));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                        .build()
                        )
                        .then(
                                Commands.literal("leaderboard")
                                        .requires(context -> context.getSender().hasPermission("emeraldbank.leaderboard"))
                                        .executes(EmeraldCommands::printLeaderboard)
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
    }

    static int printLeaderboard(CommandContext<CommandSourceStack> context) {
        var result = Component.text();
        int i =0;
        var offlinePlayers = new ArrayList<>(List.of(Bukkit.getServer().getOfflinePlayers()));
        offlinePlayers.sort((o1, o2) -> Double.compare(getEconomy().getBalance(o2), getEconomy().getBalance(o1)));
        for (@NotNull OfflinePlayer offlinePlayer : offlinePlayers) {
            i++;
            var name = offlinePlayer.getName();
            if (name == null) {
                name = offlinePlayer.getUniqueId().toString();
            }
            result.append(deserializeMiniMessage(getMessages().leaderboard, tagResolver("player", Component.text(name)), tagResolver("balance", formatCurrency(getEconomy().getBalance(offlinePlayer)))));
            if (i == offlinePlayers.size()) {
                break;
            }
            result.appendNewline();
        }
        context.getSource().getSender().sendMessage(result);
        return i;
    }

}
