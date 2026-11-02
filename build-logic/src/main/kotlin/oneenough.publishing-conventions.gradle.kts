import java.util.Properties
import me.modmuss50.mpp.ReleaseType

plugins {
    id("me.modmuss50.mod-publish-plugin")
}

// As with OELib, publishing previews are the default. Enable upload only after reviewing them.
publishMods {
    dryRun.set(providers.gradleProperty("curseforgeDryRun").map { it.toBooleanStrict() }.orElse(true))
    type.set(ReleaseType.STABLE)
}

data class ReleaseMod(val module: String, val projectId: String, val slug: String, val dependency: String)
val releaseMods = listOf(
    ReleaseMod("OneEnoughItem", "1312660", "one-enough-item", "oelib"),
    ReleaseMod("OneEnoughBlock", "1356867", "one-enough-block", "one-enough-item"),
    ReleaseMod("OneEnoughFluid", "1366406", "one-enough-fluid", "one-enough-block"),
)
val releaseFiles = mutableListOf<TaskProvider<Sync>>()
listOf("1.20.1", "1.21.1", "26.1").forEach { minecraft ->
    val loader = if (minecraft == "1.20.1") "forge" else "neoforge"
    val prefix = when (minecraft) { "1.20.1" -> ""; "1.21.1" -> ":neoforge121"; else -> ":neoforge261" }
    releaseMods.forEach { mod ->
        val moduleProject = project("$prefix:${mod.module}")
        val metadata = Properties().apply {
            providers.fileContents(moduleProject.layout.projectDirectory.file("gradle.properties")).asText.get()
                .reader().use { load(it) }
        }
        val releaseVersion = metadata.getProperty("mod_version")
        val originalName = if (loader == "forge") "${mod.module}-$releaseVersion.jar"
            else "${mod.module.lowercase()}-$loader-$minecraft-$releaseVersion.jar"
        val releaseName = "${mod.module}-$loader-$minecraft-$releaseVersion.jar"
        val suffix = mod.module + minecraft.replace(".", "")
        val prepared = tasks.register<Sync>("prepareRelease$suffix") {
            dependsOn("${moduleProject.path}:${if (loader == "forge") "reobfJar" else "jar"}")
            from(moduleProject.layout.buildDirectory.file("libs/$originalName"))
            into(layout.buildDirectory.dir("release-candidates/$suffix"))
            rename { releaseName }
        }
        releaseFiles.add(prepared)
        publishMods {
            curseforge("curseforge$suffix") {
                accessToken.set(providers.environmentVariable("CURSEFORGE_TOKEN"))
                projectId.set(mod.projectId)
                projectSlug.set(mod.slug)
                version.set(releaseVersion)
                file.set(layout.file(prepared.map { it.destinationDir.resolve(releaseName) }))
                displayName.set("${mod.module} $releaseVersion - $loader $minecraft")
                changelog.set(providers.gradleProperty("releaseChangelog").orElse(""))
                minecraftVersions.add(minecraft)
                modLoaders.add(loader)
                javaVersions.add(JavaVersion.toVersion(when (minecraft) { "1.20.1" -> 17; "1.21.1" -> 21; else -> 25 }))
                clientRequired.set(true)
                serverRequired.set(true)
                requires(mod.dependency)
            }
        }
        val prerequisite = when (mod.module) {
            "OneEnoughBlock" -> "OneEnoughItem"
            "OneEnoughFluid" -> "OneEnoughBlock"
            else -> null
        }
        if (prerequisite != null) {
            tasks.matching { it.name == "publishCurseforge$suffix" }.configureEach {
                mustRunAfter("publishCurseforge$prerequisite${minecraft.replace(".", "")}")
            }
        }
    }
}

tasks.register<Sync>("prepareRelease") {
    group = "publishing"
    description = "Collects all nine reviewed release candidates without uploading them."
    releaseFiles.forEach { from(it.map { task -> task.destinationDir }) }
    into(layout.buildDirectory.dir("release-review/jars"))
}
