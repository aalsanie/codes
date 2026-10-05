pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = "codes"

include("codes-spring")
include("reference-spring-orders")
project(":reference-spring-orders").projectDir = file("reference/spring-orders")
