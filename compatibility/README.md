# Semantic compatibility fixtures

These snapshots protect Codes' semantic and boundary contracts independently from the Java API snapshots under `api/`.

They cover:

* standard outcome codes and states;
* built-in HTTP mappings;
* built-in gRPC mappings;
* rendered Spring RFC 9457 HTTP problem responses.

Core semantic snapshots do not freeze human-readable messages, occurrence details, or issue paths.

`spring-http-problems.snapshot` is intentionally a boundary golden fixture. The same contract is rendered through Spring MVC and Spring WebFlux and verifies HTTP status, `application/problem+json`, problem type semantics, title, occurrence detail, request `instance`, top-level `code` and `issues`, application fallback for unmapped outcomes, and sensitive-detail non-disclosure. JSON member ordering is intentionally not frozen.

A change to that fixture is therefore a reviewed Spring wire-contract change.

A snapshot change represents a contract change and must be reviewed together with the code that requires it.

For HTTP, absence from `http-mappings.snapshot` means `HttpOutcomeMapper.standard()` returns `Unmapped`.
