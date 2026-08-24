package io.github.aalsanie.codes

internal fun assertTrue(
    value: Boolean,
    message: String = "expected true",
) {
    if (!value) throw AssertionError(message)
}

internal fun assertFalse(
    value: Boolean,
    message: String = "expected false",
) {
    if (value) throw AssertionError(message)
}

internal fun assertNull(
    value: Any?,
    message: String = "expected null",
) {
    if (value != null) throw AssertionError("$message, got <$value>")
}

internal fun assertSame(
    expected: Any?,
    actual: Any?,
) {
    if (expected !== actual) throw AssertionError("expected same instance <$expected>, got <$actual>")
}

internal fun assertNotSame(
    unexpected: Any?,
    actual: Any?,
) {
    if (unexpected === actual) throw AssertionError("expected distinct instances <$actual>")
}

internal fun assertEquals(
    expected: Any?,
    actual: Any?,
) {
    if (expected != actual) throw AssertionError("expected <$expected>, got <$actual>")
}

internal fun assertNotEquals(
    unexpected: Any?,
    actual: Any?,
) {
    if (unexpected == actual) throw AssertionError("did not expect <$actual>")
}

internal inline fun <reified T : Throwable> assertFails(block: () -> Unit): T {
    try {
        block()
    } catch (throwable: Throwable) {
        if (throwable is T) return throwable
        throw AssertionError("expected ${T::class.java.name}, got ${throwable::class.java.name}", throwable)
    }
    throw AssertionError("expected ${T::class.java.name} to be thrown")
}
