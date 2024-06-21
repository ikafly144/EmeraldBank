package net.sabafly.emeraldbank;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.milkbowl.vault.economy.Economy;
import net.sabafly.emeraldbank.configuration.Messages;
import net.sabafly.emeraldbank.economy.EmeraldEconomy;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

public final class EmeraldBank extends JavaPlugin {

    @Getter
    private Messages messages;
    private final Path dataDir;
    @Getter
    private final EmeraldEconomy economy = new EmeraldEconomy();

    public EmeraldBank(Path dataDir, Messages messages) {
        this.dataDir = dataDir;
        this.messages = messages;
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
        getComponentLogger().info(MiniMessage.miniMessage().deserialize("Enabled <version>", TagResolver.builder().tag("version", Tag.inserting(Component.text(getPluginMeta().getVersion()))).build()));
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        getServer().getServicesManager().register(Economy.class, economy, this, ServicePriority.High);
        return true;
    }

    public static EmeraldBank getInstance() {
        return getPlugin(EmeraldBank.class);
    }

    public void reloadMessages() {
        try {
            messages = EmeraldBootstrapper.loadMessages(this.dataDir.resolve("messages.yml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
