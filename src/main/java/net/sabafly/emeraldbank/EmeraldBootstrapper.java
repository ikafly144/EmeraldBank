package net.sabafly.emeraldbank;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.sabafly.emeraldbank.configuration.Messages;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.loader.HeaderMode;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.UnaryOperator;

public class EmeraldBootstrapper implements PluginBootstrap {

    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        context.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, new EmeraldCommand());
    }

    @Override
    public @NotNull JavaPlugin createPlugin(@NotNull PluginProviderContext context) {
        final Messages message;
        try {
            message = loadMessages(context.getDataDirectory().resolve("messages.yml"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load messages", e);
        }
        return new EmeraldBank(context.getDataDirectory(), message);
    }

    public static Messages loadMessages(Path path) throws IOException {
        final YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(path)
                .indent(2)
                .headerMode(HeaderMode.PRESET)
                .nodeStyle(NodeStyle.BLOCK)
                .defaultOptions(UnaryOperator.identity())
                .build();

        CommentedConfigurationNode messageRoot;
        messageRoot = loader.load();
        Messages message = messageRoot.get(Messages.class);
        messageRoot.set(message);
        loader.save(messageRoot);
        return message;
    }
}
