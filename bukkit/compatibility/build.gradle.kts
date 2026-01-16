repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // papi
    maven("https://repo.infernalsuite.com/repository/maven-snapshots/")  // slime world
    maven("https://repo.momirealms.net/releases/")
    maven("https://mvn.lumine.io/repository/maven-public/") // model engine
    maven("https://repo.viaversion.com") // via
}

dependencies {
    compileOnly(project(":core"))
    compileOnly(project(":bukkit"))
    compileOnly("net.momirealms:sparrow-nbt:${rootProject.properties["sparrow_nbt_version"]}")
    // NMS
    compileOnly("net.momirealms:craft-engine-nms-helper:${rootProject.properties["nms_helper_version"]}")
    // Platform
    compileOnly("io.papermc.paper:paper-api:${rootProject.properties["paper_version"]}-R0.1-SNAPSHOT")
    // Placeholder
    compileOnly("me.clip:placeholderapi:${rootProject.properties["placeholder_api_version"]}")
    // SlimeWorld
    compileOnly("com.infernalsuite.asp:api:4.0.0-SNAPSHOT")
    // ModelEngine
    compileOnly("com.ticxo.modelengine:ModelEngine:R4.0.8")
    // BetterModel
    compileOnly("io.github.toxicity188:bettermodel:1.14.0")
    compileOnly("com.mojang:authlib:${rootProject.properties["authlib_version"]}")
    // LuckPerms
    compileOnly("net.luckperms:api:5.4")
    // viaversion
    compileOnly("com.viaversion:viaversion-api:5.3.2")
    // CustomNameplates
    compileOnly("net.momirealms:custom-nameplates:3.0.33")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
    dependsOn(tasks.clean)
}