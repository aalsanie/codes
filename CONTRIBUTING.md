# Contributing

Keep changes focused, tested, and justified.

## Contracts

Codes publishes two artifacts:

- `codes`
- `codes-spring`

The core contract is the `ProblemType` value: an absolute RFC 9457 type URI, HTTP status, and non-blank title.

The Spring contract is the explicit conversion from a `ProblemType` to Spring's native `ProblemDetail`.

Public Java API changes require updating the matching snapshot under `api/`.

Changes that affect rendered Spring problem responses require updating the MVC and WebFlux contract tests in the Spring reference application.

The core artifact must remain free of runtime dependencies and publish no Maven dependencies.

`codes-spring` must remain a thin bridge. Its published dependency surface is limited to `codes`; applications provide Spring Web.

Do not introduce auto-configuration, exception mapping, registries, serialization frameworks, or unrelated abstractions as part of a Spring integration change.

## Build

Run the full repository gate.

Windows:

```powershell
.\gradlew.bat clean verifyAll --stacktrace
```

Linux/macOS:

```bash
./gradlew clean verifyAll --stacktrace
```

For publication or dependency changes, also verify the artifacts from a clean isolated Maven repository.

Windows:

```powershell
.\scripts\prepare-compatibility-repository.ps1 `
    -Repository .\build\compatibility-maven
```

Linux/macOS:

```bash
bash scripts/prepare-compatibility-repository.sh build/compatibility-maven
```

CI verifies the supported Java, Kotlin, Spring, Gradle/Maven consumer, and operating-system matrix.

## Pull requests

Keep changes small enough to review and include tests for behavior changes.

Do not mix unrelated cleanup with behavioral changes.

Do not weaken a compatibility, coverage, dependency, publication, or security check just to make CI green.

## AI-assisted contributions

AI-generated work without human understanding is **not** allowed.

Contributions require a human who reviews, understands, and takes responsibility for the work.

If AI was used, the contributor must:

- understand every submitted change
- verify factual and compatibility claims
- run the relevant tests
- be able to explain the design and tradeoffs
- remove generated code that is unnecessary or outside scope
- take responsibility for regressions

The following may be rejected without detailed review:

- unreviewed generated code
- prompt dumps
- fabricated tests, benchmarks, or compatibility claims
- large generated rewrites without a concrete reason
- generated issue or pull-request text containing claims the contributor did not verify
- "the AI said it works" as technical justification
