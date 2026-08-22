import net.momirealms.common
import net.momirealms.nbt
import net.momirealms.netty
import net.momirealms.paperServer

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.momirealms.net/releases/")
}

dependencies {
    // Adventure comes from paper-api on purpose: this module must compile — and run — against
    // the server's real net.kyori, not against CraftEngine's shaded copy.
    paperServer(project)
    common(project)
    nbt(project)
    netty(project)

    compileOnly(project(":core"))
    compileOnly(project(":bukkit"))

    compileOnly("io.github.miniplaceholders:miniplaceholders-api:${rootProject.properties["miniplaceholders_version"]}")
}

tasks.shadowJar {
    // Deliberately NOT `relocation.applyCommon(this)`. Relocating net.kyori here would rewrite the
    // descriptors of the expansions and of the render path, and MiniPlaceholders would then be
    // handed types it has never heard of (LambdaConversionException at registration time). This
    // jar is nested in the plugin jar and added to the plugin classloader at runtime, where the
    // server's Adventure and the MiniPlaceholders API are both visible.
    archiveClassifier = ""
    archiveFileName = "miniplaceholders.jarinjar"
}
