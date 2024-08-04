package net.sabafly.emeraldbank.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.PaperCommandSourceStack;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.sabafly.emeraldbank.EmeraldBank;
import net.sabafly.emeraldbank.economy.EmeraldEconomy;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class BankAccountArgumentType implements CustomArgumentType<String, String> {
    @Override
    public @NotNull String parse(@NotNull StringReader reader) throws CommandSyntaxException {
        String account = reader.readUnquotedString();
        if (!getEconomy().getBanks().contains(account)) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(reader);
        }
        return account;
    }

    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(final @NotNull CommandContext<S> context, final @NotNull SuggestionsBuilder builder) {
        if (!getEconomy().hasBankSupport())
            return builder.buildFuture();
        if (!(context.getSource() instanceof PaperCommandSourceStack source))
            return builder.buildFuture();
        if (!(source.getBukkitSender() instanceof Player player)) {
            getEconomy().getBanks().stream()
                    .filter(bank -> bank.startsWith(builder.getRemaining()))
                    .toList()
                    .forEach(builder::suggest);
        } else {
            getEconomy().getBanks().stream()
                .filter(bank -> bank.startsWith(builder.getRemaining()))
                .filter(bank -> getEconomy().isBankMember(bank, player).transactionSuccess() || player.hasPermission("emeraldbank.admin"))
                    .toList()
                    .forEach(builder::suggest);
        }
        return builder.buildFuture();
    }

    private static EmeraldEconomy getEconomy() {
        return EmeraldBank.getInstance().getEconomy();
    }

    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }
}
