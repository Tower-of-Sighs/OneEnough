import oneenough.ModDependencies

plugins {
    id("oneenough.java-conventions")
}

group = property("mod_group_id").toString()
version = property("mod_version").toString()
base.archivesName.set(property("mod_name").toString())
extensions.add("modDependencies", ModDependencies(project))
sourceSets.main.get().resources.srcDir("src/generated/resources")
repositories {
    mavenCentral()
    maven("https://maven.neoforged.net/releases")
    maven("https://maven.sighs.cc/repository/maven-releases/") {
        content {
            includeGroupByRegex("cc\\.sighs(\\..*)?")
            includeGroup("com.flechazo")
        }
    }
    maven("https://maven.sighs.cc/repository/maven-snapshots/") {
        content { includeGroupByRegex("cc\\.sighs(\\..*)?") }
    }
    maven("https://maven.blamejared.com/") {
        name = "Jared's maven"
        content { includeGroup("mezz.jei") }
    }
    maven("https://www.cursemaven.com") {
        name = "Curse Maven"
        content { includeGroup("curse.maven") }
    }
    flatDir { dirs("libs", rootProject.file("libs")) }
}

val resourceProperties = listOf(
    "minecraft_version", "minecraft_version_range", "forge_version", "forge_version_range",
    "loader_version_range", "neo_version", "neo_version_range", "mod_id", "mod_name", "mod_license", "mod_version",
    "mod_authors", "mod_description", "oelib_version_range",
).filter { project.hasProperty(it) }.associateWith { project.property(it).toString() }

tasks.processResources {
    inputs.properties(resourceProperties)
    filesMatching(listOf("META-INF/mods.toml", "META-INF/neoforge.mods.toml", "pack.mcmeta")) {
        expand(resourceProperties)
    }
}

tasks.jar {
    manifest.attributes(mapOf(
        "Specification-Title" to project.property("mod_id"),
        "Specification-Vendor" to project.property("mod_authors"),
        "Specification-Version" to "1",
        "Implementation-Title" to project.name,
        "Implementation-Version" to project.version.toString(),
        "Implementation-Vendor" to project.property("mod_authors"),
    ))
}