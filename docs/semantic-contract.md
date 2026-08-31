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

Applications keep their own domain error or result model and use Codes where a shared outcome identity is useful.

## Identity

`OutcomeCode` is the machine identity of an outcome.

```text
com.example.payments:PAYMENT_DECLINED
```

The namespace and name form the identity used for registry lookup, mappings, structured logging, observability, and other machine-readable uses.

Human-readable messages are not identifiers and must not be used for branching or matching.

## Lifecycle state

### `SUCCEEDED`

The modeled operation reached a successful terminal outcome.

Transport status, response shape, resource creation, and follow-up work are separate concerns.

### `PENDING`

The modeled operation has not reached a terminal outcome.

Applications define pending outcomes when that distinction is meaningful to the operation.

### `FAILED`

The modeled operation ended without producing a successful result for the caller.

`FAILED` is not a transactional guarantee. External state may already have changed before a timeout, cancellation, or other failure was observed.

## Definition and occurrence

`OutcomeDefinition` contains:

```text
code
state
defaultMessage
```

`Outcome` contains:

```text
definition
detail
issues
```

`detail` and `issues` belong to one occurrence. `Outcome.toString()` and `OutcomeException.getMessage()` do not include occurrence `detail`.

## Standard outcome catalog

An outcome belongs in the standard catalog when its meaning:

* is independent of a specific protocol, framework, or serialization format;
* applies across unrelated application domains;
* provides a useful distinction to callers, mapping policy, or observability;
* represents a distinct application outcome rather than only a transport representation.

| Outcome | State | Meaning |
|---|---|---|
| `OK` | `SUCCEEDED` | The operation completed successfully. |
| `INVALID_ARGUMENT` | `FAILED` | One or more arguments are invalid independent of current application state. |
| `UNAUTHENTICATED` | `FAILED` | The operation requires an authenticated identity and no valid identity is available. |
| `PERMISSION_DENIED` | `FAILED` | The caller is not permitted to perform the operation. |
| `NOT_FOUND` | `FAILED` | A required or requested application resource or entity cannot be found. |
| `ALREADY_EXISTS` | `FAILED` | The operation conflicts with a resource or entity that already exists. |
| `FAILED_PRECONDITION` | `FAILED` | Required application state or a precondition is not satisfied. |
| `OUT_OF_RANGE` | `FAILED` | A supplied value is outside the valid range for the operation. |
| `RATE_LIMITED` | `FAILED` | A caller or workload exceeded an allowed operation rate. |
| `CANCELLED` | `FAILED` | The operation was cancelled before a successful result was produced. |
| `DEADLINE_EXCEEDED` | `FAILED` | The operation did not produce a successful result before its deadline. |
| `ABORTED` | `FAILED` | The operation was aborted because of a conflict or coordination condition. |
| `UNIMPLEMENTED` | `FAILED` | The requested operation or capability is not implemented or supported. |
| `UNAVAILABLE` | `FAILED` | A required capability is temporarily unavailable. |
| `INTERNAL` | `FAILED` | An internal implementation or invariant failure prevented a successful result. |
| `DATA_LOSS` | `FAILED` | Unrecoverable data loss or corruption was detected. |
| `RESOURCE_EXHAUSTED` | `FAILED` | A required capacity, quota, or other resource limit was exhausted. |

Applications define more specific outcomes where the distinction matters.

## Structured issues

`Issue` contains an optional code, optional application-defined path, and required message. Issue path syntax is owned by the application.

## Validation

`ValidationResult` aggregates independent issues and can convert them to an outcome. Conversion requires the failure definition explicitly.

```java
validation.toOutcome(StandardOutcomes.INVALID_ARGUMENT);
```

A valid result produces `StandardOutcomes.OK`. An invalid result produces the supplied failed definition. The supplied definition is validated even for a valid result so invalid conversion policy fails immediately.

`ValidationResult` is a convenience type. It is not the application result model and does not define control flow.

## Protocol mappings

`OutcomeMapper<T>` maps an application outcome definition to a boundary representation.

```text
NOT_FOUND -> HTTP 404
NOT_FOUND -> gRPC NOT_FOUND
```

Different outcomes can map to the same boundary value while keeping distinct application identities.

`MappingResult.Unmapped` means the mapper has no policy for the outcome. Applications can add or override mappings explicitly.

## Namespace contract

Custom codes use `namespace:NAME`.

Namespaces:

* contain lowercase ASCII letters, digits, and hyphens;
* use dot-separated segments;
* start each segment with a lowercase ASCII letter;
* end each segment with a lowercase ASCII letter or digit.

Names use uppercase ASCII letters, digits, and underscores.

`io.github.aalsanie.codes` and its child namespaces are reserved. Applications should use namespaces they control. Syntax validation does not verify namespace ownership.

## Compatibility

The machine-readable semantic contract includes:

* `OutcomeCode` identity;
* standard outcome membership;
* standard `OutcomeState` assignments;
* built-in HTTP mappings;
* built-in gRPC mappings.

Human-readable text may evolve without changing outcome identity:

* `OutcomeDefinition.defaultMessage`;
* `Outcome.detail`;
* `Issue.message`.

Consumers must use `OutcomeCode`, not human-readable text, when behavior depends on identity.

`api/codes.api` protects the public Java API shape. `compatibility/` protects semantic catalog and mapping behavior.
