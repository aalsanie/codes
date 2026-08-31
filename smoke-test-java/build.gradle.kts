import java.util.Properties

plugins {
    java
}

val codesVersion = Properties().run {
    rootProject.projectDir.parentFile.resolve("gradle.properties").inputStream().use { load(it) }
    getProperty("VERSION_NAME") ?: error("VERSION_NAME is missing from gradle.properties")
}

dependencies {
    implementation("io.github.aalsanie:codes:$codesVersion")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
    options.compilerArgs.addAll(listOf("-Xlint:all,-serial", "-Werror"))
}

val javaSmoke by tasks.registering(JavaExec::class) {
    dependsOn(tasks.named("classes"))
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("SmokeJava")
}

tasks.named("check") {
    dependsOn(javaSmoke)
}
