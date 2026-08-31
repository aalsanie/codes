# Compatibility policy

Codes is pre-1.0. Minor releases may contain source or binary breaking changes. Breaking changes are documented in `CHANGELOG.md`.

The published `codes` artifact:

* targets Java 17;
* depends only on `org.jspecify:jspecify` for nullability annotations;
* supports Java and Kotlin consumers.

The following are part of the semantic contract:

* standard outcome codes;
* standard `OutcomeState` assignments;
* built-in HTTP mappings;
* built-in gRPC mappings.

Public Java API compatibility is checked against `api/codes.api`. Semantic compatibility is checked against the snapshots under `compatibility/`.

Human-readable messages are not machine identity and may change without changing `OutcomeCode`.
