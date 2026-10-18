package com.cleanflow.app.help

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cleanflow.app.databinding.ActivityHelpSupportBinding

class HelpSupportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHelpSupportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpSupportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        // Hotline Call Intent
        binding.btnCallHotline.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:0800253263"))
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Emergency Hotline: 0800 253 263", Toast.LENGTH_LONG).show()
            }
        }

        // Email Support Intent
        binding.btnEmailSupport.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@cleanflow.org")
                putExtra(Intent.EXTRA_SUBJECT, "CleanFlow Water Emergency Inquiry")
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Support Email: support@cleanflow.org", Toast.LENGTH_LONG).show()
            }
        }
    }
}