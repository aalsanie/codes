# Protocol mappings

Standard mappings provided by `HttpOutcomeMapper.standard()` and `GrpcOutcomeMapper.standard()`.

| Outcome               | HTTP                        | gRPC                  |
|-----------------------|-----------------------------|-----------------------|
| `OK`                  | `200 OK`                    | `OK`                  |
| `CREATED`             | `201 Created`               | `OK`                  |
| `ACCEPTED`            | `202 Accepted`              | `OK`                  |
| `NO_CONTENT`          | `204 No Content`            | `OK`                  |
| `INVALID_ARGUMENT`    | `400 Bad Request`           | `INVALID_ARGUMENT`    |
| `UNAUTHENTICATED`     | `401 Unauthorized`          | `UNAUTHENTICATED`     |
| `PERMISSION_DENIED`   | `403 Forbidden`             | `PERMISSION_DENIED`   |
| `NOT_FOUND`           | `404 Not Found`             | `NOT_FOUND`           |
| `ALREADY_EXISTS`      | `409 Conflict`              | `ALREADY_EXISTS`      |
| `FAILED_PRECONDITION` | unmapped                    | `FAILED_PRECONDITION` |
| `OUT_OF_RANGE`        | `400 Bad Request`           | `OUT_OF_RANGE`        |
| `PAYLOAD_TOO_LARGE`   | `413 Payload Too Large`     | `RESOURCE_EXHAUSTED`  |
| `RATE_LIMITED`        | `429 Too Many Requests`     | `RESOURCE_EXHAUSTED`  |
| `CANCELLED`           | unmapped                    | `CANCELLED`           |
| `DEADLINE_EXCEEDED`   | unmapped                    | `DEADLINE_EXCEEDED`   |
| `ABORTED`             | unmapped                    | `ABORTED`             |
| `UNIMPLEMENTED`       | `501 Not Implemented`       | `UNIMPLEMENTED`       |
| `UNAVAILABLE`         | `503 Service Unavailable`   | `UNAVAILABLE`         |
| `INTERNAL`            | `500 Internal Server Error` | `INTERNAL`            |
| `DATA_LOSS`           | `500 Internal Server Error` | `DATA_LOSS`           |
| `RESOURCE_EXHAUSTED`  | unmapped                    | `RESOURCE_EXHAUSTED`  |

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
