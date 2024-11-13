package com.example.againSPOT.Fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.againSPOT.databinding.FragmentProfileBinding
import com.example.againSPOT.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.util.Log
import com.example.againSPOT.Login

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentProfileBinding.inflate(inflater, container, false)

        // Set user data when the view is created
        setUserData()

        binding.apply {
            name.isEnabled = false
            email.isEnabled = false
            phone.isEnabled = false

            editBtn.setOnClickListener {
                name.isEnabled = !name.isEnabled
                email.isEnabled = !email.isEnabled
                phone.isEnabled = !phone.isEnabled
            }

            saveInfoBtn.setOnClickListener {
                val name = binding.name.text.toString()
                val email = binding.email.text.toString()
                val phone = binding.phone.text.toString()

                updateUserData(name, email, phone)
            }

            logOut.setOnClickListener {
                auth.signOut()
                val intent = Intent(requireContext(), Login::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                activity?.finish()
            }
        }

        return binding.root
    }

    private fun updateUserData(name: String, email: String, phone: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userReference = database.getReference("user").child(userId)
            val userData = hashMapOf(
                "name" to name,
                "email" to email,
                "phone" to phone
            )
            userReference.updateChildren(userData as Map<String, Any>).addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                binding.name.isEnabled = false
                binding.email.isEnabled = false
                binding.phone.isEnabled = false
                setUserData() // Refresh data after update
            }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Profile update failed", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userReference = database.getReference("user").child(userId)

            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userProfile = snapshot.getValue(UserModel::class.java)
                        if (userProfile != null) {
                            binding.name.setText(userProfile.name ?: "No name provided")
                            binding.email.setText(userProfile.email ?: "No email provided")
                            binding.phone.setText(userProfile.phone ?: "No phone number provided")
                        } else {
                            Log.d("ProfileFragment", "User profile is null")
                        }
                    } else {
                        Log.d("ProfileFragment", "Snapshot does not exist")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to load profile data", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}