package com.cleanflow.app.auth

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cleanflow.app.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val authHelper = AuthHelper.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBackToLogin.setOnClickListener {
            finish()
        }

        binding.btnResetPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val emailValidation = Validators.validateEmail(email)

            if (emailValidation is ValidationResult.Error) {
                binding.layoutEmail.error = emailValidation.message
                return@setOnClickListener
            } else {
                binding.layoutEmail.error = null
            }

            setLoading(true)

            authHelper.sendPasswordResetEmail(email) { result ->
                setLoading(false)
                when (result) {
                    is AuthResult.Success -> {
                        Toast.makeText(
                            this,
                            "Password reset link sent to $email. Please check your inbox.",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                    is AuthResult.Error -> {
                        Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnResetPassword.isEnabled = !loading
        binding.btnBackToLogin.isEnabled = !loading
        binding.btnResetPassword.text = if (loading) "Sending Link..." else "Send Reset Email"
    }
}