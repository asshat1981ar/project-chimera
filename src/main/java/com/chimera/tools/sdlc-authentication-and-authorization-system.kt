package com.chimera.auth

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * Authentication and Authorization module for Chimera RPG.
 * Provides secure user session management, password hashing,
 * and role-based access control for NPC state persistence.
 */
class AuthManager private constructor() {

    companion object {
        @Volatile
        private var instance: AuthManager? = null
        private const val KEY_TRANSFORMATION = "AES/ECB/PKCS5Padding"
        private const val HASH_ITERATIONS = 10000
        private const val KEY_SIZE = 256

        fun getInstance(): AuthManager {
            return instance ?: synchronized(this) {
                instance ?: AuthManager().also { instance = it }
            }
        }
    }

    private var currentUser: UserSession? = null
    private val userDatabase = mutableMapOf<String, UserAccount>()
    private val encryptionKey = generateEncryptionKey()

    /**
     * User session containing authentication state and permissions
     */
    data class UserSession(
        val userId: String,
        val username: String,
        val role: UserRole,
        val token: String,
        val sessionStartTime: Long = System.currentTimeMillis(),
        val permissions: Set<Permission>
    )

    /**
     * User account stored in the database
     */
    data class UserAccount(
        val userId: String,
        val username: String,
        val passwordHash: String,
        val salt: String,
        val role: UserRole,
        val saveSlots: MutableMap<Int, SaveSlot> = mutableMapOf(),
        val npcStates: MutableMap<String, NPCDisposition> = mutableMapOf()
    )

    /**
     * Save slot containing player progress and NPC states
     */
    data class SaveSlot(
        val slotId: Int,
        val playerName: String,
        val level: Int,
        val experience: Int,
        val inventory: List<String>,
        val npcDispositions: Map<String, NPCDisposition>,
        val timestamp: Long = System.currentTimeMillis()
    )

    /**
     * NPC disposition state for narrative progression
     */
    data class NPCDisposition(
        val npcId: String,
        val name: String,
        var trustLevel: Int = 50,
        var questsCompleted: List<String> = emptyList(),
        var dialogueState: String = "neutral",
        val memoryFlags: MutableSet<String> = mutableSetOf()
    )

    /**
     * User roles with hierarchical permissions
     */
    enum class UserRole {
        GUEST,        // Read-only access
        PLAYER,       // Standard player with save access
        MODERATOR,    // Player with moderation capabilities
        ADMIN         // Full system access
    }

    /**
     * System permissions for fine-grained access control
     */
    enum class Permission {
        LOGIN,
        CREATE_ACCOUNT,
        SAVE_GAME,
        LOAD_GAME,
        DELETE_SAVE,
        MODERATE_USERS,
        VIEW_NPC_STATES,
        MODIFY_NPC_STATES
    }

    /**
     * Registers a new user account with secure password hashing
     */
    fun registerUser(username: String, password: String): Result<String> {
        return try {
            if (username.isBlank() || password.length < 8) {
                return Result.failure(IllegalArgumentException("Invalid username or password"))
            }

            if (userDatabase.containsKey(username)) {
                return Result.failure(IllegalArgumentException("Username already exists"))
            }

            val salt = generateSalt()
            val passwordHash = hashPassword(password, salt)
            val userId = generateUserId()

            val account = UserAccount(
                userId = userId,
                username = username,
                passwordHash = passwordHash,
                salt = salt,
                role = UserRole.PLAYER
            )

            userDatabase[username] = account
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Authenticates user and creates a secure session
     */
    fun login(username: String, password: String): Result<UserSession> {
        return try {
            val account = userDatabase[username] ?: return Result.failure(IllegalArgumentException("User not found"))

            val expectedHash = hashPassword(password, account.salt)
            if (!constantTimeEquals(expectedHash, account.passwordHash)) {
                return Result.failure(SecurityException("Invalid credentials"))
            }

            val token = generateSessionToken()
            val permissions = calculatePermissions(account.role)

            val session = UserSession(
                userId = account.userId,
                username = account.username,
                role = account.role,
                token = token,
                permissions = permissions
            )

            currentUser = session
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Terminates current user session
     */
    fun logout() {
        currentUser = null
    }

    /**
     * Checks if current user has required permission
     */
    fun hasPermission(permission: Permission): Boolean {
        val session = currentUser ?: return false
        return permission in session.permissions
    }

    /**
     * Saves game progress to user's save slot with NPC state persistence
     */
    fun saveGame(slotId: Int, saveSlot: SaveSlot): Result<Unit> {
        val session = currentUser ?: return Result.failure(SecurityException("Not authenticated"))
        if (!hasPermission(SaveGame)) return Result.failure(SecurityException("Insufficient permissions"))

        return try {
            val encryptedSave = encryptSaveData(saveSlot)
            val account = userDatabase[session.username]!!
            account.saveSlots[slotId] = encryptedSave
            Result.unit()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Loads game progress from user's save slot with NPC state restoration
     */
    fun loadGame(slotId: Int): Result<SaveSlot> {
        val session = currentUser ?: return Result.failure(SecurityException("Not authenticated"))
        if (!hasPermission(LoadGame)) return Result.failure(SecurityException("Insufficient permissions"))

        return try {
            val account = userDatabase[session.username]!!
            val encryptedSave = account.saveSlots[slotId] ?: return Result.failure(IllegalArgumentException("Save slot not found"))
            val saveSlot = decryptSaveData(encryptedSave)
            Result.success(saveSlot)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates NPC disposition state for narrative progression
     */
    fun updateNPCDisposition(npcId: String, disposition: NPCDisposition): Result<Unit> {
        val session = currentUser ?: return Result.failure(SecurityException("Not authenticated"))
        if (!hasPermission(MODIFY_NPC_STATES)) return Result.failure(SecurityException("Insufficient permissions"))

        return try {
            val account = userDatabase[session.username]!!
            account.npcStates[npcId] = disposition
            Result.unit()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retrieves NPC disposition state
     */
    fun getNPCDisposition(npcId: String): Result<NPCDisposition?> {
        val session = currentUser ?: return Result.failure(SecurityException("Not authenticated"))
        if (!hasPermission(VIEW_NPC_STATES)) return Result.failure(SecurityException("Insufficient permissions"))

        return try {
            val account = userDatabase[session.username]
            val npcState = account?.npcStates?.get(npcId)
            Result.success(npcState)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes a save slot
     */
    fun deleteSave(slotId: Int): Result<Unit> {
        val session = currentUser ?: return Result.failure(SecurityException("Not authenticated"))
        if (!hasPermission(DeleteSave)) return Result.failure(SecurityException("Insufficient permissions"))

        return try {
            val account = userDatabase[session.username]!!
            if (account.saveSlots.remove(slotId) != null) {
                Result.unit()
            } else {
                Result.failure(IllegalArgumentException("Save slot not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets current active session
     */
    fun getCurrentSession(): UserSession? = currentUser

    /**
     * Changes user password with re-authentication
     */
    fun changePassword(oldPassword: String, newPassword: String): Result<Unit> {
        val session = currentUser ?: return Result.failure(SecurityException("Not authenticated"))

        return try {
            val account = userDatabase[session.username]!!
            val expectedHash = hashPassword(oldPassword, account.salt)
            if (!constantTimeEquals(expectedHash, account.passwordHash)) {
                return Result.failure(SecurityException("Invalid current password"))
            }

            if (newPassword.length < 8) {
                return Result.failure(IllegalArgumentException("New password must be at least 8 characters"))
            }

            val newSalt = generateSalt()
            val newHash = hashPassword(newPassword, newSalt)
            account.passwordHash = newHash
            account.salt = newSalt

            Result.unit()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateUserId(): String {
        return "usr_${System.currentTimeMillis()}_${generateSalt().take(8)}"
    }

    private fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    private fun hashPassword(password: String, salt: String): String {
        val saltedPassword = password + salt
        val digest = MessageDigest.getInstance("SHA-256")
        var hash = saltedPassword.toByteArray()

        repeat(HASH_ITERATIONS) {
            hash = digest.digest(hash)
        }

        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    private fun generateSessionToken(): String {
        val random = SecureRandom()
        val token = ByteArray(32)
        random.nextBytes(token)
        return Base64.encodeToString(token, Base64.NO_WRAP)
    }

    private fun calculatePermissions(role: UserRole): Set<Permission> {
        return when (role) {
            UserRole.GUEST -> setOf(Permission.LOGIN, Permission.VIEW_NPC_STATES)
            UserRole.PLAYER -> setOf(
                Permission.LOGIN,
                Permission.SAVE_GAME,
                Permission.LOAD_GAME,
                Permission.DELETE_SAVE,
                Permission.VIEW_NPC_STATES
            )
            UserRole.MODERATOR -> setOf(
                Permission.LOGIN,
                Permission.SAVE_GAME,
                Permission.LOAD_GAME,
                Permission.DELETE_SAVE,
                Permission.VIEW_NPC_STATES,
                Permission.MODIFY_NPC_STATES
            )
            UserRole.ADMIN -> setOf(
                Permission.LOGIN,
                Permission.CREATE_ACCOUNT,
                Permission.SAVE_GAME,
                Permission.LOAD_GAME,
                Permission.DELETE_SAVE,
                Permission.MODERATE_USERS,
                Permission.VIEW_NPC_STATES,
                Permission.MODIFY_NPC_STATES
            )
        }
    }

    private fun generateEncryptionKey(): ByteArray {
        val key = ByteArray(KEY_SIZE / 8)
        SecureRandom().nextBytes(key)
        return key
    }

    private fun encryptSaveData(saveSlot: SaveSlot): SaveSlot {
        // Simplified encryption - in production use proper AES encryption
        val serialized = saveSlot.toString().toByteArray()
        return saveSlot // Placeholder for actual encryption
    }

    private fun decryptSaveData(encryptedSave: SaveSlot): SaveSlot {
        // Simplified decryption - in production use proper AES decryption
        return encryptedSave // Placeholder for actual decryption
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].toInt() xor b[i].toInt())
        }
        return result == 0
    }
}

fun main() {
    val authManager = AuthManager.getInstance()

    // Register a new user
    val registration = authManager.registerUser("hero", "securePassword123")
    when (registration) {
        is Result.Success -> println("User registered with ID: ${registration.getOrNull()}")
        is Result.Failure -> println("Registration failed: ${registration.exceptionOrNull()?.message}")
    }

    // Login the user
    val login = authManager.login("hero", "securePassword123")
    when (login) {
        is Result.Success -> {
            println("Login successful for: ${login.getOrNull()?.username}")
            println("User role: ${login.getOrNull()?.role}")
            println("Has save permission: ${authManager.hasPermission(AuthManager.Permission.SAVE_GAME)}")
        }
        is Result.Failure -> println("Login failed: ${login.exceptionOrNull()?.message}")
    }

    println("Auth system demonstration complete")
}