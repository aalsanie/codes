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
final class PaymentOutcomes {
    static final OutcomeDefinition PAYMENT_DECLINED = OutcomeDefinition.custom(
        "com.example.payments",
        "PAYMENT_DECLINED",
        OutcomeState.FAILED,
        "The payment was declined."
    );

    private PaymentOutcomes() {
    }
}
```

For `ErrorInfo.reason`, the outcome name must satisfy the Google RPC reason contract: at most 63 characters and UPPER_SNAKE_CASE without a trailing underscore. Codes rejects a lossy mapping rather than changing the identity.

## 3. Configure the gRPC mapping

```java
GrpcOutcomeMapper grpc = GrpcOutcomeMapper.standard()
    .withMapping(
        PaymentOutcomes.PAYMENT_DECLINED,
        GrpcStatusCode.FAILED_PRECONDITION
    );

GoogleRpcOutcomeMapper errors = new GoogleRpcOutcomeMapper(
    grpc,
    GrpcOutcomeExposure.publicErrors()
);
```

`publicErrors()` exposes the reusable message and structured issues. It does not expose occurrence `detail`.

## 4. Send the error through the existing service

```java
Outcome outcome = Outcome.of(
    PaymentOutcomes.PAYMENT_DECLINED,
    null,
    List.of(Issue.at("paymentMethod", "Payment method is unavailable."))
);

StatusRuntimeException error = GrpcOutcomeExceptions
    .toStatusRuntimeException(outcome, errors)
    .orNull();

if (error == null) {
    throw new IllegalStateException("PAYMENT_DECLINED has no gRPC mapping");
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

assert identity.getDomain().equals("com.example.payments");
assert identity.getReason().equals("PAYMENT_DECLINED");
```

Structured issues are carried in `google.rpc.BadRequest`. Occurrence detail is carried only when `exposeDetail` is explicitly enabled.

The application identity therefore survives:

```text
com.example.payments:PAYMENT_DECLINED
    -> gRPC FAILED_PRECONDITION
    -> ErrorInfo.domain  = com.example.payments
    -> ErrorInfo.reason  = PAYMENT_DECLINED
```
