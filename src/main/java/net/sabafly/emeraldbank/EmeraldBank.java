package net.sabafly.emeraldbank;

import com.google.gson.Gson;
import io.papermc.paper.ServerBuildInfo;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.sabafly.emeraldbank.commands.EmeraldCommands;
import net.sabafly.emeraldbank.configuration.Config;
import net.sabafly.emeraldbank.configuration.ConfigurationLoader;
import net.sabafly.emeraldbank.economy.EmeraldEconomy;
import net.sabafly.emeraldbank.placeholder.EmeraldBankPlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;

@SuppressWarnings("UnstableApiUsage")
public final class EmeraldBank extends JavaPlugin {

    @Getter
    private static Path dataDir;
    @Getter
    private final EmeraldEconomy economy = new EmeraldEconomy();

    @Getter
    private Config configuration;

    public EmeraldBank(@NotNull Path dataDir) {
        EmeraldBank.dataDir = dataDir;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getComponentLogger().info(MiniMessage.miniMessage().deserialize("Disabled <version>", TagResolver.builder().tag("version", Tag.inserting(Component.text(getPluginMeta().getVersion()))).build()));
    }

    @Override
    public void onEnable() {
        loadConfiguration();

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

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, new EmeraldCommands());
        getSLF4JLogger().info("Commands registered");

        getComponentLogger().info(MiniMessage.miniMessage().deserialize("Enabled <version>", TagResolver.builder().tag("version", Tag.inserting(Component.text(getPluginMeta().getVersion()))).build()));
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, task -> updateCheck(), 1, 60 * 60 * 20);
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

    public void loadConfiguration() {
        try {
            this.configuration = ConfigurationLoader.loadConfig(dataDir.resolve("config.yml"));
        } catch (ConfigurateException e) {
            getSLF4JLogger().error("Failed to load configuration", e);
            if (configuration == null) {
                this.configuration = new Config();
                getSLF4JLogger().warn("Using default configuration");
            }
        }
    }

    private void updateCheck() {
        getSLF4JLogger().info("Checking for updates");
        try (var client = HttpClient.newHttpClient()) {
            var param = URLEncoder.encode("loaders=[\"paper\"]&game_versions=[\"" + ServerBuildInfo.buildInfo().minecraftVersionId() + "\"]", StandardCharsets.UTF_8);
            var uri = URI.create("https://api.modrinth.com/v2/project/fPQBnIe2/version?" + param);
            var request = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(10))
                    .build();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApplyAsync(HttpResponse::body).thenAcceptAsync(buf -> {
                        var raw = new Gson().fromJson(buf, Object.class);
                        // .[0].version_number
                        var version = ((java.util.List<?>) raw).getFirst();
                        var versionNumber = ((java.util.Map<?, ?>) version).get("version_number");
                        if (getPluginMeta().getVersion().equals(versionNumber)) {
                            getSLF4JLogger().info("You are running the latest version");
                        } else {
                            getSLF4JLogger().info("A new version is available");
                        }
                        getSLF4JLogger().info("Latest version: {}", versionNumber);
                        getSLF4JLogger().info("Current version: {}", getPluginMeta().getVersion());
                    }).join();
        } catch (Exception e) {
            getSLF4JLogger().error("Failed to check for updates: {}", e.getLocalizedMessage());
        }
    }

}
