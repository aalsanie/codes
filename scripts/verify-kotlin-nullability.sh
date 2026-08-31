#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
pom="$root_dir/smoke-test-maven-kotlin/pom.xml"
source_dir="$root_dir/smoke-test-maven-kotlin/src/main/kotlin"

kotlin_version="${1:?Usage: verify-kotlin-nullability.sh <kotlin-version>}"
codes_version="$(sh "$root_dir/scripts/version.sh")"

nullable_probe="$source_dir/NullableReturnProbe.kt"
null_marked_probe="$source_dir/NullMarkedProbe.kt"
output_file="$(mktemp)"

trap 'rm -f "$nullable_probe" "$null_marked_probe" "$output_file"' EXIT

mvn --batch-mode --no-transfer-progress \
    -f "$pom" \
    "-Dcodes.version=$codes_version" \
    "-Dkotlin.version=$kotlin_version" \
    clean verify

cat > "$nullable_probe" <<'EOF'
import io.github.aalsanie.codes.Outcome
import io.github.aalsanie.codes.StandardOutcomes

fun nullableReturnProbe() {
    val outcome = Outcome.of(StandardOutcomes.NOT_FOUND)
    val detail: String = outcome.detail
    println(detail)
}
EOF

cat > "$null_marked_probe" <<'EOF'
import io.github.aalsanie.codes.Outcome

fun nullMarkedProbe() {
    Outcome.of(null)
}
EOF

set +e

mvn --batch-mode --no-transfer-progress --offline \
    -f "$pom" \
    "-Dcodes.version=$codes_version" \
    "-Dkotlin.version=$kotlin_version" \
    clean compile >"$output_file" 2>&1

status=$?

set -e

cat "$output_file"

if [[ $status -eq 0 ]]; then
    echo "Expected Kotlin nullability probes to fail compilation." >&2
    exit 1
fi

grep -Fq "NullableReturnProbe.kt" "$output_file" || {
    echo "Nullable return contract was not rejected by the Kotlin compiler." >&2
    exit 1
}

grep -Fq "NullMarkedProbe.kt" "$output_file" || {
    echo "NullMarked parameter contract was not rejected by the Kotlin compiler." >&2
    exit 1
}

echo "Kotlin $kotlin_version JSpecify nullability contract verified."
