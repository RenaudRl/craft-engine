import net.momirealms.PublishExtension
import net.momirealms.RelocationExtension
import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("java")
}

subprojects {

    apply {
        plugin("java")
        plugin("java-library")
        plugin("com.gradleup.shadow")
        plugin("maven-publish")
    }

    repositories {
        // The BTC fork of MiniPlaceholders publishes under the upstream coordinates, which Maven
        // Central also serves. exclusiveContent pins the module to the BTC repo so the public
        // artifact can never silently shadow the fork.
        exclusiveContent {
            forRepository {
                maven {
                    name = "btcRepo"
                    url = uri(
                        providers.gradleProperty("btcRepoDir")
                            .getOrElse(rootProject.file("../BTCVelocity/repo").absolutePath)
                    )
                }
            }
            filter {
                includeModule("io.github.miniplaceholders", "miniplaceholders-api")
            }
        }
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots")
    }

    // BTC Studio unified static Maven repo: committed under BTCVelocity/repo and uploaded
    // as-is to https://borntocraftstudio.net/public/repo/ . Overridable via -PbtcRepoDir
    // so this fork still builds when BTCVelocity is not checked out next to it.
    extensions.configure<PublishingExtension>("publishing") {
        repositories {
            maven {
                name = "btcRepo"
                url = uri(
                    providers.gradleProperty("btcRepoDir")
                        .getOrElse(rootProject.file("../BTCVelocity/repo").absolutePath)
                )
            }
        }
    }

    extensions.create<RelocationExtension>("relocation")
    extensions.create<PublishExtension>("publication")

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(21)
        dependsOn(tasks.clean)
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
        withSourcesJar()
        disableAutoTargetJvm()
    }

    tasks.processResources {
        filteringCharset = "UTF-8"

        filesMatching(arrayListOf("craft-engine.properties")) {
            expand(
                rootProject.properties + mapOf(
                    "proxy_version" to getTimestamp(),
                    "git_version" to versionBanner(),
                    "builder" to builderName()
                )
            )
        }

        filesMatching(arrayListOf("commands.yml", "config.yml")) {
            expand(
                Pair("config_version", rootProject.properties["config_version"]!!)
            )
        }
    }
}

fun versionBanner(): String = project.providers.exec {
    commandLine("git", "rev-parse", "--short=8", "HEAD")
}.standardOutput.asText.map { it.trim() }.getOrElse("Unknown")

fun builderName(): String = providers.exec {
    commandLine("git", "config", "user.name")
}.standardOutput.asText.map { it.trim() }.getOrElse("Unknown")

fun getTimestamp(): String {
    return SimpleDateFormat("yyyyMMdd_HHmm").format(Date())
}