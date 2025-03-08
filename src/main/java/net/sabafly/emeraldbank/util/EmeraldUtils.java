package net.sabafly.emeraldbank.util;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.TagPattern;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.sabafly.emeraldbank.EmeraldBank;
import net.sabafly.emeraldbank.bank.Economy;
import net.sabafly.emeraldbank.configuration.Settings;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public class EmeraldUtils {
    public static Settings.Messages getMessages() {
        return EmeraldBank.getInstance().getSettings().messages;
    }

    public static Economy getEconomy() {
        return EmeraldBank.getInstance().getEconomy();
    }

    public static boolean depositPlayer(OfflinePlayer account, double amount) {
        return getEconomy().depositPlayer(account, amount).transactionSuccess();
    }

    public static boolean withdrawPlayer(Player account, double amount) {
        return getEconomy().withdrawPlayer(account, amount).transactionSuccess();
    }

    public static boolean payPlayer(Player from, OfflinePlayer to, double amount, int cost) {
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

    @Deprecated
    public static @NotNull World getWorld() {
        return Objects.requireNonNull(EmeraldBank.getInstance().getServer().getWorld(NamespacedKey.minecraft("overworld")));
    }

    public static @NotNull TextComponent formatCurrency(double balance) {
        return Component.text(EmeraldBank.getInstance().getEconomy().format(balance));
    }
}
