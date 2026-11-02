import net.neoforged.moddevgradle.dsl.ModDevExtension
import net.neoforged.moddevgradle.legacyforge.dsl.ObfuscationExtension

plugins {
    id("oneenough.java-conventions")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
val modDev = extensions.getByType<ModDevExtension>()
val isForge = pluginManager.hasPlugin("net.neoforged.moddev.legacyforge")
repositories {
    maven("https://repo.sleeping.town/") {
        content { includeGroup("dev.emi") }
    }
}

val gameTest by sourceSets.creating
configurations[gameTest.implementationConfigurationName].extendsFrom(configurations.implementation.get())
configurations[gameTest.compileOnlyConfigurationName].extendsFrom(configurations.compileOnly.get())
configurations[gameTest.runtimeOnlyConfigurationName].extendsFrom(configurations.runtimeOnly.get())
gameTest.compileClasspath += sourceSets.main.get().output
gameTest.runtimeClasspath += sourceSets.main.get().output
modDev.addModdingDependenciesTo(gameTest)
val testRuntime = configurations[gameTest.runtimeOnlyConfigurationName]
val remappedTestRuntime = if (isForge) {
    extensions.getByType<ObfuscationExtension>().createRemappingConfiguration(testRuntime)
} else testRuntime

val regressionMod = modDev.mods.create("oneenough_regression") {
    sourceSet(gameTest)
}
modDev.runs.configureEach {
    loadedMods.set(provider { modDev.mods.filter { it != regressionMod } })
}
modDev.runs.named("gameTestServer") {
    loadedMods.add(regressionMod)
    systemProperty("${if (isForge) "forge" else "neoforge"}.enabledGameTestNamespaces", "oneenough_regression")
    gameDirectory.set(layout.buildDirectory.dir("gametest"))
}
modDev.runs.create("regressionClient") {
    client()
    sourceSet.set(gameTest)
    loadedMods.add(regressionMod)
    systemProperty("oneenough.runClientRegressions", "true")
    gameDirectory.set(layout.buildDirectory.dir("regression-client"))
}
dependencies {
    if (isForge || project.property("minecraft_version") == "1.21.1") {
        add(remappedTestRuntime.name, libs.findLibrary(if (isForge) "emi-forge" else "emi-neoforge").get())
    }
}

// Isolated multi-process synchronization regression; opt-in runs only.
if (isForge) {
    val syncRoot = rootProject.layout.buildDirectory.dir("multiplayer-sync/forge120")
    modDev.runs.create("syncServer") {
        server()
        sourceSet.set(gameTest)
        loadedMods.add(regressionMod)
        gameDirectory.set(syncRoot.map { it.dir("server") })
        programArgument("--nogui")
        systemProperty("oneenough.syncProbe", "true")
        systemProperty("oneenough.syncProbeRoot", syncRoot.get().asFile.absolutePath)
        jvmArgument("-Xmx1536m")
    }
    (1..3).forEach { index ->
        modDev.runs.create("syncClient$index") {
            client()
            sourceSet.set(gameTest)
            loadedMods.add(regressionMod)
            gameDirectory.set(syncRoot.map { it.dir("client$index") })
            programArguments.addAll("--username", "OESync$index", "--quickPlayMultiplayer", "127.0.0.1:25586", "--width", "640", "--height", "360")
            systemProperty("oneenough.syncProbe", "true")
            systemProperty("oneenough.syncProbeRoot", syncRoot.get().asFile.absolutePath)
            systemProperty("oneenough.syncProbeClient", index.toString())
            jvmArgument("-Xmx1536m")
        }
    }
}
