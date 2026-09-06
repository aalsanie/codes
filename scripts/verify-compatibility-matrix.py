#!/usr/bin/env python3

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent


def properties(path: Path) -> dict[str, str]:
    result: dict[str, str] = {}
    for raw in path.read_text(encoding="utf-8").splitlines():
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        key, value = line.split("=", 1)
        result[key] = value
    return result


matrix = json.loads((ROOT / "compatibility" / "matrix.json").read_text(encoding="utf-8"))
gradle = properties(ROOT / "gradle.properties")

spring_floor = "6.0.0"
grpc_floor = "1.75.0"
kotlin_versions = ["1.9.24", "2.0.21", "2.1.21", "2.2.20", "2.4.10"]

expected_axes = {
    "java": ["17", "21", "25"],
    "spring": [spring_floor, gradle["springFrameworkVersion"]],
    "grpc": [grpc_floor, gradle["grpcVersion"]],
    "kotlin": {
        "versions": kotlin_versions,
        "spring": spring_floor,
        "grpc": grpc_floor,
    },
    "os": ["ubuntu-latest", "windows-latest", "macos-latest"],
}

if matrix != expected_axes:
    raise SystemExit(
        "compatibility/matrix.json drifted from the declared 0.4.x support policy.\n"
        f"Expected: {expected_axes}\n"
        f"Actual:   {matrix}"
    )

if matrix["kotlin"]["spring"] != matrix["spring"][0]:
    raise SystemExit("Kotlin/JSpecify Spring version must be the declared Spring compatibility floor.")

if matrix["kotlin"]["grpc"] != matrix["grpc"][0]:
    raise SystemExit("Kotlin/JSpecify gRPC version must be the declared gRPC compatibility floor.")

print("Compatibility matrix policy verified.")
