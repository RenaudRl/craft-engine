rootProject.name = "craft-engine"
include(":core")
include(":bukkit")
include(":bukkit:compatibility")
include(":bukkit:loader")
include(":bukkit:paper-loader")
include(":common-files")
pluginManagement {
    plugins {
        kotlin("jvm") version "2.1.20"
    }
    repositories {
        maven("https://plugins.gradle.org/m2/")
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://maven.fabricmc.net/")
    }
}