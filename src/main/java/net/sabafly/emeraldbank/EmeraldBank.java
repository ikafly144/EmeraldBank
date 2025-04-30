package net.sabafly.emeraldbank;

import com.google.gson.Gson;
import io.papermc.paper.ServerBuildInfo;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.sabafly.emeraldbank.bank.Economy;
import net.sabafly.emeraldbank.bank.User;
import net.sabafly.emeraldbank.commands.EmeraldCommands;
import net.sabafly.emeraldbank.configuration.ConfigurationLoader;
import net.sabafly.emeraldbank.configuration.Settings;
import net.sabafly.emeraldbank.database.Database;
import net.sabafly.emeraldbank.external.EssentialsAccess;
import net.sabafly.emeraldbank.external.OpenInvAccess;
import net.sabafly.emeraldbank.placeholder.EmeraldBankPlaceholderExpansion;
import net.sabafly.emeraldbank.util.LogUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
public final class EmeraldBank extends JavaPlugin implements Listener {

    @Getter
    private static Path dataDir;
    private final Economy economy = new Economy();
    private Database database;
    @Nullable
    private EssentialsAccess essentialsAccess = null;

    private Settings settings;

    public EmeraldBank(@NotNull Path dataDir) {
        EmeraldBank.dataDir = dataDir;
    }

    public static Settings config() {
        return getInstance().settings;
    }

    public static void setConfig(Settings settings) {
        EmeraldBank.getInstance().settings = settings;
        try {
            ConfigurationLoader.saveConfig(dataDir.resolve("config.yml"), settings);
        } catch (ConfigurateException e) {
            LogUtils.getLogger().error("Could not save config", e);
        }
    }

    public static Database database() {
        return getInstance().database;
    }

    public static Economy economy() {
        return getInstance().economy;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        database().getUser(player.getUniqueId());

        Bukkit.getScheduler().runTaskLater(this, () -> {
            User user = database().getUser(player.getUniqueId());
            user.notifyOfflineTransaction();
            if (user.player().isPresent())
                database().saveUser(user);
        }, 20 * 10);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        database.close();
        getComponentLogger().info(MiniMessage.miniMessage().deserialize("Disabled <version>", TagResolver.builder().tag("version", Tag.inserting(Component.text(getPluginMeta().getVersion()))).build()));
    }

    @Override
    public void onLoad() {
        if (getServer().getPluginManager().getPlugin("Essentials") != null) {
            this.essentialsAccess = new EssentialsAccess();
            essentialsAccess.load();
            getSLF4JLogger().info("Detected Essentials plugin, enabling support for Essentials");
        }
    }

    @Override
    public void onEnable() {
        if (!ServerBuildInfo.buildInfo().isBrandCompatible(Key.key("papermc","paper"))) {
            getSLF4JLogger().error("This plugin is not compatible with {} server", ServerBuildInfo.buildInfo().brandName());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        loadConfiguration();

        this.database = config().database.createDatabase();
        this.database.setup();

        if (config().loadOfflinePlayersInventories)
            OpenInvAccess.load();

        if (!setupEconomy()) {
            getComponentLogger().warn(MiniMessage.miniMessage().deserialize("<red>Disabled due to no Vault dependency found!", TagResolver.empty()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new EmeraldBankPlaceholderExpansion(this).register();
        }

        if (essentialsAccess != null) {
            essentialsAccess.enable(this);
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

    public boolean loadConfiguration() {
        try {
            this.settings = ConfigurationLoader.loadConfig(dataDir.resolve("config.yml"));
            return true;
        } catch (ConfigurateException e) {
            getSLF4JLogger().error("Failed to load configuration", e);
            if (settings == null) {
                this.settings = new Settings();
                getSLF4JLogger().warn("Using default configuration");
            }
            return false;
        }
    }

    private void updateCheck() {
        getSLF4JLogger().info("Checking for updates");
        try (var client = HttpClient.newHttpClient()) {
            var param = URLEncoder.encode("featured=true&loaders=[\"paper\"]&game_versions=[\"" + ServerBuildInfo.buildInfo().minecraftVersionId() + "\"]", StandardCharsets.UTF_8);
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
                        if (!getPluginMeta().getVersion().equals(versionNumber)) {
                            getSLF4JLogger().info("A new version is available");
                            getSLF4JLogger().info("Latest version: {}", versionNumber);
                            getSLF4JLogger().info("Current version: {}", getPluginMeta().getVersion());
                        }
                    }).join();
        } catch (Exception e) {
            getSLF4JLogger().error("Failed to check for updates: {}", e.getLocalizedMessage());
        }
    }

}
