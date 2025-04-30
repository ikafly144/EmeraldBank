package net.sabafly.emeraldbank.configuration;

import com.google.common.base.Preconditions;
import net.sabafly.emeraldbank.EmeraldBank;
import net.sabafly.emeraldbank.configuration.serializer.NamespacedKeySerializer;
import net.sabafly.emeraldbank.configuration.type.DoubleOr;
import net.sabafly.emeraldbank.configuration.type.IntOr;
import net.sabafly.emeraldbank.util.LogUtils;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.loader.HeaderMode;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;
import org.spongepowered.configurate.transformation.MoveStrategy;
import org.spongepowered.configurate.util.MapFactories;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigurationLoader {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static @NotNull Settings loadConfig(Path path) throws ConfigurateException {
        YamlConfigurationLoader loader=defaultLoader(path);
        CommentedConfigurationNode node;

        if (Files.notExists(path)) {
            node = loader.createNode(config -> config.set(Settings.class, new Settings()));
        } else {
            node = loader.load();
        }

        var trans = transformer();

        var start = trans.version(node);
        if (start != CURRENT_VERSION) {
            trans.apply(node);
            LOGGER.info("Updated configuration from {} to version {}", start, CURRENT_VERSION);
        }

        Settings settings = node.get(Settings.class, new Settings());

        loader.save(loader.createNode(c -> c.set(Settings.class, settings)));
        return settings;
    }

    public static void saveConfig(Path path, Settings settings) throws ConfigurateException {
        YamlConfigurationLoader loader = defaultLoader(path);

        CommentedConfigurationNode node = loader.createNode(c -> c.set(Settings.class, settings));
        loader.save(node);
    }

    private static YamlConfigurationLoader defaultLoader(Path path) {
        return YamlConfigurationLoader.builder()
                .defaultOptions(options -> options
                        .mapFactory(MapFactories.insertionOrdered())
                        .shouldCopyDefaults(true)
                        .header(Settings.HEADER)
                        .serializers(
                                builder -> builder
                                        .register(IntOr.Default.SERIALIZER)
                                        .register(IntOr.Disabled.SERIALIZER)
                                        .register(DoubleOr.Default.SERIALIZER)
                                        .register(DoubleOr.Disabled.SERIALIZER)
                                        .register(NamespacedKey.class, new NamespacedKeySerializer())
                                        .build()
                        )
                )
                .indent(2)
                .nodeStyle(NodeStyle.BLOCK)
                .headerMode(HeaderMode.PRESET)
                .path(path)
                .build();
    }

    static final int CURRENT_VERSION = 0;

    static ConfigurationTransformation.Versioned transformer() {
        var builder = ConfigurationTransformation.versionedBuilder()
                .versionKey(Settings.VERSION_FIELD)
                .addVersion(0, initialTransform())
                .build();
        Preconditions.checkState(builder.latestVersion() == CURRENT_VERSION, "Latest version is not current");
        return builder;
    }

    private static ConfigurationTransformation initialTransform() {
        return ConfigurationTransformation.builder()
                .moveStrategy(MoveStrategy.OVERWRITE)
                .addAction(NodePath.path(), (inputPath, valueAtPath) -> {
                    var path = EmeraldBank.getDataDir().resolve("messages.yml");
                    if (Files.exists(path)) {
                        CommentedConfigurationNode node = YamlConfigurationLoader.builder().path(path).build().load();
                        valueAtPath.node("messages").set(node);
                        try {
                            Files.delete(path);
                            Files.createFile(EmeraldBank.getDataDir().resolve("messages.yml_has_been_moved_to_config.yml"));
                        } catch (IOException ignored) {
                        }
                    }
                    return null;
                })
                .build();
    }

}
