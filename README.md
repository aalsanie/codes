# Codes

[![Maven Central](https://img.shields.io/maven-central/v/io.github.aalsanie/codes)](https://central.sonatype.com/artifact/io.github.aalsanie/codes)
[![CI](https://github.com/aalsanie/codes/actions/workflows/ci.yml/badge.svg)](https://github.com/aalsanie/codes/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

Typed application outcomes, validation, and explicit HTTP/gRPC mappings for Kotlin and Java.

## Install

Gradle:

```kotlin
dependencies {
    implementation("io.github.aalsanie:codes:0.1.0")
}
```

Maven:

```xml
<dependency>
    <groupId>io.github.aalsanie</groupId>
    <artifactId>codes</artifactId>
    <version>0.1.0</version>
</dependency>
```

Java 17+. Kotlin language/API baseline: 2.2.

## Outcomes

Use a standard outcome definition and add runtime detail when needed:

```kotlin
val outcome = Outcome.of(
    StandardOutcomes.NOT_FOUND,
    detail = "customerId=123",
)

if (outcome.isFailed) {
    println(outcome.code)     // io.github.aalsanie.codes.standard:NOT_FOUND
    println(outcome.message)  // The requested resource was not found.
    println(outcome.detail)   // customerId=123
}
```

`message` is stable and comes from the definition. `detail` is per-occurrence context.

## Validation

```kotlin
val validation = ValidationResult.invalid(
    Issue.at("email", "Invalid email address."),
)

val outcome = validation.toOutcome("Request validation failed")
```

Combine independent validation results when needed:

```kotlin
val result = ValidationResult.combine(emailValidation, nameValidation)
```

A valid result converts to `StandardOutcomes.OK`. An invalid result converts to `StandardOutcomes.INVALID_ARGUMENT` and keeps its issues.

## Custom outcomes

```kotlin
val paymentDeclined = OutcomeDefinition.custom(
    namespace = "com.example.payments",
    name = "PAYMENT_DECLINED",
    state = OutcomeState.FAILED,
    defaultMessage = "The payment was declined.",
)

val outcome = Outcome.of(
    paymentDeclined,
    detail = "issuer declined",
)
```

Custom codes use `namespace:NAME`:

- namespaces are lowercase dot-separated names such as `com.example.payments`
- names use uppercase letters, digits, and underscores such as `PAYMENT_DECLINED`
- `io.github.aalsanie.codes` and its child namespaces are reserved

## HTTP

```kotlin
val mapper = HttpOutcomeMapper.standard()

val status = mapper
    .map(StandardOutcomes.NOT_FOUND)
    .orNull()

check(status == HttpStatusCode.NOT_FOUND)
```

Add a custom mapping without changing the original mapper:

```kotlin
val mapper = HttpOutcomeMapper.standard()
    .withMapping(paymentDeclined, HttpStatusCode.of(422))
```

Some standard outcomes are deliberately left unmapped for HTTP because their HTTP representation is application-specific. See [Protocol mappings](docs/protocol-mappings.md).

## gRPC

```kotlin
val status = GrpcOutcomeMapper.standard()
    .map(StandardOutcomes.NOT_FOUND)
    .orNull()

check(status == GrpcStatusCode.NOT_FOUND)
```

The standard gRPC mapper covers all standard outcomes.

## Java

The same API is directly usable from Java:

```java
Outcome outcome = Outcome.of(
    StandardOutcomes.NOT_FOUND,
    "customer 123"
);

HttpStatusCode status = HttpOutcomeMapper.standard()
    .map(outcome)
    .orNull();
```

## Reference

- [Standard HTTP and gRPC mappings](docs/protocol-mappings.md)

## License

Apache License 2.0.
