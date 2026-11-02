# Codes

[![Maven Central](https://img.shields.io/maven-central/v/io.github.aalsanie/codes)](https://central.sonatype.com/artifact/io.github.aalsanie/codes) [![CI](https://github.com/aalsanie/codes/actions/workflows/ci.yml/badge.svg)](https://github.com/aalsanie/codes/actions/workflows/ci.yml) [![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

Define reusable [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457.html) problem types in Java. Codes keeps the type, status, and title consistent across error responses, with a dependency-free core and an optional Spring adapter.

## Install

Java 17+. Add one of these to your Gradle `dependencies` block:

Core:

```kotlin
implementation("io.github.aalsanie:codes:0.4.0")
```

Spring (also includes `codes`):

```kotlin
implementation("io.github.aalsanie:codes-spring:0.4.0")
```

Spring applications supply Spring Web; `codes-spring` does not include it transitively. [Core](https://central.sonatype.com/artifact/io.github.aalsanie/codes) · [Spring](https://central.sonatype.com/artifact/io.github.aalsanie/codes-spring) on Maven Central.

## Use

Define a problem type once:

```java
static final ProblemType ORDER_NOT_FOUND = ProblemType.of(
    URI.create("https://api.example.com/problems/order-not-found"),
    404,
    "Order not found"
);
```

With Spring, create a new occurrence with its own detail:

```java
ProblemDetail problem = ProblemDetails.forTypeAndDetail(
    ORDER_NOT_FOUND,
    "Order o-123 was not found."
);
```

`ProblemDetails.forType(...)` creates an occurrence without a detail. Return the result from a Spring exception handler; Spring handles the HTTP response.

Codes requires an absolute type URI other than `about:blank`, an HTTP status from 100 to 599, and a non-blank title. Exception handling and occurrence-specific data remain application concerns.

[Spring integration](docs/integration-spring.md) · [Problem type contract](docs/semantic-contract.md) · [Compatibility](docs/compatibility-policy.md) · [Contributing](CONTRIBUTING.md)

[Apache-2.0](LICENSE).

**NOTE:** If an application only creates one or two `ProblemDetail` instances directly, a local helper may be simpler.
