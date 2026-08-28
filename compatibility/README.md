# Semantic compatibility fixtures

These snapshots protect Codes' semantic contract alongside the JVM API baseline in `api/codes.api`.

They cover:

* standard outcome codes and states;
* built-in HTTP mappings;
* built-in gRPC mappings.

Human-readable messages, occurrence details, and issue paths are not part of these snapshots.

A snapshot change represents a change to the semantic contract and should be reviewed together with the code that requires it.

For HTTP, absence from `http-mappings.snapshot` means `HttpOutcomeMapper.standard()` returns `Unmapped`.
