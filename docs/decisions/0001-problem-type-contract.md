# ADR 0001: Codes 0.4.0 problem type contract

- Status: Accepted
- Target: 0.4.0
- Scope: Product and public API direction only

## Context

Codes 0.4.0-RC1 models generic application outcomes and maps them to HTTP and gRPC. Research and user feedback showed that this is broader than the problem developers consistently need and makes the library harder to understand.

For Spring applications, the concrete recurring problem is smaller:

- RFC 9457 defines a reusable problem type through stable metadata such as a type URI, title, and HTTP status.
- Spring Framework provides `ProblemDetail` for an individual problem occurrence.
- Applications that want to reuse a problem type across controllers, exception handlers, tests, and documentation still need to define that stable metadata themselves.

Spring Framework issue #37399 tracks the upstream discussion for a reusable representation of this concept.

## Decision

Codes 0.4.0 will be a tiny Java library for reusable RFC 9457 problem type definitions, with an optional Spring bridge for creating native Spring `ProblemDetail` instances.

The core concept is a **problem type**, not an outcome, result, exception, or workflow state.

A problem type represents stable definition metadata:

```text
type URI
title
HTTP status
```

The RFC 9457 `type` URI is the machine-readable identity of the problem type.

The core artifact remains independent of Spring so the definition can be reused from application code, tests, documentation tooling, or other HTTP integrations.

## Definition vs occurrence

Codes owns the reusable definition.

```text
Problem type definition
-----------------------
type
title
status
```

Codes does not own occurrence-specific data.

```text
Problem occurrence
------------------
detail
instance
extension members
request data
exception data
trace or diagnostic data
```

In Spring applications, occurrence data belongs to Spring's `ProblemDetail` and to application error handling.

## Spring boundary

`codes-spring` will be a thin optional bridge from a Codes problem type to Spring Framework's native `ProblemDetail`.

It will not replace or wrap Spring's error handling model.

Spring continues to own:

- `ProblemDetail`
- `ErrorResponse`
- `@ExceptionHandler`
- `@ControllerAdvice`
- MVC and WebFlux rendering
- localization
- extension properties
- request-specific problem details

The bridge must add no hidden exception mapping, global configuration, or framework lifecycle behavior.

## 0.4.0 non-goals

Codes 0.4.0 will not provide:

- generic outcomes or result types
- success, pending, or failure state modeling
- exception hierarchies or exception scanning
- validation aggregation
- error registries
- protocol-neutral status abstractions
- gRPC integration
- Spring Boot auto-configuration
- annotations
- controller advice
- logging, metrics, tracing, or retry policy
- OpenAPI generation
- localization infrastructure
- automatic problem type discovery

Applications remain responsible for their domain errors and exception strategy.

## Removed 0.4.0-RC1 scope

The final 0.4.0 design intentionally does not preserve the RC1 outcome model.

The following concepts are superseded and are candidates for removal in the implementation phases that follow this decision:

- `Outcome`
- `OutcomeCode`
- `OutcomeDefinition`
- `OutcomeState`
- `OutcomeMapper`
- `MappingResult`
- `OutcomeRegistry`
- `OutcomeException` and related helpers
- `Issue`
- `ValidationResult`
- `StandardOutcomes`
- core HTTP and gRPC status abstractions and mappers
- `codes-grpc-java`

RC1 is not a compatibility baseline for the final 0.4.0 API.

## Dependency contract

The core `io.github.aalsanie:codes` artifact must remain dependency-free at runtime and must publish no Maven dependencies.

JSpecify may remain compile-only for nullability metadata.

The Spring integration must stay optional and minimal. It must not pull in Spring Boot or introduce unrelated framework dependencies.

## API design rules

The 0.4.0 implementation must follow these rules:

1. Prefer RFC 9457 terminology over Codes-specific terminology.
2. Keep the public API as small as the problem allows.
3. Do not model occurrence data in core.
4. Do not duplicate Spring types or behavior when Spring already owns them.
5. Do not add a second machine identity when the RFC 9457 `type` URI already identifies the problem type.
6. Keep behavior explicit; no classpath scanning, hidden mappings, or automatic registration.
7. Any new public abstraction must be justified by a concrete problem type use case.

## Release direction

The target is the final `0.4.0` release.

The existing `0.4.0-RC1` represents the superseded outcome-oriented design and will not be used as a strict API promotion baseline for final 0.4.0.

Before release, the repository must verify the new public API, Spring wire behavior, published Maven artifacts, supported Java/Spring/Kotlin consumers, and the zero-dependency core contract.

## Consequence

P02 and later work must be reviewed against this decision.

If an implementation requires reintroducing generic outcomes, gRPC, occurrence modeling, exception frameworks, or other excluded concerns, that work is outside 0.4.0 unless this decision is explicitly revisited first.
