import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import kotlin.properties.ReadOnlyProperty

// Mod identity — required, must be present and non-blank.
val Project.modName: String by required()
val Project.modId: String by required()
val Project.modGroup: String by required()
val Project.archiveName: String by required()
val Project.versionDisplayFormat: String by required()

val Project.minecraftVersion: String by required()

val Project.modVersion: String by optionalOrEnv("MOD_VERSION")
val Project.devUserName: String by optional { "Developer" }

val Project.extJavaArgs: String by optional()
val Project.useLwjgl3ify: Boolean by flag(default = true)
val Project.enableHotswap: Boolean by flagOrEnv("HOTSWAP", default = true)

val Project.generateTags: Boolean by flag(default = true)

val Project.accessTransformers: String by optional()
val Project.useMixin: Boolean by flag(default = false)
val Project.mixinPackage: String by requiredIf { useMixin }
val Project.mixinRefmap: String by optional { "mixins.$modId.refmap.json" }

val Project.useCoreMod: Boolean by flag { coreModClass.isNotBlank() }
val Project.coreModClass: String by optional()
val Project.enableCoreModDebug: Boolean by flag(default = false)
val Project.forceLoadAsMod: Boolean by flag(default = false)

val Project.minimizeShadowedDependencies: Boolean by flag(default = true)
val Project.relocateShadowedDependencies: Boolean by flag(default = true)

val Project.separateRunDirectories: Boolean by flag(default = false)

val Project.publishToModrinth: Boolean by flagOrEnv("PUBLISH_TO_MODRINTH")
val Project.modrinthApiKey: String by requiredOrEnvIf("MODRINTH_API_KEY") { publishToModrinth }
val Project.modrinthProjectId: String by requiredIf { publishToModrinth }
val Project.modrinthRelations: String by optional()

val Project.publishToCurseForge: Boolean by flagOrEnv("PUBLISH_TO_CURSEFORGE")
val Project.curseForgeApiKey: String by requiredOrEnvIf("CURSEFORGE_API_KEY") { publishToCurseForge }
val Project.curseForgeProjectId: String by requiredIf { publishToCurseForge }
val Project.curseForgeRelations: String by optional()

val Project.releaseType: String by optionalOrEnv("RELEASE_TYPE", default = "release")
val Project.changelogLocation: String by optionalOrEnv("CHANGELOG_LOCATION", default = "CHANGELOG.md")

val Project.publishToMaven: Boolean by flagOrEnv("PUBLISH_TO_MAVEN")
val Project.customMavenPublishUrl: String by requiredIf { publishToMaven }
val Project.mavenArtifactGroup: String by optional { modGroup.substringBeforeLast(".") }
val Project.artifactGroupId: String by optional { mavenArtifactGroup }
val Project.mavenArtifactId: String by optional { archiveName }
val Project.mavenUser: String by optionalOrEnv("MAVEN_USER")
val Project.mavenPassword: String by optionalOrEnv("MAVEN_PASSWORD")

val Project.enableJUnit: Boolean by flag(default = true)

val Project.jvmdgShadowPath: String by optional { defaultShadowPath }

val Project.deploymentDebug: Boolean by flagOrEnv("DEPLOYMENT_DEBUG")

// Derived properties.
val Project.modPath: String by optional { modGroup.replace('.', '/') }
val Project.defaultShadowPath: String by optional { "${modPath}/shadow" }


// Required property getters, panics if unset
@Suppress("UnstableApiUsage")
private fun requiredIf(predicate: Project.() -> Boolean): ReadOnlyProperty<Project, String> {
    return ReadOnlyProperty { project, property ->
        val name = property.name
        if (project.predicate()) {
            project.gradleProperty(name)
                .filter { it.isNotBlank() }
                .orNull ?: throw GradleException("Required property \"$name\" is missing or blank.")
        } else {
            project.gradleProperty(name).getOrElse("")
        }
    }
}

@Suppress("UnstableApiUsage")
private fun required(): ReadOnlyProperty<Project, String> {
    return ReadOnlyProperty { project, property ->
        val name = property.name
        project.gradleProperty(name)
            .filter { it.isNotBlank() }
            .orNull ?: throw GradleException("Required property \"$name\" is missing or blank.")
    }
}

@Suppress("UnstableApiUsage")
private fun requiredOrEnv(envName: String): ReadOnlyProperty<Project, String> {
    return ReadOnlyProperty { project, property ->
        val name = property.name
        project.gradleProperty(name)
            .filter { it.isNotBlank() }
            .orElse(project.env(envName))
            .filter { it.isNotBlank() }
            .orNull
            ?: throw GradleException("Required property \"$name\" or environment variable \"$envName\" is missing or blank.")
    }
}

@Suppress("UnstableApiUsage")
private fun requiredOrEnvIf(envName: String, predicate: Project.() -> Boolean): ReadOnlyProperty<Project, String> {
    return ReadOnlyProperty { project, property ->
        val name = property.name
        if (project.predicate()) {
            project.gradleProperty(name)
                .filter { it.isNotBlank() }
                .orElse(project.env(envName))
                .filter { it.isNotBlank() }
                .orNull
                ?: throw GradleException("Required property \"$name\" or enviroment variable \"$envName\" is missing or blank.")
        } else {
            project.gradleProperty(name)
                .filter { it.isNotBlank() }
                .orElse(project.env(envName))
                .getOrElse("")
        }
    }
}

// Optional property getters, defaults to an empty string
@Suppress("UnstableApiUsage")
private fun optional(default: Project.() -> String): ReadOnlyProperty<Project, String> {
    return ReadOnlyProperty { project, property ->
        project.gradleProperty(property.name)
            .filter { it.isNotBlank() }
            .getOrElse(project.default())
    }
}

private fun optional(default: String = ""): ReadOnlyProperty<Project, String> = optional { default }

@Suppress("UnstableApiUsage")
private fun optionalOrEnv(envName: String, default: Project.() -> String): ReadOnlyProperty<Project, String> {
    return ReadOnlyProperty { project, property ->
        project.gradleProperty(property.name)
            .filter { it.isNotBlank() }
            .orElse(project.env(envName))
            .filter { it.isNotBlank() }
            .getOrElse(project.default())
    }
}

private fun optionalOrEnv(envName: String, default: String = ""): ReadOnlyProperty<Project, String> =
    optionalOrEnv(envName) { default }

// Boolean property getters, defaults to false
private fun flag(default: Project.() -> Boolean): ReadOnlyProperty<Project, Boolean> {
    return ReadOnlyProperty { project, property ->
        val name = property.name
        project.gradleProperty(name)
            .map {
                it.toBooleanStrictOrNull()
                    ?: throw GradleException("Property \"$name\" must be either \"true\" or \"false\" but \"$it\" was found.")
            }.orNull ?: project.default() // Property unset
    }
}

@Suppress("UnstableApiUsage")
private fun flagOrEnv(envName: String, default: Project.() -> Boolean): ReadOnlyProperty<Project, Boolean> {
    return ReadOnlyProperty { project, property ->
        val name = property.name
        project.gradleProperty(name)
            .filter { it.isNotBlank() }
            .orElse(project.env(envName))
            .map {
                it.toBooleanStrictOrNull()
                    ?: throw GradleException("Property \"$name\" must be either \"true\" or \"false\" but \"$it\" was found.")
            }.orNull ?: project.default() // Property unset
    }
}

private fun flagOrEnv(envName: String, default: Boolean = false): ReadOnlyProperty<Project, Boolean> =
    flagOrEnv(envName) { default }

private fun flag(default: Boolean = false): ReadOnlyProperty<Project, Boolean> = flag { default }

private fun Project.env(name: String): Provider<String> = providers.environmentVariable(name)

private fun Project.gradleProperty(name: String): Provider<String> = providers.gradleProperty(name)
