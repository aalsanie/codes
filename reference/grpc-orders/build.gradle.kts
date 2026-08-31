plugins {
    java
    application
    id("com.google.protobuf") version "0.10.0"
}

version = "0.3.0-reference"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Xlint:all,-serial")
}

val grpcVersion = providers.gradleProperty("grpcVersion").get()
val commonProtosVersion = providers.gradleProperty("protoGoogleCommonProtosVersion").get()

dependencies {
    implementation(project(":"))
    implementation("io.grpc:grpc-netty-shaded:$grpcVersion")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("io.grpc:grpc-stub:$grpcVersion")
    implementation("com.google.api.grpc:proto-google-common-protos:$commonProtosVersion")
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")

    testImplementation(platform("org.junit:junit-bom:${providers.gradleProperty("junitVersion").get()}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${providers.gradleProperty("protobufVersion").get()}"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
    }
    generateProtoTasks {
        all().configureEach {
            plugins {
                create("grpc")
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("com.example.orders.grpc.OrdersGrpcApplication")
}
