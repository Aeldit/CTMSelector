import dev.kikugie.stonecutter.StonecutterSettings

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        maven("https://maven.kikugie.dev/releases") {
            name = "StoneCutter"
        }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.4"
}

extensions.configure<StonecutterSettings> {
    kotlinController = true
    centralScript = "build.gradle.kts"

    shared {
        versions("1.20.2", "1.20.4", "1.20.6", "1.21.0")
        vcsVersion = "1.20.6"
    }

    create(rootProject)
}

rootProject.name = "ctm-selector"