package io.github.aalsanie.codes.protocol.http;

import java.util.Objects;

public final class HttpStatusCode implements Comparable<HttpStatusCode> {
    public static final HttpStatusCode OK = new HttpStatusCode(200);
    public static final HttpStatusCode CREATED = new HttpStatusCode(201);
    public static final HttpStatusCode ACCEPTED = new HttpStatusCode(202);
    public static final HttpStatusCode NO_CONTENT = new HttpStatusCode(204);
    public static final HttpStatusCode BAD_REQUEST = new HttpStatusCode(400);
    public static final HttpStatusCode UNAUTHORIZED = new HttpStatusCode(401);
    public static final HttpStatusCode FORBIDDEN = new HttpStatusCode(403);
    public static final HttpStatusCode NOT_FOUND = new HttpStatusCode(404);
    public static final HttpStatusCode CONFLICT = new HttpStatusCode(409);
    public static final HttpStatusCode PRECONDITION_FAILED = new HttpStatusCode(412);
    public static final HttpStatusCode PAYLOAD_TOO_LARGE = new HttpStatusCode(413);
    public static final HttpStatusCode TOO_MANY_REQUESTS = new HttpStatusCode(429);
    public static final HttpStatusCode INTERNAL_SERVER_ERROR = new HttpStatusCode(500);
    public static final HttpStatusCode NOT_IMPLEMENTED = new HttpStatusCode(501);
    public static final HttpStatusCode SERVICE_UNAVAILABLE = new HttpStatusCode(503);
    public static final HttpStatusCode GATEWAY_TIMEOUT = new HttpStatusCode(504);

    private final int value;

    private HttpStatusCode(int value) {
        this.value = value;
    }

    public static HttpStatusCode of(int value) {
        if (value < 100 || value > 599) {
            throw new IllegalArgumentException("HTTP status code must be between 100 and 599");
        }
        return switch (value) {
            case 200 -> OK;
            case 201 -> CREATED;
            case 202 -> ACCEPTED;
            case 204 -> NO_CONTENT;
            case 400 -> BAD_REQUEST;
            case 401 -> UNAUTHORIZED;
            case 403 -> FORBIDDEN;
            case 404 -> NOT_FOUND;
            case 409 -> CONFLICT;
            case 412 -> PRECONDITION_FAILED;
            case 413 -> PAYLOAD_TOO_LARGE;
            case 429 -> TOO_MANY_REQUESTS;
            case 500 -> INTERNAL_SERVER_ERROR;
            case 501 -> NOT_IMPLEMENTED;
            case 503 -> SERVICE_UNAVAILABLE;
            case 504 -> GATEWAY_TIMEOUT;
            default -> new HttpStatusCode(value);
        };
    }

    public int getValue() {
        return value;
    }

    public int getFamily() {
        return value / 100;
    }

    public boolean isInformational() {
        return getFamily() == 1;
    }

    public boolean isSuccessful() {
        return getFamily() == 2;
    }

    public boolean isRedirection() {
        return getFamily() == 3;
    }

    public boolean isClientError() {
        return getFamily() == 4;
    }

    public boolean isServerError() {
        return getFamily() == 5;
    }

    @Override
    public int compareTo(HttpStatusCode other) {
        return Integer.compare(value, Objects.requireNonNull(other, "other").value);
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof HttpStatusCode status && value == status.value);
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
