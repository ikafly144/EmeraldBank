package net.sabafly.emeraldbank.util;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.TagPattern;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.sabafly.emeraldbank.EmeraldBank;
import net.sabafly.emeraldbank.economy.EmeraldEconomy;
import net.sabafly.emeraldbank.configuration.Messages;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class EmeraldUtils {
    public static Messages getMessages() {
        return EmeraldBank.getInstance().getMessages();
    }

    public static EmeraldEconomy getEconomy() {
        return EmeraldBank.getInstance().getEconomy();
    }

    public static boolean depositPlayer(Player account, double amount) {
        return getEconomy().depositPlayer(account, amount).transactionSuccess();
    }

    public static boolean withdrawPlayer(Player account, double amount) {
        return getEconomy().withdrawPlayer(account, amount).transactionSuccess();
    }

    public static boolean payPlayer(Player from, Player to, double amount, int cost) {
        return withdrawPlayer(from, amount) && depositPlayer(to, amount - cost);
    }

    public static Component deserializeMiniMessage(String message, TagResolver... resolvers) {
        return MiniMessage.miniMessage().deserialize(message, resolvers);
    }

    public static Message serializeBrigadierMessage(String message, TagResolver... resolvers) {
        return MessageComponentSerializer.message().serialize(deserializeMiniMessage(message, resolvers));
    }

    public static CommandSyntaxException createCommandException(String message, TagResolver... resolvers) {
        return new SimpleCommandExceptionType(serializeBrigadierMessage(message, resolvers)).create();
    }

    public static TagResolver tagResolver(@TagPattern @NotNull String name, Component value) {
        return TagResolver.builder().tag(name, Tag.inserting(value)).build();
    }

    public static TagResolver tagResolver(@TagPattern @NotNull String name, ComponentLike value) {
        return TagResolver.builder().tag(name, Tag.inserting(value)).build();
    }

    public static @NotNull World getWorld() {
        return Objects.requireNonNull(EmeraldBank.getInstance().getServer().getWorld(NamespacedKey.minecraft("overworld")));
    }
}
