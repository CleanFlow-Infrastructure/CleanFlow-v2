package com.cleanflow.app

import android.content.Intent
import android.os.Bundle
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cleanflow.app.auth.AuthHelper
import com.cleanflow.app.auth.LoginActivity
import com.cleanflow.app.auth.SessionManager
import com.cleanflow.app.data.AppDatabase
import com.cleanflow.app.databinding.ActivitySplashBinding
import com.cleanflow.app.home.HomeActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var sessionManager: SessionManager
    private val authHelper = AuthHelper.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Animate branding logo on startup
        val animSet = AnimationSet(true).apply {
            addAnimation(AlphaAnimation(0.2f, 1.0f).apply { duration = 1200 })
            addAnimation(ScaleAnimation(0.85f, 1.0f, 0.85f, 1.0f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f).apply { duration = 1200 })
        }
        binding.containerBranding.startAnimation(animSet)

        // Seed database pre-population in background
        val db = AppDatabase.getDatabase(this)

        lifecycleScope.launch {
            AppDatabase.populateSampleDataIfEmpty(db)
            delay(1800L) // 1.8 seconds splash display

            if (authHelper.isLoggedIn || sessionManager.isLoggedIn()) {
                startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
            } else {
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            }
            finish()
        }
    }
}