plugins {
    `java-library`
    id("com.vanniktech.maven.publish")
}

group = providers.gradleProperty("GROUP").get()
version = providers.gradleProperty("VERSION_NAME").get()

val artifactId = "codes-grpc-java"
val pomName = "Codes gRPC Java Adapter"
val pomDescription = "gRPC Java and google.rpc boundary adapter for Codes application outcomes."
val expectedPomDependencies = listOf(
    "${project.group}:codes:${project.version}:compile",
    "com.google.api.grpc:proto-google-common-protos:${providers.gradleProperty("protoGoogleCommonProtosVersion").get()}:compile",
    "io.grpc:grpc-api:${providers.gradleProperty("grpcVersion").get()}:compile",
    "io.grpc:grpc-protobuf:${providers.gradleProperty("grpcVersion").get()}:runtime",
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
    api(project(":"))
    api("io.grpc:grpc-api:${providers.gradleProperty("grpcVersion").get()}")
    api("com.google.api.grpc:proto-google-common-protos:${providers.gradleProperty("protoGoogleCommonProtosVersion").get()}")
    implementation("io.grpc:grpc-protobuf:${providers.gradleProperty("grpcVersion").get()}")

    testImplementation(platform("org.junit:junit-bom:${providers.gradleProperty("junitVersion").get()}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    systemProperty(
        "codes.apiSnapshot",
        rootProject.file("api/codes-grpc-java.api").absolutePath,
    )
}

sourceSets.test {
    java.srcDir(rootProject.file("testing/api-snapshot/src/main/java"))
}

tasks.register("verifyPublishedPomContract") {
    group = "verification"
    description = "Verifies the Codes gRPC Java POM metadata and direct dependency budget."

    dependsOn("generatePomFileForMavenPublication")

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
            "$artifactId direct dependency budget changed. "
                + "Expected $expectedPomDependencies, found $actual."
        }
    }
}

tasks.check {
    dependsOn(tasks.named("verifyPublishedPomContract"))
}

tasks.jar {
    manifest {
        attributes["Automatic-Module-Name"] = "io.github.aalsanie.codes.grpc"
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
