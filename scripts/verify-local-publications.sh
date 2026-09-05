#!/usr/bin/env sh
set -eu

root_dir="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
version="$(sh "$root_dir/scripts/version.sh")"
repository="${MAVEN_REPO_LOCAL:-$HOME/.m2/repository}"
group_path="io/github/aalsanie"

for artifact in codes codes-spring codes-grpc-java; do
    artifact_dir="$repository/$group_path/$artifact/$version"
    jar="$artifact_dir/$artifact-$version.jar"
    pom="$artifact_dir/$artifact-$version.pom"

    test -s "$jar" || {
        echo "Missing local publication JAR: $jar" >&2
        exit 1
    }
    test -s "$pom" || {
        echo "Missing local publication POM: $pom" >&2
        exit 1
    }
done

echo "Verified Maven Local publications for codes, codes-spring, and codes-grpc-java ($version)."
