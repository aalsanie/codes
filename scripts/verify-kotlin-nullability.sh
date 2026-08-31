#!/usr/bin/env sh
set -eu

root_dir="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
pom="$root_dir/smoke-test-maven-kotlin/pom.xml"
source_dir="$root_dir/smoke-test-maven-kotlin/src/main/kotlin"

kotlin_version="${1:?Usage: verify-kotlin-nullability.sh <kotlin-version>}"
codes_version="$(sh "$root_dir/scripts/version.sh")"

nullable_probe="$source_dir/NullableReturnProbe.kt"
null_marked_probe="$source_dir/NullMarkedProbe.kt"
output_file="$(mktemp)"

cleanup() {
    rm -f "$nullable_probe" "$null_marked_probe" "$output_file"
}
trap cleanup EXIT HUP INT TERM

mvn --batch-mode --no-transfer-progress \
    -f "$pom" \
    "-Dcodes.version=$codes_version" \
    "-Dkotlin.version=$kotlin_version" \
    clean verify

expect_compile_failure() {
    probe_file="$1"
    : > "$output_file"

    set +e
    mvn --batch-mode --no-transfer-progress --offline \
        -f "$pom" \
        "-Dcodes.version=$codes_version" \
        "-Dkotlin.version=$kotlin_version" \
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

cat > "$nullable_probe" <<'EOF'
import io.github.aalsanie.codes.Outcome
import io.github.aalsanie.codes.StandardOutcomes

fun nullableReturnProbe() {
    val outcome = Outcome.of(StandardOutcomes.NOT_FOUND)
    val detail: String = outcome.detail
    println(detail)
}
EOF

expect_compile_failure "$nullable_probe"

cat > "$null_marked_probe" <<'EOF'
import io.github.aalsanie.codes.Outcome

fun nullMarkedProbe() {
    Outcome.of(null)
}
EOF

expect_compile_failure "$null_marked_probe"

echo "Kotlin $kotlin_version JSpecify nullability contract verified."
