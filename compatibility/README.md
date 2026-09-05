# Semantic compatibility fixtures

These snapshots protect Codes' semantic and boundary contracts independently from the Java API snapshots under `api/`.

They cover:

* standard outcome codes and states;
* built-in HTTP mappings;
* built-in gRPC mappings;
* Spring RFC 9457 problem-detail output.

Core semantic snapshots do not freeze human-readable messages, occurrence details, or issue paths.

`spring-http-problems.snapshot` is intentionally a boundary golden fixture. It verifies the RFC field roles and exposure policy for safe, public, custom, validation, unmapped, and sensitive-detail cases. A change to that fixture is therefore a reviewed Spring wire-contract change.

A snapshot change represents a contract change and must be reviewed together with the code that requires it.

For HTTP, absence from `http-mappings.snapshot` means `HttpOutcomeMapper.standard()` returns `Unmapped`.
