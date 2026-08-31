# Observability

`OutcomeCode` is the stable machine-readable dimension in Codes. `OutcomeState` is a bounded lifecycle dimension.

Suitable fields include:

```text
outcome.code=com.example.payments:PAYMENT_DECLINED
outcome.state=FAILED
```

## Logging

```java
logger.info(
    "payment completed outcomeCode={} outcomeState={}",
    outcome.getCode(),
    outcome.getState()
);
```

Log `detail` and issue messages only when the application's data-classification policy permits it. Codes intentionally omits `detail` from `Outcome.toString()`.

## Metrics

A bounded set of outcome codes can be used as metric tags:

```java
registry.counter(
    "application.operations",
    "outcome.code", outcome.getCode().getValue(),
    "outcome.state", outcome.getState().name()
).increment();
```

Do not use `detail`, `Issue.message`, request identifiers, customer identifiers, or other occurrence-specific values as metric labels. They can create unbounded cardinality and can expose sensitive data.

Applications that dynamically create a large number of custom outcome definitions should also avoid using every code as a metric tag. Prefer a bounded catalog.

## Tracing

The same stable fields can be attached to a span:

```java
span.setAttribute("app.outcome.code", outcome.getCode().getValue());
span.setAttribute("app.outcome.state", outcome.getState().name());
```

Codes does not provide an observability adapter. Logging, metrics, and tracing libraries already expose APIs for attaching these values directly, so an adapter would not currently remove meaningful integration code.
