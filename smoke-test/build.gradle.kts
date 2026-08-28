import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.4.10"
}

val codesVersion =
    Properties().run {
        rootProject.projectDir
            .parentFile
            .resolve("gradle.properties")
            .inputStream()
            .use { load(it) }

        getProperty("VERSION_NAME")
            ?: error("VERSION_NAME is missing from gradle.properties")
    }

dependencies {
    implementation("io.github.aalsanie:codes:$codesVersion")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        allWarningsAsErrors.set(true)
        freeCompilerArgs.add("-Xjdk-release=17")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Werror"))
}

val kotlinSmoke by tasks.registering(JavaExec::class) {
    dependsOn(tasks.named("classes"))
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("SmokeKotlinKt")
}

val javaSmoke by tasks.registering(JavaExec::class) {
    dependsOn(tasks.named("classes"))
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("SmokeJava")
}

tasks.named("check") {
    dependsOn(kotlinSmoke, javaSmoke)
}
