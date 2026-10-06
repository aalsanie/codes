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

expected = {
    "java": ["17", "21", "25"],
    "spring": ["6.0.0", gradle["springFrameworkVersion"]],
    "kotlin": {
        "versions": ["1.9.24", "2.4.10"],
        "spring": "6.0.0",
    },
}

if matrix != expected:
    raise SystemExit(
        "compatibility/matrix.json drifted from the declared support policy.\n"
        f"Expected: {expected}\n"
        f"Actual:   {matrix}"
    )

print("Compatibility matrix policy verified.")
