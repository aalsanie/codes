# Migrating to 0.4.0

Codes 0.4.0 narrows the library to reusable RFC 9457 problem type definitions.

## From 0.3.x

The outcome model has been removed.

Stay on 0.3.1 if your application needs Codes to model domain outcomes, validation aggregation, registries, or protocol-neutral mappings. Codes 0.4.x is not a replacement for an application's domain result or error model.

Migrate only reusable HTTP problem definitions to `ProblemType`.

For example:

```java
ProblemType ORDER_NOT_FOUND = ProblemType.of(
    URI.create("https://api.example.com/problems/order-not-found"),
    404,
    "Order not found"
);
```

Application exceptions, domain results, validation models, and workflow states remain application-owned.

## From 0.4.0-RC1

The RC1 outcome-oriented Spring and gRPC APIs are superseded.

* `Outcome`, `OutcomeDefinition`, `OutcomeCode`, registries, validation helpers, and status mappers have no 0.4.0 replacement.
* `codes-grpc-java` has no continuation in 0.4.0.
* Migrate only stable public HTTP problem definitions to `ProblemType`.
* Use Spring's native `ProblemDetail`, `ErrorResponse`, `@ExceptionHandler`, and related APIs for occurrence handling.

There is intentionally no mechanical one-to-one migration for most RC1 types because those concerns are no longer owned by Codes.
