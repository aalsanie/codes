#!/usr/bin/env python3

from __future__ import annotations

import json
import os
import re
import subprocess
import sys
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
WORKFLOW_FILE = "release-candidate.yml"
PROMOTION_FILES = (
    "CHANGELOG.md",
    "README.md",
    "docs/ten-minute-grpc.md",
    "docs/ten-minute-spring.md",
    "gradle.properties",
)


class PromotionError(RuntimeError):
    pass


def run_git(*args: str, check: bool = True) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        ["git", "-C", str(ROOT), *args],
        check=check,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
    )


def git_line(*args: str) -> str:
    return run_git(*args).stdout.strip()


def git_file(ref: str, path: str) -> str:
    return run_git("show", f"{ref}:{path}").stdout


def property_value(text: str, key: str) -> str:
    prefix = f"{key}="
    matches = [line[len(prefix):] for line in text.splitlines() if line.startswith(prefix)]
    if len(matches) != 1 or not matches[0]:
        raise PromotionError(f"Expected exactly one non-empty {key} property")
    return matches[0]


def select_latest_rc(final_version: str, tags: list[str]) -> tuple[str, str]:
    pattern = re.compile(rf"^v{re.escape(final_version)}-RC([1-9][0-9]*)$")
    candidates: list[tuple[int, str]] = []
    for tag in tags:
        match = pattern.fullmatch(tag)
        if match:
            candidates.append((int(match.group(1)), tag))

    if not candidates:
        raise PromotionError(f"No release candidate tag found for {final_version}")

    _, tag = max(candidates)
    return tag, tag.removeprefix("v")


def validate_promotion_contents(
    rc_version: str,
    final_version: str,
    changed_paths: set[str],
    rc_contents: dict[str, str],
    final_contents: dict[str, str],
) -> None:
    expected_paths = set(PROMOTION_FILES)
    if changed_paths != expected_paths:
        unexpected = sorted(changed_paths - expected_paths)
        missing = sorted(expected_paths - changed_paths)
        details: list[str] = []
        if unexpected:
            details.append("unexpected changes: " + ", ".join(unexpected))
        if missing:
            details.append("missing version-only changes: " + ", ".join(missing))
        raise PromotionError("RC-to-final promotion is not version-only; " + "; ".join(details))

    for path in PROMOTION_FILES:
        rc_text = rc_contents[path]
        final_text = final_contents[path]
        if rc_version not in rc_text:
            raise PromotionError(
                f"Promotion policy is stale: {path} does not contain {rc_version} at the RC tag"
            )
        expected = rc_text.replace(rc_version, final_version)
        if final_text != expected:
            raise PromotionError(
                f"{path} contains changes other than replacing {rc_version} with {final_version}"
            )


def validate_no_structural_changes(summary: str) -> None:
    if summary.strip():
        raise PromotionError(
            "RC-to-final promotion must not create, delete, rename, or change file modes: "
            + summary.strip().replace("\n", "; ")
        )


def github_json(url: str, token: str) -> object:
    request = urllib.request.Request(
        url,
        headers={
            "Accept": "application/vnd.github+json",
            "Authorization": f"Bearer {token}",
            "User-Agent": "codes-release-promotion",
            "X-GitHub-Api-Version": "2022-11-28",
        },
    )
    try:
        with urllib.request.urlopen(request, timeout=20) as response:
            return json.load(response)
    except urllib.error.HTTPError as error:
        raise PromotionError(f"GitHub API request failed with HTTP {error.code}: {url}") from error
    except urllib.error.URLError as error:
        raise PromotionError(f"GitHub API request failed: {url}: {error.reason}") from error


def verify_remote_rc(rc_tag: str, rc_sha: str) -> None:
    repository = os.environ.get("GITHUB_REPOSITORY")
    token = os.environ.get("GITHUB_TOKEN")
    api_url = os.environ.get("GITHUB_API_URL", "https://api.github.com").rstrip("/")
    if not repository:
        raise PromotionError("GITHUB_REPOSITORY is required")
    if not token:
        raise PromotionError("GITHUB_TOKEN is required")

    query = urllib.parse.urlencode(
        {
            "head_sha": rc_sha,
            "per_page": "100",
        }
    )
    workflow = urllib.parse.quote(WORKFLOW_FILE, safe="")
    runs_url = f"{api_url}/repos/{repository}/actions/workflows/{workflow}/runs?{query}"
    runs_payload = github_json(runs_url, token)
    if not isinstance(runs_payload, dict):
        raise PromotionError("Unexpected GitHub workflow-runs response")

    runs = runs_payload.get("workflow_runs")
    if not isinstance(runs, list):
        raise PromotionError("GitHub workflow-runs response is missing workflow_runs")

    successful = [
        run for run in runs
        if isinstance(run, dict)
        and run.get("head_sha") == rc_sha
        and run.get("event") == "push"
        and run.get("conclusion") == "success"
    ]
    if not successful:
        raise PromotionError(
            f"No successful {WORKFLOW_FILE} push workflow exists for {rc_tag} ({rc_sha})"
        )

    encoded_tag = urllib.parse.quote(rc_tag, safe="")
    release_url = f"{api_url}/repos/{repository}/releases/tags/{encoded_tag}"
    release = github_json(release_url, token)
    if not isinstance(release, dict):
        raise PromotionError("Unexpected GitHub release response")
    if release.get("tag_name") != rc_tag:
        raise PromotionError(f"GitHub prerelease tag does not match {rc_tag}")
    if release.get("draft") is not False or release.get("prerelease") is not True:
        raise PromotionError(f"{rc_tag} must exist as a published GitHub prerelease")


def verify() -> None:
    final_properties = (ROOT / "gradle.properties").read_text(encoding="utf-8")
    final_version = property_value(final_properties, "VERSION_NAME")
    if not re.fullmatch(r"[0-9]+\.[0-9]+\.[0-9]+", final_version):
        raise PromotionError(f"Final version is not a release version: {final_version}")

    tags = [line for line in git_line("tag", "--list", f"v{final_version}-RC*").splitlines() if line]
    rc_tag, rc_version = select_latest_rc(final_version, tags)

    ancestry = run_git("merge-base", "--is-ancestor", rc_tag, "HEAD", check=False)
    if ancestry.returncode != 0:
        raise PromotionError(f"Latest release candidate {rc_tag} is not an ancestor of HEAD")

    rc_sha = git_line("rev-parse", f"{rc_tag}^{{commit}}")
    final_sha = git_line("rev-parse", "HEAD^{commit}")
    if rc_sha == final_sha:
        raise PromotionError("Final release must be a version-only promotion commit after the RC")

    validate_no_structural_changes(run_git("diff", "--summary", rc_tag, "HEAD").stdout)

    changed = {
        line for line in git_line("diff", "--name-only", rc_tag, "HEAD").splitlines() if line
    }
    rc_contents = {path: git_file(rc_tag, path) for path in PROMOTION_FILES}
    final_contents = {
        path: (ROOT / path).read_text(encoding="utf-8")
        for path in PROMOTION_FILES
    }
    validate_promotion_contents(
        rc_version,
        final_version,
        changed,
        rc_contents,
        final_contents,
    )

    verify_remote_rc(rc_tag, rc_sha)

    print(
        f"Verified {rc_tag} ({rc_sha}) -> v{final_version} ({final_sha}) "
        "as a successful version-only RC promotion."
    )


def main() -> int:
    try:
        verify()
        return 0
    except PromotionError as error:
        print(f"Release promotion verification failed: {error}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
