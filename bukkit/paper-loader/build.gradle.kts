import net.minecrell.pluginyml.paper.PaperPluginDescription
import xyz.jpenilla.runpaper.task.RunServer
import xyz.jpenilla.runtask.pluginsapi.DownloadPluginsSpec
import java.net.URI

plugins {
    id("com.gradleup.shadow") version "9.3.0"
    id("de.eldoria.plugin-yml.paper") version "0.7.1"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

repositories {
    maven("https://jitpack.io/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.momirealms.net/releases/")
    mavenCentral()
}

dependencies {
    // Platform
    compileOnly("io.papermc.paper:paper-api:${rootProject.properties["paper_version"]}-R0.1-SNAPSHOT")

    implementation(project(":core"))
    implementation(project(":bukkit"))
    implementation(project(":bukkit:compatibility"))
    implementation(project(":common-files"))

    // concurrentutil
    implementation(files("${rootProject.rootDir}/libs/concurrentutil-${rootProject.properties["concurrent_util_version"]}.jar"))

    implementation("net.momirealms:sparrow-util:${rootProject.properties["sparrow_util_version"]}")
    implementation("net.momirealms:antigrieflib:${rootProject.properties["anti_grief_version"]}")
    implementation("net.momirealms:craft-engine-nms-helper-mojmap:${rootProject.properties["nms_helper_version"]}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
    dependsOn(tasks.clean)
}

paper {
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.STARTUP
    main = "net.momirealms.craftengine.bukkit.plugin.PaperCraftEnginePlugin"
    bootstrapper = "net.momirealms.craftengine.bukkit.plugin.PaperCraftEngineBootstrap"
    version = rootProject.properties["project_version"] as String
    name = "CraftEngine"
    apiVersion = "1.21"
    authors = listOf("XiaoMoMi")
    contributors = listOf("https://github.com/Xiao-MoMi/craft-engine/graphs/contributors")
    foliaSupported = true
    serverDependencies {
        // Core integrations
        register("PlaceholderAPI") { required = false }
        register("LuckPerms") { required = false }
        register("ViaVersion") { required = false }

        // AdvancedSlimePaper
        register("SlimeWorldPlugin") { required = false }
        register("SlimeWorldManager") { required = false }
        register("ASPaperPlugin") { required = false }

        // external tag
        register("CustomNameplates") { required = false }

        // external models
        register("ModelEngine") { required = false }
        register("BetterModel") { required = false }
    }
}

artifacts {
    implementation(tasks.shadowJar)
}

tasks {
    shadowJar {
        manifest {
            attributes["paperweight-mappings-namespace"] = "mojang"
        }
        archiveFileName = "${rootProject.name}-paper-plugin-${rootProject.properties["project_version"]}.jar"
        destinationDirectory.set(file("$rootDir/target"))
        relocate("net.kyori", "net.momirealms.craftengine.libraries")
        relocate("net.momirealms.sparrow.nbt", "net.momirealms.craftengine.libraries.nbt")
        relocate("net.momirealms.antigrieflib", "net.momirealms.craftengine.libraries.antigrieflib")
        relocate("org.incendo", "net.momirealms.craftengine.libraries")
        relocate("dev.dejvokep", "net.momirealms.craftengine.libraries")
        relocate("org.bstats", "net.momirealms.craftengine.libraries.bstats")
        relocate("com.github.benmanes.caffeine", "net.momirealms.craftengine.libraries.caffeine")
        relocate("com.ezylang.evalex", "net.momirealms.craftengine.libraries.evalex")
        relocate("net.bytebuddy", "net.momirealms.craftengine.libraries.bytebuddy")
        relocate("org.yaml.snakeyaml", "net.momirealms.craftengine.libraries.snakeyaml")
        relocate("org.ahocorasick", "net.momirealms.craftengine.libraries.ahocorasick")
        relocate("net.jpountz", "net.momirealms.craftengine.libraries.jpountz")
        relocate("software.amazon.awssdk", "net.momirealms.craftengine.libraries.awssdk")
        relocate("software.amazon.eventstream", "net.momirealms.craftengine.libraries.eventstream")
        relocate("com.google.common.jimfs", "net.momirealms.craftengine.libraries.jimfs")
        relocate("org.apache.commons", "net.momirealms.craftengine.libraries.commons")
        relocate("io.leangen.geantyref", "net.momirealms.craftengine.libraries.geantyref")
        relocate("ca.spottedleaf.concurrentutil", "net.momirealms.craftengine.libraries.concurrentutil")
        relocate("io.netty.handler.codec.http", "net.momirealms.craftengine.libraries.netty.handler.codec.http")
        relocate("io.netty.handler.codec.rtsp", "net.momirealms.craftengine.libraries.netty.handler.codec.rtsp")
        relocate("io.netty.handler.codec.spdy", "net.momirealms.craftengine.libraries.netty.handler.codec.spdy")
        relocate("io.netty.handler.codec.http2", "net.momirealms.craftengine.libraries.netty.handler.codec.http2")
        relocate("io.github.bucket4j", "net.momirealms.craftengine.libraries.bucket4j")
    }
}

/**
 * Register Run Dev Server Tasks
 */
listOf(
    "1.21.10",
).forEach {
    registerPaperTask(it)
}

fun registerPaperTask(
    version: String,
    dirName: String = version,
    javaVersion : Int = 21,
    serverJar: File? = null,
    downloadPlugins: Action<DownloadPluginsSpec>? = null
) {
    listOf(version, "${version}-with-viaversion").forEach { taskName ->
        tasks.register(taskName, RunServer::class) {
            group = "run dev server"
            minecraftVersion(version)
            serverJar?.let { serverJar(it) }
            pluginJars.from(tasks.shadowJar.flatMap { it.archiveFile })
            runDirectory = rootProject.layout.projectDirectory.dir("runPaper/${dirName}")
            javaLauncher = javaToolchains.launcherFor {
                vendor = JvmVendorSpec.JETBRAINS
                languageVersion = JavaLanguageVersion.of(javaVersion)
            }
            systemProperties["com.mojang.eula.agree"] = true
            systemProperties["net.momirealms.craftengine.dev"] = true
            jvmArgs("-Dsun.stdout.encoding=UTF-8")
            jvmArgs("-Dsun.stderr.encoding=UTF-8")
            jvmArgs("-Ddisable.watchdog=true")
            jvmArgs("-Xlog:redefine+class*=info")
            jvmArgs("-XX:+AllowEnhancedClassRedefinition")
            if (taskName.contains("viaversion")) {
                downloadPlugins {
                    url("https://ci.viaversion.com/job/ViaVersion/lastBuild/artifact/build/libs/${getJenkinsArtifactFileName("https://ci.viaversion.com/job/ViaVersion/lastSuccessfulBuild/api/json?tree=artifacts[*]")}")
                    url("https://ci.viaversion.com/view/ViaBackwards/job/ViaBackwards/662/artifact/build/libs/${getJenkinsArtifactFileName("https://ci.viaversion.com/job/ViaBackwards/lastSuccessfulBuild/api/json?tree=artifacts[*]")}")
                }
            }
        }
    }
}

fun getJenkinsArtifactFileName(url: String): String {
    val response = URI.create(url).toURL().readText()
    val regex = """"fileName":"([^"]+)"""".toRegex()
    return regex.find(response)?.groupValues?.get(1) ?: throw Exception("fileName not found")
}