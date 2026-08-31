import java.util.Properties

plugins {
    kotlin("jvm") version "2.4.10"
}

val codesVersion = Properties().run {
    rootProject.projectDir.parentFile.resolve("gradle.properties").inputStream().use { load(it) }
    getProperty("VERSION_NAME") ?: error("VERSION_NAME is missing from gradle.properties")
}

dependencies {
    implementation("io.github.aalsanie:codes:$codesVersion")
}

kotlin {
    jvmToolchain(17)
}

val kotlinSmoke by tasks.registering(JavaExec::class) {
    dependsOn(tasks.named("classes"))
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("SmokeKotlinKt")
}

tasks.named("check") {
    dependsOn(kotlinSmoke)
}
