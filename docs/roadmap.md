# Roadmap

## 0.3.0

Published artifact: `io.github.aalsanie:codes`.

Goals:

* Java 17 core;
* no core runtime dependencies;
* unchanged `0.2.0` semantic catalog and built-in mapping behavior;
* Java API snapshot and semantic snapshots;
* Gradle/Maven Java/Kotlin consumer verification;
* explicit boundary-exposure policy;
* Spring and grpc-java reference integrations.

The Spring and grpc-java adapter sources are present in the repository as `0.4.0-SNAPSHOT` modules but are not part of the `0.3.0` Maven release.

## 0.4.0

Candidate published artifacts:

```text
io.github.aalsanie:codes-spring
io.github.aalsanie:codes-grpc-java
```

They must remain thin bridges to native Spring RFC 9457 and grpc-java/Google RPC types. No custom HTTP problem format or custom gRPC protobuf will be introduced.

## Later 0.x

Changes are driven by reference applications, external integration feedback, and repeated adapter code. Additional framework adapters require demonstrated demand.

No planned work includes a `Result<T>` replacement, retry framework, custom serialization format, automatic exception taxonomy, or a large standard outcome catalog.

## 1.0.0

`1.0.0` is a compatibility milestone, not a popularity milestone. See `compatibility-policy.md` for the gate.
