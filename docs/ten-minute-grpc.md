# gRPC Java in ten minutes

This example adds Codes at a gRPC server boundary. It does not replace the service's domain model.

## 1. Add the adapter

```kotlin
dependencies {
    implementation("io.github.aalsanie:codes-grpc-java:0.4.0-RC1")
}
```

Codes `0.4.x` declares gRPC Java 1.75.0 as its compatibility floor.

## 2. Define the stable application outcome

```java
final class CheckoutOutcomes {
    static final OutcomeDefinition CHECKOUT_INVALID = OutcomeDefinition.custom(
        "com.example.checkout",
        "CHECKOUT_INVALID",
        OutcomeState.FAILED,
        "Checkout request is invalid."
    );

    private CheckoutOutcomes() {
    }
}
```

For `ErrorInfo.reason`, the outcome name must satisfy the Google RPC reason contract: at most 63 characters and UPPER_SNAKE_CASE without a trailing underscore. Codes rejects a lossy mapping rather than changing the identity.

## 3. Configure the gRPC mapping

```java
GrpcOutcomeMapper grpc = GrpcOutcomeMapper.standard()
    .withMapping(
        CheckoutOutcomes.CHECKOUT_INVALID,
        GrpcStatusCode.INVALID_ARGUMENT
    );

GoogleRpcOutcomeMapper errors = new GoogleRpcOutcomeMapper(
    grpc,
    GrpcOutcomeExposure.publicErrors()
);
```

`publicErrors()` exposes the reusable message and request-field issues. It does not expose occurrence `detail`.

## 4. Send the error through the existing service

```java
Outcome outcome = Outcome.of(
    CheckoutOutcomes.CHECKOUT_INVALID,
    null,
    List.of(Issue.at("paymentMethod", "Payment method is invalid."))
);

StatusRuntimeException error = GrpcOutcomeExceptions
    .toStatusRuntimeException(outcome, errors)
    .orNull();

if (error == null) {
    throw new IllegalStateException("CHECKOUT_INVALID has no gRPC mapping");
}

responseObserver.onError(error);
```

The client receives a real `google.rpc.Status` in the gRPC trailers.

## Decode it

```java
com.google.rpc.Status status = StatusProto.fromThrowable(error);

ErrorInfo identity = status.getDetailsList().stream()
    .filter(any -> any.is(ErrorInfo.class))
    .findFirst()
    .orElseThrow()
    .unpack(ErrorInfo.class);

assert identity.getDomain().equals("com.example.checkout");
assert identity.getReason().equals("CHECKOUT_INVALID");

BadRequest request = status.getDetailsList().stream()
    .filter(any -> any.is(BadRequest.class))
    .findFirst()
    .orElseThrow()
    .unpack(BadRequest.class);

assert request.getFieldViolations(0).getField().equals("paymentMethod");
```

Structured issues are carried in `google.rpc.BadRequest` only when the mapped gRPC status is `INVALID_ARGUMENT` or `OUT_OF_RANGE`. Every exposed issue must have a path that the application intends as a request-field path. Codes rejects incompatible or pathless issue exposure instead of changing its meaning.

Occurrence detail is carried only when `exposeDetail` is explicitly enabled.

The application identity therefore survives:

```text
com.example.checkout:CHECKOUT_INVALID
    -> gRPC INVALID_ARGUMENT
    -> ErrorInfo.domain  = com.example.checkout
    -> ErrorInfo.reason  = CHECKOUT_INVALID
```

For outcomes such as `FAILED_PRECONDITION`, Codes does not coerce generic `Issue` values into `PreconditionFailure`; that type has different semantics and requires information the core `Issue` model does not claim to contain.
