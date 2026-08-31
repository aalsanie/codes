# Contributing

## Before changing semantics

Changes to standard outcome membership, standard outcome state, or a built-in HTTP/gRPC mapping change the semantic contract. Update the relevant fixture under `compatibility/` in the same pull request and explain why the semantic change is required.

## Build

```bash
./gradlew clean verifyAll
./gradlew publishToMavenLocal
./gradlew -p smoke-test-java clean check
./gradlew -p smoke-test-kotlin clean check
```

Maven consumer checks:

```bash
version=$(./scripts/version.sh)
mvn -f smoke-test-maven-java/pom.xml -Dcodes.version="$version" package
mvn -f smoke-test-maven-kotlin/pom.xml -Dcodes.version="$version" package
```

## Pull requests

Keep changes focused. Include tests for behavior changes. Public API changes require updating `api/codes.api`; semantic changes require updating the matching compatibility snapshot.

Do not add framework dependencies to the core artifact.
