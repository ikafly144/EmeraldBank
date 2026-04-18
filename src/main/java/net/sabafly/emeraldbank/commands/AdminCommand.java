package net.sabafly.emeraldbank.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.sabafly.emeraldbank.bank.User;
import org.bukkit.entity.Player;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;
import static net.sabafly.emeraldbank.EmeraldBank.database;
import static net.sabafly.emeraldbank.EmeraldBank.economy;
import static net.sabafly.emeraldbank.util.EmeraldUtils.*;

public class AdminCommand {

    public static LiteralCommandNode<CommandSourceStack> command() {
        return Commands.literal("admin")
                .requires(context -> context.getSender().hasPermission("emeraldbank.admin"))
                .then(Commands.literal("wallet")
                        .then(Commands.literal("get")
                                .then(Commands.argument("player", ArgumentTypes.player())
                                        .executes(context -> WalletCommand.printWallet(context, getTargetPlayer(context)))
                                )
                        )
                        .then(Commands.literal("add")
                                .then(Commands.argument("player", ArgumentTypes.player())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(context -> {
                                                    final Player target = getTargetPlayer(context);
                                                    final int amount = getAmount(context);
                                                    final User user = database().getUser(target.getUniqueId());
                                                    user.addWallet(amount);
                                                    database().saveUser(user);
                                                    context.getSource().getSender().sendMessage(miniMessage().deserialize(getMessages().addWallet, tagResolver("value", formatCurrency(amount)), tagResolver("player", target.name())));
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                        )
                        .then(Commands.literal("remove")
                                .then(Commands.argument("player", ArgumentTypes.player())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(context -> {
                                                    final Player target = getTargetPlayer(context);
                                                    final int amount = getAmount(context);
                                                    final User user = database().getUser(target.getUniqueId());
                                                    if (user.wallet() < amount) {
                                                        throw createCommandException(getMessages().errorWithdrawWallet, tagResolver("value", formatCurrency(amount)), tagResolver("player", target.name()));
                                                    }
                                                    user.removeWallet(amount);
                                                    database().saveUser(user);
                                                    context.getSource().getSender().sendMessage(miniMessage().deserialize(getMessages().withdrawWallet, tagResolver("value", formatCurrency(amount)), tagResolver("player", target.name())));
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                        )
                        .then(Commands.literal("set")
                                .then(Commands.argument("player", ArgumentTypes.player())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                .executes(context -> {
                                                    final Player target = getTargetPlayer(context);
                                                    final int amount = getAmount(context);
                                                    final User user = database().getUser(target.getUniqueId());
                                                    user.setWallet(amount);
                                                    database().saveUser(user);
                                                    context.getSource().getSender().sendMessage(miniMessage().deserialize(getMessages().wallet, tagResolver("player", target.name()), tagResolver("value", formatCurrency(amount))));
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                        )
                )
                .then(Commands.literal("bank")
                        .then(Commands.literal("get")
                                .then(Commands.argument("bank", new BankArgumentType())
                                        .executes(context -> {
                                            ensureBankingEnabled();
                                            return BankCommand.printBankBalance(context, getBank(context));
                                        })
                                )
                        )
                        .then(Commands.literal("deposit")
                                .then(Commands.argument("bank", new BankArgumentType())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(context -> {
                                                    ensureBankingEnabled();
                                                    final String bank = getBank(context);
                                                    final int amount = getAmount(context);
                                                    if (!economy().bankDeposit(bank, amount).transactionSuccess()) {
                                                        throw createCommandException(getMessages().errorBankingDeposit, tagResolver("value", formatCurrency(amount)), tagResolver("bank", Component.text(bank)));
                                                    }
                                                    context.getSource().getSender().sendMessage(miniMessage().deserialize(getMessages().bankingDeposit, tagResolver("value", formatCurrency(amount)), tagResolver("bank", Component.text(bank)), tagResolver("cost", formatCurrency(0))));
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                        )
                        .then(Commands.literal("withdraw")
                                .then(Commands.argument("bank", new BankArgumentType())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(context -> {
                                                    ensureBankingEnabled();
                                                    final String bank = getBank(context);
                                                    final int amount = getAmount(context);
                                                    if (!economy().bankWithdraw(bank, amount).transactionSuccess()) {
                                                        throw createCommandException(getMessages().errorBankingWithdraw, tagResolver("value", formatCurrency(amount)), tagResolver("bank", Component.text(bank)));
                                                    }
                                                    context.getSource().getSender().sendMessage(miniMessage().deserialize(getMessages().bankingWithdraw, tagResolver("value", formatCurrency(amount)), tagResolver("bank", Component.text(bank)), tagResolver("cost", formatCurrency(0))));
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                        )
                        .then(Commands.literal("set")
                                .then(Commands.argument("bank", new BankArgumentType())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                .executes(context -> {
                                                    ensureBankingEnabled();
                                                    final String bank = getBank(context);
                                                    final int amount = getAmount(context);
                                                    final double balance = economy().bankBalance(bank).balance;
                                                    if (amount > balance) {
                                                        final double delta = amount - balance;
                                                        if (!economy().bankDeposit(bank, delta).transactionSuccess()) {
                                                            throw createCommandException(getMessages().errorBankingDeposit, tagResolver("value", formatCurrency(delta)), tagResolver("bank", Component.text(bank)));
                                                        }
                                                    } else if (amount < balance) {
                                                        final double delta = balance - amount;
                                                        if (!economy().bankWithdraw(bank, delta).transactionSuccess()) {
                                                            throw createCommandException(getMessages().errorBankingWithdraw, tagResolver("value", formatCurrency(delta)), tagResolver("bank", Component.text(bank)));
                                                        }
                                                    }
                                                    return BankCommand.printBankBalance(context, bank);
                                                })
                                        )
                                )
                        )
                )
                .build();
    }

    private static Player getTargetPlayer(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        return context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
    }

    private static int getAmount(CommandContext<CommandSourceStack> context) {
        return context.getArgument("amount", Integer.class);
    }

    private static String getBank(CommandContext<CommandSourceStack> context) {
        return context.getArgument("bank", String.class);
    }

    private static void ensureBankingEnabled() throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        if (!economy().hasBankSupport()) {
            throw createCommandException(getMessages().errorBankingDisabled);
        }
    }

}
