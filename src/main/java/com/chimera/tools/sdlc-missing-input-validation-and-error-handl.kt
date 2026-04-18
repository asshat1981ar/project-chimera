package com.chimera.tools

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.*
import kotlin.reflect.KProperty1

/**
 * Centralized error response formatting for consistent API error responses.
 */
@Serializable
data class ErrorResponse(
    val timestamp: String,
    val status: Int,
    val error: String,
    val message: String? = null,
    val path: String? = null
)

/**
 * HTTP status codes with semantic meaning for the Chimera NPC simulation API.
 */
enum class HttpStatusCode(val value: Int, val description: String) {
    OK(200, "Success"),
    BAD_REQUEST(400, "Invalid input data"),
    UNAUTHORIZED(401, "Authentication required"),
    FORBIDDEN(403, "Insufficient permissions"),
    NOT_FOUND(404, "Resource not found"),
    CONFLICT(409, "Resource conflict"),
    UNPROCESSABLE_ENTITY(422, "Semantic validation failure"),
    INTERNAL_SERVER_ERROR(500, "Internal server error"),
    NOT_IMPLEMENTED(501, "Feature not implemented"),
    SERVICE_UNAVAILABLE(503, "Service temporarily unavailable");

    companion object {
        fun fromInt(code: Int): HttpStatusCode =
            values().find { it.value == code } ?: INTERNAL_SERVER_ERROR
    }
}

/**
 * Structured validation result with detailed error information.
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<ValidationError> = emptyList()
)

/**
 * Detailed validation error with field context and constraint information.
 */
data class ValidationError(
    val field: String,
    val code: ValidationErrorCode,
    val message: String,
    val value: Any? = null
)

/**
 * Enumeration of all possible validation error types for the Chimera system.
 */
enum class ValidationErrorCode {
    REQUIRED_FIELD_MISSING,
    INVALID_FORMAT,
    VALUE_OUT_OF_RANGE,
    INVALID_ENUM_VALUE,
    STRING_TOO_LONG,
    STRING_TOO_SHORT,
    INVALID_EMAIL_FORMAT,
    INVALID_UUID,
    NEGATIVE_NUMBER,
    EMPTY_COLLECTION,
    INVALID_RELATIONSHIP_TYPE,
    INVALID_STANCE_TYPE,
    INVALID_STATE_TRANSITION,
    DUPLICATE_IDENTIFIER,
    INVALID_JSON_STRUCTURE;

    override fun toString(): String = name.lowercase()
}

/**
 * Comprehensive input validator for Chimera NPC simulation domain objects.
 * Validates relationships, stances, game states, and business rules.
 */
class ChimeraValidator {

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        /**
         * Validates a name field (required, 2-100 chars, alphanumeric + spaces only).
         */
        fun validateName(field: KProperty1<*, *>, value: Any?): ValidationError? {
            val fieldName = field.name
            if (value == null) {
                return ValidationError(fieldName, ValidationErrorCode.REQUIRED_FIELD_MISSING, "'$fieldName' is required")
            }
            val str = value.toString()
            if (str.isBlank()) {
                return ValidationError(fieldName, ValidationErrorCode.REQUIRED_FIELD_MISSING, "'$fieldName' cannot be blank")
            }
            if (str.length < 2) {
                return ValidationError(fieldName, ValidationErrorCode.STRING_TOO_SHORT,
                    "'$fieldName' must be at least 2 characters")
            }
            if (str.length > 100) {
                return ValidationError(fieldName, ValidationErrorCode.STRING_TOO_LONG,
                    "'$fieldName' must not exceed 100 characters")
            }
            if (!str.matches(Regex("^[a-zA-Zа-яА-Я\\s'-]+$"))) {
                return ValidationError(fieldName, ValidationErrorCode.INVALID_FORMAT,
                    "'$fieldName' contains invalid characters")
            }
            return null
        }

        /**
         * Validates UUID strings.
         */
        fun validateUUID(field: KProperty1<*, *>, value: Any?): ValidationError? {
            val fieldName = field.name
            if (value == null) return ValidationError(fieldName, ValidationErrorCode.REQUIRED_FIELD_MISSING, "'$fieldName' is required")
            val str = value.toString()
            return try {
                UUID.fromString(str)
                null
            } catch (e: IllegalArgumentException) {
                ValidationError(fieldName, ValidationErrorCode.INVALID_UUID, "'$fieldName' must be a valid UUID", str)
            }
        }

        /**
         * Validates enum values for known domain enums.
         */
        inline fun <reified T : Enum<T>> validateEnum(
            field: KProperty1<*, *>,
            value: Any?
        ): ValidationError? {
            val fieldName = field.name
            if (value == null) return ValidationError(fieldName, ValidationErrorCode.REQUIRED_FIELD_MISSING, "'$fieldName' is required")
            val enumClass = T::class.java
            return try {
                Enum.valueOf(enumClass, value.toString())
                null
            } catch (e: IllegalArgumentException) {
                ValidationError(fieldName, ValidationErrorCode.INVALID_ENUM_VALUE,
                    "'$fieldName' must be a valid ${enumClass.simpleName}", value)
            }
        }

        /**
         * Validates numeric ranges (inclusive).
         */
        fun validateRange(
            field: KProperty1<*, *>,
            value: Any?,
            min: Number,
            max: Number
        ): ValidationError? {
            val fieldName = field.name
            if (value == null) return ValidationError(fieldName, ValidationErrorCode.REQUIRED_FIELD_MISSING, "'$fieldName' is required")
            val doubleValue = when (value) {
                is Int -> value.toDouble()
                is Long -> value.toDouble()
                is Float -> value.toDouble()
                is Double -> value
                is String -> value.toDoubleOrNull() ?: return ValidationError(
                    fieldName, ValidationErrorCode.INVALID_FORMAT, "'$fieldName' must be a number", value
                )
                else -> return ValidationError(
                    fieldName, ValidationErrorCode.INVALID_FORMAT, "'$fieldName' must be a numeric type", value
                )
            }
            if (doubleValue < min.toDouble() || doubleValue > max.toDouble()) {
                return ValidationError(fieldName, ValidationErrorCode.VALUE_OUT_OF_RANGE,
                    "'$fieldName' must be between $min and $max", value)
            }
            return null
        }

        /**
         * Validates a collection is not empty.
         */
        fun validateNonEmptyCollection(
            field: KProperty1<*, *>,
            value: Any?
        ): ValidationError? {
            val fieldName = field.name
            if (value == null) return ValidationError(fieldName, ValidationErrorCode.REQUIRED_FIELD_MISSING, "'$fieldName' is required")
            val collection = when (value) {
                is Collection<*> -> value
                is Array<*> -> value.toList()
                else -> return ValidationError(
                    fieldName, ValidationErrorCode.INVALID_FORMAT, "'$fieldName' must be a collection", value
                )
            }
            if (collection.isEmpty()) {
                return ValidationError(fieldName, ValidationErrorCode.EMPTY_COLLECTION,
                    "'$fieldName' cannot be empty")
            }
            return null
        }

        /**
         * Validates relationship archetype types.
         */
        fun validateRelationshipType(field: KProperty1<*, *>, value: Any?): ValidationError? {
            val fieldName = field.name
            if (value == null) return ValidationError(fieldName, ValidationErrorCode.REQUIRED_FIELD_MISSING, "'$fieldName' is required")
            val str = value.toString()
            val validTypes = setOf("friend", "rival", "neutral", "ally", "enemy", "acquaintance")
            return if (str in validTypes) null else
true else
                ValidationError(fieldName, ValidationErrorCode.INVALID_RELATIONSHIP_TYPE,
                    "'$fieldName' must be one of: ${validTypes.joinToString(", ")}", value)
        }

        /**
         * Validates stance types for duel engine.
         */
        fun validateStanceType(field: KProperty1<*, *>, value: Any?): ValidationError? {
            val fieldName = field.name
            if (value == null) return ValidationError(fieldName, ValidationErrorCode.REQUIRED_FIELD_MISSING, "'$fieldName' is required")
            val str = value.toString()
            val validStances = setOf("aggressive", "defensive", "balanced", "evasive", "calculated")
            return if (str in validStances) null else
true else
                ValidationError(fieldName, ValidationErrorCode.INVALID_STANCE_TYPE,
                    "'$fieldName' must be one of: ${validStances.joinToString(", ")}", value)
        }

        /**
         * Validates game state transitions.
         */
        fun validateStateTransition(
            field: KProperty1<*, *>,
            currentState: String,
            nextState: String
        ): ValidationError? {
            val fieldName = field.name
            val validTransitions = mapOf(
                "idle" to setOf("active", "paused"),
                "active" to setOf("victory", "defeat", "paused", "idle"),
                "paused" to setOf("active", "idle"),
                "victory" to setOf("idle"),
                "defeat" to setOf("idle", "active")
            )
            val allowed = validTransitions[currentState]?.contains(nextState)
            if (allowed == false) {
                return ValidationError(fieldName, ValidationErrorCode.INVALID_STATE_TRANSITION,
                    "Invalid transition from '$currentState' to '$nextState'", "$currentState -> $nextState")
            }
            return null
        }

        /**
         * Validates email format.
         */
        fun validateEmail(field: KProperty1<*, *>, value: Any?): ValidationError? {
            val fieldName = field.name
            if (value == null) return ValidationError(fieldName, ValidationErrorCode.REQUIRED_FIELD_MISSING, "'$fieldName' is required")
            val str = value.toString()
            val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
            return if (str.matches(emailRegex)) null else
true else
                ValidationError(fieldName, ValidationErrorCode.INVALID_EMAIL_FORMAT,
                    "'$fieldName' must be a valid email address", str)
        }

        /**
         * Aggregate validation of multiple fields.
         */
        fun validateAll(vararg validations: ValidationResult): ValidationResult {
            val allErrors = validations.flatMap { it.errors }
            return ValidationResult(allErrors.isEmpty(), allErrors)
        }
    }
}

/**
 * HTTP exception with structured error response.
 */
class HttpException(
    val statusCode: HttpStatusCode,
    val message: String = statusCode.description,
    val details: Map<String, Any>? = null
) : RuntimeException(message) {
    fun toErrorResponse(path: String? = null): ErrorResponse {
        return ErrorResponse(
            timestamp = Instant.now().toString(),
            status = statusCode.value,
            error = statusCode.description,
            message = this.message,
            path = path
        )
    }
}

/**
 * Validation exception with field-specific errors.
 */
class ValidationException(val errors: List<ValidationError>) :
    RuntimeException("Validation failed with ${errors.size} error(s)") {

    fun toErrorResponse(path: String? = null): ErrorResponse {
        val firstError = errors.first()
        return ErrorResponse(
            timestamp = Instant.now().toString(),
            status = HttpStatusCode.BAD_REQUEST.value,
            error = HttpStatusCode.BAD_REQUEST.description,
            message = errors.joinToString("; ") { it.message },
            path = path
        )
    }
}

/**
 * Centralized error handler for converting exceptions to standardized API responses.
 */
class ErrorHandler {

    companion object {
        /**
         * Handles an exception and converts it to a structured ErrorResponse.
         */
        fun handle(exception: Exception, path: String? = null): ErrorResponse {
            return when (exception) {
                is HttpException -> exception.toErrorResponse(path)
                is ValidationException -> exception.toErrorResponse(path)
                is IllegalArgumentException -> ErrorResponse(
                    timestamp = Instant.now().toString(),
                    status = HttpStatusCode.BAD_REQUEST.value,
                    error = HttpStatusCode.BAD_REQUEST.description,
                    message = exception.message,
                    path = path
                )
                is NoSuchElementException -> ErrorResponse(
                    timestamp = Instant.now().toString(),
                    status = HttpStatusCode.NOT_FOUND.value,
                    error = HttpStatusCode.NOT_FOUND.description,
                    message = exception.message,
                    path = path
                )
                else -> ErrorResponse(
                    timestamp = Instant.now().toString(),
                    status = HttpStatusCode.INTERNAL_SERVER_ERROR.value,
                    error = HttpStatusCode.INTERNAL_SERVER_ERROR.description,
                    message = "An unexpected error occurred",
                    path = path
                )
            }
        }

        /**
         * Wraps a lambda with error handling for API endpoints.
         */
        inline fun <T> handleRequest(path: String? = null, crossinline block: () -> T): ResultWrapper<T> {
            return try {
                ResultWrapper.success(block())
            } catch (e: Exception) {
                ResultWrapper.error(ErrorHandler.handle(e, path))
            }
        }
    }
}

/**
 * Wrapper for successful or failed request results.
 */
data class ResultWrapper<out T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorResponse? = null
) {
    companion object {
        fun <T> success(data: T): ResultWrapper<T> = ResultWrapper(true, data = data)
        fun <T> error(errorResponse: ErrorResponse): ResultWrapper<T> = ResultWrapper(false, error = errorResponse)
    }
}

/**
 * Input validation decorator for API endpoints.
 */
class ValidatedEndpoint(
    private val validator: ChimeraValidator
) {
    /**
     * Validates all non-null parameters against their respective validation rules.
     */
    fun validate(vararg validations: ValidationResult): ValidationResult {
        return validator.validateAll(*validations)
    }

    /**
     * Ensures a request is valid before processing.
     */
    fun ensureValid(vararg validations: ValidationResult): Unit {
        val result = validate(*validations)
        if (!result.isValid) {
            throw ValidationException(result.errors)
        }
    }
}

/**
 * Sample domain model for Chimera NPC simulation.
 */
data class NpcCharacter(
    val id: UUID,
    val name: String,
    val relationshipType: String,
    val stance: String,
    val trustLevel: Int,
    val energy: Double
)

/**
 * Example application demonstrating integrated validation and error handling.
 */
object ChimeraApplication {

    private val validator = ChimeraValidator()
    private val endpoint = ValidatedEndpoint(validator)

    /**
     * Creates a new NPC character with full validation.
     */
    fun createCharacter(
        id: String,
        name: String,
        relationshipType: String,
        stance: String,
        trustLevel: Int,
        energy: Double
    ): ResultWrapper<NpcCharacter> {
        return ErrorHandler.handleRequest("/characters") {
            endpoint.ensureValid(
                ValidationResult(
                    isValid = true,
                    errors = validator.validateName(
                        NpcCharacter::id,
                        id
                    ) ?: validator.validateUUID(NpcCharacter::id, id)
                ),
                ValidationResult(
                    isValid = true,
                    errors = validator.validateName(NpcCharacter::name, name)
                ),
                ValidationResult(
                    isValid = true,
                    errors = validator.validateRelationshipType(NpcCharacter::relationshipType, relationshipType)
                ),
                ValidationResult(
                    isValid = true,
                    errors = validator.validateStanceType(NpcCharacter::stance, stance)
                ),
                ValidationResult(
                    isValid = true,
                    errors = validator.validateRange(NpcCharacter::trustLevel, trustLevel, 0, 100)
                ),
                ValidationResult(
                    isValid = true,
                    errors = validator.validateRange(NpcCharacter::energy, energy, 0.0, 100.0)
                )
            )

            NpcCharacter(
                id = UUID.fromString(id),
                name = name,
                relationshipType = relationshipType,
                stance = stance,
                trustLevel = trustLevel,
                energy = energy
            )
        }
    }

    /**
     * Processes a state transition for an NPC.
     */
    fun transitionState(
        currentState: String,
        nextState: String
    ): ResultWrapper<Unit> {
        return ErrorHandler.handleRequest("/state/transition") {
            endpoint.ensureValid(
                ValidationResult(
                    isValid = true,
                    errors = validator.validateStateTransition(
                        NpcCharacter::id,
                        currentState,
                        nextState
                    ) ?: ValidationError(
                        "state",
                        ValidationErrorCode.INVALID_STATE_TRANSITION,
                        "Invalid state transition"
                    )
                )
            )
            // State transition logic would go here
        }
    }
}

/**
 * Main entry point demonstrating the validation and error handling system.
 */
fun main() {
    // Example: Valid character creation
    val validResult = ChimeraApplication.createCharacter(
        id = UUID.randomUUID().toString(),
        name = "Aeloria",
        relationshipType = "ally",
        stance = "balanced",
        trustLevel = 75,
        energy = 85.5
    )
    println("Valid character: $validResult")

    // Example: Invalid character creation (bad trust level)
    val invalidResult = ChimeraApplication.createCharacter(
        id = UUID.randomUUID().toString(),
        name = "Malakar",
        relationshipType = "enemy",
        stance = "balanced",
        trustLevel = 150,  // Out of range
        energy = 85.5
    )
    println("Invalid character: $invalidResult")

    // Example: Invalid state transition
    val transitionResult = ChimeraApplication.transitionState("victory", "active")
    println("State transition: $transitionResult")
}