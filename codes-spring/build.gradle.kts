plugins {
    `java-library`
}

group = providers.gradleProperty("GROUP").get()
version = "0.4.0-SNAPSHOT"

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
    api("org.springframework:spring-web:${providers.gradleProperty("springFrameworkVersion").get()}")

    testImplementation(platform("org.junit:junit-bom:${providers.gradleProperty("junitVersion").get()}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes["Automatic-Module-Name"] = "io.github.aalsanie.codes.spring"
    }
}
