import java.util.Properties

plugins {
    java
}

val codesVersion = Properties().run {
    rootProject.projectDir.parentFile.resolve("gradle.properties").inputStream().use { load(it) }
    getProperty("VERSION_NAME") ?: error("VERSION_NAME is missing from gradle.properties")
}

val consumerJavaVersion = providers.gradleProperty("consumerJavaVersion")
    .orElse("17")
    .map(String::toInt)

val springFrameworkOverride = providers.gradleProperty("springFrameworkOverride").orNull
val grpcVersionOverride = providers.gradleProperty("grpcVersionOverride").orNull

dependencies {
    implementation("io.github.aalsanie:codes:$codesVersion")
    implementation("io.github.aalsanie:codes-spring:$codesVersion")
    implementation("io.github.aalsanie:codes-grpc-java:$codesVersion")
}

configurations.configureEach {
    resolutionStrategy.eachDependency {
        if (springFrameworkOverride != null && requested.group == "org.springframework") {
            useVersion(springFrameworkOverride)
            because("Codes Spring compatibility matrix")
        }
        if (grpcVersionOverride != null && requested.group == "io.grpc") {
            useVersion(grpcVersionOverride)
            because("Codes gRPC compatibility matrix")
        }
    }
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
