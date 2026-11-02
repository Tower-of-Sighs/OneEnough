package oneenough

import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import net.neoforged.moddevgradle.legacyforge.dsl.LegacyForgeExtension
import net.neoforged.moddevgradle.legacyforge.dsl.MixinExtension
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.getByType

/** Declares the compile dependency and the corresponding development source mod together. */
class ModDependencies(private val project: Project) {
    fun dependency(name: String) {
        val dependency = if (name.startsWith(":")) project.project(name) else requireNotNull(project.parent).project(name)
        project.dependencies.add("implementation", project.dependencies.project(mapOf("path" to dependency.path)))
        dependency.pluginManager.withPlugin("net.neoforged.moddev.legacyforge") {
            val id = dependency.property("mod_id").toString()
            val sources = dependency.extensions.getByType<JavaPluginExtension>().sourceSets.getByName("main")
            project.extensions.getByType<LegacyForgeExtension>().mods.create(id).sourceSet(sources)
            project.extensions.getByType<MixinExtension>().config("$id.mixins.json")
        }
        dependency.pluginManager.withPlugin("net.neoforged.moddev") {
            val id = dependency.property("mod_id").toString()
            val sources = dependency.extensions.getByType<JavaPluginExtension>().sourceSets.getByName("main")
            project.extensions.getByType<NeoForgeExtension>().mods.create(id).sourceSet(sources)
        }
    }
}
