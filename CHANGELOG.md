# Changelog

## Unreleased

## 0.3.0

### Changed

* Reimplemented the published core in Java 17.
* Removed the Kotlin standard library from the core artifact's runtime dependency graph.
* Replaced the Kotlin-specific ABI baseline with a deterministic public Java API snapshot.
* Kept the 17-outcome standard catalog and its semantic state assignments unchanged from `0.2.0`.
* Kept the built-in HTTP and gRPC mapping tables unchanged from `0.2.0`.
* Kept protocol mapping failures explicit through `MappingResult.Unmapped`.
* Added an explicit boundary-exposure policy for `message`, occurrence `detail`, and `Issue` data.

### Added

* Added Gradle Java and Kotlin published-artifact consumer builds.
* Added Maven Java and Kotlin consumer builds.
* Added a Kotlin compiler compatibility matrix to CI.
* Added a dependency-free core runtime verification task.
* Added Spring and grpc-java reference applications that integrate Codes without adapters.
* Added `codes-spring` and `codes-grpc-java` as `0.4.0-SNAPSHOT` incubating modules built in CI but excluded from the `0.3.0` Maven release.
* Added compatibility, integration, security, contribution, and boundary-exposure documentation.

### Removed

* Removed Kotlin source from the published core.
* Removed the core dependency on `org.jetbrains.kotlin:kotlin-stdlib`.
* Removed Kotlin compiler and Kotlin coverage plugins from the core build.
* Removed Kotlin compiler-generated companion classes and default-argument methods from the public JVM API.
* Removed the Kotlin extension form of `Outcome.toException`; use `OutcomeExceptions.toException(...)` or `new OutcomeException(...)`.

### Breaking

`0.3.0` is source and binary incompatible with `0.2.x` where callers depended on Kotlin compiler-generated JVM members such as `Companion`, Kotlin default-argument bridge methods, `OutcomeState.entries`, or the `Outcome.toException` extension.

The semantic outcome identities, outcome states, standard catalog, and built-in protocol mapping tables remain unchanged from `0.2.0`.

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
