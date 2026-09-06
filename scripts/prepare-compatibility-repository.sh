#!/usr/bin/env sh
set -eu

root_dir="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
repository="${1:?Usage: prepare-compatibility-repository.sh <repository-path>}"

case "$repository" in
    /*) ;;
    *) repository="$root_dir/$repository" ;;
esac

rm -rf "$repository"
mkdir -p "$repository"

"$root_dir/gradlew" \
    "-Dmaven.repo.local=$repository" \
    :publishToMavenLocal \
    :codes-spring:publishToMavenLocal \
    :codes-grpc-java:publishToMavenLocal \
    --stacktrace

MAVEN_REPO_LOCAL="$repository" sh "$root_dir/scripts/verify-local-publications.sh"
python "$root_dir/scripts/verify-published-pom-contracts.py" "$repository"

printf '%s\n' "$repository"
