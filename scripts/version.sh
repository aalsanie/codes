#!/usr/bin/env sh
set -eu
awk -F= '$1 == "VERSION_NAME" { print $2 }' "$(dirname "$0")/../gradle.properties"
