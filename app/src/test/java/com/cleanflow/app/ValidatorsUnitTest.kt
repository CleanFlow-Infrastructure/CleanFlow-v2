package com.cleanflow.app

import com.cleanflow.app.auth.ValidationResult
import com.cleanflow.app.auth.Validators
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsUnitTest {

    // --- EMAIL VALIDATION TESTS ---

    @Test
    fun validateEmail_validEmail_returnsSuccess() {
        val result = Validators.validateEmail("sipho.ndlovu@cleanflow.org")
        assertTrue("Valid email should return Success", result is ValidationResult.Success)
    }

    @Test
    fun validateEmail_emptyEmail_returnsError() {
        val result = Validators.validateEmail("")
        assertTrue(result is ValidationResult.Error)
        assertEquals("Email address is required.", (result as ValidationResult.Error).message)
    }

    @Test
    fun validateEmail_invalidFormatMissingAtSymbol_returnsError() {
        val result = Validators.validateEmail("siphondlovucleanflow.org")
        assertTrue(result is ValidationResult.Error)
        assertEquals("Please enter a valid email address.", (result as ValidationResult.Error).message)
    }

    @Test
    fun validateEmail_invalidFormatMissingDomain_returnsError() {
        val result = Validators.validateEmail("sipho@cleanflow")
        assertTrue(result is ValidationResult.Error)
        assertEquals("Please enter a valid email address.", (result as ValidationResult.Error).message)
    }

    // --- PASSWORD VALIDATION TESTS ---

    @Test
    fun validatePassword_validPassword_returnsSuccess() {
        val result = Validators.validatePassword("password123")
        assertTrue("Valid password should return Success", result is ValidationResult.Success)
    }

    @Test
    fun validatePassword_emptyPassword_returnsError() {
        val result = Validators.validatePassword("")
        assertTrue(result is ValidationResult.Error)
        assertEquals("Password is required.", (result as ValidationResult.Error).message)
    }

    @Test
    fun validatePassword_tooShort_returnsError() {
        val result = Validators.validatePassword("pass1")
        assertTrue(result is ValidationResult.Error)
        assertEquals("Password must be at least 8 characters long.", (result as ValidationResult.Error).message)
    }

    @Test
    fun validatePassword_missingNumbers_returnsError() {
        val result = Validators.validatePassword("password")
        assertTrue(result is ValidationResult.Error)
        assertEquals("Password must contain both letters and numbers.", (result as ValidationResult.Error).message)
    }

    @Test
    fun validatePassword_missingLetters_returnsError() {
        val result = Validators.validatePassword("12345678")
        assertTrue(result is ValidationResult.Error)
        assertEquals("Password must contain both letters and numbers.", (result as ValidationResult.Error).message)
    }

    // --- CONFIRM PASSWORD TESTS ---

    @Test
    fun validateConfirmPassword_matchingPasswords_returnsSuccess() {
        val result = Validators.validateConfirmPassword("password123", "password123")
        assertTrue("Matching passwords should return Success", result is ValidationResult.Success)
    }

    @Test
    fun validateConfirmPassword_mismatchedPasswords_returnsError() {
        val result = Validators.validateConfirmPassword("password123", "password456")
        assertTrue(result is ValidationResult.Error)
        assertEquals("Passwords do not match.", (result as ValidationResult.Error).message)
    }

    // --- FULL NAME VALIDATION TESTS ---

    @Test
    fun validateFullName_validName_returnsSuccess() {
        val result = Validators.validateFullName("Sipho Ndlovu")
        assertTrue("Valid full name should return Success", result is ValidationResult.Success)
    }

    @Test
    fun validateFullName_emptyName_returnsError() {
        val result = Validators.validateFullName("")
        assertTrue(result is ValidationResult.Error)
        assertEquals("Full name is required.", (result as ValidationResult.Error).message)
    }

    @Test
    fun validateFullName_tooShort_returnsError() {
        val result = Validators.validateFullName("A")
        assertTrue(result is ValidationResult.Error)
        assertEquals("Please enter a valid full name.", (result as ValidationResult.Error).message)
    }

    // --- PHONE NUMBER VALIDATION TESTS ---

    @Test
    fun validatePhone_validPhone_returnsSuccess() {
        val result = Validators.validatePhone("+27 82 123 4567")
        assertTrue("Valid phone number should return Success", result is ValidationResult.Success)
    }

    @Test
    fun validatePhone_emptyPhone_returnsSuccess() {
        val result = Validators.validatePhone("")
        assertTrue("Optional empty phone number should return Success", result is ValidationResult.Success)
    }

    @Test
    fun validatePhone_tooShortPhone_returnsError() {
        val result = Validators.validatePhone("123")
        assertTrue(result is ValidationResult.Error)
        assertEquals("Please enter a valid phone number.", (result as ValidationResult.Error).message)
    }
}