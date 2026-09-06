#!/usr/bin/env bash
set -euo pipefail

root_dir="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
manifest="$root_dir/adoption/rc1/manifest.json"
version="${1:-0.4.0-RC1}"
report="${2:-$root_dir/build/reports/rc1-adopters.md}"
work_dir="${RC_ADOPTER_WORK_DIR:-$root_dir/build/rc-adopters}"

manifest_version="$(python - "$manifest" <<'PY'
import json
import sys
from pathlib import Path
print(json.loads(Path(sys.argv[1]).read_text(encoding="utf-8"))["version"])
PY
)"

if [[ "$version" != "$manifest_version" ]]; then
    echo "RC adopter manifest is for $manifest_version, not $version." >&2
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
    echo "# Codes $version real-adopter gate"
    echo
    echo "Generated: $(date -u +"%Y-%m-%dT%H:%M:%SZ")"
    echo
    echo "Codes library patches used by pilots: **0**"
    echo
    echo "Both pilots resolve Codes from Maven Central. No composite build, Maven Local repository, or source substitution is used."
    echo "Integration time below is automated clean clone + patch + build/test time, not human coding time."
    echo
} > "$report"

overall=0

pilot_value() {
    local pilot_id="$1"
    local field="$2"
    python - "$manifest" "$pilot_id" "$field" <<'PY'
import json
import sys
from pathlib import Path

manifest = json.loads(Path(sys.argv[1]).read_text(encoding="utf-8"))
pilot = next(p for p in manifest["pilots"] if p["id"] == sys.argv[2])
print(pilot[sys.argv[3]])
PY
}

run_irp() {
    local id="integration-reliability-platform"
    local repo commit patch_dir patch_file start end elapsed status log

    repo="$(pilot_value "$id" repository)"
    commit="$(pilot_value "$id" commit)"
    patch_file="$root_dir/$(pilot_value "$id" patch)"
    patch_dir="$work_dir/$id"
    log="$work_dir/$id.log"

    git clone --quiet --filter=blob:none --no-checkout "$repo" "$patch_dir"
    git -C "$patch_dir" checkout --quiet --detach "$commit"
    git -C "$patch_dir" apply --check "$patch_file"
    git -C "$patch_dir" apply "$patch_file"

    start="$(date +%s)"
    status="PASS"

    if ! (
        cd "$patch_dir"
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
        echo "- Boundary: $(pilot_value "$id" boundary)"
        echo "- Differentiator: $(pilot_value "$id" differentiator)"
        echo "- Result: **$status**"
        echo "- Automated clean integration/build time: ${elapsed}s"
        echo "- Manual mapping removed: $(pilot_value "$id" manual_mapping_removed)"
        echo "- Dependency conflicts: $(pilot_value "$id" dependency_conflicts)"
        if [[ "$status" == "PASS" ]]; then
            echo "- Missing Codes API: None observed."
        else
            echo "- Missing Codes API: unresolved; inspect the failed build before classifying."
        fi
        echo "- Expected identity: \`$(pilot_value "$id" expected_identity)\`"
        echo "- Custom Codes library patches: 0"
        echo
        echo "Patch statistics:"
        echo
        echo '```text'
        git -C "$patch_dir" apply --stat "$patch_file"
        echo '```'
        echo
        echo "Build log: \`$(basename "$log")\`"
        echo
    } >> "$report"
}

run_patient() {
    local id="patient-mgmt-microservices"
    local repo commit patch_dir patch_file start end elapsed status log

    repo="$(pilot_value "$id" repository)"
    commit="$(pilot_value "$id" commit)"
    patch_file="$root_dir/$(pilot_value "$id" patch)"
    patch_dir="$work_dir/$id"
    log="$work_dir/$id.log"

    git clone --quiet --filter=blob:none --no-checkout "$repo" "$patch_dir"
    git -C "$patch_dir" checkout --quiet --detach "$commit"
    git -C "$patch_dir" apply --check "$patch_file"
    git -C "$patch_dir" apply "$patch_file"

    start="$(date +%s)"
    status="PASS"

    if ! (
        cd "$patch_dir/patient-service"
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

        cd "$patch_dir/billing-service"
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
        echo "- Boundary: $(pilot_value "$id" boundary)"
        echo "- Differentiator: $(pilot_value "$id" differentiator)"
        echo "- Result: **$status**"
        echo "- Automated clean integration/build time: ${elapsed}s"
        echo "- Manual mapping removed: $(pilot_value "$id" manual_mapping_removed)"
        echo "- Dependency conflicts: $(pilot_value "$id" dependency_conflicts)"
        if [[ "$status" == "PASS" ]]; then
            echo "- Missing Codes API: None observed."
        else
            echo "- Missing Codes API: unresolved; inspect the failed build before classifying."
        fi
        echo "- Expected identity: \`$(pilot_value "$id" expected_identity)\`"
        echo "- Custom Codes library patches: 0"
        echo
        echo "Patch statistics:"
        echo
        echo '```text'
        git -C "$patch_dir" apply --stat "$patch_file"
        echo '```'
        echo
        echo "Build log: \`$(basename "$log")\`"
        echo
    } >> "$report"
}

run_irp
run_patient

{
    echo "## Gate"
    echo
    if [[ "$overall" -eq 0 ]]; then
        echo "**PASS** — both independent external integrations succeeded from Maven Central without custom Codes library patches."
    else
        echo "**FAIL** — at least one external integration failed."
    fi
} >> "$report"

cat "$report"
exit "$overall"
