import org.gradle.api.artifacts.VersionCatalogsExtension
import java.util.Properties

plugins {
    java
    eclipse
    idea
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
val platformFile = layout.projectDirectory.file("../platform.properties")
if (platformFile.asFile.isFile) {
    val platformProperties = Properties().apply {
        providers.fileContents(platformFile).asText.get().reader().use { load(it) }
    }
    platformProperties.forEach { key, value -> extra[key.toString()] = value.toString() }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(
        findProperty("java_version")?.toString() ?: libs.findVersion("java").get().requiredVersion
    ))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}
