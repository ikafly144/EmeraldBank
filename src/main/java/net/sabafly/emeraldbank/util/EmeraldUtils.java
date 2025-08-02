package net.sabafly.emeraldbank.util;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.TagPattern;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.sabafly.emeraldbank.EmeraldBank;
import net.sabafly.emeraldbank.configuration.Settings;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;
import static net.sabafly.emeraldbank.EmeraldBank.config;
import static net.sabafly.emeraldbank.EmeraldBank.economy;

public class EmeraldUtils {
    public static Settings.Messages getMessages() {
        return config().messages;
    }

    public static boolean depositPlayer(OfflinePlayer account, double amount) {
        return economy().depositPlayer(account, amount).transactionSuccess();
    }

    public static boolean withdrawPlayer(Player account, double amount) {
        return economy().withdrawPlayer(account, amount).transactionSuccess();
    }

    public static boolean payPlayer(Player from, OfflinePlayer to, double amount, int cost) {
        return withdrawPlayer(from, amount) && depositPlayer(to, amount - cost);
    }

    public static Message serializeBrigadierMessage(String message, TagResolver... resolvers) {
        return MessageComponentSerializer.message().serialize(miniMessage().deserialize(message, resolvers));
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

    @Deprecated
    public static @NotNull World getWorld() {
        return Objects.requireNonNull(EmeraldBank.getInstance().getServer().getWorld(NamespacedKey.minecraft("overworld")));
    }

    public static @NotNull TextComponent formatCurrency(double balance) {
        return Component.text(economy().format(balance));
    }
}
