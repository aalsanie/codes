# Compatibility contracts

These fixtures and matrix definitions protect Codes' semantic, boundary, publication, and consumer contracts independently from the Java API snapshots under `api/`.

They cover:

* standard outcome codes and states;
* built-in HTTP mappings;
* built-in gRPC mappings;
* rendered Spring RFC 9457 HTTP problem responses;
* decoded `google.rpc.Status` payloads after `StatusRuntimeException` trailer round trips;
* published-artifact compatibility across supported framework, JDK, language, build-tool, and operating-system combinations.

Core semantic snapshots do not freeze human-readable messages, occurrence details, or issue paths.

`spring-http-problems.snapshot` is intentionally a boundary golden fixture. The same contract is rendered through Spring MVC and Spring WebFlux and verifies HTTP status, `application/problem+json`, problem type semantics, title, occurrence detail, request `instance`, top-level `code` and `issues`, application fallback for unmapped outcomes, and sensitive-detail non-disclosure. JSON member ordering is intentionally not frozen.

A change to that fixture is therefore a reviewed Spring wire-contract change.

`grpc-google-rpc-status.snapshot` is the gRPC adapter wire fixture. It is captured after a `google.rpc.Status` is encoded into a `StatusRuntimeException` and decoded from its trailers again. It verifies exact application identity in `ErrorInfo.domain` and `ErrorInfo.reason`, safe message behavior, explicit detail exposure through `DebugInfo`, and structured issues through `BadRequest`.

A change to that fixture is therefore a reviewed gRPC wire-contract change.

A snapshot change represents a contract change and must be reviewed together with the code that requires it.

## Production compatibility matrix

`compatibility/matrix.json` is the executable support policy for `0.4.x`.

Current matrix:

* Java: 17, 21, 25.
* Spring Framework: 6.0.0 and 7.0.9.
* gRPC Java: 1.75.0 and 1.83.1.
* Kotlin compiler consumers: 1.9.24, 2.0.21, 2.1.21, 2.2.20, 2.4.10.
* Build tools: Gradle and Maven.
* Consumer languages: Java and Kotlin.
* Operating systems: Linux, Windows, macOS.

Spring Framework 6.0.0 is the Codes `0.4.x` binary compatibility floor for the Spring 6 generation. Spring 7.0.9 is the current Spring 7 baseline used to build the adapter. This compatibility floor is not a statement about upstream maintenance or security support for old Spring releases.

Codes explicitly supports gRPC Java 1.75.0 as its `0.4.x` compatibility floor and 1.83.1 as the current build baseline. The minimum is a Codes support policy, not an upstream gRPC LTS declaration.

Framework/JDK compatibility is exercised as a full Cartesian product:

```text
3 Java versions × 2 Spring versions × 2 gRPC versions = 12 published-artifact jobs
```

Every compatibility job starts with an empty Maven repository, publishes `codes`, `codes-spring`, and `codes-grpc-java` into that repository, verifies the published POMs, and then runs an external consumer. Gradle consumer repositories exclude `io.github.aalsanie` from Maven Central so Codes artifacts cannot be accidentally resolved remotely.

For HTTP, absence from `http-mappings.snapshot` means `HttpOutcomeMapper.standard()` returns `Unmapped`.
