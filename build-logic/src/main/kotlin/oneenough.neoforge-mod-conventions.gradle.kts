import org.gradle.api.artifacts.VersionCatalogsExtension
import org.slf4j.event.Level

plugins {
    id("oneenough.mod-conventions")
    id("net.neoforged.moddev")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
val modId = property("mod_id").toString()
val minecraftVersion = property("minecraft_version").toString()
base.archivesName.set("$modId-neoforge-$minecraftVersion")

neoForge {
    version = property("neo_version").toString()
    mods.create(modId) { sourceSet(sourceSets.main.get()) }
    runs.configureEach {
        systemProperty("neoforge.enabledGameTestNamespaces", modId)
        logLevel.set(Level.INFO)
    }
    runs.create("client") { client() }
    runs.create("server") {
        server()
        programArgument("--nogui")
    }
    runs.create("gameTestServer") { type.set("gameTestServer") }
}

dependencies {
    val oelibVersion = libs.findVersion("oelib").get().requiredVersion
    listOf("oelib", "bless", "config", "data", "dev", "event", "misc", "network", "registry", "renderer").forEach {
        implementation("cc.sighs.oelib:$it-neoforge-$minecraftVersion:$oelibVersion")
    }
    compileOnly(libs.findLibrary("optics-neoforge").get()) { isTransitive = false }
}
