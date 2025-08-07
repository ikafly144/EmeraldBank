package net.sabafly.emeraldbank.commands;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.adventure.AdventureComponent;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.TriState;
import net.sabafly.emeraldbank.configuration.Settings;
import net.sabafly.emeraldbank.configuration.Settings.Currency;
import net.sabafly.emeraldbank.util.PlayerInventoryUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;
import static net.sabafly.emeraldbank.EmeraldBank.config;
import static net.sabafly.emeraldbank.EmeraldBank.setConfig;
import static net.sabafly.emeraldbank.util.EmeraldUtils.createCommandException;
import static net.sabafly.emeraldbank.util.EmeraldUtils.tagResolver;

public class ExchangeCommand {
    public static LiteralCommandNode<CommandSourceStack> command() {
        return Commands.literal("exchange")
                .requires(context -> config().exchangeEnabled)
                .then(Commands.literal("from")
                        .requires(context -> context.getSender().hasPermission("emeraldbank.currency.exchange"))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .then(Commands.argument("currency", ArgumentTypes.namespacedKey())
                                        .suggests(ExchangeCommand::currencySuggestion)
                                        .then(Commands.literal("to")
                                                .then(Commands.argument("target", ArgumentTypes.namespacedKey())
                                                        .requires(context -> context.getSender().permissionValue("emeraldbank.currency.exchange.*") != TriState.FALSE)
                                                        .suggests(ExchangeCommand::currencySuggestion)
                                                        .executes(commandContext -> {
                                                            if (!(commandContext.getSource().getExecutor() instanceof Player player))
                                                                throw net.minecraft.commands.CommandSourceStack.ERROR_NOT_PLAYER.create();
                                                            try {
                                                                final var currencyKey = commandContext.getArgument("currency", NamespacedKey.class);
                                                                final var currency = config().getCurrency(currencyKey);
                                                                final var targetKey = commandContext.getArgument("target", NamespacedKey.class);
                                                                final var target = config().getCurrency(targetKey);
                                                                if (commandContext.getSource().getSender().permissionValue("emeraldbank.currency.exchange." + targetKey.asString()) == TriState.FALSE)
                                                                    throw createCommandException(config().messages.errorNoPermission, tagResolver("currency", Component.text(target.name)));
                                                                final var amount = commandContext.getArgument("amount", Integer.class);
                                                                if (currencyKey.equals(targetKey))
                                                                    throw createCommandException(config().messages.errorSameCurrency, tagResolver("currency", Component.text(currency.name)));
                                                                return exchange(player, currency, target, amount);
                                                            } catch (IllegalStateException e) {
                                                                throw createCommandException(config().messages.errorCurrencyNotFound);
                                                            }
                                                        })
                                                )
                                        )
                                )
                        )
                )
                .then(Commands.literal("rate")
                        .then(Commands.argument("currency", ArgumentTypes.namespacedKey())
                                .suggests(ExchangeCommand::currencySuggestion)
                                .then(Commands.literal("get")
                                        .requires(context -> context.getSender().hasPermission("emeraldbank.currency.rate.get"))
                                        .executes(context -> {
                                            try {
                                                final var currencyKey = context.getArgument("currency", NamespacedKey.class);
                                                final var currency = config().getCurrency(currencyKey);
                                                context.getSource().getSender().sendMessage(miniMessage().deserialize(config().messages.rateValueOfCurrency, tagResolver("currency", Component.text(currency.name)), tagResolver("value", Component.text(currency.rate))));
                                                return (int) Math.floor(currency.rate);
                                            } catch (IllegalStateException e) {
                                                throw createCommandException(config().messages.errorCurrencyNotFound);
                                            }
                                        })
                                )
                                .then(Commands.literal("to")
                                        .requires(context -> context.getSender().hasPermission("emeraldbank.currency.rate.get"))
                                        .then(Commands.argument("target", ArgumentTypes.namespacedKey())
                                                .suggests(ExchangeCommand::currencySuggestion)
                                                .executes(context -> {
                                                    try {
                                                        final var currencyKey = context.getArgument("currency", NamespacedKey.class);
                                                        final var currency = config().getCurrency(currencyKey);
                                                        final var targetKey = context.getArgument("target", NamespacedKey.class);
                                                        final var target = config().getCurrency(targetKey);
                                                        context.getSource().getSender().sendMessage(miniMessage().deserialize(config().messages.exchangeRate, tagResolver("currency", Component.text(currency.name)), tagResolver("target", Component.text(target.name)), tagResolver("value", Component.text(currency.rate / target.rate))));
                                                        return (int) Math.floor(currency.rate / target.rate);
                                                    } catch (IllegalStateException e) {
                                                        throw createCommandException(config().messages.errorCurrencyNotFound);
                                                    }
                                                })
                                        )
                                )
                                .then(Commands.literal("set")
                                        .requires(context -> context.getSender().hasPermission("emeraldbank.currency.rate.set"))
                                        .then(Commands.argument("rate", DoubleArgumentType.doubleArg(1))
                                                .executes(context -> {
                                                    final Settings config = config();
                                                    try {
                                                        final var currencyKey = context.getArgument("currency", NamespacedKey.class);
                                                        final var currency = config.getCurrency(currencyKey);
                                                        final var rate = context.getArgument("rate", Double.class);
                                                        currency.rate = rate;
                                                        setConfig(config);
                                                        context.getSource().getSender().sendMessage(miniMessage().deserialize(config.messages.setRate, tagResolver("currency", Component.text(currency.name)), tagResolver("value", Component.text(rate))));
                                                        return (int) Math.floor(rate);
                                                    } catch (IllegalStateException e) {
                                                        throw createCommandException(config.messages.errorCurrencyNotFound);
                                                    }
                                                })
                                        )
                                )
                        )
                )
                .build();
    }

    private static int exchange(@NotNull Player player, @NotNull Currency currency, @NotNull Currency target, int amount) throws CommandSyntaxException {
        player.sendMessage(miniMessage().deserialize(config().messages.exchangeStart, tagResolver("currency", Component.text(currency.name)), tagResolver("target", Component.text(target.name)), tagResolver("value", Component.text(amount))));
        final double rate = currency.rate / target.rate;
        final int receive = (int) Math.floor(amount * rate);
        player.sendMessage(miniMessage().deserialize(config().messages.exchangeReceive, tagResolver("currency", Component.text(target.name)), tagResolver("value", Component.text(receive))));
        final int costAmount = (int) Math.ceil(receive * target.cost.or(0));

        var currencyAmount = PlayerInventoryUtils.getCurrencyCount(player, currency);
        var realAmount = (int) Math.ceil(receive / rate);
        if (target.cost.enabled()) {
            player.sendMessage(miniMessage().deserialize(config().messages.exchangeCost, tagResolver("currency", Component.text(currency.name)), tagResolver("value", Component.text(realAmount))));
        }
        player.sendMessage(miniMessage().deserialize(config().messages.exchangeRate, tagResolver("currency", Component.text(currency.name)), tagResolver("target", Component.text(target.name)), tagResolver("value", Component.text(rate))));

        if (realAmount < 1)
            throw createCommandException(config().messages.errorExchangeTooLow, tagResolver("value", Component.text(realAmount)), tagResolver("currency", Component.text(currency.name)));
        if (currencyAmount < realAmount + costAmount)
            throw createCommandException(config().messages.errorNotEnoughCurrency, tagResolver("value", Component.text(realAmount + costAmount)), tagResolver("currency", Component.text(currency.name)));
        Preconditions.checkState(PlayerInventoryUtils.removeCurrency(player, currency, realAmount + costAmount));
        PlayerInventoryUtils.addCurrencyItem(player, target, receive);
        return realAmount;
    }

    private static CompletableFuture<Suggestions> currencySuggestion(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        config().currencies.entrySet().stream()
                .filter(entry -> {
                    try {
                        final var currency = context.getArgument("currency", NamespacedKey.class);
                        return !entry.getKey().equals(currency);
                    } catch (IllegalArgumentException ignored) {
                        return true;
                    }
                })
                .forEach((entry) -> builder.suggest(entry.getKey().asMinimalString(), new AdventureComponent(
                        miniMessage()
                                .deserialize(entry.getValue().name)
                                .appendSpace()
                                .append(miniMessage().deserialize(config().messages.rateValue, tagResolver("value", Component.text(entry.getValue().rate))))
                )));
        return builder.buildFuture();
    }

}
