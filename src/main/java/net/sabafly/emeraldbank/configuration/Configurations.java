package net.sabafly.emeraldbank.configuration;

import com.mojang.logging.LogUtils;
import io.papermc.paper.configuration.constraint.Constraint;
import io.papermc.paper.configuration.constraint.Constraints;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.slf4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.util.CheckedFunction;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.lang.reflect.Type;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.UnaryOperator;

public abstract class Configurations<G, M> {

    private static final Logger LOGGER = LogUtils.getClassLogger();
    protected final Path globalFolder;
    protected final Class<G> globalConfigClass;
    protected final Class<M> messagesClass;
    protected final String globalConfigFileName;
    protected final String messagesFileName;

    public Configurations(
            Path globalFolder,
            Class<G> globalConfigClass,
            Class<M> messagesClass,
            String globalConfigFileName,
            String messagesFileName
    ) {
        this.globalFolder = globalFolder;
        this.globalConfigClass = globalConfigClass;
        this.messagesClass = messagesClass;
        this.globalConfigFileName = globalConfigFileName;
        this.messagesFileName = messagesFileName;
    }

    protected ObjectMapper.Factory.Builder createObjectMapper() {
        return ObjectMapper.factoryBuilder()
                .addConstraint(Constraint.class, new Constraint.Factory())
                .addConstraint(Constraints.Min.class, Number.class, new Constraints.Min.Factory());
    }

    protected YamlConfigurationLoader.Builder createLoaderBuilder() {
        return ConfigurationLoaders.naturallySorted();
    }

    protected abstract boolean isConfigType(final Type type);

    protected abstract int globalConfigVersion();

    protected abstract int messagesVersion();

    protected ObjectMapper.Factory.Builder createGlobalObjectMapperFactoryBuilder() {
        return this.createObjectMapper();
    }

    @MustBeInvokedByOverriders
    protected YamlConfigurationLoader.Builder createGlobalLoaderBuilder() {
        return this.createLoaderBuilder();
    }

    static <T> CheckedFunction<ConfigurationNode, T, SerializationException> creator(final Class<? extends T> type, final boolean refreshNode) {
        return node -> {
            final T instance = node.require(type);
            if (refreshNode) {
                node.set(type, instance);
            }
            return instance;
        };
    }

    static <T> CheckedFunction<ConfigurationNode, T, SerializationException> reloader(Class<T> type, T instance) {
        return node -> {
            ObjectMapper.Factory factory = (ObjectMapper.Factory) Objects.requireNonNull(node.options().serializers().get(type));
            ObjectMapper.Mutable<T> mutable = (ObjectMapper.Mutable<T>) factory.get(type);
            mutable.load(instance, node);
            return instance;
        };
    }

    public G initializeGlobalConfiguration() throws ConfigurateException {
        return this.initializeGlobalConfiguration(creator(this.globalConfigClass, true));
    }

    private void trySaveFileNode(YamlConfigurationLoader loader, ConfigurationNode node, String filename) throws ConfigurateException {
        try {
            loader.save(node);
        } catch (ConfigurateException ex) {
            if (ex.getCause() instanceof AccessDeniedException) {
                LOGGER.warn("Could not save {}: Paper could not persist the full set of configuration settings in the configuration file. Any setting missing from the configuration file will be set with its default value in memory. Admins should make sure to review the configuration documentation at https://docs.papermc.io/paper/configuration for more details.", filename, ex);
            } else throw ex;
        }
    }

    protected G initializeGlobalConfiguration(final CheckedFunction<ConfigurationNode, G, SerializationException> creator) throws ConfigurateException {
        final Path configFile = this.globalFolder.resolve(this.globalConfigFileName);
        final YamlConfigurationLoader loader = this.createGlobalLoaderBuilder()
                .defaultOptions(this.applyObjectMapperFactory(this.createGlobalObjectMapperFactoryBuilder().build()))
                .path(configFile)
                .build();
        final ConfigurationNode node;
        if (Files.notExists(configFile)) {
            node = CommentedConfigurationNode.root(loader.defaultOptions());
            node.node(Configuration.VERSION_FIELD).raw(this.globalConfigVersion());
            GlobalConfiguration.isFirstStart = true;
        } else {
            node = loader.load();
            this.verifyGlobalConfigVersion(node);
        }
        this.applyGlobalConfigTransformations(node);
        final G instance = creator.apply(node);
        trySaveFileNode(loader, node, configFile.toString());
        return instance;
    }

    protected void verifyGlobalConfigVersion(final ConfigurationNode globalNode) {
        final ConfigurationNode version = globalNode.node(Configuration.VERSION_FIELD);
        if (version.virtual()) {
            LOGGER.warn("The global config file didn't have a version set, assuming latest");
            version.raw(this.globalConfigVersion());
        } else if (version.getInt() > this.globalConfigVersion()) {
            LOGGER.error("Loading a newer configuration than is supported ({} > {})! You may have to backup & delete your global config file to start the server.", version.getInt(), this.globalConfigVersion());
        }
    }

    protected void applyGlobalConfigTransformations(final ConfigurationNode node) throws ConfigurateException {
    }

    private UnaryOperator<ConfigurationOptions> applyObjectMapperFactory(final ObjectMapper.Factory factory) {
        return options -> options.serializers(builder -> builder
                .register(this::isConfigType, factory.asTypeSerializer())
                .registerAnnotatedObjects(factory)
                .build()
        );
    }


}
