# Spring Integration

`codes-spring` converts a reusable Codes `ProblemType` into Spring Framework's native `ProblemDetail`.

## Dependency

```kotlin
dependencies {
    implementation("io.github.aalsanie:codes-spring:0.4.0")
}
```

The application supplies Spring Web. `codes-spring` does not impose a Spring Framework version transitively.

## Define problem types

```java
enum OrderProblems implements ProblemType {
    ORDER_NOT_FOUND(
        URI.create("https://api.example.com/problems/order-not-found"),
        404,
        "Order not found"
    );

    private final URI type;
    private final int status;
    private final String title;

    OrderProblems(URI type, int status, String title) {
        this.type = type;
        this.status = status;
        this.title = title;
    }

    public URI getType() {
        return type;
    }

    public int getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }
}
```

## Create a problem occurrence

```java
ProblemDetail problem = ProblemDetails.forType(
    OrderProblems.ORDER_NOT_FOUND
);
```

For an occurrence-specific detail:

```java
ProblemDetail problem = ProblemDetails.forTypeAndDetail(
    OrderProblems.ORDER_NOT_FOUND,
    "Order o-123 was not found."
);
```

The bridge sets only `type`, `status`, `title`, and the explicitly supplied `detail`.

It does not set the request-specific `instance`, add extension properties, map exceptions, or register controller advice.

## Exception handlers

Different internal exceptions can intentionally expose the same public problem type:

```java
@ExceptionHandler(OrderNotFoundException.class)
ProblemDetail handleOrderNotFound(OrderNotFoundException ex) {
    return ProblemDetails.forTypeAndDetail(
        OrderProblems.ORDER_NOT_FOUND,
        "Order " + ex.orderId() + " was not found."
    );
}

@ExceptionHandler(ArchivedOrderNotFoundException.class)
ProblemDetail handleArchivedOrderNotFound(ArchivedOrderNotFoundException ex) {
    return ProblemDetails.forTypeAndDetail(
        OrderProblems.ORDER_NOT_FOUND,
        "Archived order " + ex.orderId() + " was not found."
    );
}
```

Spring remains responsible for rendering the `ProblemDetail` through MVC or WebFlux.
