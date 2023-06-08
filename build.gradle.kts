import de.undercouch.gradle.tasks.download.*

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.7.20"
    id("org.jetbrains.intellij") version "1.13.1"
    id("de.undercouch.download").version("5.3.0")
}

group = "com.cppcxy"
version = "1.1.6"

val emmyluaCodeStyleVersion = "1.1.6"
val emmyluaCodeStyleProjectUrl = "https://github.com/CppCXY/EmmyLuaCodeStyle"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    pluginName.set("EmmyLua-CodeStyle")
    version.set("2023.1")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf("com.tang:1.4.5-IDEA231"))
}

task("download", type = Download::class) {
    src(
        arrayOf(
            "${emmyluaCodeStyleProjectUrl}/releases/download/${emmyluaCodeStyleVersion}/darwin-arm64.zip",
            "${emmyluaCodeStyleProjectUrl}/releases/download/${emmyluaCodeStyleVersion}/darwin-x64.zip",
            "${emmyluaCodeStyleProjectUrl}/releases/download/${emmyluaCodeStyleVersion}/linux-x64.zip",
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
    from(zipTree("temp/darwin-x64.zip")) {
        into("CodeFormat")
    }
    from(zipTree("temp/darwin-arm64.zip")) {
        into("CodeFormat")
    }
    from(zipTree("temp/linux-x64.zip")) {
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
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    patchPluginXml {
        sinceBuild.set("231")
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
