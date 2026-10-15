package com.cleanflow.app.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cleanflow.app.auth.AuthHelper
import com.cleanflow.app.auth.AuthResult
import com.cleanflow.app.auth.LoginActivity
import com.cleanflow.app.auth.SessionManager
import com.cleanflow.app.auth.ValidationResult
import com.cleanflow.app.auth.Validators
import com.cleanflow.app.data.AppDatabase
import com.cleanflow.app.databinding.ActivityProfileBinding
import com.cleanflow.app.models.User
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var db: AppDatabase
    private val authHelper = AuthHelper.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        db = AppDatabase.getDatabase(this)

        binding.btnBack.setOnClickListener { finish() }

        loadProfile()
        loadStats()

        // Save Profile Changes
        binding.btnSaveProfile.setOnClickListener {
            val newName = binding.etEditName.text.toString().trim()
            val newPhone = binding.etEditPhone.text.toString().trim()

            val nameValidation = Validators.validateFullName(newName)
            val phoneValidation = Validators.validatePhone(newPhone)

            var isValid = true
            if (nameValidation is ValidationResult.Error) {
                binding.layoutEditName.error = nameValidation.message
                isValid = false
            } else {
                binding.layoutEditName.error = null
            }

            if (phoneValidation is ValidationResult.Error) {
                binding.layoutEditPhone.error = phoneValidation.message
                isValid = false
            } else {
                binding.layoutEditPhone.error = null
            }

            if (!isValid) return@setOnClickListener

            binding.btnSaveProfile.isEnabled = false
            binding.btnSaveProfile.text = "Saving..."

            val userId: String = sessionManager.getUserId()
            val userEmail: String = sessionManager.getUserEmail()

            // Update Firebase Auth profile
            authHelper.updateUserProfile(newName) { result ->
                lifecycleScope.launch {
                    val existingUser = db.userDao().getUserById(userId)
                    val updatedUser = existingUser?.copy(fullName = newName, phone = newPhone)
                        ?: User(uid = userId, fullName = newName, email = userEmail, phone = newPhone)

                    db.userDao().upsertUser(updatedUser)

                    binding.btnSaveProfile.isEnabled = true
                    binding.btnSaveProfile.text = "Save Profile Changes"

                    binding.tvFullName.text = newName
                    binding.tvAvatar.text = if (newName.isNotEmpty()) newName.first().uppercase() else "?"

                    Toast.makeText(this@ProfileActivity, "Profile updated successfully! ✓", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Change Password
        binding.btnChangePassword.setOnClickListener {
            val currentPassword = binding.etCurrentPassword.text.toString().trim()
            val newPassword = binding.etNewPassword.text.toString().trim()

            val passValidation = Validators.validatePassword(newPassword)

            var isValid = true
            if (currentPassword.isEmpty()) {
                binding.layoutCurrentPassword.error = "Current password is required."
                isValid = false
            } else {
                binding.layoutCurrentPassword.error = null
            }

            if (passValidation is ValidationResult.Error) {
                binding.layoutNewPassword.error = passValidation.message
                isValid = false
            } else {
                binding.layoutNewPassword.error = null
            }

            if (!isValid) return@setOnClickListener

            binding.btnChangePassword.isEnabled = false
            binding.btnChangePassword.text = "Updating..."

            authHelper.changePassword(currentPassword, newPassword) { result ->
                binding.btnChangePassword.isEnabled = true
                binding.btnChangePassword.text = "Update Password"

                when (result) {
                    is AuthResult.Success -> {
                        binding.etCurrentPassword.text?.clear()
                        binding.etNewPassword.text?.clear()
                        Toast.makeText(this, "Password updated successfully! ✓", Toast.LENGTH_LONG).show()
                    }
                    is AuthResult.Error -> {
                        Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // Logout
        binding.btnLogout.setOnClickListener {
            authHelper.signOut()
            sessionManager.logout()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun loadProfile() {
        val userId: String = sessionManager.getUserId()
        val email: String = sessionManager.getUserEmail()
        binding.tvEmail.text = email

        lifecycleScope.launch {
            AppDatabase.populateSampleDataIfEmpty(db)

            val user = db.userDao().getUserById(userId)
            if (user != null) {
                binding.tvFullName.text = user.fullName
                binding.etEditName.setText(user.fullName)
                binding.etEditPhone.setText(user.phone)
                binding.tvRole.text = user.role.replaceFirstChar { it.uppercase() }
                binding.tvAvatar.text = if (user.fullName.isNotEmpty()) user.fullName.first().uppercase() else "?"

                val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                binding.tvMemberSince.text = sdf.format(Date(user.createdAt))
            } else {
                Toast.makeText(this@ProfileActivity, "Loaded session for $email", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadStats() {
        val userId: String = sessionManager.getUserId()

        lifecycleScope.launch {
            AppDatabase.populateSampleDataIfEmpty(db)

            val reports = db.reportDao().getReportsByUserId(userId)
            val total = reports.size
            val resolved = reports.count { it.status == "Resolved" }
            val pending = reports.count { it.status == "Pending" || it.status == "Assigned" }

            binding.tvTotalReports.text = total.toString()
            binding.tvResolvedCount.text = resolved.toString()
            binding.tvPendingCount.text = pending.toString()
        }
    }
}