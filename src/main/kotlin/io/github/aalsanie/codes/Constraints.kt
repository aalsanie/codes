package io.github.aalsanie.codes

internal object Constraints {
    const val MAX_NAMESPACE_LENGTH = 128
    const val MAX_NAME_LENGTH = 64
    const val MAX_MESSAGE_LENGTH = 1024
    const val MAX_DETAIL_LENGTH = 4096
    const val MAX_PATH_LENGTH = 256

    fun requireNamespace(namespace: String) {
        require(namespace.length in 1..MAX_NAMESPACE_LENGTH) {
            "namespace length must be between 1 and $MAX_NAMESPACE_LENGTH"
        }

        val segments = namespace.split('.')
        require(segments.none { it.isEmpty() }) {
            "namespace must not contain empty segments"
        }

        segments.forEach { segment ->
            require(segment.length <= 63) {
                "namespace segment length must not exceed 63"
            }
            require(segment.first() in 'a'..'z') {
                "namespace segments must start with a lowercase ASCII letter"
            }
            require(segment.last() in 'a'..'z' || segment.last() in '0'..'9') {
                "namespace segments must end with a lowercase ASCII letter or digit"
            }
            require(segment.all { it in 'a'..'z' || it in '0'..'9' || it == '-' }) {
                "namespace segments may contain only lowercase ASCII letters, digits, and hyphens"
            }
        }
    }

    fun requireName(name: String) {
        require(name.length in 1..MAX_NAME_LENGTH) {
            "name length must be between 1 and $MAX_NAME_LENGTH"
        }
        require(name.first() in 'A'..'Z') {
            "name must start with an uppercase ASCII letter"
        }
        require(name.all { it in 'A'..'Z' || it in '0'..'9' || it == '_' }) {
            "name may contain only uppercase ASCII letters, digits, and underscores"
        }
    }

    fun requireMessage(message: String) {
        require(message.isNotBlank()) {
            "message must not be blank"
        }
        require(message.length <= MAX_MESSAGE_LENGTH) {
            "message length must not exceed $MAX_MESSAGE_LENGTH"
        }
        require('\u0000' !in message) {
            "message must not contain NUL"
        }
    }

    fun requireDetail(detail: String) {
        require(detail.isNotBlank()) {
            "detail must not be blank"
        }
        require(detail.length <= MAX_DETAIL_LENGTH) {
            "detail length must not exceed $MAX_DETAIL_LENGTH"
        }
        require('\u0000' !in detail) {
            "detail must not contain NUL"
        }
    }

    fun requirePath(path: String) {
        require(path.isNotBlank()) {
            "path must not be blank"
        }
        require(path.length <= MAX_PATH_LENGTH) {
            "path length must not exceed $MAX_PATH_LENGTH"
        }
        require('\u0000' !in path) {
            "path must not contain NUL"
        }
    }
}
