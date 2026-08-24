import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.abi.BinariesSource
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("jvm") version "2.4.10"
    id("org.jetbrains.kotlinx.kover") version "0.9.9"
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
    id("com.vanniktech.maven.publish") version "0.37.0"
}

group = providers.gradleProperty("GROUP").get()
version = providers.gradleProperty("VERSION_NAME").get()

kotlin {
    @OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)
    abiValidation {
        binariesSource.set(BinariesSource.MAVEN_PUBLICATIONS)
    }

    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        allWarningsAsErrors.set(true)
        progressiveMode.set(true)
        freeCompilerArgs.add("-Xjdk-release=17")
        jvmDefault.set(JvmDefaultMode.NO_COMPATIBILITY)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Werror"))
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

ktlint {
    version.set("1.8.0")
}

kover {
    reports {
        total {
            html {
                onCheck = true
            }
            xml {
                onCheck = true
            }
            verify {
                onCheck = true
                rule("minimum line coverage") {
                    minBound(95, CoverageUnit.LINE, AggregationType.COVERED_PERCENTAGE)
                }
                rule("minimum branch coverage") {
                    minBound(90, CoverageUnit.BRANCH, AggregationType.COVERED_PERCENTAGE)
                }
            }
        }
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
        providers.gradleProperty("POM_ARTIFACT_ID").get(),
        providers.gradleProperty("VERSION_NAME").get(),
    )
    publishToMavenCentral(automaticRelease = true)
    if (signingConfigured) {
        signAllPublications()
    }
    pom {
        name.set(providers.gradleProperty("POM_NAME"))
        description.set(providers.gradleProperty("POM_DESCRIPTION"))
        inceptionYear.set(providers.gradleProperty("POM_INCEPTION_YEAR"))
        url.set(providers.gradleProperty("POM_URL"))
        licenses {
            license {
                name.set(providers.gradleProperty("POM_LICENSE_NAME"))
                url.set(providers.gradleProperty("POM_LICENSE_URL"))
                distribution.set(providers.gradleProperty("POM_LICENSE_DIST"))
            }
        }
        developers {
            developer {
                id.set(providers.gradleProperty("POM_DEVELOPER_ID"))
                name.set(providers.gradleProperty("POM_DEVELOPER_NAME"))
                url.set(providers.gradleProperty("POM_DEVELOPER_URL"))
            }
        }
        scm {
            url.set(providers.gradleProperty("POM_SCM_URL"))
            connection.set(providers.gradleProperty("POM_SCM_CONNECTION"))
            developerConnection.set(providers.gradleProperty("POM_SCM_DEV_CONNECTION"))
        }
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
