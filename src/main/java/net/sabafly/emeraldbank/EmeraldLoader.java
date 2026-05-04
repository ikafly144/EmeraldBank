package net.sabafly.emeraldbank;

import com.google.gson.Gson;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings({"unused", "UnstableApiUsage"})
public class EmeraldLoader implements PluginLoader {
    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        PluginLibraries pluginLibraries = loadLibraries();
        pluginLibraries.asDependencies().forEach(resolver::addDependency);

        resolver.addRepository(new RemoteRepository.Builder("central", "default", MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR).build());
        resolver.addRepository(new RemoteRepository.Builder("papermc", "default", "https://repo.papermc.io/repository/maven-public/").build());
        resolver.addRepository(new RemoteRepository.Builder("sonatype", "default", "https://oss.sonatype.org/content/groups/public/").build());

        classpathBuilder.addLibrary(resolver);
    }

    private PluginLibraries loadLibraries() {
        try (var in = EmeraldLoader.class.getResourceAsStream("/paper-libraries.json")) {
            if (in == null) {
                throw new IllegalStateException("paper-libraries.json is missing from plugin resources");
            }

            PluginLibraries pluginLibraries = new Gson()
                    .fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), PluginLibraries.class);
            if (pluginLibraries == null) {
                throw new IllegalStateException("paper-libraries.json is empty");
            }
            return pluginLibraries;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read paper-libraries.json", e);
        }
    }

    private record PluginLibraries(List<String> dependencies) {
        private Stream<Dependency> asDependencies() {
            return (dependencies == null ? List.<String>of() : dependencies).stream()
                    .map(coordinates -> new Dependency(new DefaultArtifact(coordinates), "compile"));
        }
    }
}
