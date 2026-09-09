# Compatibility policy

Codes is pre-1.0. Minor releases may contain source or binary breaking changes. Breaking changes are documented in `CHANGELOG.md`.

All published Codes artifacts target Java 17 and support Java and Kotlin consumers.

The `codes` core artifact has zero runtime dependencies. `codes-spring` and `codes-grpc-java` depend on the boundary libraries they adapt.

## 0.4.x compatibility

The `0.4.x` line is verified with:

* Java 17, 21, and 25;
* Spring Framework 6.0.0 and 7.0.9;
* gRPC Java 1.75.0 and 1.83.1;
* Kotlin 1.9.24, 2.0.21, 2.1.21, 2.2.20, and 2.4.10;
* Gradle and Maven consumers;
* Linux, Windows, and macOS.

Spring Framework 6.0.0 is the Codes compatibility floor for the Spring 6 generation. Spring 7.0.9 is the current Spring 7 verification baseline.

gRPC Java 1.75.0 is the Codes compatibility floor. gRPC Java 1.83.1 is the current verification baseline.

These are Codes compatibility statements, not upstream maintenance or security-support declarations.

## Public contract

The machine-readable contract includes:

* `OutcomeCode` identity;
* standard outcome membership;
* standard `OutcomeState` assignments;
* built-in HTTP mappings;
* built-in gRPC mappings.

The Spring adapter preserves Codes identity in the RFC 9457 `code` extension. The gRPC adapter preserves identity in `google.rpc.ErrorInfo.domain` and `ErrorInfo.reason`.

Human-readable messages are not machine identity and may change without changing `OutcomeCode`.
