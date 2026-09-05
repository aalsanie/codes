plugins {
    `java-library`
    id("com.vanniktech.maven.publish")
}

group = providers.gradleProperty("GROUP").get()
version = providers.gradleProperty("VERSION_NAME").get()

val artifactId = "codes-spring"
val pomName = "Codes Spring Adapter"
val pomDescription = "Spring Framework HTTP boundary adapter for Codes application outcomes."
val expectedPomDependencies = listOf(
    "${project.group}:codes:${project.version}:compile",
    "org.springframework:spring-web:${providers.gradleProperty("springFrameworkVersion").get()}:compile",
).sorted()

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
    compileOnly("org.jspecify:jspecify:1.0.1")

    api(project(":"))
    api("org.springframework:spring-web:${providers.gradleProperty("springFrameworkVersion").get()}")

    testImplementation(platform("org.junit:junit-bom:${providers.gradleProperty("junitVersion").get()}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.springframework:spring-webmvc:${providers.gradleProperty("springFrameworkVersion").get()}")
    testImplementation("org.springframework:spring-webflux:${providers.gradleProperty("springFrameworkVersion").get()}")
    testImplementation("org.springframework:spring-test:${providers.gradleProperty("springFrameworkVersion").get()}")
    testImplementation("tools.jackson.core:jackson-databind:${providers.gradleProperty("jackson3Version").get()}")
    testImplementation("jakarta.servlet:jakarta.servlet-api:6.1.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    systemProperty(
        "codes.apiSnapshot",
        rootProject.file("api/codes-spring.api").absolutePath,
    )
    systemProperty(
        "codes.springHttpSnapshot",
        rootProject.file("compatibility/spring-http-problems.snapshot").absolutePath,
    )
}

sourceSets.test {
    java.srcDir(rootProject.file("testing/api-snapshot/src/main/java"))
}

tasks.register("verifyPublishedPomContract") {
    group = "verification"
    description = "Verifies the Codes Spring POM metadata and direct dependency budget."

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
            listOf("groupId", "artifactId", "version", "scope")
                .joinToString(":") { value(it) }
        }.sorted()

        check(actual == expectedPomDependencies) {
            "$artifactId direct dependency budget changed. " +
                "Expected $expectedPomDependencies, found $actual."
        }
    }
}

tasks.check {
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
