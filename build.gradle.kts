import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.minecrell.pluginyml.GeneratePluginDescription
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription
import xyz.jpenilla.runpaper.task.RunServer

plugins {
    java
    id("de.eldoria.plugin-yml.paper") version "0.9.0"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("com.gradleup.shadow") version "9.4.3"
    id("com.modrinth.minotaur") version "2.+"
    id("io.papermc.hangar-publish-plugin") version "0.+"
}

val projectGroup: String by project
val pluginVersion: String by project

allprojects {
    group = projectGroup
    version = pluginVersion
}

val javaVersion: String by project
val minecraftVersion: String by project
val pluginArtifactName: String by project
val supportedMinecraftVersions: String by project
val modrinthProjectId: String by project
val modrinthLoaders: String by project
val modrinthVersionType: String by project
val hangarProjectId: String by project
val hangarChannel: String by project

val targetJavaVersion = javaVersion.toInt()
val supportedMcVersions = supportedMinecraftVersions.split(',').map(String::trim).filter(String::isNotEmpty)
val modrinthLoaderList = modrinthLoaders.split(',').map(String::trim).filter(String::isNotEmpty)
val releaseChangelog = providers.environmentVariable("RELEASE_CHANGELOG")
    .orElse("Automated release for ${project.version}")

val buildNumber: String? = System.getenv("BUILD_NUMBER")
val releaseVersion =
    version.toString() + (if (buildNumber != null) "+build.$buildNumber" else "")

val publishPluginRelease by tasks.registering {
    group = "publishing"
    description = "Builds and publishes plugin artifacts to Modrinth and Hangar."
}

plugins.withId("com.modrinth.minotaur") {
    tasks.named("publishPluginRelease") {
        dependsOn(tasks.named("build"))
        dependsOn(tasks.named("modrinth"))
    }
}

plugins.withId("io.papermc.hangar-publish-plugin") {
    tasks.named("publishPluginRelease") {
        dependsOn(tasks.named("publishPluginPublicationToHangar"))
    }
}
repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
    maven {
        url = uri("https://repo.extendedclip.com/releases/")
    }
    maven {
        name = "essentialsxReleases"
        url = uri("https://repo.essentialsx.net/releases")
    }
    maven {
        name = "lunarclient"
        url = uri("https://repo.lunarclient.dev")
    }
}

dependencies {
    paperLibrary("com.h2database:h2:2.4.240")
    paperLibrary("com.mysql:mysql-connector-j:9.7.0")
    implementation("com.zaxxer:HikariCP:7.1.0")
    implementation("commons-dbutils:commons-dbutils:1.8.1")
    implementation("org.spongepowered:configurate-yaml:4.2.0+lunar.5")
    implementation("com.vdurmont:semver4j:3.1.0")

    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1") {
        isTransitive = false
    }
    compileOnly("me.clip:placeholderapi:2.12.2") {
        isTransitive = false
    }
    compileOnly("com.github.Jikoo:OpenInv:5.3.3")
    compileOnly("net.essentialsx:EssentialsX:2.21.2") {
        isTransitive = false
    }

    compileOnly("org.projectlombok:lombok:1.18.46")
    annotationProcessor("org.projectlombok:lombok:1.18.46")
    paperweight.paperDevBundle(property("paperVersion") as String)
}

paper {
    name = rootProject.name
    main = "net.sabafly.emeraldbank.EmeraldBank"
    version = releaseVersion
    apiVersion = "1.21.6"
    website = "https://modrinth.com/plugin/emeraldbank"
    bootstrapper = "net.sabafly.emeraldbank.EmeraldBootstrapper"
    loader = "net.sabafly.emeraldbank.EmeraldLoader"
    generateLibrariesJson = true
    foliaSupported = true
    authors = listOf("ikafly144")

    bootstrapDependencies {
        register("Vault") {
            required = false
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            joinClasspath = true
        }
        register("Essentials") {
            required = false
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            joinClasspath = true
        }
    }
    serverDependencies {
        register("Vault") {
            required = true
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            joinClasspath = true
        }
        register("OpenInv") {
            required = false
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            joinClasspath = true
        }
        register("PlaceholderAPI") {
            required = false
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            joinClasspath = true
        }
        register("Essentials") {
            required = false
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            joinClasspath = true
        }
        register("Towny") {
            required = false
            load = PaperPluginDescription.RelativeLoadOrder.AFTER
            joinClasspath = false
        }
    }

    permissions {
        register("emeraldbank.bypass") {
            default = BukkitPluginDescription.Permission.Default.FALSE
            description = "Allows the player to bypass the EmeraldBank command"
            childrenMap = mapOf(
                "emeraldbank.bypass.cost" to false,
                "emeraldbank.bypass.deposit" to false,
                "emeraldbank.bypass.member" to false,
                "emeraldbank.bypass.owner" to false
            )
        }
        register("emeraldbank.admin") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Allows the player to use all EmeraldBank command"
            childrenMap = mapOf(
                "emeraldbank.balance.all" to true,
                "emeraldbank.wallet.balance.all" to true,
                "emeraldbank.reload" to true,
                "emeraldbank.currency.rate.set" to true,
                "emeraldbank.default" to true
            )
        }
        register("emeraldbank.default") {
            default = BukkitPluginDescription.Permission.Default.TRUE
            description = "Allows the player to use the EmeraldBank command"
            childrenMap = mapOf(
                "emeraldbank.balance" to true,
                "emeraldbank.pay" to true,
                "emeraldbank.leaderboard" to true,
                "emeraldbank.wallet" to true,
                "emeraldbank.banking" to true,
                "emeraldbank.currency" to true
            )
        }
        register("emeraldbank.wallet") {
            default = BukkitPluginDescription.Permission.Default.TRUE
            description = "Allows the player to use the EmeraldBank command"
            childrenMap = mapOf(
                "emeraldbank.wallet.balance" to true,
                "emeraldbank.wallet.add" to true,
                "emeraldbank.wallet.withdraw" to true
            )
        }
        register("emeraldbank.banking") {
            default = BukkitPluginDescription.Permission.Default.TRUE
            description = "Allows the player to use the EmeraldBank command"
            childrenMap = mapOf(
                "emeraldbank.banking.account" to true,
                "emeraldbank.banking.withdraw" to true,
                "emeraldbank.banking.deposit" to true,
                "emeraldbank.banking.balance" to true,
                "emeraldbank.banking.send" to true,
                "emeraldbank.banking.pay" to true
            )
        }
        register("emeraldbank.banking.account") {
            default = BukkitPluginDescription.Permission.Default.TRUE
            description = "Allows the player to use the EmeraldBank command"
            childrenMap = mapOf(
                "emeraldbank.banking.account.create" to true,
                "emeraldbank.banking.account.delete" to true,
                "emeraldbank.banking.account.add" to true,
                "emeraldbank.banking.account.remove" to true,
                "emeraldbank.banking.account.list" to true,
                "emeraldbank.banking.account.transfer" to true
            )
        }
        register("emeraldbank.currency") {
            default = BukkitPluginDescription.Permission.Default.TRUE
            description = "Allows the player to use the EmeraldBank command"
            childrenMap = mapOf(
                "emeraldbank.currency.rate" to true,
                "emeraldbank.currency.exchange" to true,
                "emeraldbank.currency.exchange.*" to true
            )
        }
        register("emeraldbank.currency.rate") {
            default = BukkitPluginDescription.Permission.Default.TRUE
            description = "Allows the player to use the EmeraldBank command"
            childrenMap = mapOf(
                "emeraldbank.currency.rate.get" to true
            )
        }
    }
}

tasks.named<GeneratePluginDescription>("generatePaperPluginDescription") {
    useDefaultCentralProxy()
}

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"

    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}

tasks.named<Jar>("jar") {
    enabled = false
}

val shadowJarTask = tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName = project.name
    archiveClassifier = ""
    archiveVersion = releaseVersion

    dependencies {
        exclude(dependency("org.slf4j:slf4j-api"))
        exclude(dependency("net.kyori:option"))
    }

    minimize()

    relocate("org.spongepowered.configurate", "net.sabafly.configurate")
    relocate("io.leangen.geantyref", "net.sabafly.geantyref")

    relocate("com.zaxxer.hikari", "net.sabafly.libs.com.zaxxer.hikari")
    relocate("org.apache.commons.dbutils", "net.sabafly.libs.org.apache.commons.dbutils")

    relocate("com.vdurmont.semver4j", "net.sabafly.libs.com.vdurmont.semver4j")
}

tasks.build {
    dependsOn(shadowJarTask)
}

modrinth {
    token.set(providers.environmentVariable("MODRINTH_TOKEN"))
    projectId.set(modrinthProjectId)
    versionNumber.set(project.version.toString())
    versionName.set("$pluginArtifactName ${project.version}")
    versionType.set(modrinthVersionType)
    uploadFile.set(shadowJarTask)
    gameVersions.addAll(supportedMcVersions)
    loaders.addAll(modrinthLoaderList)
    changelog.set(releaseChangelog)
}

tasks.named("modrinth") {
    dependsOn(shadowJarTask)
}

hangarPublish {
    publications.register("plugin") {
        version = project.version.toString()
        id = hangarProjectId
        channel = hangarChannel
        changelog = releaseChangelog.get()
        apiKey = providers.environmentVariable("HANGAR_API_TOKEN").orElse("").get()

        platforms {
            paper {
                jar = shadowJarTask.flatMap { it.archiveFile }
                platformVersions = supportedMcVersions
            }
        }
    }
}

tasks.named("publishPluginPublicationToHangar") {
    dependsOn(shadowJarTask)
}

runPaper.folia.registerTask {
    downloadPlugins {
        modrinth("openinv", "5.3.1")
        github("SirBlobman", "Vault-Folia", "v1.7.3-folia", "Vault-1.7.3.jar")
        modrinth("foliaperms", "1.12.0")
        modrinth("placeholderapi", "2.12.2")
    }
    runDirectory = file("run/folia")
    minecraftVersion("1.21.11")
}

tasks.named<RunServer>("runServer") {
    downloadPlugins {
        modrinth("openinv", "5.3.1")
        github("MilkBowl", "Vault", "1.7.3", "Vault.jar")
        modrinth("luckperms", "v5.5.17-bukkit")
        modrinth("placeholderapi", "2.12.2")
        modrinth("towny", "0.102.0.0")
    }
    runDirectory = file("run/paper")
    minecraftVersion("26.1.2")
}

tasks.register<RunServer>("runLegacyServer") {
    description = "Runs a legacy server with Minecraft 1.21.6 for testing purposes."
    downloadPlugins {
        modrinth("openinv", "5.3.1")
        github("MilkBowl", "Vault", "1.7.3", "Vault.jar")
        modrinth("luckperms", "v5.5.17-bukkit")
        modrinth("placeholderapi", "2.12.2")
        modrinth("towny", "0.102.0.0")
    }
    pluginJars.from(shadowJarTask.flatMap { it.archiveFile })
    runDirectory = file("run/legacy")
    minecraftVersion("1.21.6")
}
