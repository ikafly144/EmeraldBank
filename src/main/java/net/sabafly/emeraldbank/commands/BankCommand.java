package net.sabafly.emeraldbank.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;
import static net.sabafly.emeraldbank.EmeraldBank.*;
import static net.sabafly.emeraldbank.util.EmeraldUtils.*;

public class BankCommand {
    static LiteralCommandNode<CommandSourceStack> command() {
        return
                Commands.literal("bank")
                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking") && economy().hasBankSupport())
                        .then(
                                Commands.literal("account")
                                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking.account"))
                                        .then(
                                                Commands.literal("create")
                                                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking.account.create"))
                                                        .then(
                                                                Commands.argument("account", StringArgumentType.word())
                                                                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking.account.create") && context.getSender() instanceof Player)
                                                                        .executes(context -> {
                                                                            final String account = context.getArgument("account", String.class);
                                                                            if (!(context.getSource().getExecutor() instanceof Player player))
                                                                                throw net.minecraft.commands.CommandSourceStack.ERROR_NOT_PLAYER.create();
                                                                            if (!economy().hasBankSupport())
                                                                                throw createCommandException(getMessages().errorBankingDisabled);
                                                                            if (economy().getBanks().contains(account))
                                                                                throw createCommandException(getMessages().errorBankingExists, tagResolver("bank", Component.text(account)));
                                                                            final int cost = config().banking.tax.createCost.or(0);
                                                                            if (cost > 0) {
                                                                                final EconomyResponse response = economy().withdrawPlayer(player, cost);
                                                                                if (!response.transactionSuccess()) {
                                                                                    throw createCommandException(getMessages().errorBankingCreateCost, tagResolver("bank", Component.text(account)), tagResolver("value", formatCurrency(cost)));
                                                                                }
                                                                            }
                                                                            final EconomyResponse response = economy().createBank(account, player);
                                                                            if (!response.transactionSuccess()) {
                                                                                throw createCommandException(getMessages().errorBankingCreate, tagResolver("bank", Component.text(account)));
                                                                            }
                                                                            player.sendMessage(miniMessage().deserialize(getMessages().bankingCreate, tagResolver("bank", Component.text(account)), tagResolver("cost", formatCurrency(cost))));
                                                                            return Command.SINGLE_SUCCESS;
                                                                        })
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .then(
                                                Commands.literal("delete")
                                                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking.account.delete"))
                                                        .then(
                                                                Commands.argument("account", new BankAccountArgumentType())
                                                                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking.account.delete"))
                                                                        .executes(context -> {
                                                                            final isBankOwner result = getIsBankOwner(context);
                                                                            final var balance = economy().bankBalance(result.account).balance;
                                                                            if (balance > 0)
                                                                                throw createCommandException(getMessages().errorBankingDeleteRemaining, tagResolver("bank", Component.text(result.account)), tagResolver("value", formatCurrency(balance)));
                                                                            final EconomyResponse response = economy().deleteBank(result.account);
                                                                            if (!response.transactionSuccess()) {
                                                                                throw createCommandException(getMessages().errorBankingDelete, tagResolver("bank", Component.text(result.account)));
                                                                            }
                                                                            context.getSource().getSender().sendMessage(miniMessage().deserialize(getMessages().bankingDelete, tagResolver("bank", Component.text(result.account))));
                                                                            return Command.SINGLE_SUCCESS;
                                                                        })
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .then(
                                                Commands.literal("add")
                                                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking.account.add"))
                                                        .then(
                                                                Commands.argument("account", new BankAccountArgumentType())
                                                                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking.account.add"))
                                                                        .then(
                                                                                Commands.argument("target", ArgumentTypes.player())
                                                                                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking.account.add"))
                                                                                        .executes(context -> {
                                                                                            final isBankOwner result = getIsBankOwner(context);
                                                                                            final Player target = context.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
                                                                                            if (economy().isBankMember(result.account(), target).transactionSuccess())
                                                                                                throw createCommandException(getMessages().errorBankingMemberExists, tagResolver("player", target.name()), tagResolver("bank", Component.text(result.account())));
                                                                                            int cost = config().banking.tax.addMemberCost.or(0);
                                                                                            if (context.getSource().getSender().hasPermission("emeraldbank.admin"))
                                                                                                cost = 0;
                                                                                            if (cost > 0 && context.getSource().getExecutor() instanceof Player player) {
                                                                                                final EconomyResponse response = economy().withdrawPlayer(player, cost);
                                                                                                if (!response.transactionSuccess()) {
                                                                                                    throw createCommandException(getMessages().errorBankingAddMemberCost, tagResolver("player", target.name()), tagResolver("bank", Component.text(result.account())), tagResolver("cost", formatCurrency(cost)));
                                                                                                }
                                                                                            }
                                                                                            if (!(context.getSource().getExecutor() instanceof Player) && context.getSource().getSender().hasPermission("emeraldbank.admin"))
                                                                                                throw net.minecraft.commands.CommandSourceStack.ERROR_NOT_PLAYER.create();
                                                                                            database().addMember(result.account(), target.getUniqueId());
                                                                                            context.getSource().getSender().sendMessage(miniMessage().deserialize(getMessages().bankingAddMember, tagResolver("player", target.name()), tagResolver("bank", Component.text(result.account())), tagResolver("cost", formatCurrency(cost))));
                                                                                            return Command.SINGLE_SUCCESS;
                                                                                        })
                                                                                        .build()
                                                                        )
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .then(
                                                Commands.literal("remove")
                                                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking.account.remove"))
                                                        .then(
                                                                Commands.argument("account", new BankAccountArgumentType())
                                                                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking.account.remove"))
                                                                        .then(
                                                                                Commands.argument("player", ArgumentTypes.player())
                                                                                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking.account.remove"))
                                                                                        .executes(context -> {
                                                                                            final isBankOwner result = getIsBankOwner(context);
                                                                                            final Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
                                                                                            if (economy().isBankOwner(result.account, target).transactionSuccess())
                                                                                                throw createCommandException(getMessages().errorBankingRemoveOwner, tagResolver("player", target.name()), tagResolver("bank", Component.text(result.account)));
                                                                                            if (!economy().isBankMember(result.account, result.account).transactionSuccess())
                                                                                                throw createCommandException(getMessages().errorBankingNotMember, tagResolver("player", target.name()), tagResolver("bank", Component.text(result.account)));
                                                                                            if (database().getMembers(result.account).size() == 1)
                                                                                                throw createCommandException(getMessages().errorBankingRemoveLastMember, tagResolver("player", target.name()), tagResolver("bank", Component.text(result.account)));
                                                                                            database().removeMember(result.account, target.getUniqueId());
                                                                                            context.getSource().getSender().sendMessage(miniMessage().deserialize(getMessages().bankingRemoveMember, tagResolver("player", target.name()), tagResolver("bank", Component.text(result.account))));
                                                                                            return Command.SINGLE_SUCCESS;
                                                                                        })
                                                                                        .build()
                                                                        )
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .then(
                                                Commands.literal("list")
                                                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking.account.list"))
                                                        .executes(context -> {
                                                            final List<String> banks = economy().getBanks();
                                                            context.getSource().getSender().sendMessage(miniMessage().deserialize(getMessages().bankingList, tagResolver("banks", Component.text(String.join(", ", banks)))));
                                                            return Command.SINGLE_SUCCESS;
                                                        })
                                                        .then(
                                                                Commands.argument("account", new BankAccountArgumentType())
                                                                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking.account.list"))
                                                                        .executes(context -> {
                                                                            final String account = context.getArgument("account", String.class);
                                                                            final var members = database().getMembers(account);
                                                                            context.getSource().getSender().sendMessage(miniMessage().deserialize(getMessages().bankingMembers, tagResolver("bank", Component.text(account)), tagResolver("members", Component.join(JoinConfiguration.separators(Component.text(", "), Component.empty()), members.stream().map(m -> {
                                                                                final boolean isOwner = economy().isBankOwner(account, Bukkit.getOfflinePlayer(m.getUuid())).transactionSuccess();
                                                                                return Component.empty()
                                                                                        .color(isOwner ? NamedTextColor.GREEN : NamedTextColor.WHITE)
                                                                                        .append(
                                                                                                Component.text(m.getName())
                                                                                                        .hoverEvent(HoverEvent.showText(Component.text(isOwner ? "Owner" : "Member")))
                                                                                        );
                                                                            }).toList()))));
                                                                            return Command.SINGLE_SUCCESS;
                                                                        })
                                                                        .build()
                                                        )
                                        )
                                        .then(
                                                Commands.literal("addowner")
                                                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking.account.transfer"))
                                                        .then(
                                                                Commands.argument("account", new BankAccountArgumentType())
                                                                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking.account.transfer"))
                                                                        .then(
                                                                                Commands.argument("target", ArgumentTypes.player())
                                                                                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking.account.transfer"))
                                                                                        .executes(context -> {
                                                                                            final isBankOwner result = getIsBankOwner(context);
                                                                                            final Player target = context.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
                                                                                            int cost = config().banking.tax.transferBankCost.or(0);
                                                                                            if (context.getSource().getSender().hasPermission("emeraldbank.bypass.cost"))
                                                                                                cost = 0;
                                                                                            if (cost > 0 && context.getSource().getExecutor() instanceof Player player) {
                                                                                                final EconomyResponse response = economy().withdrawPlayer(player, cost);
                                                                                                if (!response.transactionSuccess()) {
                                                                                                    throw createCommandException(getMessages().errorBankingTransferCost, tagResolver("player", target.name()), tagResolver("bank", Component.text(result.account)), tagResolver("cost", formatCurrency(cost)));
                                                                                                }
                                                                                            }
                                                                                            database().addOwner(result.account, target.getUniqueId());
                                                                                            context.getSource().getSender().sendMessage(miniMessage().deserialize(getMessages().bankingAddOwner, tagResolver("player", target.name()), tagResolver("bank", Component.text(result.account)), tagResolver("cost", formatCurrency(cost))));
                                                                                            return Command.SINGLE_SUCCESS;
                                                                                        })
                                                                                        .build()
                                                                        )
                                                        )
                                                        .build()
                                        )
                                        .then(Commands.literal("removeowner")
                                                .requires(context -> context.getSender().hasPermission("emeraldbank.banking.account.transfer"))
                                                .then(
                                                        Commands.argument("account", new BankAccountArgumentType())
                                                                .requires(context -> context.getSender().hasPermission("emeraldbank.banking.account.transfer"))
                                                                .then(
                                                                        Commands.argument("player", ArgumentTypes.player())
                                                                                .requires(context -> context.getSender().hasPermission("emeraldbank.banking.account.transfer"))
                                                                                .executes(context -> {
                                                                                    final isBankOwner result = getIsBankOwner(context);
                                                                                    final Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
                                                                                    if (!economy().isBankOwner(result.account(), target).transactionSuccess())
                                                                                        throw createCommandException(getMessages().errorBankingRemoveOwner, tagResolver("player", target.name()), tagResolver("bank", Component.text(result.account())));
                                                                                    if (!economy().isBankMember(result.account(), target).transactionSuccess())
                                                                                        throw createCommandException(getMessages().errorBankingNotMember, tagResolver("player", target.name()), tagResolver("bank", Component.text(result.account())));
                                                                                    if (database().getOwners(result.account()).size() == 1)
                                                                                        throw createCommandException(getMessages().errorBankingRemoveLastOwner, tagResolver("player", target.name()), tagResolver("bank", Component.text(result.account)));
                                                                                    database().removeOwner(result.account(), target.getUniqueId());
                                                                                    context.getSource().getSender().sendMessage(miniMessage().deserialize(getMessages().bankingRemoveOwner, tagResolver("player", target.name()), tagResolver("bank", Component.text(result.account))));
                                                                                    return Command.SINGLE_SUCCESS;
                                                                                })
                                                                                .build()
                                                                )
                                                                .build()
                                                )
                                        .build())
                        )
                        .then(
                                Commands.literal("balance")
                                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking.balance"))
                                        .then(
                                                Commands.argument("account", new BankAccountArgumentType())
                                                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking.balance"))
                                                        .executes(context -> {
                                                            isBankMember result = getIsBankMember(context);
                                                            final double balance = economy().bankBalance(result.account).balance;
                                                            context.getSource().getSender().sendMessage(miniMessage().deserialize(getMessages().balanceBank, tagResolver("bank", Component.text(result.account)), tagResolver("value", formatCurrency(balance))));
                                                            return (int) balance;
                                                        })
                                                        .build()
                                        )
                                        .build()
                        )
                        .then(
                                Commands.literal("deposit")
                                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking.deposit"))
                                        .then(
                                                Commands.argument("account", new BankAccountArgumentType())
                                                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking.deposit"))
                                                        .then(
                                                                Commands.argument("amount", IntegerArgumentType.integer(1))
                                                                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking.deposit"))
                                                                        .executes(context -> {
                                                                            final isBankMember result = getIsBankMember(context);
                                                                            final int amount = context.getArgument("amount", Integer.class);
                                                                            return bankDeposit(context, result, amount);
                                                                        })
                                                                        .build()
                                                        )
                                                        .then(
                                                                Commands.literal("all")
                                                                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking.deposit") && context.getExecutor() instanceof Player)
                                                                        .executes(context -> {
                                                                            final isBankMember result = getIsBankMember(context);
                                                                            if (!(context.getSource().getExecutor() instanceof Player player))
                                                                                throw net.minecraft.commands.CommandSourceStack.ERROR_NOT_PLAYER.create();
                                                                            final int amount = (int) economy().getBalance(player);
                                                                            return bankDeposit(context, result, amount);
                                                                        })
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .build()
                        )
                        .then(
                                Commands.literal("withdraw")
                                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking.withdraw"))
                                        .then(
                                                Commands.argument("account", new BankAccountArgumentType())
                                                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking.withdraw"))
                                                        .then(
                                                                Commands.argument("amount", IntegerArgumentType.integer(1))
                                                                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking.withdraw"))
                                                                        .executes(context -> {
                                                                            final isBankMember result = getIsBankMember(context);
                                                                            final int amount = context.getArgument("amount", Integer.class);
                                                                            int cost = config().banking.tax.withdrawCost.or(0);
                                                                            if (context.getSource().getSender().hasPermission("emeraldbank.bypass.cost"))
                                                                                cost = 0;
                                                                            if (amount <= cost)
                                                                                throw createCommandException(getMessages().errorBankingWithdrawCost, tagResolver("value", formatCurrency(amount)), tagResolver("bank", Component.text(result.account)), tagResolver("cost", formatCurrency(cost)));
                                                                            var response = economy().bankWithdraw(result.account(), amount);
                                                                            if (!response.transactionSuccess()) {
                                                                                throw createCommandException(getMessages().errorBankingWithdraw, tagResolver("value", formatCurrency(amount)), tagResolver("bank", Component.text(result.account)));
                                                                            }
                                                                            if ((context.getSource().getExecutor() instanceof Player player) && !economy().depositPlayer(player, response.amount - cost).transactionSuccess()) {
                                                                                throw createCommandException(getMessages().errorBankingWithdraw, tagResolver("value", formatCurrency(amount)), tagResolver("bank", Component.text(result.account)));
                                                                            }
                                                                            context.getSource().getSender().sendMessage(miniMessage().deserialize(getMessages().bankingWithdraw, tagResolver("value", formatCurrency(response.amount)), tagResolver("bank", Component.text(result.account)), tagResolver("cost", formatCurrency(cost))));
                                                                            return amount;
                                                                        })
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .build()
                        )
                        .then(
                                Commands.literal("send")
                                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking.send"))
                                        .then(
                                                Commands.argument("account", new BankAccountArgumentType())
                                                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking.send"))
                                                        .then(
                                                                Commands.argument("target", new BankAccountArgumentType())
                                                                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking.send"))
                                                                        .then(
                                                                                Commands.argument("amount", IntegerArgumentType.integer(1))
                                                                                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking.send"))
                                                                                        .executes(context -> {
                                                                                            final isBankMember result = getIsBankMember(context);
                                                                                            final int amount = context.getArgument("amount", Integer.class);
                                                                                            final String target = context.getArgument("target", String.class);
                                                                                            return bankSend(context, result, amount, target);
                                                                                        })
                                                                                        .build()
                                                                        )
                                                                        .then(
                                                                                Commands.literal("all")
                                                                                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking.send"))
                                                                                        .executes(context -> {
                                                                                            final isBankMember result = getIsBankMember(context);
                                                                                            final int amount = (int) economy().bankBalance(result.account).balance;
                                                                                            final String target = context.getArgument("target", String.class);
                                                                                            return bankSend(context, result, amount, target);
                                                                                        })
                                                                                        .build()
                                                                        )
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .build()
                        )
                        .then(
                                Commands.literal("pay")
                                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking.pay"))
                                        .then(
                                                Commands.argument("account", new BankAccountArgumentType())
                                                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking.pay"))
                                                        .then(
                                                                Commands.argument("target", ArgumentTypes.player())
                                                                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking.pay"))
                                                                        .then(
                                                                                Commands.argument("amount", IntegerArgumentType.integer(1))
                                                                                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking.pay"))
                                                                                        .executes(context -> {
                                                                                            final isBankMember result = getIsBankMember(context);
                                                                                            final int amount = context.getArgument("amount", Integer.class);
                                                                                            final Player target = context.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
                                                                                            return payBank(context, amount, result.account, target);
                                                                                        })
                                                                                        .build()
                                                                        )
                                                                        .then(
                                                                                Commands.literal("all")
                                                                                        .requires(context -> context.getSender().hasPermission("emeraldbank.banking.pay"))
                                                                                        .executes(context -> {
                                                                                            final isBankMember result = getIsBankMember(context);
                                                                                            final int amount = (int) economy().bankBalance(result.account).balance;
                                                                                            final Player target = context.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
                                                                                            return payBank(context, amount, result.account, target);
                                                                                        })
                                                                                        .build()
                                                                        )
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .build()
                        )
                        .build();
    }



    private static int bankSend(CommandContext<CommandSourceStack> context, isBankMember result, int amount, String target) throws CommandSyntaxException {
        var response = economy().bankWithdraw(result.account, amount);
        if (!response.transactionSuccess()) {
            throw createCommandException(getMessages().errorBankingSend, tagResolver("value", formatCurrency(amount)), tagResolver("bank_from", Component.text(result.account)), tagResolver("bank_to", Component.text(target)));
        }
        response = economy().bankDeposit(target, response.amount);
        if (!response.transactionSuccess()) {
            throw createCommandException(getMessages().errorBankingSend, tagResolver("value", formatCurrency(amount)), tagResolver("bank_from", Component.text(result.account)), tagResolver("bank_to", Component.text(target)));
        }
        context.getSource().getSender().sendMessage(miniMessage().deserialize(getMessages().bankingSend, tagResolver("value", formatCurrency(amount)), tagResolver("bank_from", Component.text(result.account)), tagResolver("bank_to", Component.text(target))));
        database().getMembers(target).forEach(member -> Optional.ofNullable(Bukkit.getServer().getPlayer(member.getUuid())).ifPresent(p -> sendReceivedMessage(p, amount, target, result.account)));
        return amount;
    }

    private static int bankDeposit(CommandContext<CommandSourceStack> context, isBankMember result, int amount) throws CommandSyntaxException {
        int cost = config().banking.tax.depositCost.or(0);
        if (context.getSource().getSender().hasPermission("emeraldbank.bypass.cost"))
            cost = 0;
        if (amount <= cost) {
            throw createCommandException(getMessages().errorBankingDepositCost, tagResolver("value", formatCurrency(amount)), tagResolver("bank", Component.text(result.account)), tagResolver("cost", formatCurrency(cost)));
        }
        if (!context.getSource().getSender().hasPermission("emeraldbank.bypass.deposit")
            && (context.getSource().getExecutor() instanceof Player player)
            && !economy().withdrawPlayer(player, amount).transactionSuccess()) {
            throw createCommandException(getMessages().errorBankingDeposit, tagResolver("value", formatCurrency(amount)), tagResolver("bank", Component.text(result.account)));
        }
        if (!economy().bankDeposit(result.account(), amount - cost).transactionSuccess()) {
            throw createCommandException(getMessages().errorBankingDeposit, tagResolver("value", formatCurrency(amount)), tagResolver("bank", Component.text(result.account)));
        }
        context.getSource().getSender().sendMessage(miniMessage().deserialize(getMessages().bankingDeposit, tagResolver("value", formatCurrency(amount)), tagResolver("bank", Component.text(result.account)), tagResolver("cost", formatCurrency(cost))));
        return amount;
    }

    static int payBank(CommandContext<CommandSourceStack> context, int amount, String bankFrom, Player to) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        int cost = config().banking.tax.payCost.or(config().banking.tax.withdrawCost.or(0));
        if (context.getSource().getSender().hasPermission("emeraldbank.bypass.cost"))
            cost = 0;
        if (amount <= cost)
            throw createCommandException(getMessages().errorBankingPayCost, tagResolver("player", to.name()), tagResolver("value", formatCurrency(amount)), tagResolver("bank", Component.text(bankFrom)), tagResolver("cost", formatCurrency(cost)));
        if (!economy().hasBankSupport())
            throw createCommandException(getMessages().errorBankingDisabled);
        EconomyResponse response = economy().bankWithdraw(bankFrom, amount);
        if (!response.transactionSuccess())
            throw createCommandException(getMessages().errorBankingPay, tagResolver("value", formatCurrency(amount)), tagResolver("bank", Component.text(bankFrom)), tagResolver("player", to.name()));
        response = economy().depositPlayer(to, response.amount - cost);
        if (!response.transactionSuccess())
            throw createCommandException(getMessages().errorBankingPay, tagResolver("value", formatCurrency(amount)), tagResolver("bank", Component.text(bankFrom)), tagResolver("player", to.name()));
        context.getSource().getSender().sendMessage(miniMessage().deserialize(getMessages().bankingPay, tagResolver("value", formatCurrency(amount)), tagResolver("bank", Component.text(bankFrom)), tagResolver("player", to.name()), tagResolver("cost", formatCurrency(cost))));
        var src = context.getSource();
        PayCommand.sendReceivedMessage(to, amount, src != null ? src.getExecutor() != null ? src.getExecutor().name() : Component.text("SERVER") : Component.text("SERVER"));
        return amount;
    }

    private static @NotNull isBankOwner getIsBankOwner(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final String account = context.getArgument("account", String.class);
        if (!economy().hasBankSupport())
            throw createCommandException(getMessages().errorBankingDisabled);
        if (!economy().getBanks().contains(account))
            throw createCommandException(getMessages().errorBankingNoBank, tagResolver("bank", Component.text(account)));
        if (context.getSource().getSender() instanceof Player player && !(context.getSource().getSender().hasPermission("emeraldbank.bypass.owner")) && !economy().isBankOwner(account, player).transactionSuccess()) {
            throw createCommandException(getMessages().errorBankingNotOwner, tagResolver("bank", Component.text(account)), tagResolver("player", player.name()));
        }
        return new isBankOwner(account);
    }

    private record isBankOwner(String account) {
    }

    private static @NotNull isBankMember getIsBankMember(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final String account = context.getArgument("account", String.class);
        if (!economy().hasBankSupport())
            throw createCommandException(getMessages().errorBankingDisabled);
        if (context.getSource().getSender() instanceof Player player && !(context.getSource().getSender().hasPermission("emeraldbank.bypass.member")) && !economy().isBankMember(account, player).transactionSuccess()) {
            throw createCommandException(getMessages().errorBankingNotMember, tagResolver("bank", Component.text(account)), tagResolver("player", player.name()));
        }
        return new isBankMember(account);
    }

    private record isBankMember(String account) {
    }

    static void sendReceivedMessage(Player player, int amount,String destBank , String srcBank) {
        player.sendMessage(miniMessage().deserialize(getMessages().receiveBank, tagResolver("value", formatCurrency(amount)), tagResolver("source", Component.text(srcBank)), tagResolver("destination", Component.text(destBank))));
    }

}
