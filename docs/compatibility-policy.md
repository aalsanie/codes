# Compatibility policy

Codes is pre-1.0. Minor releases may contain source or binary breaking changes. Breaking changes are documented in `CHANGELOG.md`.

All published Codes artifacts target Java 17 and support Java and Kotlin consumers.

The published `codes` core artifact has zero runtime dependencies. `codes-spring` and `codes-grpc-java` depend only on the boundary libraries documented in their artifact contracts and verified published POM budgets.

The following are part of the semantic contract:

* standard outcome codes;
* standard `OutcomeState` assignments;
* built-in HTTP mappings;
* built-in gRPC mappings.

Public Java API compatibility is checked independently for:

* `api/codes.api`;
* `api/codes-spring.api`;
* `api/codes-grpc-java.api`.

Boundary wire contracts are checked independently from Java API compatibility:

* rendered Spring RFC 9457 problem responses are frozen by `compatibility/spring-http-problems.snapshot`;
* decoded `google.rpc.Status` payloads are frozen by `compatibility/grpc-google-rpc-status.snapshot`.

The executable compatibility matrix under `compatibility/` covers the supported Spring and gRPC baselines, Java runtimes, Kotlin compilers, Gradle and Maven consumers, and supported CI operating systems. Published POM checks protect the dependency contract of each artifact.

Human-readable messages are not machine identity and may change without changing `OutcomeCode`.
