@file:Suppress("VulnerableLibrariesLocal", "AvoidDuplicateDependencies")

plugins {
    alias(conventions.plugins.repositories)
    alias(conventions.plugins.minecraft)
    alias(conventions.plugins.publish)
    alias(conventions.plugins.shadow)
    alias(conventions.plugins.jvmdg)
    alias(conventions.plugins.idea)
    alias(conventions.plugins.test)
    alias(conventions.plugins.jvm)
}

repositories {
    maven {
        name = "tterrag Maven"
        url = uri("https://maven.tterrag.com/")
    }
    exclusiveContent {
        forRepository {
            maven {
                name = "Red Studio"
                url = uri("https://repo.redstudio.dev/beta")
            }
        }
        filter {
            includeGroup("dev.redstudio")
        }
    }
    maven {
        name = "JitPack"
        url = uri("https://www.jitpack.io")
    }
}

configurations {
    compileOnly {
        // exclude GNU trove, FastUtil is superior and still updated
        exclude(group = "net.sf.trove4j", module = "trove4j")
        // exclude javax.annotation from findbugs, JetBrains annotations are superior
        exclude(group = "com.google.code.findbugs", module = "jsr305")
        // exclude scala as we don't use it for anything and causes import confusion
        exclude(group = "org.scala-lang")
        exclude(group = "org.scala-lang.modules")
        exclude(group = "org.scala-lang.plugins")
    }
}

dependencies {
    compileOnlyApi(deps.jspecify)
    compileOnlyApi(deps.annotations)

    // Lombok
    compileOnly(deps.lombok)
    annotationProcessor(deps.lombok)
    testCompileOnly(deps.lombok)
    testAnnotationProcessor(deps.lombok)

    implementation(deps.hei)
    runtimeOnly(deps.theOneProbe)

    // Mod dependencies
    api(deps.codeChickenLib) { isTransitive = false }
    implementation(deps.gregtech) { isTransitive = false }
    compileOnly(deps.gregicalityMultiblocks) { isTransitive = false }

    // Transitive GregTech dependencies
    compileOnly(deps.craftTweaker)
    compileOnly(deps.ae2Uel) { isTransitive = false }
    compileOnly(deps.groovyscript) { isTransitive = false }

    compileOnlyApi(deps.configanytime)
    compileOnlyApi(deps.modularui) { isTransitive = false }

    // Optional dependencies. Uncomment the ones you need
//    runtimeOnly(deps.ctm) { isTransitive = false }
//    runtimeOnly(rfg.deobf("curse.maven:supercritical-1185871:6793777"))
//    runtimeOnly(deps.fluidloggedApi3)
//    runtimeOnly(deps.fluidloggedApi2)
//    runtimeOnly(rfg.deobf("curse.maven:lolasm-460609:6333774"))
//    runtimeOnly(deps.flare)
//    runtimeOnly(deps.vintagefix)
//    runtimeOnly(deps.alfheimLightingEngine)
//    runtimeOnly(deps.redCoreMc)
//    runtimeOnly(deps.zbgt)
//    runtimeOnly(deps.mcjtylibRefilmed)
//    runtimeOnly(deps.refinedtools)
//    runtimeOnly(rfg.deobf("curse.maven:tool-belt-260262:3459767"))
//    runtimeOnly(rfg.deobf("curse.maven:baubles-lts-655747:3916343"))
//    runtimeOnly(deps.groovyscript) { isTransitive = false }
//    runtimeOnly(deps.xaerosWorldMap)
//    runtimeOnly(deps.xaerosMinimap)

    // OptiFine
    // Copied from GTCEu, originally used to download latest Vintagium from GitHub
    // Using Gradle's Ant integration seems to be the least hacky way to download an arbitrary file without a plugin
//    file("libs/optifine").mkdirs()
//    ant.withGroovyBuilder {
//        "get"("src" to "https://github.com/OpenCubicChunks/OptiFineDevTweaker/releases/download/2.6.15/aa_do_not_rename_OptiFineDevTweaker-2.6.15-all.jar",
//              "dest" to "libs/optifine/",
//              "skipexisting" to "true")
//        "get"("src" to "https://github.com/SynArchive/OptiFine-Archive/raw/refs/heads/main/1.12.2/preview_OptiFine_1.12.2_HD_U_G6_pre1.jar",
//              "dest" to "libs/optifine/",
//              "skipexisting" to "true")
//    }
//    runtimeOnly(fileTree("libs/optifine"))
}
