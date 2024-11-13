package com.example.adminspot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.adminspot.databinding.ActivityAdminProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.example.adminspot.model.AdminModel

class AdminProfile : AppCompatActivity() {
    private val binding: ActivityAdminProfileBinding by lazy {
        ActivityAdminProfileBinding.inflate(layoutInflater)
    }
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        binding.backHomeBtnProfile.setOnClickListener {
            finish()
        }

        binding.name.isEnabled = false
        binding.email.isEnabled = false
        binding.phone.isEnabled = false

        var isEnable = false
        binding.edit.setOnClickListener {
            isEnable = !isEnable
            binding.name.isEnabled = isEnable
            binding.email.isEnabled = isEnable
            binding.phone.isEnabled = isEnable
            if (isEnable) {
                binding.name.requestFocus()
            }
        }

        binding.editBtn.setOnClickListener {
            if (isEnable) {
                saveUpdatedProfile()
            }
        }

        loadAdminProfile()
    }

    private fun loadAdminProfile() {
        val adminId = auth.currentUser?.uid ?: return
        database.child("admin").child(adminId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val admin = snapshot.getValue(AdminModel::class.java)
                admin?.let {
                    binding.name.setText(it.username)
                    binding.email.setText(it.email)
                    binding.phone.setText(it.phone)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors
                Toast.makeText(this@AdminProfile, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveUpdatedProfile() {
        val adminId = auth.currentUser?.uid ?: return
        val updatedUsername = binding.name.text.toString().trim()
        val updatedEmail = binding.email.text.toString().trim()
        val updatedPhone = binding.phone.text.toString().trim()

        val updatedAdmin = AdminModel(username = updatedUsername, email = updatedEmail, phone = updatedPhone)
        database.child("admin").child(adminId).setValue(updatedAdmin).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                binding.name.isEnabled = false
                binding.email.isEnabled = false
                binding.phone.isEnabled = false
            } else {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
