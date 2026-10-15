package com.cleanflow.app.auth

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest

sealed class AuthResult<out T> {
    data class Success<out T>(val data: T) : AuthResult<T>()
    data class Error(val message: String, val exception: Exception? = null) : AuthResult<Nothing>()
}

class AuthHelper private constructor() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    companion object {
        @Volatile
        private var INSTANCE: AuthHelper? = null

        fun getInstance(): AuthHelper {
            return INSTANCE ?: synchronized(this) {
                val instance = AuthHelper()
                INSTANCE = instance
                instance
            }
        }
    }

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    val isLoggedIn: Boolean
        get() = currentUser != null

    fun registerWithEmail(
        email: String,
        password: String,
        fullName: String,
        onResult: (AuthResult<FirebaseUser>) -> Unit
    ) {
        firebaseAuth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(fullName.trim())
                            .build()
                        user.updateProfile(profileUpdates).addOnCompleteListener {
                            onResult(AuthResult.Success(user))
                        }
                    } else {
                        onResult(AuthResult.Error("User registration succeeded but user profile was null."))
                    }
                } else {
                    val errorMsg = parseAuthException(task.exception)
                    onResult(AuthResult.Error(errorMsg, task.exception))
                }
            }
    }

    fun loginWithEmail(
        email: String,
        password: String,
        onResult: (AuthResult<FirebaseUser>) -> Unit
    ) {
        firebaseAuth.signInWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        onResult(AuthResult.Success(user))
                    } else {
                        onResult(AuthResult.Error("Authentication succeeded but session user was null."))
                    }
                } else {
                    val errorMsg = parseAuthException(task.exception)
                    onResult(AuthResult.Error(errorMsg, task.exception))
                }
            }
    }

    fun signInWithGoogleIdToken(
        idToken: String,
        onResult: (AuthResult<FirebaseUser>) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        onResult(AuthResult.Success(user))
                    } else {
                        onResult(AuthResult.Error("Google Sign-In succeeded but user profile was null."))
                    }
                } else {
                    val errorMsg = parseAuthException(task.exception)
                    onResult(AuthResult.Error(errorMsg, task.exception))
                }
            }
    }

    fun sendPasswordResetEmail(
        email: String,
        onResult: (AuthResult<Unit>) -> Unit
    ) {
        firebaseAuth.sendPasswordResetEmail(email.trim())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(AuthResult.Success(Unit))
                } else {
                    val errorMsg = parseAuthException(task.exception)
                    onResult(AuthResult.Error(errorMsg, task.exception))
                }
            }
    }

    fun updateUserProfile(
        displayName: String,
        onResult: (AuthResult<Unit>) -> Unit
    ) {
        val user = currentUser
        if (user == null) {
            onResult(AuthResult.Error("No active user session found. Please sign in again."))
            return
        }

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName.trim())
            .build()

        user.updateProfile(profileUpdates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onResult(AuthResult.Success(Unit))
            } else {
                val errorMsg = parseAuthException(task.exception)
                onResult(AuthResult.Error(errorMsg, task.exception))
            }
        }
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        onResult: (AuthResult<Unit>) -> Unit
    ) {
        val user = currentUser
        if (user == null || user.email.isNullOrEmpty()) {
            onResult(AuthResult.Error("No authenticated user session found."))
            return
        }

        val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
        user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
            if (reauthTask.isSuccessful) {
                user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        onResult(AuthResult.Success(Unit))
                    } else {
                        val errorMsg = parseAuthException(updateTask.exception)
                        onResult(AuthResult.Error(errorMsg, updateTask.exception))
                    }
                }
            } else {
                val errorMsg = parseAuthException(reauthTask.exception)
                onResult(AuthResult.Error("Re-authentication failed: $errorMsg", reauthTask.exception))
            }
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    private fun parseAuthException(e: Exception?): String {
        if (e == null) return "An unexpected error occurred. Please try again."
        val message = e.message ?: ""
        return when {
            message.contains("The email address is already in use", ignoreCase = true) ->
                "An account with this email address already exists."
            message.contains("The email address is badly formatted", ignoreCase = true) ->
                "Invalid email address format."
            message.contains("There is no user record", ignoreCase = true) ||
            message.contains("user-not-found", ignoreCase = true) ->
                "No account found with this email address."
            message.contains("password is invalid", ignoreCase = true) ||
            message.contains("wrong-password", ignoreCase = true) ||
            message.contains("invalid-credential", ignoreCase = true) ->
                "Incorrect email or password. Please try again."
            message.contains("network", ignoreCase = true) ->
                "Network error. Please check your internet connection and try again."
            else -> e.localizedMessage ?: "Authentication failed. Please try again."
        }
    }
}