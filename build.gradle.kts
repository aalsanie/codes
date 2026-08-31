plugins {
    `java-library`
    jacoco
    id("com.vanniktech.maven.publish") version "0.37.0"
}

group = providers.gradleProperty("GROUP").get()
version = providers.gradleProperty("VERSION_NAME").get()

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:all,-serial", "-Werror"))
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:${providers.gradleProperty("junitVersion").get()}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

jacoco {
    toolVersion = "0.8.14"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        html.required.set(true)
        xml.required.set(true)
    }
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)
    violationRules {
        rule {
            limit {
                counter = "LINE"
                minimum = "0.95".toBigDecimal()
            }
            limit {
                counter = "BRANCH"
                minimum = "0.90".toBigDecimal()
            }
        }
    }
}

tasks.register("verifyCoreRuntimeDependencies") {
    group = "verification"
    description = "Fails when the Codes core artifact gains a runtime dependency."
    doLast {
        val firstLevel = configurations.runtimeClasspath.get().resolvedConfiguration.firstLevelModuleDependencies
        check(firstLevel.isEmpty()) {
            "Codes core must remain dependency-free at runtime: ${firstLevel.joinToString { "${it.moduleGroup}:${it.moduleName}:${it.moduleVersion}" }}"
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
    dependsOn(tasks.named("verifyCoreRuntimeDependencies"))
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.jar {
    manifest {
        attributes["Automatic-Module-Name"] = "io.github.aalsanie.codes"
    }
}

tasks.register("verifyAll") {
    group = "verification"
    description = "Verifies the core, incubating adapters, and reference applications."
    dependsOn(
        tasks.check,
        ":codes-spring:check",
        ":codes-grpc-java:check",
        ":reference-spring-orders:check",
        ":reference-grpc-orders:check",
    )
}

val signingConfigured = providers.gradleProperty("signingInMemoryKey").isPresent

mavenPublishing {
    coordinates(
        providers.gradleProperty("GROUP").get(),
        providers.gradleProperty("POM_ARTIFACT_ID").get(),
        providers.gradleProperty("VERSION_NAME").get(),
    )

    publishToMavenCentral(automaticRelease = true)

    if (signingConfigured) {
        signAllPublications()
    }
}

tasks.configureEach {
    if (name.contains("MavenCentral", ignoreCase = true)) {
        doFirst {
            val required = listOf("mavenCentralUsername", "mavenCentralPassword", "signingInMemoryKey")
            val missing = required.filterNot { providers.gradleProperty(it).isPresent }
            require(missing.isEmpty()) {
                "Maven Central publication requires Gradle properties: ${missing.joinToString()}"
            }
        }
    }
}
