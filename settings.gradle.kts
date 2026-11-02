pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "OneEnough"
include("OneEnoughItem", "OneEnoughBlock", "OneEnoughFluid")

mapOf("neoforge121" to "1.21.1", "neoforge261" to "26.1").forEach { (platform, minecraft) ->
    include(":$platform")
    project(":$platform").projectDir = file("versions/$minecraft")
    listOf("OneEnoughItem", "OneEnoughBlock", "OneEnoughFluid").forEach { mod ->
        include(":$platform:$mod")
        project(":$platform:$mod").projectDir = file("versions/$minecraft/$mod")
    }
}
