# Compatibility policy

Codes is pre-1.0. Minor releases may contain source or binary breaking changes. Breaking changes are documented in `CHANGELOG.md`.

All published artifacts target Java 17 and support Java and Kotlin consumers.

The `codes` core artifact has no runtime dependencies and publishes no Maven dependencies.

`codes-spring` depends on `codes` and compiles against Spring Web, but it does not publish Spring Framework as a transitive dependency. Applications supply their own supported Spring version.

## 0.4.x compatibility

The `0.4.x` line is verified with:

* Java 17, 21, and 25;
* Spring Framework 6.0.0 and 7.0.9;
* Kotlin 1.9.24 and 2.4.10;
* Gradle and Maven consumers.

Spring Framework 6.0.0 is the compatibility floor. Spring Framework 7.0.9 is the current verification baseline.

These are Codes compatibility statements, not upstream maintenance or security-support declarations.

## Public contract

The machine-readable identity of a `ProblemType` is its RFC 9457 type URI.

The stable definition also contains the associated HTTP status and title.

Occurrence-specific detail, instance, and extension properties are not part of the Codes core contract.
