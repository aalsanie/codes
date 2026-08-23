package io.github.aalsanie.codes.protocol.grpc

enum class GrpcStatusCode(
    val value: Int,
) {
    OK(0),
    CANCELLED(1),
    UNKNOWN(2),
    INVALID_ARGUMENT(3),
    DEADLINE_EXCEEDED(4),
    NOT_FOUND(5),
    ALREADY_EXISTS(6),
    PERMISSION_DENIED(7),
    RESOURCE_EXHAUSTED(8),
    FAILED_PRECONDITION(9),
    ABORTED(10),
    OUT_OF_RANGE(11),
    UNIMPLEMENTED(12),
    INTERNAL(13),
    UNAVAILABLE(14),
    DATA_LOSS(15),
    UNAUTHENTICATED(16),
    ;

    companion object {
        private val byValue = entries.associateBy(GrpcStatusCode::value)

        @JvmStatic
        fun fromValue(value: Int): GrpcStatusCode? = byValue[value]
    }
}
