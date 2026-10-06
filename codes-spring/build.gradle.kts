plugins {
    `java-library`
    jacoco
    id("com.vanniktech.maven.publish")
}

group = providers.gradleProperty("GROUP").get()
version = providers.gradleProperty("VERSION_NAME").get()

val artifactId = "codes-spring"
val pomName = "Codes Spring"
val pomDescription = "Spring Framework bridge for Codes RFC 9457 problem types."
val springFrameworkVersion = providers.gradleProperty("springFrameworkOverride")
    .orElse(providers.gradleProperty("springFrameworkVersion"))
val expectedPomDependencies = listOf(
    "${project.group}:codes:${project.version}:compile",
)

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:all,-serial", "-Werror"))
}

dependencies {
    compileOnly("org.jspecify:jspecify:1.0.0")
    api(project(":"))
    compileOnly("org.springframework:spring-web:${springFrameworkVersion.get()}")

    testImplementation(platform("org.junit:junit-bom:${providers.gradleProperty("junitVersion").get()}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.jspecify:jspecify:1.0.0")
    testImplementation("org.springframework:spring-web:${springFrameworkVersion.get()}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

sourceSets.test {
    java.srcDir(rootProject.file("testing/api-snapshot/src/main/java"))
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
    systemProperty("codes.apiSnapshot", rootProject.file("api/codes-spring.api").absolutePath)
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

tasks.register("verifyPublishedPomContract") {
    group = "verification"
    description = "Verifies the Codes Spring POM metadata and dependency budget."

    dependsOn("generatePomFileForMavenPublication")

    val artifactId = artifactId
    val pomName = pomName
    val pomDescription = pomDescription
    val expectedPomDependencies = expectedPomDependencies
    val pomFile = layout.buildDirectory.file("publications/maven/pom-default.xml")
    inputs.file(pomFile)

    doLast {
        val document = javax.xml.parsers.DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse(pomFile.get().asFile)
        val projectElement = document.documentElement

        fun directText(name: String): String = (0 until projectElement.childNodes.length)
            .map(projectElement.childNodes::item)
            .first { it.nodeName == name }
            .textContent

        check(directText("name") == pomName) {
            "Unexpected $artifactId POM name: ${directText("name")}"
        }
        check(directText("description") == pomDescription) {
            "Unexpected $artifactId POM description: ${directText("description")}"
        }

        val dependencies = document.getElementsByTagName("dependency")
        val actual = (0 until dependencies.length).map { index ->
            val dependency = dependencies.item(index) as org.w3c.dom.Element
            fun value(name: String): String = dependency.getElementsByTagName(name).item(0).textContent
            listOf("groupId", "artifactId", "version", "scope").joinToString(":") { value(it) }
        }.sorted()

        check(actual == expectedPomDependencies.sorted()) {
            "$artifactId dependency budget changed. " +
                "Expected ${expectedPomDependencies.sorted()}, found $actual."
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
    dependsOn(tasks.named("verifyPublishedPomContract"))
}

tasks.jar {
    manifest {
        attributes["Automatic-Module-Name"] = "io.github.aalsanie.codes.spring"
    }
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

val signingConfigured = providers.gradleProperty("signingInMemoryKey").isPresent

mavenPublishing {
    coordinates(
        providers.gradleProperty("GROUP").get(),
        artifactId,
        providers.gradleProperty("VERSION_NAME").get(),
    )

    pom {
        name.set(pomName)
        description.set(pomDescription)
    }

    publishToMavenCentral(automaticRelease = true)

    if (signingConfigured) {
        signAllPublications()
    }
}
