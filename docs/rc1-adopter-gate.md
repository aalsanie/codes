# 0.4.0-RC1 real-adopter gate

The RC is not accepted because it has downloads, stars, or because the Codes repository's own reference applications compile.

The gate uses two independent codebases outside this repository at pinned commits and applies small application patches that consume the published Maven Central artifacts.

## Pilot 1 — Integration Reliability Platform

Repository: `aalsanie/integration-reliability-platform`

Pinned commit: `1f99ad8ec4b0b7da2a20ab20101a6adb677cb735`

Environment:

* Java 21
* Spring Boot 4.1.0
* Gradle
* existing Spring MVC error boundary
* existing Testcontainers integration suite

The pilot replaces one existing handwritten duplicate-event HTTP mapping with `codes-spring`. The existing application remains responsible for deciding that `DuplicateInboundEventException` means `ALREADY_EXISTS`.

Expected identity:

```text
io.github.aalsanie.codes.standard:ALREADY_EXISTS
```

The pilot includes a real MockMvc integration test against the application boundary.

## Pilot 2 — Patient Management Microservices

Repository: `pratham2402/patient-mgmt-microservices`

Pinned commit: `26645990986a4f17b755ec17ed390c9e90112d36`

Environment:

* Java 21
* Spring Boot 3.5.5
* Maven
* REST patient service
* gRPC billing service

This pilot exercises the actual differentiator.

The patient service replaces its handwritten validation `Map<String,String>` response with `codes-spring`. The billing service adds validation through `codes-grpc-java`.

Both boundaries use:

```text
io.github.aalsanie.codes.standard:INVALID_ARGUMENT
```

The HTTP side verifies the RFC 9457 `code` property and structured issues. The gRPC side decodes a real `StatusRuntimeException` and verifies `ErrorInfo` plus `BadRequest`.

The repository currently pins gRPC 1.69.0. Codes declares 1.75.0 as its `0.4.x` compatibility floor, so the pilot upgrades the application's gRPC dependency line and protoc gRPC plugin to 1.75.0. That is recorded as an adopter dependency conflict; it is not hidden with a Codes patch.

## What the automated report records

For each pilot the gate records:

* repository and pinned commit;
* exact patch;
* pass/fail result;
* automated clone + patch + clean build/test elapsed time;
* changed-line statistics;
* manual boundary mapping removed;
* dependency conflicts;
* missing Codes API observed by the successful integration;
* whether any custom Codes library patch was used.

The elapsed time is machine integration time from a clean CI workspace. It is not presented as human coding time.

A successful report may state `Missing API: None observed`. A failed build is not converted into that claim; it fails the gate for investigation.

## What counts as success

Both external repositories must:

1. clone at the pinned commit;
2. accept the stored application patch with `git apply --check`;
3. resolve `0.4.0-RC1` from Maven Central;
4. build and test without a source or binary patch to Codes;
5. verify the intended boundary identity.

Only then is the Step 5 exit gate green.

The patches are reproducible adopter experiments. They are not claims that the upstream maintainers have merged Codes.
