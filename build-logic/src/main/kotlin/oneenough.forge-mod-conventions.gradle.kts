import net.neoforged.moddevgradle.legacyforge.dsl.LegacyForgeExtension
import net.neoforged.moddevgradle.legacyforge.dsl.MixinExtension
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.slf4j.event.Level

plugins {
    id("oneenough.mod-conventions")
    id("net.neoforged.moddev.legacyforge")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
// Project properties include module gradle.properties and inherited root defaults.
fun property(name: String) = project.property(name).toString()
val modId = property("mod_id")
val mainSources = sourceSets.main.get()

extensions.configure<LegacyForgeExtension> {
    version = "${property("minecraft_version")}-${property("forge_version")}"
    // MDG discovers META-INF/accesstransformer.cfg automatically and uses official mappings.
    mods.create(modId) {
        sourceSet(mainSources)
    }
    runs.configureEach {
        systemProperty("forge.logging.markers", "REGISTRIES")
        systemProperty("forge.logging.console.level", "debug")
        systemProperty("forge.enabledGameTestNamespaces", modId)
        logLevel.set(Level.DEBUG)
    }
    runs.create("client") {
        client()
    }
    runs.create("server") {
        server()
        programArgument("--nogui")
    }
    runs.create("gameTestServer") {
        type.set("gameTestServer")
    }
    runs.create("data") {
        data()
        gameDirectory.set(layout.projectDirectory.dir("run-data"))
        programArguments.addAll("--mod", modId, "--all", "--output",
            file("src/generated/resources/").absolutePath,
            "--existing", file("src/main/resources/").absolutePath)
    }
}

extensions.configure<MixinExtension> {
    add(mainSources, "$modId.refmap.json")
    config("$modId.mixins.json")
}




dependencies {
    add("annotationProcessor", "${libs.findLibrary("mixin-processor").get().get()}:processor")
    listOf("oelib", "kubejs", "rhino", "architectury", "jupiter", "jei").forEach {
        add("modImplementation", libs.findLibrary(it).get())
    }
    // Remap each OELib module for both compilation and development runs.
    // Remapping the aggregate alone does not remap its nested mod jars.
    add("modImplementation", libs.findBundle("oelib-api").get())
    // ConfigSchema's public API uses LensGetter; the runtime copy is embedded by OELib misc.
    compileOnly(libs.findLibrary("optics").get()) {
        isTransitive = false
    }
    add("modCompileOnly", libs.findLibrary("jei-api").get())
    listOf("sakura", "mysterious-mountain-lib").forEach {
        add("modRuntimeOnly", libs.findLibrary(it).get())
    }
}

tasks.jar {
    manifest.attributes("MixinConfigs" to "$modId.mixins.json")
}
