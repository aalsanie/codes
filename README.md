# Codes

[![Maven Central](https://img.shields.io/maven-central/v/io.github.aalsanie/codes)](https://central.sonatype.com/artifact/io.github.aalsanie/codes)
[![CI](https://github.com/aalsanie/codes/actions/workflows/ci.yml/badge.svg)](https://github.com/aalsanie/codes/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

Codes provides stable application outcome identities and explicit boundary mappings for JVM applications. Applications keep their own domain error model and use Codes where multiple parts of a system need to agree on outcome meaning without coupling that meaning to HTTP, gRPC, serialization, or a framework.

## Install

Gradle:

```kotlin
dependencies {
    implementation("io.github.aalsanie:codes:0.2.0")
}
```

Maven:

```xml
<dependency>
    <groupId>io.github.aalsanie</groupId>
    <artifactId>codes</artifactId>
    <version>0.2.0</version>
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

`OutcomeCode` is the stable machine identity. `message` comes from the outcome definition and `detail` is per-occurrence context.

## Standard outcomes

A common outcome catalog:

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

`OK` is the standard successful outcome. Applications can define their own success, pending, or failure outcomes when the standard catalog does not match the operation.

## Validation

```kotlin
val validation = ValidationResult.invalid(
    Issue.at("email", "Invalid email address."),
)

val outcome = validation.toOutcome(
    StandardOutcomes.INVALID_ARGUMENT,
    "Request validation failed",
)
```

Combine independent validation results when needed:

```kotlin
val result = ValidationResult.combine(
    emailValidation,
    nameValidation,
)
```

A valid result converts to `StandardOutcomes.OK`. An invalid result converts to the supplied failed outcome and keeps its issues.

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

* namespaces are lowercase dot-separated names such as `com.example.payments`
* names use uppercase letters, digits, and underscores such as `PAYMENT_DECLINED`
* `io.github.aalsanie.codes` and its child namespaces are reserved

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

Some standard outcomes are left unmapped for HTTP where the correct status depends on the application.

HTTP status codes such as `CREATED`, `ACCEPTED`, `NO_CONTENT`, and `PAYLOAD_TOO_LARGE` remain available for custom mappings.

See [HTTP and gRPC mappings](docs/protocol-mappings.md).

## gRPC

```kotlin
val status = GrpcOutcomeMapper.standard()
    .map(StandardOutcomes.NOT_FOUND)
    .orNull()

check(status == GrpcStatusCode.NOT_FOUND)
```

The standard gRPC mapper covers all standard outcomes.

For example:

```text
RATE_LIMITED        -> RESOURCE_EXHAUSTED
RESOURCE_EXHAUSTED  -> RESOURCE_EXHAUSTED
```

Different application outcomes can map to the same protocol status while keeping their own identity inside the application.

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

ValidationResult validation =
    ValidationResult.invalid(Issue.at("email", "Invalid email address."));

Outcome validationOutcome =
    validation.toOutcome(StandardOutcomes.INVALID_ARGUMENT);
```

## Reference

* [Semantic contract](docs/semantic-contract.md)
* [HTTP and gRPC mappings](docs/protocol-mappings.md)

## License

Apache License 2.0.
