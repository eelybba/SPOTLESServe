package com.example.adminspot

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.adminspot.databinding.ActivityMainBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var logout: ImageButton

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    // Request code for PendingOrder activity
    private val REQUEST_CODE_PENDING_ORDER = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Check if the user is authenticated when the activity starts
        val user = Firebase.auth.currentUser
        if (user == null) {
            Log.d("MainActivity", "User is not logged in, redirecting to Login")
            // If no user is logged in, redirect to the login screen
            startActivity(Intent(this, Login::class.java))
            finish()
        }

        // Set up click listeners for different actions
        binding.addService.setOnClickListener {
            startActivity(Intent(this, AddService::class.java))
        }

        binding.allServiceMenu.setOnClickListener {
            startActivity(Intent(this, AllServices::class.java))
        }

        binding.addFAQ.setOnClickListener {
            startActivity(Intent(this, AddFAQ::class.java))
        }

        binding.allFAQ.setOnClickListener {
            startActivity(Intent(this, AllFAQ::class.java))
        }

        binding.outForDeliveryBtn.setOnClickListener {
            startActivity(Intent(this, OutForDelivery::class.java))
        }

        binding.profileAdmin.setOnClickListener {
            startActivity(Intent(this, AdminProfile::class.java))
        }

        binding.pendingOrderTextView.setOnClickListener {
            Log.d("MainActivity", "Opening PendingOrder activity")
            // Start PendingOrder activity and expect a result
            startActivityForResult(Intent(this, PendingOrder::class.java), REQUEST_CODE_PENDING_ORDER)
        }

        logout = findViewById(R.id.logOut)
        logout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        // Create an AlertDialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to logout?")

        // Set up the buttons
        builder.setPositiveButton("Yes") { dialog, _ ->
            // User clicked Yes, so log out
            Firebase.auth.signOut()
            startActivity(Intent(this, Login::class.java))
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            // User clicked Cancel, so dismiss the dialog
            dialog.dismiss()
        }

        // Show the AlertDialog
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PENDING_ORDER && resultCode == RESULT_OK) {
            // Get pending order count from the result
            val pendingOrderCount = data?.getIntExtra("PendingOrderCount", 1) ?: 1
            Log.d("MainActivity", "Pending order count received: $pendingOrderCount")
            //binding.pendingOrder111.text = pendingOrderCount.toString() // Update the TextView
        }
    }
}
