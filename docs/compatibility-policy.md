# Compatibility policy

## 0.x releases

Codes is pre-1.0. Minor releases may contain source or binary breaking changes when the change is needed to settle the long-lived API. Every break must be listed in `CHANGELOG.md`.

Semantic changes to standard outcome identity, state, or built-in protocol mappings are reviewed separately from Java API changes.

## Java baseline

The published `codes` artifact:

* targets Java 17 class files;
* has no runtime dependencies;
* is tested on JDK 17 in the primary verification job;
* is tested on later runtime JDKs in CI.

The public API snapshot is stored in `api/codes.api` and checked during `test`.

## Kotlin consumers

The core is Java and contains no Kotlin metadata dependency or Codes-owned Kotlin runtime dependency.

CI compiles a Maven Kotlin consumer against this matrix:

```text
1.9.24
2.0.21
2.1.21
2.2.20
2.4.10
```

A Kotlin version is supported only while that consumer check remains green. The Gradle Kotlin smoke build uses the current compiler separately from the Maven matrix.

## Build tools

The repository verifies published-artifact consumption with:

* Gradle + Java;
* Gradle + Kotlin;
* Maven + Java;
* Maven + Kotlin.

## Semantic compatibility

The following are semantic API:

* standard outcome code membership;
* standard `OutcomeState` assignments;
* built-in HTTP mappings;
* built-in gRPC mappings.

They are stored as snapshots under `compatibility/`.

Human-readable messages are not machine identity and may change without changing `OutcomeCode`.

## 1.0 gate

`1.0.0` requires a settled core model, explicit compatibility policy, passing Java/Kotlin and Maven/Gradle consumer matrices, production-quality Spring and grpc-java adapters, at least one non-maintainer integration with feedback, and two consecutive minor lines without a fundamental core redesign.
