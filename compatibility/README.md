# Compatibility

Compatibility verification uses clean locally published Maven artifacts rather than Gradle project dependencies.

The matrix covers:

* Java 17, 21, and 25;
* Spring Framework 6.0.0 and 7.0.9;
* Kotlin 1.9.24 and 2.4.10;
* Gradle and Maven consumers.

The checks also verify that:

* the core artifact publishes no dependencies;
* `codes-spring` publishes only its dependency on `codes`;
* Java and Kotlin consumers can create a `ProblemType`;
* Spring consumers can convert it to a native `ProblemDetail`.
