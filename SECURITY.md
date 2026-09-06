# Security

## Reporting a vulnerability

Do **not** disclose a suspected vulnerability in a public GitHub issue, pull request, discussion, or comment.

Use GitHub's private vulnerability reporting / Security Advisories for this repository.

If private reporting is temporarily unavailable, contact the maintainer using the contact information published on the maintainer's GitHub profile and clearly mark the message as a security report.

Do not use a public issue as a fallback.

Please include, when applicable:

- affected Codes version
- affected artifact: `codes`, `codes-spring`, or `codes-grpc-java`
- affected surface: core API, Spring boundary, gRPC boundary, publication, or build tooling
- Java and Kotlin versions
- Spring or gRPC version
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

Pre-release versions may receive a fix through a newer release candidate or the final release rather than a patch to the affected pre-release.

Older release lines are not guaranteed to receive security fixes.

## Boundary data

Codes can carry application-controlled occurrence `detail` and structured `Issue` content.

The Spring and gRPC adapters use conservative defaults and do not expose protected occurrence detail unless the application explicitly opts in.

A case where safe defaults expose protected data is security-relevant and should be reported privately.

Suspected compromise of a published artifact, signature, or release process should also be reported privately.
