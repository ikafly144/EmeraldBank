package net.sabafly.emeraldbank.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.handler.LifecycleEventHandler;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.sabafly.emeraldbank.EmeraldBank;
import net.sabafly.emeraldbank.util.EmeraldUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

import static net.sabafly.emeraldbank.economy.EmeraldEconomy.formatCurrency;
import static net.sabafly.emeraldbank.util.EmeraldUtils.*;

public class EmeraldCommands implements LifecycleEventHandler<ReloadableRegistrarEvent<Commands>> {
    @Override
    public void run(@NotNull ReloadableRegistrarEvent<Commands> event) {
        final Commands commands = event.registrar();
        commands.register(
                Commands.literal("emeraldbank")
                        .then(
                                Commands.literal("reload")
                                        .requires(context -> context.getSender().hasPermission("emeraldbank.reload"))
                                        .executes(context -> {
                                            try {
                                                EmeraldBank.getInstance().reloadConfiguration();
                                            } catch (IOException e) {
                                                throw new RuntimeException(e);
                                            }
                                            context.getSource().getSender().sendMessage(MiniMessage.miniMessage().deserialize(EmeraldBank.getInstance().getMessages().reload));
                                            return Command.SINGLE_SUCCESS;
                                        })
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
                        .build(),
                List.of("em", "embank", "emeraldbank")
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
    }

}
