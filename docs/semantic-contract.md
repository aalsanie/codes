# Semantic contract

A `ProblemType` is a reusable [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457.html) problem type definition containing:

* an absolute type URI other than `about:blank`;
* an HTTP status from 100 to 599;
* a non-blank human-readable title.

These are Codes validation rules. RFC 9457 also permits relative type URI references, but Codes requires absolute URIs. `about:blank` represents a status-only problem with no additional semantics; use the HTTP framework's native support for it (for example, Spring's `ProblemDetail.forStatus(...)`).

## Definition and occurrence

The type URI is the stable machine-readable identity of the problem. The title summarizes the type and normally remains stable across occurrences, except for localization.

`ProblemType` holds only definition metadata. The `detail`, `instance`, request or exception information, traces, and extension properties belong to each occurrence. For Spring, `codes-spring` copies the definition into a new `ProblemDetail`; the application and Spring own the rest of the response lifecycle.

Changing a type URI changes the public problem identity. Changing the Java exception or controller that produces it does not require a new URI.

## Scope

Codes does not provide exception mapping, validation aggregation, registries, workflow states, or protocol-neutral status abstractions.
