package net.sabafly.emeraldbank;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings({"UnstableApiUsage", "unused"})
public class EmeraldLoader implements PluginLoader {
    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        List<String> dependencies = List.of(
                "com.h2database:h2:2.3.232",
                "com.mysql:mysql-connector-j:9.2.0"
        );

        for (String dependency : dependencies) {
            resolver.addDependency(new Dependency(new DefaultArtifact(dependency), "compile"));
        }

        resolver.addRepository(new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2/").build());
        resolver.addRepository(new RemoteRepository.Builder("papermc", "default", "https://repo.papermc.io/repository/maven-public/").build());
        resolver.addRepository(new RemoteRepository.Builder("sonatype", "default", "https://oss.sonatype.org/content/groups/public/").build());

        classpathBuilder.addLibrary(resolver);
    }
}
