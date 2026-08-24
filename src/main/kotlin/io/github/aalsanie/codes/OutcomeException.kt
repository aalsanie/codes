@file:JvmName("OutcomeExceptions")

package io.github.aalsanie.codes

class OutcomeException
    @JvmOverloads
    constructor(
        val outcome: Outcome,
        cause: Throwable? = null,
    ) : RuntimeException(outcome.message, cause) {
        init {
            require(outcome.isFailed) {
                "only failed outcomes can be converted to OutcomeException"
            }
        }
    }

@JvmOverloads
fun Outcome.toException(cause: Throwable? = null): OutcomeException = OutcomeException(this, cause)
