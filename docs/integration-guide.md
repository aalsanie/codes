# Integration guide

Codes does not require applications to replace their domain result model.

A typical application keeps domain-specific results and converts only the shared outcome identity at a boundary.

```text
domain result
    |
    +-- application payload/state
    |
    +-- Codes Outcome
            |
            +-- HTTP mapping
            +-- gRPC mapping
            +-- logging/metrics identity
```

## HTTP

Start with `HttpOutcomeMapper.standard()` and add only mappings owned by the application's HTTP contract.

Do not force `FAILED_PRECONDITION`, `DEADLINE_EXCEEDED`, `ABORTED`, `CANCELLED`, or `RESOURCE_EXHAUSTED` into a generic HTTP status without an application decision.

The Spring reference application under `reference/spring-orders` performs the mapping manually. It demonstrates:

* custom success `ORDER_CREATED -> 201`;
* pending `ORDER_PROCESSING -> 202`;
* validation issues;
* `NOT_FOUND`;
* `ALREADY_EXISTS`;
* custom domain failure;
* explicit unmapped fallback;
* safe response exposure.

`codes-spring` extracts only the repeated status and RFC 9457 conversion logic from that integration.

## gRPC

Start with `GrpcOutcomeMapper.standard()`. Add explicit mappings for custom application outcomes.

The gRPC reference application under `reference/grpc-orders` converts outcomes manually to:

* canonical gRPC status codes;
* `google.rpc.ErrorInfo` for machine identity;
* `google.rpc.BadRequest` for caller-safe validation issues.

`codes-grpc-java` extracts that repeated conversion without defining a new protobuf error format.

## Domain models

Codes can be used with exceptions, sealed result types, records, Arrow, or plain application objects. Codes does not prescribe control flow.

Use a custom `OutcomeDefinition` when a domain distinction matters to callers, boundary policy, or observability. Do not add a custom outcome only to mirror an HTTP status.
