# Protocol mappings

Standard mappings provided by `HttpOutcomeMapper.standard()` and `GrpcOutcomeMapper.standard()`.

Mappings are explicit projections from application semantics to protocol status. They do not define the identity or state
of an outcome.

| Outcome               | HTTP                        | gRPC                  |
|-----------------------|-----------------------------|-----------------------|
| `OK`                  | `200 OK`                    | `OK`                  |
| `INVALID_ARGUMENT`    | `400 Bad Request`           | `INVALID_ARGUMENT`    |
| `UNAUTHENTICATED`     | `401 Unauthorized`          | `UNAUTHENTICATED`     |
| `PERMISSION_DENIED`   | `403 Forbidden`             | `PERMISSION_DENIED`   |
| `NOT_FOUND`           | `404 Not Found`             | `NOT_FOUND`           |
| `ALREADY_EXISTS`      | `409 Conflict`              | `ALREADY_EXISTS`      |
| `FAILED_PRECONDITION` | unmapped                    | `FAILED_PRECONDITION` |
| `OUT_OF_RANGE`        | `400 Bad Request`           | `OUT_OF_RANGE`        |
| `RATE_LIMITED`        | `429 Too Many Requests`     | `RESOURCE_EXHAUSTED`  |
| `CANCELLED`           | unmapped                    | `CANCELLED`           |
| `DEADLINE_EXCEEDED`   | unmapped                    | `DEADLINE_EXCEEDED`   |
| `ABORTED`             | unmapped                    | `ABORTED`             |
| `UNIMPLEMENTED`       | `501 Not Implemented`       | `UNIMPLEMENTED`       |
| `UNAVAILABLE`         | `503 Service Unavailable`   | `UNAVAILABLE`         |
| `INTERNAL`            | `500 Internal Server Error` | `INTERNAL`            |
| `DATA_LOSS`           | `500 Internal Server Error` | `DATA_LOSS`           |
| `RESOURCE_EXHAUSTED`  | unmapped                    | `RESOURCE_EXHAUSTED`  |

## Why some HTTP mappings are absent

Codes only supplies a standard HTTP mapping when the projection is broadly unambiguous.

For example, `FAILED_PRECONDITION` is not universally HTTP `412 Precondition Failed`: HTTP 412 has specific conditional
request semantics. `DEADLINE_EXCEEDED` is not universally `504 Gateway Timeout`: HTTP 504 specifically describes a
gateway or proxy timing out while waiting for an upstream server.

Applications should map such outcomes according to their own HTTP contract.

## Custom mappings

Add a mapping:

```kotlin
val mapper = HttpOutcomeMapper.standard()
    .withMapping(paymentDeclined, HttpStatusCode.of(422))
```

Override an existing mapping:

```kotlin
val mapper = HttpOutcomeMapper.standard()
    .withOverride(StandardOutcomes.NOT_FOUND, HttpStatusCode.of(410))
```

The same operations are available on `GrpcOutcomeMapper`.

`withMapping` rejects duplicate mappings. `withOverride` rejects outcomes that are not already mapped.

HTTP status constants such as `HttpStatusCode.CREATED`, `ACCEPTED`, `NO_CONTENT`, and `PAYLOAD_TOO_LARGE` remain available
for explicit application mappings even though those names are not standard application outcomes.
