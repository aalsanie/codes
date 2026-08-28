# Changelog

## Unreleased

## 0.2.0

### Changed

* Reduced the standard outcome catalog from 21 outcomes to 17.
* `ValidationResult.toOutcome(...)` now requires the failure `OutcomeDefinition` to be provided explicitly instead of always using `INVALID_ARGUMENT`.
* Updated the built-in HTTP and gRPC mappings to match the standard outcome catalog.
* Updated the default messages for `INVALID_ARGUMENT` and `RATE_LIMITED`.
* Updated the public API baseline for the `0.2.0` breaking changes.

### Removed

* Removed `StandardOutcomes.CREATED`, `ACCEPTED`, `NO_CONTENT`, and `PAYLOAD_TOO_LARGE`.
* Removed the corresponding built-in HTTP and gRPC mappings. The HTTP status constants remain available for custom mappings.

### Added

* Added semantic compatibility checks for standard outcome codes and states.
* Added semantic compatibility checks for the built-in HTTP and gRPC mappings.

### Breaking

* `0.2.0` is source and binary incompatible with `0.1.x`.
* Calls to `ValidationResult.toOutcome()` must now specify the failure definition.
* Applications using one of the removed standard outcomes must replace it with an appropriate custom or remaining standard outcome.

## 0.1.0

* Initial public release.
