package net.sabafly.emeraldbank.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static net.sabafly.emeraldbank.EmeraldBank.economy;

public class BankArgumentType implements CustomArgumentType<String, String> {
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
        if (!economy().hasBankSupport()) {
            return builder.buildFuture();
        }
        economy().getBanks().stream()
                .filter(bank -> bank.startsWith(builder.getRemaining()))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }
}
