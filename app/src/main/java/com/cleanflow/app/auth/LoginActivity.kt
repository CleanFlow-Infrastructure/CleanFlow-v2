package com.cleanflow.app.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cleanflow.app.data.AppDatabase
import com.cleanflow.app.databinding.ActivityLoginBinding
import com.cleanflow.app.home.HomeActivity
import com.cleanflow.app.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var db: AppDatabase
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
                Toast.makeText(this, "Google sign in cancelled or failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        db = AppDatabase.getDatabase(this)

        // Google Sign-In setup
        val webClientId = "398916676238-6l31m7svfoos096okiavsl0afl8nodj6.apps.googleusercontent.com"
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Check active session
        if (authHelper.isLoggedIn || sessionManager.isLoggedIn()) {
            goToHome()
            return
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            val emailValidation = Validators.validateEmail(email)
            val passwordValidation = Validators.validatePassword(password)

            var isValid = true
            if (emailValidation is ValidationResult.Error) {
                binding.layoutEmail.error = emailValidation.message
                isValid = false
            } else {
                binding.layoutEmail.error = null
            }

            if (passwordValidation is ValidationResult.Error) {
                binding.layoutPassword.error = passwordValidation.message
                isValid = false
            } else {
                binding.layoutPassword.error = null
            }

            if (!isValid) return@setOnClickListener

            setLoading(true)

            authHelper.loginWithEmail(email, password) { result ->
                when (result) {
                    is AuthResult.Success -> {
                        handleAuthSuccess(result, result.data.displayName ?: "Community Member", email)
                    }
                    is AuthResult.Error -> {
                        setLoading(false)
                        Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        binding.btnGoogleSignIn.setOnClickListener {
            setLoading(true)
            googleSignInClient.signOut().addOnCompleteListener {
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            }
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun handleAuthSuccess(
        result: AuthResult<FirebaseUser>,
        fallbackName: String,
        email: String
    ) {
        if (result is AuthResult.Success) {
            val firebaseUser = result.data
            val userId = firebaseUser.uid
            val userEmail = firebaseUser.email ?: email
            val fullName = firebaseUser.displayName ?: fallbackName

            sessionManager.saveSession(userId, userEmail)

            lifecycleScope.launch {
                val existingUser = db.userDao().getUserById(userId)
                if (existingUser == null) {
                    val newUser = User(
                        uid = userId,
                        fullName = fullName,
                        email = userEmail,
                        role = "community"
                    )
                    db.userDao().upsertUser(newUser)
                }
                setLoading(false)
                Toast.makeText(this@LoginActivity, "Welcome back, $fullName!", Toast.LENGTH_SHORT).show()
                goToHome()
            }
        } else if (result is AuthResult.Error) {
            setLoading(false)
            Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnLogin.isEnabled = !loading
        binding.btnGoogleSignIn.isEnabled = !loading
        binding.btnRegister.isEnabled = !loading
        binding.btnLogin.text = if (loading) "Signing in..." else "Sign In"
    }

    private fun goToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}