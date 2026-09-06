#!/usr/bin/env python3

import argparse
import xml.etree.ElementTree as ET
from pathlib import Path


def properties(path: Path) -> dict[str, str]:
    result: dict[str, str] = {}
    for raw in path.read_text(encoding="utf-8").splitlines():
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        key, value = line.split("=", 1)
        result[key] = value
    return result


parser = argparse.ArgumentParser()
parser.add_argument("repository", type=Path)
args = parser.parse_args()

root = Path(__file__).resolve().parent.parent
props = properties(root / "gradle.properties")
version = props["VERSION_NAME"]
group_path = Path("io/github/aalsanie")

expected = {
    "codes": [],
    "codes-spring": sorted([
        ("io.github.aalsanie", "codes", version, "compile"),
        ("org.springframework", "spring-web", props["springFrameworkVersion"], "compile"),
    ]),
    "codes-grpc-java": sorted([
        ("io.github.aalsanie", "codes", version, "compile"),
        ("com.google.api.grpc", "proto-google-common-protos", props["protoGoogleCommonProtosVersion"], "compile"),
        ("io.grpc", "grpc-api", props["grpcVersion"], "compile"),
        ("io.grpc", "grpc-protobuf", props["grpcVersion"], "runtime"),
    ]),
}

namespace = {"m": "http://maven.apache.org/POM/4.0.0"}

for artifact, expected_dependencies in expected.items():
    pom = args.repository / group_path / artifact / version / f"{artifact}-{version}.pom"
    jar = pom.with_suffix(".jar")

    if not pom.is_file() or pom.stat().st_size == 0:
        raise SystemExit(f"Missing published POM: {pom}")
    if not jar.is_file() or jar.stat().st_size == 0:
        raise SystemExit(f"Missing published JAR: {jar}")

    document = ET.parse(pom)
    actual = []
    for dependency in document.findall("./m:dependencies/m:dependency", namespace):
        def value(name: str) -> str:
            node = dependency.find(f"m:{name}", namespace)
            return "" if node is None or node.text is None else node.text.strip()

        actual.append((
            value("groupId"),
            value("artifactId"),
            value("version"),
            value("scope") or "compile",
        ))

    actual.sort()
    if actual != expected_dependencies:
        raise SystemExit(
            f"{artifact} published POM dependency budget changed.\n"
            f"Expected: {expected_dependencies}\n"
            f"Actual:   {actual}"
        )

print(f"Published POM contracts verified in {args.repository}.")
