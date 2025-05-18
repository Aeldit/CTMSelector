plugins {
    id("java")
    id("maven-publish")
    id("fabric-loom") version "1.10-SNAPSHOT"
    id("com.modrinth.minotaur") version "2.+"
    id("me.modmuss50.mod-publish-plugin") version "0.5.+"
}

repositories {
    maven("https://api.modrinth.com/maven") {
        name = "Modrinth"
    }
}

object C {
    const val MOD_VERSION: String = "0.4.3"
    const val LOADER_VERSION: String = "0.16.13"
    val YARN_MAPPINGS: HashMap<String, String> = hashMapOf(
        "1.20.2" to "1.20.2+build.4",
        "1.20.4" to "1.20.4+build.3",
        "1.20.6" to "1.20.6+build.3",
        "1.21.1" to "1.21.1+build.3",
        "1.21.3" to "1.21.3+build.2",
        "1.21.4" to "1.21.4+build.8",
        "1.21.5" to "1.21.5+build.1",
    )
    val RANGES_NAMES: HashMap<String, String> = hashMapOf("1.21.1" to "1.21-1.21.1", "1.21.3" to "1.21.2-1.21.3")
    val MODRINTH_VERSIONS: HashMap<String, String> = hashMapOf("1.21.1" to "1.21 1.21.1", "1.21.3" to "1.21.2 1.21.3")
    val FABRIC_VERSIONS: HashMap<String, String> = hashMapOf(
        "1.20.2" to "0.91.6+1.20.2",
        "1.20.4" to "0.97.2+1.20.4",
        "1.20.6" to "0.100.8+1.20.6",
        "1.21-1.21.1" to "0.115.6+1.21.1",
        "1.21.2-1.21.3" to "0.114.0+1.21.3",
        "1.21.4" to "0.119.2+1.21.4",
        "1.21.5" to "0.121.0+1.21.5",
    )
    val MOD_MENU_VERSIONS: HashMap<String, String> = hashMapOf(
        "1.20.2" to "8.0.1",
        "1.20.4" to "9.2.0-beta.2",
        "1.20.6" to "10.0.0",
        "1.21-1.21.1" to "11.0.3",
        "1.21.2-1.21.3" to "12.0.0",
        "1.21.4" to "13.0.3",
        "1.21.5" to "14.0.0-rc.2",
    )
    val CONTINUITY_VERSIONS: HashMap<String, String> = hashMapOf(
        "1.20.2" to "3.0.0+1.20.2",
        "1.20.4" to "3.0.0+1.20.2",
        "1.20.6" to "3.0.0+1.20.5",
        "1.21-1.21.1" to "3.0.0+1.21",
        "1.21.2-1.21.3" to "3.0.0+1.21.3",
        "1.21.4" to "3.0.0+1.21.4",
        "1.21.5" to "3.0.1-test.4+1.21.5",
    )
}

class ModData {
    val mcVersion = property("minecraft_version").toString()
    val yarnMappings = C.YARN_MAPPINGS[mcVersion]

    val isj21 = mcVersion !in setOf("1.20.2", "1.20.4")
    val javaVersion = if (isj21) "21" else "17"

    val hasVersionRange = C.RANGES_NAMES.containsKey(mcVersion)

    val rangedName = C.RANGES_NAMES.getOrDefault(mcVersion, mcVersion)

    val versionsList = C.MODRINTH_VERSIONS.getOrDefault(mcVersion, mcVersion).split(" ")
    val min = if (hasVersionRange) versionsList[0] else mcVersion
    val max = if (hasVersionRange) versionsList[versionsList.size - 1] else mcVersion

    val fabricVersion = C.FABRIC_VERSIONS[rangedName]
    val modmenuVersion = C.MOD_MENU_VERSIONS[rangedName]

    val fullVersion = "${C.MOD_VERSION}+$rangedName"

    val continuityVersion = C.CONTINUITY_VERSIONS[rangedName]
}

val mod = ModData()

// Sets the name of the output jar files
base {
    archivesName = "${rootProject.name}-${mod.fullVersion}"
    group = "fr.aeldit.ctms"
}

dependencies {
    minecraft("com.mojang:minecraft:${mod.mcVersion}")
    mappings("net.fabricmc:yarn:${mod.yarnMappings}:v2")
    modImplementation("net.fabricmc:fabric-loader:${C.LOADER_VERSION}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${mod.fabricVersion}")

    // ModMenu
    modImplementation("maven.modrinth:modmenu:${mod.modmenuVersion}")

    // Continuity
    modLocalRuntime("maven.modrinth:continuity:${mod.continuityVersion}")
}

loom {
    runConfigs.all {
        ideConfigGenerated(true) // Run configurations are not created for subprojects by default
        runDir = "../../run" // Use a shared run folder and just create separate worlds
    }
}

java {
    sourceCompatibility = if (mod.isj21) JavaVersion.VERSION_21 else JavaVersion.VERSION_17
    targetCompatibility = if (mod.isj21) JavaVersion.VERSION_21 else JavaVersion.VERSION_17
}

val buildAndCollect = tasks.register<Copy>("buildAndCollect") {
    group = "build"
    from(tasks.remapJar.get().archiveFile)
    into(rootProject.layout.buildDirectory.file("libs/${rootProject.name}-${mod.fullVersion}"))
    dependsOn("build")
}

if (stonecutter.current.isActive) {
    rootProject.tasks.register("buildActive") {
        group = "project"
        dependsOn(buildAndCollect)
    }
}

tasks {
    processResources {
        inputs.property("version", mod.fullVersion)
        inputs.property("min", mod.min)
        inputs.property("max", mod.max)
        inputs.property("java_version", mod.javaVersion)
        inputs.property("loader_version", C.LOADER_VERSION)
        inputs.property("fabric_api_version", mod.fabricVersion)
        inputs.property("modmenu_version", mod.modmenuVersion)
        inputs.property("continuity_version", mod.continuityVersion)

        filesMatching("fabric.mod.json") {
            expand(
                mapOf(
                    "version" to mod.fullVersion,
                    "min" to mod.min,
                    "max" to mod.max,
                    "java_version" to mod.javaVersion,
                    "loader_version" to C.LOADER_VERSION,
                    "fabric_api_version" to mod.fabricVersion,
                    "modmenu_version" to mod.modmenuVersion,
                    "continuity_version" to mod.continuityVersion
                )
            )
        }
    }

    jar {
        from("LICENSE")
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = if (mod.isj21) 21 else 17
    }
}

publishMods {
    modrinth {
        accessToken = System.getenv("MODRINTH_TOKEN")

        projectId = "6OpnBWtt"
        displayName = "[${mod.rangedName}] CTM Selector ${C.MOD_VERSION}"
        version = mod.fullVersion
        type = STABLE

        file = tasks.remapJar.get().archiveFile

        if (mod.hasVersionRange) {
            minecraftVersions.addAll(mod.versionsList)
        } else {
            minecraftVersions.add(mod.mcVersion)
        }
        modLoaders.add("fabric")

        requires("fabric-api", "modmenu", "continuity")

        changelog = rootProject.file("changelogs/latest.md")
            .takeIf { it.exists() }
            ?.readText()
            ?: "No changelog provided."

        dryRun = false
    }
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))

    projectId.set(rootProject.name)
    if (rootProject.file("README.md").exists()) {
        syncBodyFrom.set(rootProject.file("README.md").readText())
    }

    debugMode = false
}