# Changelog

## Unreleased

## 0.3.1

### Changed

* Restored the published core to zero runtime dependencies.
* Kept JSpecify as a compile-time-only dependency for nullability metadata.
* Preserved Kotlin nullability semantics across the supported Kotlin compiler matrix.

### Added

* Added publication verification that prevents runtime dependencies from being introduced.
* Added verification that the published Maven POM remains dependency-free.

## 0.3.0

### Changed

* Reimplemented the published core in Java 17.
* Removed the Kotlin standard library from the core dependency graph.
* Added `org.jspecify:jspecify:1.0.0` as the core's only dependency for nullability annotations.
* Replaced Kotlin-specific ABI compatibility with a deterministic public Java API snapshot.
* Preserved the 17 standard outcomes, `OutcomeState` assignments, and built-in HTTP and gRPC mappings from `0.2.0`.

### Added

* Added Gradle and Maven consumer verification for Java and Kotlin.
* Added Kotlin compiler compatibility and JSpecify nullability contract verification.
* Added publication checks for the expected runtime and Maven dependency contract.

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

### Added

* Added semantic compatibility checks for standard outcome codes, states, and built-in mappings.

## 0.1.0

* Initial public release.
