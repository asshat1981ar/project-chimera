package com.chimera.tools

/**
 * Comprehensive input validation and sanitization layer for the Chimera simulation engine.
 * Provides schema validation, sanitization, and clear error handling for all external inputs.
 */
object ChimeraInputValidator {

    /**
     * Validates and sanitizes a string input.
     * - Trims whitespace
     * - Rejects empty or blank strings
     * - Optionally enforces max length
     * - Optionally enforces allowed character patterns
     */
    fun validateString(
        value: String?,
        fieldName: String = "input",
        allowEmpty: Boolean = false,
        maxLength: Int? = null,
        regex: Regex? = null
    ): ValidatedString {
        requireNotNull(value) { "[$fieldName] must not be null" }

        val sanitized = value.trim()

        if (!allowEmpty && sanitized.isBlank()) {
            throw InvalidInputException("[$fieldName] must not be blank")
        }

        maxLength?.let {
            require(sanitized.length <= it) {
                "[${fieldName}] exceeds maximum length of $it (actual: ${sanitized.length})"
            }
        }

        regex?.let {
            require(sanitized.matches(it)) {
                "[${fieldName}] contains invalid characters. Expected pattern: $it"
            }
        }

        return ValidatedString(sanitized)
    }

    /**
     * Validates an integer input with optional range constraints.
     */
    fun validateInt(
        value: Int?,
        fieldName: String = "input",
        min: Int? = null,
        max: Int? = null
    ): ValidatedInt {
        requireNotNull(value) { "[$fieldName] must not be null" }

        min?.let {
            require(value >= it) { "[$fieldName] must be >= $it (actual: $value)" }
        }

        max?.let {
            require(value <= it) { "[$fieldName] must be <= $it (actual: $value)" }
        }

        return ValidatedInt(value)
    }

    /**
     * Validates a long input with optional range constraints.
     */
    fun validateLong(
        value: Long?,
        fieldName: String = "input",
        min: Long? = null,
        max: Long? = null
    ): ValidatedLong {
        requireNotNull(value) { "[$fieldName] must not be null" }

        min?.let {
            require(value >= it) { "[$fieldName] must be >= $it (actual: $value)" }
        }

        max?.let {
            require(value <= it) { "[$fieldName] must be <= $it (actual: $value)" }
        }

        return ValidatedLong(value)
    }

    /**
     * Validates a float/double input with optional range constraints.
     */
    fun validateDouble(
        value: Double?,
        fieldName: String = "input",
        min: Double? = null,
        max: Double? = null
    ): ValidatedDouble {
        requireNotNull(value) { "[$fieldName] must not be null" }

        min?.let {
            require(value >= it) { "[$fieldName] must be >= $it (actual: $value)" }
        }

        max?.let {
            require(value <= it) { "[$fieldName] must be <= $it (actual: $value)" }
        }

        return ValidatedDouble(value)
    }

    /**
     * Validates that a collection is not empty and optionally enforces size constraints.
     */
    fun <T> validateCollection(
        collection: Collection<T>?,
        fieldName: String = "input",
        minSize: Int = 0,
        maxSize: Int? = null
    ): ValidatedCollection<T> {
        requireNotNull(collection) { "[$fieldName] must not be null" }
        require(collection.isNotEmpty()) { "[$fieldName] must not be empty" }

        val size = collection.size
        minSize.let {
            require(size >= it) { "[$fieldName] must contain at least $it items (actual: $size)" }
        }

        maxSize?.let {
            require(size <= it) { "[$fieldName] must contain at most $it items (actual: $size)" }
        }

        return ValidatedCollection(collection.toList())
    }

    /**
     * Sanitizes a string to prevent injection attacks by escaping or removing dangerous characters.
     * Useful for preparing inputs for system commands, file paths, or database queries.
     */
    fun sanitizeInjectionProne(value: String): String {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
            .replace("/", "&#x2F;")
    }

    /**
     * Validates an identifier (e.g., NPC name, key, tag) with a strict alphanumeric + underscore pattern.
     */
    fun validateIdentifier(value: String?, fieldName: String = "identifier"): ValidatedIdentifier {
        val sanitized = validateString(
            value = value,
            fieldName = fieldName,
            allowEmpty = false,
            regex = Regex("^[a-zA-Z0-9_]+$")
        )
        return ValidatedIdentifier(sanitized.value)
    }

    /**
     * Validates an email address format.
     */
    fun validateEmail(value: String?, fieldName: String = "email"): ValidatedEmail {
        val sanitized = validateString(
            value = value,
            fieldName = fieldName,
            allowEmpty = false,
            regex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
        )
        return ValidatedEmail(sanitized.value)
    }

    /**
     * Validates a URL format.
     */
    fun validateUrl(value: String?, fieldName: String = "url"): ValidatedUrl {
        val sanitized = validateString(
            value = value,
            fieldName = fieldName,
            allowEmpty = false,
            regex = Regex("^(https?|ftp)://[^\\s/$.?#].[^\\s]*\$")
        )
        return ValidatedUrl(sanitized.value)
    }

    /**
     * Thrown when input validation fails.
     */
    class InvalidInputException(message: String) : IllegalArgumentException(message)

    /**
     * Wrapper for validated string values.
     */
    data class ValidatedString(val value: String)

    /**
     * Wrapper for validated integer values.
     */
    data class ValidatedInt(val value: Int)

    /**
     * Wrapper for validated long values.
     */
    data class ValidatedLong(val value: Long)

    /**
     * Wrapper for validated double values.
     */
    data class ValidatedDouble(val value: Double)

    /**
     * Wrapper for validated collection values.
     */
    data class ValidatedCollection<out T>(val value: List<T>)

    /**
     * Wrapper for validated identifier values.
     */
    data class ValidatedIdentifier(val value: String)

    /**
     * Wrapper for validated email values.
     */
    data class ValidatedEmail(val value: String)

    /**
     * Wrapper for validated URL values.
     */
    data class ValidatedUrl(val value: String)
}