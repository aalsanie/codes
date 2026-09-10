# Changelog

## Unreleased

## 0.4.0-RC1

### Added

* Added `codes-spring` for mapping failed outcomes to Spring RFC 9457 `ProblemDetail`.
* Added application-owned problem-type URI mappings and explicit Spring exposure policies.
* Added conversion from failed outcomes to Spring `ErrorResponseException`.
* Added `codes-grpc-java` for mapping failed outcomes to `google.rpc.Status` with stable identity in `ErrorInfo`.
* Added conversion to gRPC `StatusRuntimeException` and structured request-field issues through `BadRequest` for `INVALID_ARGUMENT` and `OUT_OF_RANGE`.
* Added Spring and gRPC integration guides.

### Changed

* Set all published artifacts to `0.4.0-RC1`.
* Spring problem details preserve Codes identity in the `code` extension, use the reusable outcome message as the title for explicitly mapped problem types, and keep occurrence detail in RFC `detail`.
* gRPC mappings reject lossy `ErrorInfo` identities and incompatible `BadRequest` issue representations instead of normalizing or coercing them.

## 0.3.1

### Changed

* Restored the published core to zero runtime dependencies.
* Kept JSpecify as compile-time-only nullability metadata.
* Preserved Kotlin nullability semantics across the supported Kotlin compiler matrix.

## 0.3.0

### Changed

* Reimplemented the published core in Java 17.
* Removed the Kotlin standard library from the core runtime dependency graph.
* Added JSpecify nullability metadata for Java and Kotlin consumers.
* Preserved the 17 standard outcomes, `OutcomeState` assignments, and built-in HTTP and gRPC mappings from `0.2.0`.

### Removed

* Removed Kotlin compiler-generated API such as `Companion`, default-argument bridges, and `OutcomeState.entries`.
* Removed the Kotlin `Outcome.toException` extension; use `OutcomeExceptions.toException(...)` or `new OutcomeException(...)`.

### Breaking

`0.3.0` is source and binary incompatible with `0.2.x` for callers that depend on the removed Kotlin-generated API or `Outcome.toException`.
The standard outcome identities, states, and built-in protocol mappings are unchanged from `0.2.0`.

## 0.2.0

### Changed

* Reduced the standard outcome catalog from 21 outcomes to 17.
* `ValidationResult.toOutcome(...)` requires the failure `OutcomeDefinition` explicitly.
* Updated built-in HTTP and gRPC mappings to match the standard catalog.
* Updated default messages for `INVALID_ARGUMENT` and `RATE_LIMITED`.

### Removed

* Removed `StandardOutcomes.CREATED`, `ACCEPTED`, `NO_CONTENT`, and `PAYLOAD_TOO_LARGE`.

## 0.1.0

* Initial public release.
