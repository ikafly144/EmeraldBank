package net.sabafly.emeraldbank.configuration;

import com.mojang.logging.LogUtils;
import io.papermc.paper.configuration.mapping.InnerClassFieldDiscoverer;
import net.sabafly.emeraldbank.configuration.serializer.ComponentSerializer;
import net.sabafly.emeraldbank.configuration.type.IntOr;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.lang.reflect.Type;
import java.nio.file.Path;

import static io.leangen.geantyref.GenericTypeReflector.erase;

public class EmeraldConfigurations extends Configurations<GlobalConfiguration, Messages> {

    private static final Logger LOGGER = LogUtils.getClassLogger();
    static final String GLOBAL_CONFIG_FILE_NAME = "config.yml";
    static final String MESSAGES_FILE_NAME = "messages.yml";

    private static final String GLOBAL_HEADER = """
            This is the main configuration file for EmeraldBank.
            As you can see, there's a lot to configure. Some options may impact gameplay, so use
            with caution, and make sure you know what each option does before configuring.
            """;

    public EmeraldConfigurations(final Path globalFolder) {
        super(globalFolder, GlobalConfiguration.class, Messages.class, GLOBAL_CONFIG_FILE_NAME, MESSAGES_FILE_NAME);
    }

    @Override
    protected boolean isConfigType(Type type) {
        return ConfigurationPart.class.isAssignableFrom(erase(type));
    }

    @Override
    protected int globalConfigVersion() {
        return GlobalConfiguration.CURRENT_VERSION;
    }

    @Override
    protected int messagesVersion() {
        return Messages.CURRENT_VERSION;
    }

    @Override
    protected YamlConfigurationLoader.Builder createLoaderBuilder() {
        return super.createLoaderBuilder()
                .defaultOptions(EmeraldConfigurations::defaultOptions);
    }

    private static ConfigurationOptions defaultOptions(ConfigurationOptions options) {
        return options.serializers(builder -> builder
                .register(new ComponentSerializer())
        );
    }
    @Override
    protected ObjectMapper.Factory.Builder createGlobalObjectMapperFactoryBuilder() {
        return defaultGlobalFactoryBuilder(super.createGlobalObjectMapperFactoryBuilder());
    }

    private static ObjectMapper.Factory.Builder defaultGlobalFactoryBuilder(ObjectMapper.Factory.Builder builder) {
        return builder.addDiscoverer(InnerClassFieldDiscoverer.globalConfig());
    }

    @Override
    protected YamlConfigurationLoader.Builder createGlobalLoaderBuilder() {
        return super.createGlobalLoaderBuilder()
                .defaultOptions(EmeraldConfigurations::defaultGlobalOptions);
    }

    private static ConfigurationOptions defaultGlobalOptions(ConfigurationOptions options) {
        return options
                .header(GLOBAL_HEADER)
                .serializers(builder -> builder
                        .register(IntOr.Default.SERIALIZER)
                        .register(IntOr.Disabled.SERIALIZER)
                        .build()
                );
    }

    @Override
    public GlobalConfiguration initializeGlobalConfiguration() throws ConfigurateException {
        GlobalConfiguration configuration = super.initializeGlobalConfiguration();
        GlobalConfiguration.set(configuration);
        return configuration;
    }

}
