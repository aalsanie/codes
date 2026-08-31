# Security

## Reporting

Do not open a public issue for a vulnerability that could put users at risk before a fix is available.

Use GitHub's private vulnerability reporting for this repository when available. If private reporting is unavailable, contact the maintainer through the contact information on the GitHub profile and provide the affected version, impact, reproduction steps, and any proposed mitigation.

## Supported versions

Security fixes are applied to the latest released minor line. Pre-1.0 versions may require upgrading to receive a fix.

## Boundary data

Codes treats occurrence `detail` and `Issue` content as application-controlled data. Framework adapters do not expose those fields by default. See `docs/boundary-exposure.md`.
