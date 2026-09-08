#!/usr/bin/env python3

import importlib.util
import unittest
from pathlib import Path

SCRIPT = Path(__file__).with_name("verify-release-promotion.py")
SPEC = importlib.util.spec_from_file_location("verify_release_promotion", SCRIPT)
if SPEC is None or SPEC.loader is None:
    raise RuntimeError(f"Cannot load {SCRIPT}")
MODULE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(MODULE)


class ReleasePromotionPolicyTest(unittest.TestCase):
    def test_selects_highest_numeric_rc(self):
        tag, version = MODULE.select_latest_rc(
            "0.4.0",
            ["v0.4.0-RC1", "v0.4.0-RC10", "v0.4.0-RC2", "v0.5.0-RC9"],
        )
        self.assertEqual("v0.4.0-RC10", tag)
        self.assertEqual("0.4.0-RC10", version)

    def test_requires_rc_for_same_final_version(self):
        with self.assertRaisesRegex(MODULE.PromotionError, "No release candidate"):
            MODULE.select_latest_rc("0.4.0", ["v0.3.1", "v0.5.0-RC1"])

    def test_accepts_exact_version_only_promotion(self):
        rc_version = "0.4.0-RC1"
        final_version = "0.4.0"
        rc_contents = {
            path: f"before\nversion={rc_version}\nafter\n"
            for path in MODULE.PROMOTION_FILES
        }
        final_contents = {
            path: value.replace(rc_version, final_version)
            for path, value in rc_contents.items()
        }

        MODULE.validate_promotion_contents(
            rc_version,
            final_version,
            set(MODULE.PROMOTION_FILES),
            rc_contents,
            final_contents,
        )

    def test_rejects_product_code_change(self):
        rc_version = "0.4.0-RC1"
        final_version = "0.4.0"
        rc_contents = {
            path: rc_version
            for path in MODULE.PROMOTION_FILES
        }
        final_contents = {
            path: final_version
            for path in MODULE.PROMOTION_FILES
        }

        with self.assertRaisesRegex(MODULE.PromotionError, "unexpected changes"):
            MODULE.validate_promotion_contents(
                rc_version,
                final_version,
                set(MODULE.PROMOTION_FILES) | {"src/main/java/Changed.java"},
                rc_contents,
                final_contents,
            )

    def test_rejects_missing_version_promotion_file(self):
        rc_version = "0.4.0-RC1"
        final_version = "0.4.0"
        rc_contents = {
            path: rc_version
            for path in MODULE.PROMOTION_FILES
        }
        final_contents = {
            path: final_version
            for path in MODULE.PROMOTION_FILES
        }
        changed = set(MODULE.PROMOTION_FILES)
        changed.remove("README.md")

        with self.assertRaisesRegex(MODULE.PromotionError, "missing version-only changes"):
            MODULE.validate_promotion_contents(
                rc_version,
                final_version,
                changed,
                rc_contents,
                final_contents,
            )

    def test_rejects_non_version_edit_in_allowed_file(self):
        rc_version = "0.4.0-RC1"
        final_version = "0.4.0"
        rc_contents = {
            path: f"version={rc_version}\n"
            for path in MODULE.PROMOTION_FILES
        }
        final_contents = {
            path: value.replace(rc_version, final_version)
            for path, value in rc_contents.items()
        }
        final_contents["README.md"] += "extra release edit\n"

        with self.assertRaisesRegex(MODULE.PromotionError, "changes other than replacing"):
            MODULE.validate_promotion_contents(
                rc_version,
                final_version,
                set(MODULE.PROMOTION_FILES),
                rc_contents,
                final_contents,
            )

    def test_requires_rc_version_token_in_every_promotion_file(self):
        rc_version = "0.4.0-RC1"
        final_version = "0.4.0"
        rc_contents = {
            path: rc_version
            for path in MODULE.PROMOTION_FILES
        }
        rc_contents["README.md"] = "no version here"
        final_contents = {
            path: value.replace(rc_version, final_version)
            for path, value in rc_contents.items()
        }

        with self.assertRaisesRegex(MODULE.PromotionError, "policy is stale"):
            MODULE.validate_promotion_contents(
                rc_version,
                final_version,
                set(MODULE.PROMOTION_FILES),
                rc_contents,
                final_contents,
            )

    def test_rejects_structural_changes(self):
        with self.assertRaisesRegex(MODULE.PromotionError, "must not create, delete, rename"):
            MODULE.validate_no_structural_changes(
                " mode change 100644 => 100755 README.md\n"
            )


if __name__ == "__main__":
    unittest.main()
