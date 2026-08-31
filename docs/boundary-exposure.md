# Boundary exposure

Codes separates machine identity from human and occurrence-level context. Framework adapters must not assume that every value on an `Outcome` is safe to return to a caller.

## Default policy

| Value | Default boundary behavior | Reason |
|---|---|---|
| `OutcomeCode` | expose | stable machine identifier controlled by the application |
| protocol status | expose | required by the boundary |
| `OutcomeDefinition.defaultMessage` / `Outcome.message` | do not expose automatically | applications may place internal wording in custom definitions |
| `Outcome.detail` | never expose automatically | occurrence context may contain identifiers, diagnostics, or sensitive data |
| `Issue` | do not expose automatically | path and message are application-defined |

`Outcome.toString()` and `OutcomeException.getMessage()` already omit occurrence `detail`.

## Spring adapter

`OutcomeProblemDetailMapper.safeDefaults()` produces an RFC 9457 `ProblemDetail` with:

* mapped HTTP status;
* HTTP reason phrase as title;
* `code` extension containing the `OutcomeCode` value.

It does not include outcome message, occurrence detail, or issues.

Applications can opt into those values with `SpringOutcomeExposure` when their contract defines them as caller-safe.

## grpc-java adapter

`GoogleRpcOutcomeMapper.safeDefaults()` produces a `google.rpc.Status` with:

* mapped canonical gRPC code;
* the machine outcome code as `Status.message`;
* `google.rpc.ErrorInfo` containing `OutcomeCode.name` as `reason` and `OutcomeCode.namespace` as `domain`.

It does not include occurrence detail or issues. Applications can opt into developer message, `DebugInfo`, and `BadRequest.FieldViolation` output with `GrpcOutcomeExposure`.

## Observability

Use `OutcomeCode` and `OutcomeState` as bounded machine dimensions.

Do not use occurrence detail, issue message, request identifier, user identifier, or other unbounded text as metric labels.
