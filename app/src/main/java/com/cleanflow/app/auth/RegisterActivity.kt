package com.cleanflow.app.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cleanflow.app.data.AppDatabase
import com.cleanflow.app.databinding.ActivityRegisterBinding
import com.cleanflow.app.home.HomeActivity
import com.cleanflow.app.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var db: AppDatabase
    private lateinit var sessionManager: SessionManager
    private val authHelper = AuthHelper.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        setLoading(false)
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    setLoading(true)
                    authHelper.signInWithGoogleIdToken(idToken) { authResult ->
                        handleAuthSuccess(authResult, account.displayName ?: "Community Member", account.email ?: "")
                    }
                } else {
                    Toast.makeText(this, "Google ID token was empty. Please try again.", Toast.LENGTH_LONG).show()
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign up cancelled or failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)
        sessionManager = SessionManager(this)

        // Google Sign-In setup
        val webClientId = "398916676238-6l31m7svfoos096okiavsl0afl8nodj6.apps.googleusercontent.com"
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.btnRegister.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            val nameValidation = Validators.validateFullName(fullName)
            val emailValidation = Validators.validateEmail(email)
            val phoneValidation = Validators.validatePhone(phone)
            val passwordValidation = Validators.validatePassword(password)
            val confirmValidation = Validators.validateConfirmPassword(password, confirmPassword)

            var isValid = true
            if (nameValidation is ValidationResult.Error) {
                binding.layoutFullName.error = nameValidation.message
                isValid = false
            } else {
                binding.layoutFullName.error = null
            }

            if (emailValidation is ValidationResult.Error) {
                binding.layoutEmail.error = emailValidation.message
                isValid = false
            } else {
                binding.layoutEmail.error = null
            }

            if (phoneValidation is ValidationResult.Error) {
                binding.layoutPhone.error = phoneValidation.message
                isValid = false
            } else {
                binding.layoutPhone.error = null
            }

            if (passwordValidation is ValidationResult.Error) {
                binding.layoutPassword.error = passwordValidation.message
                isValid = false
            } else {
                binding.layoutPassword.error = null
            }

            if (confirmValidation is ValidationResult.Error) {
                binding.layoutConfirmPassword.error = confirmValidation.message
                isValid = false
            } else {
                binding.layoutConfirmPassword.error = null
            }

            if (!isValid) return@setOnClickListener

            setLoading(true)

            authHelper.registerWithEmail(email, password, fullName) { result ->
                when (result) {
                    is AuthResult.Success -> {
                        handleAuthSuccess(result, fullName, email, phone)
                    }
                    is AuthResult.Error -> {
                        setLoading(false)
                        Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        binding.btnGoogleSignUp.setOnClickListener {
            setLoading(true)
            googleSignInClient.signOut().addOnCompleteListener {
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            }
        }

        binding.btnBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun handleAuthSuccess(
        result: AuthResult<FirebaseUser>,
        fullName: String,
        email: String,
        phone: String = ""
    ) {
        if (result is AuthResult.Success) {
            val firebaseUser = result.data
            val userId = firebaseUser.uid
            val userEmail = firebaseUser.email ?: email
            val userName = firebaseUser.displayName ?: fullName

            sessionManager.saveSession(userId, userEmail)

            lifecycleScope.launch {
                val newUser = User(
                    uid = userId,
                    fullName = userName,
                    email = userEmail,
                    phone = phone,
                    role = "community"
                )
                db.userDao().upsertUser(newUser)

                setLoading(false)
                Toast.makeText(this@RegisterActivity, "Account created successfully!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@RegisterActivity, HomeActivity::class.java))
                finish()
            }
        } else if (result is AuthResult.Error) {
            setLoading(false)
            Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnRegister.isEnabled = !loading
        binding.btnGoogleSignUp.isEnabled = !loading
        binding.btnBackToLogin.isEnabled = !loading
        binding.btnRegister.text = if (loading) "Creating Account..." else "Create Account"
    }
}