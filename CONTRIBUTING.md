# Contributing

Keep changes focused, tested, and justified.

## Before changing contracts

Codes has public contracts across three artifacts:

- `codes`
- `codes-spring`
- `codes-grpc-java`

Changes to standard outcome membership, standard outcome state, or built-in HTTP/gRPC mappings change the semantic contract.

Public API changes require updating the matching snapshot under `api/`.

Spring or gRPC wire changes require updating the matching snapshot under `compatibility/`.

Do not update a snapshot just because a test failed. Understand the change and explain why the contract should move.

The core artifact must remain free of runtime dependencies.

Adapters must stay thin and explicit. Do not introduce auto-configuration, serialization frameworks, registries, hidden mapping rules, or unrelated abstractions as part of an adapter change.

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

For publication or dependency changes, also verify all three artifacts from a clean isolated Maven repository.

Windows:

```powershell
.\scripts\prepare-compatibility-repository.ps1 `
    -Repository .\build\compatibility-maven
```

Linux/macOS:

```bash
bash scripts/prepare-compatibility-repository.sh build/compatibility-maven
```

The CI compatibility workflow covers the supported Java, Kotlin, Spring, gRPC, Gradle/Maven consumer, and operating-system matrix.

## Pull requests

Keep pull requests small enough to review.

Include tests for behavior changes.

Explain any:

- public API change
- semantic or wire change
- dependency change
- compatibility change

Do not mix unrelated cleanup with a behavioral change.

Do not weaken a compatibility, coverage, dependency, or publication check just to make CI green.

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
