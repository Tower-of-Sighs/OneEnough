plugins {
    base
}

listOf("assemble", "check", "build", "clean").forEach { lifecycle ->
    tasks.named(lifecycle) {
        dependsOn(childProjects.values.map { "${it.path}:$lifecycle" })
    }
}
