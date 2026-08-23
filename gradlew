#!/bin/sh
set -eu
VERSION=9.5.0
SHA256=553c78f50dafcd54d65b9a444649057857469edf836431389695608536d6b746
BASE=${GRADLE_USER_HOME:-"$HOME/.gradle"}/wrapper/dists/codes-gradle-$VERSION
ZIP=$BASE/gradle-$VERSION-bin.zip
HOME_DIR=$BASE/gradle-$VERSION
URL=https://services.gradle.org/distributions/gradle-$VERSION-bin.zip
if [ ! -x "$HOME_DIR/bin/gradle" ]; then
    mkdir -p "$BASE"
    TMP=$ZIP.tmp.$$
    trap 'rm -f "$TMP"' EXIT INT TERM
    if command -v curl >/dev/null 2>&1; then
        curl --fail --location --silent --show-error --retry 3 --output "$TMP" "$URL"
    elif command -v wget >/dev/null 2>&1; then
        wget --quiet --tries=3 --output-document="$TMP" "$URL"
    else
        echo "curl or wget is required to bootstrap Gradle" >&2
        exit 1
    fi
    if command -v sha256sum >/dev/null 2>&1; then
        ACTUAL=$(sha256sum "$TMP" | awk '{print $1}')
    elif command -v shasum >/dev/null 2>&1; then
        ACTUAL=$(shasum -a 256 "$TMP" | awk '{print $1}')
    else
        echo "sha256sum or shasum is required to verify Gradle" >&2
        exit 1
    fi
    if [ "$ACTUAL" != "$SHA256" ]; then
        echo "Gradle distribution checksum mismatch" >&2
        exit 1
    fi
    rm -rf "$HOME_DIR"
    unzip -q "$TMP" -d "$BASE"
    mv "$TMP" "$ZIP"
    trap - EXIT INT TERM
fi
exec "$HOME_DIR/bin/gradle" "$@"
