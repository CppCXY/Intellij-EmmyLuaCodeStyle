import de.undercouch.gradle.tasks.download.*

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.7.20"
    id("org.jetbrains.intellij") version "1.13.1"
    id("de.undercouch.download").version("5.3.0")
    kotlin("plugin.serialization") version "1.5.10"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")
}
data class BuildData(
        val ideaSDKShortVersion: String,
        // https://www.jetbrains.com/intellij-repository/releases
        val ideaSDKVersion: String,
        val sinceBuild: String,
        val untilBuild: String,
        val jvmTarget: String = "17",
        val targetCompatibilityLevel: JavaVersion = JavaVersion.VERSION_17,
        // https://github.com/JetBrains/gradle-intellij-plugin/issues/403#issuecomment-542890849
        val instrumentCodeCompilerVersion: String = ideaSDKVersion,
        val type: String = "IC"
)

val buildDataList = listOf(
        BuildData(
                ideaSDKShortVersion = "223",
                ideaSDKVersion = "2022.3",
                sinceBuild = "223",
                untilBuild = "241.*",
        )
)

group = "com.cppcxy"
val emmyluaCodeStyleVersion = "1.5.1"

val emmyluaCodeStyleProjectUrl = "https://github.com/CppCXY/EmmyLuaCodeStyle"

val buildVersion = System.getProperty("IDEA_VER") ?: buildDataList.first().ideaSDKShortVersion

val buildVersionData = buildDataList.find { it.ideaSDKShortVersion == buildVersion }!!

val runnerNumber = System.getenv("RUNNER_NUMBER") ?: "Dev"

version = "${emmyluaCodeStyleVersion}.${runnerNumber}-IDEA${buildVersion}"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    pluginName.set("EmmyLua-CodeStyle")
    version.set(buildVersionData.ideaSDKVersion)
    type.set(buildVersionData.type) // Target IDE Platform
    sandboxDir.set("${project.buildDir}/${buildVersionData.ideaSDKShortVersion}/idea-sandbox")
    plugins.set(listOf("com.tang:1.3.8-IDEA223"))
}

task("download", type = Download::class) {
    src(
        arrayOf(
            "${emmyluaCodeStyleProjectUrl}/releases/download/${emmyluaCodeStyleVersion}/darwin-arm64.tar.gz",
            "${emmyluaCodeStyleProjectUrl}/releases/download/${emmyluaCodeStyleVersion}/darwin-x64.tar.gz",
            "${emmyluaCodeStyleProjectUrl}/releases/download/${emmyluaCodeStyleVersion}/linux-x64.tar.gz",
            "${emmyluaCodeStyleProjectUrl}/releases/download/${emmyluaCodeStyleVersion}/win32-x64.zip",
        )
    )

    dest("temp")
}

task("unzip", type = Copy::class) {
    dependsOn("download")
    from(zipTree("temp/win32-x64.zip")) {
        into("CodeFormat")
    }
    from(tarTree("temp/darwin-x64.tar.gz")) {
        into("CodeFormat")
    }
    from(tarTree("temp/darwin-arm64.tar.gz")) {
        into("CodeFormat")
    }
    from(tarTree("temp/linux-x64.tar.gz")) {
        into("CodeFormat")
    }
    destinationDir = file("temp")
}

task("install", type = Copy::class) {
    dependsOn("unzip")
    from("temp/CodeFormat/win32-x64/bin") {
        include("CodeFormat.exe")
        into("bin/win32-x64/")
    }
    from("temp/CodeFormat/linux-x64/bin") {
        include("CodeFormat")
        into("bin/linux-x64/")
    }
    from("temp/CodeFormat/darwin-x64/bin") {
        include("CodeFormat")
        into("bin/darwin-x64/")
    }
    from("temp/CodeFormat/darwin-arm64/bin") {
        include("CodeFormat")
        into("bin/darwin-arm64/")
    }
    destinationDir = file("src/main/resources/CodeFormat")
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = buildVersionData.jvmTarget
    }

    patchPluginXml {
        sinceBuild.set(buildVersionData.sinceBuild)
        untilBuild.set(buildVersionData.untilBuild)
    }

    instrumentCode {
        compilerVersion.set(buildVersionData.instrumentCodeCompilerVersion)
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("IDEA_PUBLISH_TOKEN"))
    }

    buildPlugin {
        dependsOn("install")
    }

    withType<org.jetbrains.intellij.tasks.PrepareSandboxTask> {
        doLast {
            copy {
                from("src/main/resources/CodeFormat")
                into("$destinationDir/${pluginName.get()}/CodeFormat")
            }
        }
    }
}
