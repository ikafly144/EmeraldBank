package net.sabafly.emeraldbank;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.sabafly.emeraldbank.configuration.EmeraldConfigurations;
import net.sabafly.emeraldbank.configuration.GlobalConfiguration;
import net.sabafly.emeraldbank.configuration.Messages;
import net.sabafly.emeraldbank.economy.EmeraldEconomy;
import net.sabafly.emeraldbank.placeholder.EmeraldBankPlaceholderExpansion;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;

import java.io.IOException;
import java.nio.file.Path;

@SuppressWarnings("UnstableApiUsage")
public final class EmeraldBank extends JavaPlugin {

    @Getter
    private Messages messages;
    @Getter
    @NotNull
    private GlobalConfiguration globalConfiguration;
    private final EmeraldConfigurations configurations;
    private final Path dataDir;
    @Getter
    private final EmeraldEconomy economy = new EmeraldEconomy();

    public EmeraldBank(Path dataDir, Messages messages, EmeraldConfigurations configurations) {
        this.dataDir = dataDir;
        this.messages = messages;
        this.configurations = configurations;
        try {
            this.globalConfiguration = this.configurations.initializeGlobalConfiguration();
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getComponentLogger().info(MiniMessage.miniMessage().deserialize("Disabled <version>", TagResolver.builder().tag("version", Tag.inserting(Component.text(getPluginMeta().getVersion()))).build()));
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        if (getServer().getPluginManager().getPlugin("OpenInv") == null) {
            getComponentLogger().warn(MiniMessage.miniMessage().deserialize( "<red>Disabled due to no OpenInv dependency found!", TagResolver.empty()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!setupEconomy()) {
            getComponentLogger().warn(MiniMessage.miniMessage().deserialize("<red>Disabled due to no Vault dependency found!", TagResolver.empty()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new EmeraldBankPlaceholderExpansion(this).register();
        }
        getComponentLogger().info(MiniMessage.miniMessage().deserialize("Enabled <version>", TagResolver.builder().tag("version", Tag.inserting(Component.text(getPluginMeta().getVersion()))).build()));
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        try {
            getServer().getServicesManager().register(net.milkbowl.vault.economy.Economy.class, economy, this, ServicePriority.High);
        } catch (Exception e) {
            getSLF4JLogger().error("Failed to register economy service", e);
            return false;
        }
        return true;
    }

    public static EmeraldBank getInstance() {
        return getPlugin(EmeraldBank.class);
    }

    public void reloadConfiguration() throws IOException {
        messages = EmeraldBootstrapper.loadMessages(this.dataDir.resolve("messages.yml"));
        this.globalConfiguration = configurations.initializeGlobalConfiguration();
    }
}
