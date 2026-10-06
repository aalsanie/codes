#!/usr/bin/env sh
set -eu

root_dir="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
pom="$root_dir/smoke-test-maven-kotlin/pom.xml"
matrix_file="$root_dir/compatibility/matrix.json"

kotlin_version="${1:?Usage: verify-kotlin-nullability.sh <kotlin-version> [spring-version]}"
codes_version="$(sh "$root_dir/scripts/version.sh")"
repository="${MAVEN_REPO_LOCAL:-$HOME/.m2/repository}"

configured_spring="$(python -c 'import json,sys; m=json.load(open(sys.argv[1])); v=sys.argv[2]; p=m["kotlin"]; assert v in p["versions"], v; print(p["spring"])' "$matrix_file" "$kotlin_version")"
spring_version="${2:-$configured_spring}"

if [ "$spring_version" != "$configured_spring" ]; then
    echo "Kotlin/JSpecify Spring version must match compatibility policy: $configured_spring" >&2
    exit 1
fi

mvn --batch-mode --no-transfer-progress \
    "-Dmaven.repo.local=$repository" \
    -f "$pom" \
    "-Dcodes.version=$codes_version" \
    "-Dkotlin.version=$kotlin_version" \
    "-Dspring.framework.version=$spring_version" \
    clean verify

echo "Kotlin $kotlin_version consumer verified with strict JSpecify against Spring $spring_version."
