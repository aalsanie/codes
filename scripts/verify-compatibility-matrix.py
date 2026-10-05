#!/usr/bin/env python3

import json
from pathlib import Path

root = Path(__file__).resolve().parent.parent
matrix = json.loads((root / "compatibility/matrix.json").read_text(encoding="utf-8"))

required_keys = {"java", "spring", "kotlin"}
if set(matrix) != required_keys:
    raise SystemExit(f"Compatibility matrix keys must be {sorted(required_keys)}")

if matrix["java"] != ["17", "21", "25"]:
    raise SystemExit("Java compatibility matrix must cover 17, 21, and 25.")

if len(matrix["spring"]) < 2:
    raise SystemExit("Spring compatibility matrix must include a floor and current version.")

kotlin = matrix["kotlin"]
if set(kotlin) != {"versions", "spring"}:
    raise SystemExit("Kotlin compatibility policy must define versions and spring.")
if not kotlin["versions"]:
    raise SystemExit("Kotlin compatibility matrix must not be empty.")
if kotlin["spring"] not in matrix["spring"]:
    raise SystemExit("Kotlin compatibility Spring version must be in the Spring matrix.")

print("Compatibility matrix policy verified.")
