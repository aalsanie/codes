# Codes

[![Maven Central](https://img.shields.io/maven-central/v/io.github.aalsanie/codes)](https://central.sonatype.com/artifact/io.github.aalsanie/codes)
[![CI](https://github.com/aalsanie/codes/actions/workflows/ci.yml/badge.svg)](https://github.com/aalsanie/codes/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

Codes is a tiny Java library for reusable RFC 9457 problem type definitions.

Spring provides `ProblemDetail` for an individual error occurrence. Codes provides the stable definition that can be reused across controllers, exception handlers, tests, and documentation.

```java
ProblemType ORDER_NOT_FOUND = ProblemType.of(
    URI.create("https://api.example.com/problems/order-not-found"),
    404,
    "Order not found"
);
```

With Spring:

```java
ProblemDetail problem = ProblemDetails.forType(ORDER_NOT_FOUND);
```

or with occurrence-specific detail:

```java
ProblemDetail problem = ProblemDetails.forTypeAndDetail(
    ORDER_NOT_FOUND,
    "Order o-123 was not found."
);
```

The resulting problem keeps the reusable definition stable:

```json
{
  "type": "https://api.example.com/problems/order-not-found",
  "title": "Order not found",
  "status": 404,
  "detail": "Order o-123 was not found."
}
```

## Install

Core:

```kotlin
dependencies {
    implementation("io.github.aalsanie:codes:0.4.0")
}
```

Spring:

```kotlin
dependencies {
    implementation("io.github.aalsanie:codes-spring:0.4.0")
}
```

All artifacts require Java 17+.

The core artifact has no runtime dependencies and publishes no Maven dependencies. `codes-spring` depends on the core artifact but does not impose a Spring Framework version; the application supplies Spring Web.

## Define a problem catalog

Applications can use the factory for a small number of problem types or implement `ProblemType` directly for a reusable catalog:

```java
enum OrderProblems implements ProblemType {
    ORDER_NOT_FOUND(
        "https://api.example.com/problems/order-not-found",
        404,
        "Order not found"
    ),
    ORDER_ALREADY_CANCELLED(
        "https://api.example.com/problems/order-already-cancelled",
        409,
        "Order already cancelled"
    );

    private final URI type;
    private final int status;
    private final String title;

    OrderProblems(String type, int status, String title) {
        this.type = URI.create(type);
        this.status = status;
        this.title = title;
    }

    public URI getType() {
        return type;
    }

    public int getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }
}
```

Different application exceptions can then reuse the same public problem type:

```java
@ExceptionHandler(OrderNotFoundException.class)
ProblemDetail handleOrderNotFound(OrderNotFoundException ex) {
    return ProblemDetails.forTypeAndDetail(
        OrderProblems.ORDER_NOT_FOUND,
        "Order " + ex.orderId() + " was not found."
    );
}

@ExceptionHandler(ArchivedOrderNotFoundException.class)
ProblemDetail handleArchivedOrderNotFound(ArchivedOrderNotFoundException ex) {
    return ProblemDetails.forTypeAndDetail(
        OrderProblems.ORDER_NOT_FOUND,
        "Archived order " + ex.orderId() + " was not found."
    );
}
```

Codes does not own exception handling, controller advice, localization, extension properties, or request-specific data. Those remain application and Spring concerns.

## Do I need Codes?

If an application only creates one or two `ProblemDetail` instances directly, a local helper may be simpler.

Codes is useful when problem types are part of the API contract and need to be defined once and reused consistently across multiple handlers, modules, tests, or documentation.

## Reference

* [Spring integration](docs/integration-spring.md)
* [Semantic contract](docs/semantic-contract.md)
* [Compatibility policy](docs/compatibility-policy.md)

## License

Apache License 2.0.
