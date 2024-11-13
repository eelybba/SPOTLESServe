package com.example.adminspot

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.adminspot.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etEmail: EditText
    private lateinit var btnResetPassword: Button

    private val binding: ActivityForgotPasswordBinding by lazy {
        ActivityForgotPasswordBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        etEmail = findViewById(R.id.emailReset)
        btnResetPassword = findViewById(R.id.resetPasswordBtn)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        btnResetPassword.setOnClickListener {
            val email = etEmail.text.toString()

            if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "Reset password email sent, please check!", Toast.LENGTH_SHORT).show()

                    // Redirect to login page after the reset email is sent
                    val intent = Intent(this, Login::class.java)
                    startActivity(intent)
                    finish() // Close the current activity to prevent returning to it
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to send reset email: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}



