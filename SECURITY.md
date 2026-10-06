# Security

## Reporting a vulnerability

Do **not** disclose a suspected vulnerability in a public GitHub issue, pull request, discussion, or comment.

Use GitHub's private vulnerability reporting / Security Advisories for this repository.

If private reporting is temporarily unavailable, contact the maintainer using the contact information published on the maintainer's GitHub profile and clearly mark the message as a security report.

Do not use a public issue as a fallback.

Please include, when applicable:

- affected Codes version
- affected artifact: `codes` or `codes-spring`
- affected surface: core API, Spring bridge, publication, or build tooling
- Java and Kotlin versions
- Spring version when `codes-spring` is involved
- Gradle or Maven version
- operating system
- minimal reproduction steps
- impact and required preconditions
- any proof-of-concept material needed to reproduce safely
- any known mitigation

The maintainer will review valid private reports, coordinate remediation, and publish a security advisory when disclosure is appropriate.

Please avoid public disclosure until a fix or coordinated disclosure date is available.

## Supported versions

Security fixes target the latest stable release line.

Older release lines and pre-release versions are not guaranteed to receive security fixes.

## Application-controlled problem data

Codes does not sanitize or redact application-provided problem data.

`ProblemType` stores the type URI, status, and title supplied by the application. `ProblemDetails.forTypeAndDetail(...)` copies the supplied detail into Spring's `ProblemDetail`.

Treat titles and occurrence details as client-visible data. Do not expose exception messages, credentials, tokens, internal identifiers, stack traces, or other sensitive implementation details unless they are explicitly safe for the public API.

A case where Codes changes, corrupts, or unexpectedly exposes data beyond the values supplied through its public API is security-relevant and should be reported privately.

Suspected compromise of a published artifact, signature, dependency metadata, or release process should also be reported privately.
