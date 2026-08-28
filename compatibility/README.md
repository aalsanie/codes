# Semantic compatibility fixtures

These snapshots protect Codes' public semantic behavior independently of the JVM ABI dump in `api/codes.api`.

They intentionally cover:

- standard outcome code membership and `OutcomeState`;
- built-in HTTP mappings;
- built-in gRPC mappings.

They intentionally do **not** cover:

- `defaultMessage`;
- per-occurrence `detail`;
- `Issue.message`;
- `Issue.path` syntax beyond the public validation rules.

A snapshot change is a semantic API change. Review it intentionally and document the migration impact before updating the
fixture. Before 1.0, an intentional semantic break may ship in a minor release with migration notes. After 1.0, breaking
semantic changes require a major version.

For HTTP, absence from `http-mappings.snapshot` means `HttpOutcomeMapper.standard()` deliberately returns `Unmapped`.
