plugins {
    `maven-publish`
    kotlin("jvm") version "1.9.21"
    id("fabric-loom") version "1.5-SNAPSHOT"
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

base {
    archivesName = modId
    version="$modVersion+$mcVersion"
}

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
    fun Dependency?.include() = include(this!!)
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings("net.fabricmc:yarn:${property("deps.yarn")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${property("deps.flk")}+kotlin.$kotlinVersion")

    modImplementation("com.terraformersmc:modmenu:${property("deps.modmenu")}")
    modImplementation("dev.kikugie.worldrenderer:${property("deps.worldrenderer")}:$mcVersion").include()
    modImplementation("dev.kikugie.malilib_extras:${property("deps.malilib_extras")}:$mcVersion").include()

    modImplementation("fi.dy.masa:litematica:${property("deps.litematica")}+$mcVersion")
    testImplementation("net.fabricmc:fabric-loader-junit:${property("deps.fabric_loader")}")

    fun fapiModule(vararg names: String) =
        names.forEach { modImplementation(fabricApi.module("fabric-$it", "${property("deps.fabric_api")}")) }
    fapiModule(
        "command-api-v2",
        "message-api-v1",
        "networking-api-v1",
        "lifecycle-events-v1"
    )
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
    inputs.property("malilib", malilibVersion)
    inputs.property("litematica", litematicaVersion)

    filesMatching("fabric.mod.json") {
        expand(
            "version" to modVersion,
            "minecraft" to target,
            "malilib" to malilibVersion,
            "litematica" to litematicaVersion
        )
    }
}

yamlang {
    targetSourceSets = listOf(sourceSets.main.get())
    inputDir = "assets/$modId/lang"
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName.get()}" }
    }
}