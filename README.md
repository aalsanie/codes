# Codes

[![Maven Central](https://img.shields.io/maven-central/v/io.github.aalsanie/codes)](https://central.sonatype.com/artifact/io.github.aalsanie/codes)
[![CI](https://github.com/aalsanie/codes/actions/workflows/ci.yml/badge.svg)](https://github.com/aalsanie/codes/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

Codes provides stable application outcome identities and explicit boundary mappings for JVM applications. Applications keep their existing domain result or error model; Codes gives the system one machine identity for an outcome while HTTP and gRPC remain boundary decisions.

```text
com.example.orders:ORDER_NOT_FOUND
                    -> HTTP 404
                    -> gRPC NOT_FOUND
                    -> metric label
                    -> log identity
                    -> test assertion
```

The same `OutcomeCode` can be used wherever application code, observability, and protocol boundaries need to agree on the outcome.

## Install

`0.4.0-RC1` is the current pre-release on Maven Central.

Core:

```kotlin
dependencies {
    implementation("io.github.aalsanie:codes:0.4.0-RC1")
}
```

Spring:

```kotlin
dependencies {
    implementation("io.github.aalsanie:codes-spring:0.4.0-RC1")
}
```

gRPC Java:

```kotlin
dependencies {
    implementation("io.github.aalsanie:codes-grpc-java:0.4.0-RC1")
}
```

All artifacts require Java 17+. The core artifact has zero runtime dependencies. The Spring and gRPC artifacts depend only on the boundary libraries they adapt.

For a boundary-first walkthrough:

* [Spring in ten minutes](docs/ten-minute-spring.md)
* [gRPC Java in ten minutes](docs/ten-minute-grpc.md)

## Custom outcomes

```java
OutcomeDefinition paymentDeclined = OutcomeDefinition.custom(
    "com.example.payments",
    "PAYMENT_DECLINED",
    OutcomeState.FAILED,
    "The payment was declined."
);

Outcome outcome = Outcome.of(paymentDeclined);

HttpOutcomeMapper http = HttpOutcomeMapper.standard()
    .withMapping(paymentDeclined, HttpStatusCode.of(422));

GrpcOutcomeMapper grpc = GrpcOutcomeMapper.standard()
    .withMapping(paymentDeclined, GrpcStatusCode.FAILED_PRECONDITION);
```

`OutcomeCode` is the stable machine identity. Protocol mappings do not change that identity.

## Standard outcomes

```text
OK

INVALID_ARGUMENT
UNAUTHENTICATED
PERMISSION_DENIED
NOT_FOUND
ALREADY_EXISTS
FAILED_PRECONDITION
OUT_OF_RANGE
RATE_LIMITED
CANCELLED
DEADLINE_EXCEEDED
ABORTED
UNIMPLEMENTED
UNAVAILABLE
INTERNAL
DATA_LOSS
RESOURCE_EXHAUSTED
```

`OK` is the standard successful outcome. Applications define domain-specific success, pending, and failure outcomes when the standard catalog does not match the operation.

## Runtime occurrences

```java
Outcome outcome = Outcome.of(
    StandardOutcomes.NOT_FOUND,
    "customerId=123"
);

System.out.println(outcome.getCode());
System.out.println(outcome.getMessage());
System.out.println(outcome.getDetail());
```

`message` comes from the reusable definition. `detail` belongs to one occurrence.

## Structured issues

```java
ValidationResult validation = ValidationResult.invalid(
    Issue.at("email", "Invalid email address.")
);

Outcome outcome = validation.toOutcome(StandardOutcomes.INVALID_ARGUMENT);
```

`ValidationResult` is a small convenience for aggregating issues and converting them into an outcome.

## HTTP

```java
HttpStatusCode status = HttpOutcomeMapper.standard()
    .map(StandardOutcomes.NOT_FOUND)
    .orNull();

assert status == HttpStatusCode.NOT_FOUND;
```

Some standard outcomes are intentionally left unmapped for HTTP when the correct status depends on the application.

## gRPC

```java
GrpcStatusCode status = GrpcOutcomeMapper.standard()
    .map(StandardOutcomes.NOT_FOUND)
    .orNull();

assert status == GrpcStatusCode.NOT_FOUND;
```

The standard gRPC mapper covers all standard outcomes.

## Kotlin

Java getters and static factories are directly usable as Kotlin properties and calls:

```kotlin
val outcome = Outcome.of(StandardOutcomes.NOT_FOUND, "customerId=123")
val status = HttpOutcomeMapper.standard().map(outcome).orNull()

check(outcome.code == StandardOutcomes.NOT_FOUND.code)
check(status?.value == 404)
```

## Reference

* [Semantic contract](docs/semantic-contract.md)
* [HTTP and gRPC mappings](docs/protocol-mappings.md)
* [Compatibility policy](docs/compatibility-policy.md)

## License

Apache License 2.0.
