#!/usr/bin/env bash
set -euo pipefail

root_dir="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
manifest="$root_dir/compatibility/external/manifest.json"
version="${1:?usage: verify-rc-integrations.sh <version> [report]}"
report="${2:-$root_dir/build/reports/rc-integrations.md}"
work_dir="${RC_INTEGRATION_WORK_DIR:-$root_dir/build/rc-integrations}"

if [[ ! "$version" =~ ^[0-9]+\.[0-9]+\.[0-9]+-RC[1-9][0-9]*$ ]]; then
    echo "Invalid release-candidate version: $version" >&2
    exit 1
fi

central_base="https://repo.maven.apache.org/maven2/io/github/aalsanie"
for artifact in codes codes-spring codes-grpc-java; do
    url="$central_base/$artifact/$version/$artifact-$version.pom"
    found=false

    for attempt in $(seq 1 40); do
        if curl --silent --show-error --fail --head "$url" >/dev/null 2>&1; then
            found=true
            break
        fi
        sleep 15
    done

    if [[ "$found" != true ]]; then
        echo "Published artifact did not become visible on Maven Central: $url" >&2
        exit 1
    fi
done

rm -rf "$work_dir"
mkdir -p "$work_dir" "$(dirname "$report")"

{
    echo "# Codes $version external integration verification"
    echo
    echo "Generated: $(date -u +"%Y-%m-%dT%H:%M:%SZ")"
    echo
    echo "Each integration uses a pinned external repository, applies an application-only patch, resolves Codes from Maven Central, and runs the relevant build and tests."
    echo
} > "$report"

overall=0

integration_value() {
    local integration_id="$1"
    local field="$2"
    python - "$manifest" "$integration_id" "$field" <<'PY'
import json
import sys
from pathlib import Path

manifest = json.loads(Path(sys.argv[1]).read_text(encoding="utf-8"))
integration = next(item for item in manifest["integrations"] if item["id"] == sys.argv[2])
print(integration[sys.argv[3]])
PY
}

run_irp() {
    local id="integration-reliability-platform"
    local repo commit checkout patch_file start end elapsed status log

    repo="$(integration_value "$id" repository)"
    commit="$(integration_value "$id" commit)"
    patch_file="$root_dir/$(integration_value "$id" patch)"
    checkout="$work_dir/$id"
    log="$work_dir/$id.log"

    git clone --quiet --filter=blob:none --no-checkout "$repo" "$checkout"
    git -C "$checkout" checkout --quiet --detach "$commit"
    git -C "$checkout" apply --check "$patch_file"
    git -C "$checkout" apply "$patch_file"

    start="$(date +%s)"
    status="PASS"

    if ! (
        cd "$checkout"
        bash ./gradlew \
            "-PcodesVersion=$version" \
            clean test \
            --no-build-cache \
            --stacktrace

        bash ./gradlew \
            "-PcodesVersion=$version" \
            dependencyInsight \
            --dependency codes-spring \
            --configuration runtimeClasspath \
            --no-build-cache
    ) >"$log" 2>&1; then
        status="FAIL"
        overall=1
    fi

    end="$(date +%s)"
    elapsed="$((end - start))"

    {
        echo "## $id"
        echo
        echo "- Repository: \`$repo\`"
        echo "- Commit: \`$commit\`"
        echo "- Boundary: $(integration_value "$id" boundary)"
        echo "- Scope: $(integration_value "$id" scope)"
        echo "- Result: **$status**"
        echo "- Clean build/test time: ${elapsed}s"
        echo "- Dependency notes: $(integration_value "$id" dependency_notes)"
        echo "- Expected identity: \`$(integration_value "$id" expected_identity)\`"
        echo
        echo "Patch statistics:"
        echo
        echo '```text'
        git -C "$checkout" apply --stat "$patch_file"
        echo '```'
        echo
        echo "Build log: \`$(basename "$log")\`"
        echo
    } >> "$report"
}

run_patient() {
    local id="patient-mgmt-microservices"
    local repo commit checkout patch_file start end elapsed status log

    repo="$(integration_value "$id" repository)"
    commit="$(integration_value "$id" commit)"
    patch_file="$root_dir/$(integration_value "$id" patch)"
    checkout="$work_dir/$id"
    log="$work_dir/$id.log"

    git clone --quiet --filter=blob:none --no-checkout "$repo" "$checkout"
    git -C "$checkout" checkout --quiet --detach "$commit"
    git -C "$checkout" apply --check "$patch_file"
    git -C "$checkout" apply "$patch_file"

    start="$(date +%s)"
    status="PASS"

    if ! (
        cd "$checkout/patient-service"
        bash ./mvnw \
            --batch-mode \
            --no-transfer-progress \
            "-Dcodes.version=$version" \
            clean test

        bash ./mvnw \
            --batch-mode \
            --no-transfer-progress \
            "-Dcodes.version=$version" \
            dependency:tree \
            "-Dincludes=io.github.aalsanie:*"

        cd "$checkout/billing-service"
        bash ./mvnw \
            --batch-mode \
            --no-transfer-progress \
            "-Dcodes.version=$version" \
            clean test

        bash ./mvnw \
            --batch-mode \
            --no-transfer-progress \
            "-Dcodes.version=$version" \
            dependency:tree \
            "-Dincludes=io.github.aalsanie:*,io.grpc:*"
    ) >"$log" 2>&1; then
        status="FAIL"
        overall=1
    fi

    end="$(date +%s)"
    elapsed="$((end - start))"

    {
        echo "## $id"
        echo
        echo "- Repository: \`$repo\`"
        echo "- Commit: \`$commit\`"
        echo "- Boundary: $(integration_value "$id" boundary)"
        echo "- Scope: $(integration_value "$id" scope)"
        echo "- Result: **$status**"
        echo "- Clean build/test time: ${elapsed}s"
        echo "- Dependency notes: $(integration_value "$id" dependency_notes)"
        echo "- Expected identity: \`$(integration_value "$id" expected_identity)\`"
        echo
        echo "Patch statistics:"
        echo
        echo '```text'
        git -C "$checkout" apply --stat "$patch_file"
        echo '```'
        echo
        echo "Build log: \`$(basename "$log")\`"
        echo
    } >> "$report"
}

run_irp
run_patient

{
    echo "## Result"
    echo
    if [[ "$overall" -eq 0 ]]; then
        echo "**PASS** — both external integrations built successfully and verified the expected boundary identity from Maven Central."
    else
        echo "**FAIL** — at least one external integration failed."
    fi
} >> "$report"

cat "$report"
exit "$overall"
