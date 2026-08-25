plugins {
    java
}

val codesVersion =
    providers.gradleProperty("codesVersion")
        .orElse("0.1.0")

dependencies {
    implementation(
        "io.github.aalsanie:codes:${codesVersion.get()}",
    )
}

java {
    toolchain {
        languageVersion.set(
            JavaLanguageVersion.of(17),
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
    options.compilerArgs.addAll(
        listOf(
            "-Xlint:all",
            "-Werror",
        ),
    )
}

val javaSmoke by tasks.registering(JavaExec::class) {
    dependsOn(tasks.named("classes"))

    classpath =
        sourceSets.main.get().runtimeClasspath

    mainClass.set("SmokeJava")
}

tasks.named("check") {
    dependsOn(javaSmoke)
}
