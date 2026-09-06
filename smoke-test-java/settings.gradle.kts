pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal {
            content {
                includeGroup("io.github.aalsanie")
            }
        }
        mavenCentral {
            content {
                excludeGroup("io.github.aalsanie")
            }
        }
    }
}

rootProject.name = "codes-smoke-java"
