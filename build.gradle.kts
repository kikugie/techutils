plugins {
    `maven-publish`
    kotlin("jvm") version "1.9.21"
    id("fabric-loom") version "1.4-SNAPSHOT"
    id("me.fallenbreath.yamlang") version "1.3.1"
}

val mcVersion = stonecutter.current.version
val kotlinVersion = property("kotlin_version").toString()
val modId = property("mod.id").toString()
val modVersion = property("mod.version").toString()

val flkVersion = property("deps.flk").toString()
val modmenuVersion = property("deps.modmenu").toString()
val malilibVersion = property("deps.malilib").toString()
val litematicaVersion = property("deps.litematica").toString()

val target = ">=${project.property("mod.min_target")}- <=${project.property("mod.max_target")}"

loom {
    splitEnvironmentSourceSets()

    mods {
        create(modId) {
            sourceSets.main.get()
            sourceSets["client"]
        }
    }
}

repositories {
    mavenLocal()
    maven("https://jitpack.io") { name = "JitPack" }
    maven("https://masa.dy.fi/maven") { name = "Masa Maven" }
    maven("https://maven.terraformersmc.com/releases/") { name = "Terraformers MC" }
    maven("https://maven.kikugie.dev/releases") { name = "KikuGie Maven" }
    maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    maven("https://maven.kikugie.dev/third-party") { name = "KikuGie Third-party" }
    exclusiveContent {
        forRepository { maven("https://api.modrinth.com/maven") { name = "Modrinth" } }
        filter { includeGroup("maven.modrinth") }
    }
    exclusiveContent {
        forRepository { maven("https://www.cursemaven.com") { name = "Curseforge" } }
        filter { includeGroup("curse.maven") }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings("net.fabricmc:yarn:${property("deps.yarn")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${property("deps.flk")}+kotlin.$kotlinVersion")

    modImplementation("com.terraformersmc:modmenu:${property("deps.modmenu")}")
    modImplementation("dev.kikugie.worldrenderer:${property("deps.worldrenderer")}:$mcVersion")
    modImplementation("dev.kikugie.malilib_extras:${property("deps.malilib_extras")}:$mcVersion")

    modImplementation("fi.dy.masa:litematica:${property("deps.litematica")}+$mcVersion")
    modImplementation(fabricApi.module("fabric-command-api-v2", "${property("deps.fabric_api")}"))

    testImplementation("net.fabricmc:fabric-loader-junit:${property("deps.fabric_loader")}")

    modCompileOnly("maven.modrinth:axiom:Ltd8ZQ1T")
}

loom {
    runConfigs.all {
        ideConfigGenerated(true)
        runDir = "../../run"
        vmArgs("-Dmixin.debug.export=true")
    }
}

tasks.processResources {
    inputs.property("version", modVersion)
    inputs.property("minecraft", target)
    inputs.property("flk", flkVersion)
    inputs.property("malilib", malilibVersion)
    inputs.property("litematica", litematicaVersion)

    filesMatching("fabric.mod.json") {
        expand(
            "version" to modVersion,
            "minecraft" to target,
            "flk" to flkVersion,
            "malilib" to malilibVersion,
            "litematica" to litematicaVersion
        )
    }
}

yamlang {
    targetSourceSets = listOf(sourceSets.main.get())
    inputDir = "assets/$modId/lang"
}