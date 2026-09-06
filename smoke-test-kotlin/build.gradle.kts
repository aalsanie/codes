import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.4.10"
}

val codesVersion = Properties().run {
    rootProject.projectDir.parentFile.resolve("gradle.properties").inputStream().use { load(it) }
    getProperty("VERSION_NAME") ?: error("VERSION_NAME is missing from gradle.properties")
}

val consumerJavaVersion = providers.gradleProperty("consumerJavaVersion")
    .orElse("17")
    .map(String::toInt)

dependencies {
    implementation("io.github.aalsanie:codes:$codesVersion")
    implementation("io.github.aalsanie:codes-spring:$codesVersion")
    implementation("io.github.aalsanie:codes-grpc-java:$codesVersion")
}

kotlin {
    jvmToolchain(consumerJavaVersion.get())
    compilerOptions {
        jvmTarget.set(consumerJavaVersion.map { JvmTarget.fromTarget(it.toString()) })
        freeCompilerArgs.add("-Xjspecify-annotations=strict")
    }
}

val kotlinSmoke by tasks.registering(JavaExec::class) {
    dependsOn(tasks.named("classes"))
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("SmokeKotlinKt")
}

tasks.named("check") {
    dependsOn(kotlinSmoke)
}
