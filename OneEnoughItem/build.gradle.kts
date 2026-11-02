plugins {
    id("oneenough.forge-mod-conventions")
}

dependencies {
    modCompileOnly(libs.just.enough.characters)
}

legacyForge {
    runs.named("server") {
        gameDirectory.set(layout.projectDirectory.dir("run/server"))
        programArguments.set(emptyList())
    }
    runs.create("client2") {
        client()
        gameDirectory.set(layout.projectDirectory.dir("run/client_a"))
        programArguments.addAll("--username", "Mafuyu404")
    }
}

tasks.jar {
    exclude("data/oneenoughitem/replacements/example.json")
}
