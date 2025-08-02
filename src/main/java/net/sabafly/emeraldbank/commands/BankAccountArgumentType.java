package net.sabafly.emeraldbank.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static net.sabafly.emeraldbank.EmeraldBank.economy;

public class BankAccountArgumentType implements CustomArgumentType<String, String> {
    @Override
    public @NotNull String parse(@NotNull StringReader reader) throws CommandSyntaxException {
        String account = reader.readUnquotedString();
        if (!economy().getBanks().contains(account)) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(reader);
        }
        return account;
    }

    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(final @NotNull CommandContext<S> context, final @NotNull SuggestionsBuilder builder) {
        if (!economy().hasBankSupport())
            return builder.buildFuture();
        if (!(context.getSource() instanceof CommandSourceStack source))
            return builder.buildFuture();
        if (!(source.getSender() instanceof Player player)) {
            economy().getBanks().stream()
                    .filter(bank -> bank.startsWith(builder.getRemaining()))
                    .toList()
                    .forEach(builder::suggest);
        } else {
            economy().getBanks().stream()
                .filter(bank -> bank.startsWith(builder.getRemaining()))
                    .filter(bank -> economy().isBankMember(bank, player).transactionSuccess() || player.hasPermission("emeraldbank.admin"))
                    .toList()
                    .forEach(builder::suggest);
        }
        return builder.buildFuture();
    }

    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }
}
