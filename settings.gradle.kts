rootProject.name = "craft-engine"
include(
    ":core",
    ":core:adventure",
    ":common-files",
    ":bukkit",
    ":bukkit:legacy",
    ":bukkit:compatibility",
    ":bukkit:compatibility:legacy",
    ":bukkit:compatibility:miniplaceholders",
    ":bukkit:loader",
    ":bukkit:proxy",
    ":bukkit:paper-loader"
)

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://maven.fabricmc.net/")
    }
}
