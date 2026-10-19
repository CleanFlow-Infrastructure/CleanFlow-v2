package com.cleanflow.app.auth

import java.util.regex.Pattern

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

object Validators {

    private val EMAIL_REGEX = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    )

    fun validateEmail(email: String): ValidationResult {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult.Error("Email address is required.")
        }
        if (!EMAIL_REGEX.matcher(trimmed).matches()) {
            return ValidationResult.Error("Please enter a valid email address.")
        }
        return ValidationResult.Success
    }

    fun validatePassword(password: String): ValidationResult {
        if (password.isEmpty()) {
            return ValidationResult.Error("Password is required.")
        }
        if (password.length < 8) {
            return ValidationResult.Error("Password must be at least 8 characters long.")
        }
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        if (!hasLetter || !hasDigit) {
            return ValidationResult.Error("Password must contain both letters and numbers.")
        }
        return ValidationResult.Success
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): ValidationResult {
        if (confirmPassword.isEmpty()) {
            return ValidationResult.Error("Please confirm your password.")
        }
        if (password != confirmPassword) {
            return ValidationResult.Error("Passwords do not match.")
        }
        return ValidationResult.Success
    }

    fun validateFullName(fullName: String): ValidationResult {
        val trimmed = fullName.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult.Error("Full name is required.")
        }
        if (trimmed.length < 2) {
            return ValidationResult.Error("Please enter a valid full name.")
        }
        return ValidationResult.Success
    }

    fun validatePhone(phone: String): ValidationResult {
        val trimmed = phone.trim()
        if (trimmed.isNotEmpty() && trimmed.length < 10) {
            return ValidationResult.Error("Please enter a valid phone number.")
        }
        return ValidationResult.Success
    }
}