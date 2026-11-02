plugins {
    id("oneenough.aggregate-conventions")
    id("oneenough.publishing-conventions")
}

tasks.register("buildAll") {
    group = "build"
    description = "Builds every OneEnough mod."
    dependsOn(tasks.build)
}

tasks.register("cleanAll") {
    group = "build"
    description = "Cleans every OneEnough mod."
    dependsOn(tasks.clean)
}
