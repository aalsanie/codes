import java.util.Properties

plugins {
    java
}

val properties = Properties().apply {
    rootProject.projectDir.parentFile.resolve("gradle.properties").inputStream().use { load(it) }
}
val codesVersion = properties.getProperty("VERSION_NAME")
val defaultSpringVersion = properties.getProperty("springFrameworkVersion")
val springVersion = providers.gradleProperty("springFrameworkOverride").orElse(defaultSpringVersion)
val consumerJavaVersion = providers.gradleProperty("consumerJavaVersion")
    .orElse("17")
    .map(String::toInt)

dependencies {
    implementation("io.github.aalsanie:codes:$codesVersion")
    implementation("io.github.aalsanie:codes-spring:$codesVersion")
    implementation("org.springframework:spring-web:${springVersion.get()}")
}

java {
    toolchain {
        languageVersion.set(consumerJavaVersion.map(JavaLanguageVersion::of))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(consumerJavaVersion)
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
