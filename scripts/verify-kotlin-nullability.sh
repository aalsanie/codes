#!/usr/bin/env sh
set -eu

root_dir="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
pom="$root_dir/smoke-test-maven-kotlin/pom.xml"
source_dir="$root_dir/smoke-test-maven-kotlin/src/main/kotlin"
matrix_file="$root_dir/compatibility/matrix.json"

kotlin_version="${1:?Usage: verify-kotlin-nullability.sh <kotlin-version> [spring-version] [grpc-version]}"
codes_version="$(sh "$root_dir/scripts/version.sh")"
repository="${MAVEN_REPO_LOCAL:-$HOME/.m2/repository}"

configured_versions="$(python - "$matrix_file" "$kotlin_version" <<'PY'
import json
import sys
from pathlib import Path

matrix = json.loads(Path(sys.argv[1]).read_text(encoding="utf-8"))
kotlin_version = sys.argv[2]
policy = matrix["kotlin"]

if kotlin_version not in policy["versions"]:
    raise SystemExit(f"Unsupported Kotlin compatibility version: {kotlin_version}")

print(policy["spring"])
print(policy["grpc"])
PY
)"

configured_spring="$(printf '%s\n' "$configured_versions" | sed -n '1p')"
configured_grpc="$(printf '%s\n' "$configured_versions" | sed -n '2p')"

spring_version="${2:-$configured_spring}"
grpc_version="${3:-$configured_grpc}"

if [ "$spring_version" != "$configured_spring" ]; then
    echo "Kotlin/JSpecify Spring version must match compatibility policy: $configured_spring" >&2
    exit 1
fi

if [ "$grpc_version" != "$configured_grpc" ]; then
    echo "Kotlin/JSpecify gRPC version must match compatibility policy: $configured_grpc" >&2
    exit 1
fi

core_nullable_probe="$source_dir/CoreNullableReturnProbe.kt"
core_null_marked_probe="$source_dir/CoreNullMarkedProbe.kt"
spring_null_marked_probe="$source_dir/SpringNullMarkedProbe.kt"
grpc_null_marked_probe="$source_dir/GrpcNullMarkedProbe.kt"
output_file="$(mktemp)"

cleanup() {
    rm -f \
        "$core_nullable_probe" \
        "$core_null_marked_probe" \
        "$spring_null_marked_probe" \
        "$grpc_null_marked_probe" \
        "$output_file"
}
trap cleanup EXIT HUP INT TERM

mvn --batch-mode --no-transfer-progress \
    "-Dmaven.repo.local=$repository" \
    -f "$pom" \
    "-Dcodes.version=$codes_version" \
    "-Dkotlin.version=$kotlin_version" \
    "-Dspring.framework.version=$spring_version" \
    "-Dgrpc.version=$grpc_version" \
    clean verify

expect_compile_failure() {
    probe_file="$1"
    : > "$output_file"

    set +e
    mvn --batch-mode --no-transfer-progress --offline \
        "-Dmaven.repo.local=$repository" \
        -f "$pom" \
        "-Dcodes.version=$codes_version" \
        "-Dkotlin.version=$kotlin_version" \
        "-Dspring.framework.version=$spring_version" \
        "-Dgrpc.version=$grpc_version" \
        clean compile >"$output_file" 2>&1
    status=$?
    set -e

    cat "$output_file"

    if [ "$status" -eq 0 ]; then
        echo "Expected Kotlin compilation to reject $(basename "$probe_file")." >&2
        exit 1
    fi

    if ! grep -Fq "$(basename "$probe_file")" "$output_file"; then
        echo "Kotlin compilation failed for an unrelated reason while checking $(basename "$probe_file")." >&2
        exit 1
    fi

    rm -f "$probe_file"
}

cat > "$core_nullable_probe" <<'EOF'
import io.github.aalsanie.codes.Outcome
import io.github.aalsanie.codes.StandardOutcomes

fun coreNullableReturnProbe() {
    val outcome = Outcome.of(StandardOutcomes.NOT_FOUND)
    val detail: String = outcome.detail
    println(detail)
}
EOF
expect_compile_failure "$core_nullable_probe"

cat > "$core_null_marked_probe" <<'EOF'
import io.github.aalsanie.codes.Outcome

fun coreNullMarkedProbe() {
    Outcome.of(null)
}
EOF
expect_compile_failure "$core_null_marked_probe"

cat > "$spring_null_marked_probe" <<'EOF'
import io.github.aalsanie.codes.spring.OutcomeProblemDetailMapper

fun springNullMarkedProbe() {
    OutcomeProblemDetailMapper.safeDefaults().map(null)
}
EOF
expect_compile_failure "$spring_null_marked_probe"

cat > "$grpc_null_marked_probe" <<'EOF'
import io.github.aalsanie.codes.grpc.GoogleRpcOutcomeMapper

fun grpcNullMarkedProbe() {
    GoogleRpcOutcomeMapper.safeDefaults().map(null)
}
EOF
expect_compile_failure "$grpc_null_marked_probe"

echo "Kotlin $kotlin_version JSpecify contracts verified for core, Spring, and gRPC against Spring $spring_version and gRPC $grpc_version."
