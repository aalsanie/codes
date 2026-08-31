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
include("codes-grpc-java")
include("reference-spring-orders")
project(":reference-spring-orders").projectDir = file("reference/spring-orders")
include("reference-grpc-orders")
project(":reference-grpc-orders").projectDir = file("reference/grpc-orders")
