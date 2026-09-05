# Artifact contracts

Codes `0.4.0` has three independently consumable Maven artifacts with one shared version.

| Artifact | Purpose | Direct published dependency budget |
| --- | --- | --- |
| `io.github.aalsanie:codes` | Framework-independent outcome model and protocol mappings | No dependencies |
| `io.github.aalsanie:codes-spring` | Spring HTTP boundary mapping | `codes` and `spring-web`, both compile scope |
| `io.github.aalsanie:codes-grpc-java` | gRPC Java and `google.rpc` boundary mapping | `codes`, `grpc-api`, and `proto-google-common-protos` at compile scope; `grpc-protobuf` at runtime scope |

The adapter budgets are exact, not maximums. A direct dependency addition or scope change must update the relevant enforced POM contract and explain why it belongs in the public artifact.

The core remains framework-independent and dependency-free. JSpecify is compile-only metadata and must not appear in the published core POM or runtime classpath.

Public API snapshots live under `api/`. Semantic fixtures for the core catalog and built-in mappings live under `compatibility/`. Both are reviewed contracts rather than generated files that CI silently updates.
