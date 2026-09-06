# Spring in ten minutes

This example adds Codes at the HTTP boundary only. It does not replace the application's exception or domain model.

## 1. Add the adapter

```kotlin
dependencies {
    implementation("io.github.aalsanie:codes-spring:0.4.0-RC1")
}
```

The adapter supports the declared Spring 6 and Spring 7 compatibility lines.

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

`com.example.payments:PAYMENT_DECLINED` is the identity. HTTP 422 is a boundary decision.

## 3. Configure the HTTP boundary explicitly

```java
HttpOutcomeMapper http = HttpOutcomeMapper.standard()
    .withMapping(PaymentOutcomes.PAYMENT_DECLINED, HttpStatusCode.of(422));

OutcomeProblemDetailMapper problems = new OutcomeProblemDetailMapper(
    new SpringHttpStatusMapper(http),
    SpringOutcomeExposure.publicErrors(),
    SpringProblemTypeUriMapper.empty().withMapping(
        PaymentOutcomes.PAYMENT_DECLINED,
        URI.create("https://api.example.com/problems/payment-declined")
    )
);
```

The URI above belongs to the example application. Codes does not invent or own problem-type URIs.

`publicErrors()` exposes the reusable outcome message and structured issues but not occurrence `detail`. Use `safeDefaults()` when even those fields should remain hidden.

## 4. Use it from the application's existing exception boundary

```java
@RestControllerAdvice
final class PaymentExceptionHandler {
    private final OutcomeProblemDetailMapper problems;

    PaymentExceptionHandler(OutcomeProblemDetailMapper problems) {
        this.problems = problems;
    }

    @ExceptionHandler(PaymentDeclinedException.class)
    ResponseEntity<ProblemDetail> paymentDeclined() {
        Outcome outcome = Outcome.of(PaymentOutcomes.PAYMENT_DECLINED);
        ProblemDetail problem = problems.map(outcome).orNull();

        if (problem == null) {
            throw new IllegalStateException("PAYMENT_DECLINED has no HTTP mapping");
        }

        return ResponseEntity.status(problem.getStatus()).body(problem);
    }
}
```

Codes is not discovering exceptions. The application still decides which exception means which outcome.

## Result

A rendered response is an RFC 9457 problem document with the stable Codes identity:

```json
{
  "type": "https://api.example.com/problems/payment-declined",
  "title": "The payment was declined.",
  "status": 422,
  "code": "com.example.payments:PAYMENT_DECLINED"
}
```

Occurrence detail is absent unless the application explicitly opts into it.

## Validation issues

```java
Outcome invalid = Outcome.of(
    StandardOutcomes.INVALID_ARGUMENT,
    null,
    List.of(
        Issue.at("email", "Invalid email address."),
        Issue.at("quantity", "Must be greater than zero.")
    )
);
```

With `SpringOutcomeExposure.publicErrors()`, those issues are emitted as structured `issues` while `code` remains `io.github.aalsanie.codes.standard:INVALID_ARGUMENT`.

That is the complete integration: stable outcome definition, explicit protocol mapping, explicit exposure policy, existing application boundary.
