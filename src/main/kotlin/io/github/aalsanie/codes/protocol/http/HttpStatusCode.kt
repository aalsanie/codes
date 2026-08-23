package io.github.aalsanie.codes.protocol.http

class HttpStatusCode private constructor(
    val value: Int,
) : Comparable<HttpStatusCode> {
    val family: Int
        get() = value / 100

    val isInformational: Boolean
        get() = family == 1

    val isSuccessful: Boolean
        get() = family == 2

    val isRedirection: Boolean
        get() = family == 3

    val isClientError: Boolean
        get() = family == 4

    val isServerError: Boolean
        get() = family == 5

    override fun compareTo(other: HttpStatusCode): Int = value.compareTo(other.value)

    override fun equals(other: Any?): Boolean =
        this === other || other is HttpStatusCode && value == other.value

    override fun hashCode(): Int = value

    override fun toString(): String = value.toString()

    companion object {
        @JvmField val OK = HttpStatusCode(200)
        @JvmField val CREATED = HttpStatusCode(201)
        @JvmField val ACCEPTED = HttpStatusCode(202)
        @JvmField val NO_CONTENT = HttpStatusCode(204)
        @JvmField val BAD_REQUEST = HttpStatusCode(400)
        @JvmField val UNAUTHORIZED = HttpStatusCode(401)
        @JvmField val FORBIDDEN = HttpStatusCode(403)
        @JvmField val NOT_FOUND = HttpStatusCode(404)
        @JvmField val CONFLICT = HttpStatusCode(409)
        @JvmField val PRECONDITION_FAILED = HttpStatusCode(412)
        @JvmField val PAYLOAD_TOO_LARGE = HttpStatusCode(413)
        @JvmField val TOO_MANY_REQUESTS = HttpStatusCode(429)
        @JvmField val INTERNAL_SERVER_ERROR = HttpStatusCode(500)
        @JvmField val NOT_IMPLEMENTED = HttpStatusCode(501)
        @JvmField val SERVICE_UNAVAILABLE = HttpStatusCode(503)
        @JvmField val GATEWAY_TIMEOUT = HttpStatusCode(504)

        @JvmStatic
        fun of(value: Int): HttpStatusCode {
            require(value in 100..599) {
                "HTTP status code must be between 100 and 599"
            }
            return when (value) {
                200 -> OK
                201 -> CREATED
                202 -> ACCEPTED
                204 -> NO_CONTENT
                400 -> BAD_REQUEST
                401 -> UNAUTHORIZED
                403 -> FORBIDDEN
                404 -> NOT_FOUND
                409 -> CONFLICT
                412 -> PRECONDITION_FAILED
                413 -> PAYLOAD_TOO_LARGE
                429 -> TOO_MANY_REQUESTS
                500 -> INTERNAL_SERVER_ERROR
                501 -> NOT_IMPLEMENTED
                503 -> SERVICE_UNAVAILABLE
                504 -> GATEWAY_TIMEOUT
                else -> HttpStatusCode(value)
            }
        }
    }
}
