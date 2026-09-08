# Protocol mappings

Standard mappings provided by `HttpOutcomeMapper.standard()` and `GrpcOutcomeMapper.standard()`.

Mappings convert application outcomes to protocol status. They do not change outcome identity or lifecycle state.

| Outcome | HTTP | gRPC |
|---|---|---|
| `OK` | `200 OK` | `OK` |
| `INVALID_ARGUMENT` | `400 Bad Request` | `INVALID_ARGUMENT` |
| `UNAUTHENTICATED` | `401 Unauthorized` | `UNAUTHENTICATED` |
| `PERMISSION_DENIED` | `403 Forbidden` | `PERMISSION_DENIED` |
| `NOT_FOUND` | `404 Not Found` | `NOT_FOUND` |
| `ALREADY_EXISTS` | `409 Conflict` | `ALREADY_EXISTS` |
| `FAILED_PRECONDITION` | unmapped | `FAILED_PRECONDITION` |
| `OUT_OF_RANGE` | `400 Bad Request` | `OUT_OF_RANGE` |
| `RATE_LIMITED` | `429 Too Many Requests` | `RESOURCE_EXHAUSTED` |
| `CANCELLED` | unmapped | `CANCELLED` |
| `DEADLINE_EXCEEDED` | unmapped | `DEADLINE_EXCEEDED` |
| `ABORTED` | unmapped | `ABORTED` |
| `UNIMPLEMENTED` | `501 Not Implemented` | `UNIMPLEMENTED` |
| `UNAVAILABLE` | `503 Service Unavailable` | `UNAVAILABLE` |
| `INTERNAL` | `500 Internal Server Error` | `INTERNAL` |
| `DATA_LOSS` | `500 Internal Server Error` | `DATA_LOSS` |
| `RESOURCE_EXHAUSTED` | unmapped | `RESOURCE_EXHAUSTED` |

## Why some HTTP mappings are absent

Codes supplies a standard HTTP mapping only when the status is broadly applicable.

`FAILED_PRECONDITION` is not universally HTTP `412 Precondition Failed`; HTTP 412 has conditional-request semantics. `DEADLINE_EXCEEDED` is not universally `504 Gateway Timeout`; HTTP 504 describes a gateway or proxy timing out while waiting for an upstream server.

Applications map such outcomes according to their own HTTP contract.

## Custom mappings

```java
HttpOutcomeMapper mapper = HttpOutcomeMapper.standard()
    .withMapping(paymentDeclined, HttpStatusCode.of(422));
```

Override an existing mapping:

```java
HttpOutcomeMapper mapper = HttpOutcomeMapper.standard()
    .withOverride(StandardOutcomes.NOT_FOUND, HttpStatusCode.of(410));
```

`withMapping` rejects duplicate mappings. `withOverride` rejects outcomes that are not already mapped.

HTTP status constants such as `CREATED`, `ACCEPTED`, `NO_CONTENT`, and `PAYLOAD_TOO_LARGE` remain available for explicit application mappings even though those names are not standard application outcomes.

## gRPC structured issues

`GoogleRpcOutcomeMapper` always preserves the stable Codes identity in `ErrorInfo.domain` and `ErrorInfo.reason` for mapped failures.

When issue exposure is enabled, Codes uses `google.rpc.BadRequest` only for outcomes mapped to gRPC `INVALID_ARGUMENT` or `OUT_OF_RANGE`, matching the standard Google RPC error-detail semantics. Every exposed issue must have a path that the application intends as a request-field path.

If an outcome with issues is mapped to another gRPC status while issue exposure is enabled, the adapter rejects the mapping instead of emitting a misleading `BadRequest`. Pathless issues are rejected for the same reason.

A coded issue can populate `BadRequest.FieldViolation.reason` only when its namespace matches the enclosing outcome namespace, because the reason is scoped by the enclosing `ErrorInfo.domain`.
