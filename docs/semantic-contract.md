# Semantic contract

Codes models reusable RFC 9457 problem type definitions.

A `ProblemType` contains:

* a non-empty URI reference returned by `getType()`;
* an HTTP status between 100 and 599;
* a non-blank human-readable title.

The type URI is the stable machine-readable identity of the problem type.

```java
ProblemType ORDER_NOT_FOUND = ProblemType.of(
    URI.create("https://api.example.com/problems/order-not-found"),
    404,
    "Order not found"
);
```

## Definition and occurrence

`ProblemType` represents stable definition metadata.

It does not contain occurrence-specific information such as:

* detail;
* instance;
* request data;
* exception data;
* trace information;
* extension properties.

Those values belong to the individual RFC 9457 problem occurrence.

For Spring applications, `codes-spring` copies the stable definition into a native Spring `ProblemDetail`. Spring and the application remain responsible for the rest of the response lifecycle.

## Identity

Changing a problem type URI changes the public machine identity of the problem.

Changing the Java exception or controller path that produces the problem does not require changing the problem type URI.

The title is a stable human-readable summary. Applications may localize it at the occurrence layer when required.

## Scope

Codes does not provide exception mapping, validation aggregation, registries, workflow states, or protocol-neutral status abstractions.
