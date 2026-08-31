#!/usr/bin/env sh
set -eu

root=$(CDPATH= cd -- "$(dirname "$0")/.." && pwd)
out="$root/build/manual-core-verification"
rm -rf "$out"
mkdir -p "$out/classes"

find "$root/src/main/java" -name '*.java' -print | sort > "$out/main-sources.txt"
javac --release 17 -Xlint:all,-serial -Werror -d "$out/classes" @"$out/main-sources.txt"

javac --release 17 -Xlint:all,-serial -Werror \
    -cp "$out/classes" \
    -d "$out/classes" \
    "$root/src/test/java/io/github/aalsanie/codes/ApiSnapshot.java" \
    "$root/.verify/CoreVerifier.java"

(
    cd "$root"
    java -ea -cp "$out/classes" io.github.aalsanie.codes.CoreVerifier
)

printf '%s\n' "Dependency-free Java 17 core verification passed."
