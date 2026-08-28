# Semantic contract

This document defines the semantics that Codes treats as part of its public contract.

## Core model

Codes provides a common vocabulary for application outcomes:

* `OutcomeCode`: machine-readable identity
* `OutcomeState`: lifecycle state
* `OutcomeDefinition`: reusable outcome definition
* `Outcome`: runtime occurrence
* `Issue`: structured occurrence-level issue
* `OutcomeMapper<T>` and `MappingResult<T>`: boundary mapping
* `OutcomeRegistry`: immutable definition lookup and composition
* the standard outcome catalog
* standard HTTP and gRPC mappings

Applications can keep their existing domain error or result model and use Codes where a shared outcome identity is useful.

## Identity

`OutcomeCode` is the machine identity of an outcome.

Example:

```text
com.example.payments:PAYMENT_DECLINED
```

The namespace and name form the identity used for registry lookup, mappings, structured logging, observability, and other machine-readable uses.

Human-readable messages are not identifiers and should not be used for branching or matching.

## Lifecycle state

`OutcomeState` describes the state of the application operation represented by an outcome.

### `SUCCEEDED`

The modeled operation reached a successful terminal outcome.

Transport status, response shape, resource creation, and any follow-up work are separate concerns.

### `PENDING`

The modeled operation has not reached a terminal outcome.

Applications define pending outcomes when that distinction is meaningful to the operation.

### `FAILED`

The modeled operation ended without producing a successful result for the caller.

`FAILED` describes the observed outcome of the operation. It is not a transactional guarantee: external state may already have changed before a timeout, cancellation, or other failure was observed.

## Definition and occurrence

`OutcomeDefinition` contains the reusable part of an outcome:

```text
code
state
defaultMessage
```

`Outcome` represents one occurrence:

```text
definition
detail
issues
```

`detail` and `issues` carry context for that occurrence.

`Outcome.toString()` and `OutcomeException.message` omit `detail` by default.

## Standard outcome catalog

An outcome belongs in the standard catalog when its meaning:

* is independent of a specific protocol, framework, or serialization format
* applies across unrelated application domains
* provides a useful distinction to callers, mapping policy, or observability
* represents a distinct application outcome rather than only a lifecycle state or transport representation

The standard catalog contains 17 outcomes. `OK` is the standard successful outcome. Pending and domain-specific successful outcomes are defined by applications when needed.

| Outcome               | State       | Meaning                                                                                                               |
|-----------------------|-------------|-----------------------------------------------------------------------------------------------------------------------|
| `OK`                  | `SUCCEEDED` | The operation completed successfully.                                                                                 |
| `INVALID_ARGUMENT`    | `FAILED`    | One or more arguments supplied to the operation are invalid independent of current application state.                 |
| `UNAUTHENTICATED`     | `FAILED`    | The operation requires an authenticated identity and no valid identity is available.                                  |
| `PERMISSION_DENIED`   | `FAILED`    | The caller is not permitted to perform the operation.                                                                 |
| `NOT_FOUND`           | `FAILED`    | A required or requested application resource or entity cannot be found.                                               |
| `ALREADY_EXISTS`      | `FAILED`    | The operation conflicts with a resource or entity that already exists.                                                |
| `FAILED_PRECONDITION` | `FAILED`    | Required application state or a precondition for the operation is not satisfied.                                      |
| `OUT_OF_RANGE`        | `FAILED`    | A supplied value is outside the valid range for the operation.                                                        |
| `RATE_LIMITED`        | `FAILED`    | A caller or workload exceeded an allowed operation rate.                                                              |
| `CANCELLED`           | `FAILED`    | The operation was cancelled before a successful result was produced for the caller.                                   |
| `DEADLINE_EXCEEDED`   | `FAILED`    | The operation did not produce a successful result before its deadline. External side effects may still have occurred. |
| `ABORTED`             | `FAILED`    | The operation was aborted because of a conflict or coordination condition.                                            |
| `UNIMPLEMENTED`       | `FAILED`    | The requested operation or capability is not implemented or supported.                                                |
| `UNAVAILABLE`         | `FAILED`    | A required capability is temporarily unavailable.                                                                     |
| `INTERNAL`            | `FAILED`    | An internal implementation or invariant failure prevented a successful result.                                        |
| `DATA_LOSS`           | `FAILED`    | Unrecoverable data loss or corruption was detected.                                                                   |
| `RESOURCE_EXHAUSTED`  | `FAILED`    | A required capacity, quota, or other resource limit was exhausted.                                                    |

Applications can define more specific outcomes where the distinction matters:

```text
com.example.orders:ORDER_CREATED
com.example.payments:PAYMENT_CAPTURED
com.example.jobs:JOB_PROCESSING
```

## Structured issues

`Issue` contains:

```text
optional code
optional path
message
```

`Issue.path` identifies an application-defined logical location. Its syntax and meaning are defined by the application.

## Validation

`ValidationResult` aggregates validation issues.

Conversion to an outcome requires the failure definition explicitly:

```kotlin
validation.toOutcome(StandardOutcomes.INVALID_ARGUMENT)
```

A valid result produces `StandardOutcomes.OK`.

An invalid result produces the supplied failed definition and carries its issues.

The supplied definition must have `FAILED` state. This is validated for every conversion so an invalid conversion policy is rejected immediately.

## Protocol mappings

`OutcomeMapper<T>` maps an outcome definition to a boundary representation.

For example:

```text
NOT_FOUND -> HTTP 404
NOT_FOUND -> gRPC NOT_FOUND
```

Different outcomes can map to the same boundary value while keeping their own application identity:

```text
RATE_LIMITED       -> gRPC RESOURCE_EXHAUSTED
RESOURCE_EXHAUSTED -> gRPC RESOURCE_EXHAUSTED
```

`MappingResult.Unmapped` represents an outcome for which the mapper has no mapping.

Applications can add or override mappings explicitly.

See [Protocol mappings](protocol-mappings.md) for the built-in HTTP and gRPC mappings.

## Namespace contract

Custom outcome codes use:

```text
namespace:NAME
```

For example:

```text
com.example.payments:PAYMENT_DECLINED
```

Namespaces:

* contain lowercase ASCII letters, digits, and hyphens
* use dot-separated segments
* start each segment with a lowercase ASCII letter

Names use uppercase ASCII letters, digits, and underscores.

`io.github.aalsanie.codes` and its child namespaces are reserved for Codes.

Applications should use namespaces they control. Namespace validation checks syntax only.

## Compatibility

The machine-readable semantic contract includes:

* `OutcomeCode`
* standard outcome membership
* standard `OutcomeState` assignments
* built-in HTTP mappings
* built-in gRPC mappings

Human-readable text can evolve without changing outcome identity:

* `OutcomeDefinition.defaultMessage`
* `Outcome.detail`
* `Issue.message`

Consumers should use `OutcomeCode`, not message text, when behavior depends on outcome identity.

JVM API compatibility and semantic compatibility are checked separately:

* `api/codes.api` protects the public JVM API
* `compatibility/` protects the standard catalog and built-in mappings

A change to the standard catalog, its state assignments, or a built-in mapping is a semantic API change and should be reviewed as such.
