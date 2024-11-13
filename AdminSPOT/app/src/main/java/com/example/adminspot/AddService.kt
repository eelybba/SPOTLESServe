package com.example.adminspot

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.adminspot.databinding.ActivityAddServiceBinding
import com.example.adminspot.model.AllMenu
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class AddService : AppCompatActivity() {

    // Service Item Details
    private lateinit var allServiceName: String
    private lateinit var allServicePrice: String
    private lateinit var allServiceDescription: String
    private lateinit var allServiceSuitable: String
    private var allServiceImage: Uri? = null

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private val binding: ActivityAddServiceBinding by lazy {
        ActivityAddServiceBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        binding.addServiceBtn.setOnClickListener {
            // Get data from user-filled info
            allServiceName = binding.enterServiceName.text.toString().trim()
            allServicePrice = binding.enterServicePrice.text.toString().trim()
            allServiceDescription = binding.description.text.toString().trim()
            allServiceSuitable = binding.descriptionSuitable.text.toString().trim()

            if (allServiceName.isNotBlank() && allServicePrice.isNotBlank() && allServiceDescription.isNotBlank() && allServiceSuitable.isNotBlank()) {
                if (allServiceImage != null) {
                    uploadData()
                } else {
                    Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all the required information", Toast.LENGTH_SHORT).show()
            }
        }

        binding.selectImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun uploadData() {
        // Show progress bar before starting the upload
        binding.progressBar.visibility = View.VISIBLE

        val menuRef = database.child("menu")
        val newItemKey: String? = menuRef.push().key

        if (allServiceImage != null && newItemKey != null) {
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("menu_images/$newItemKey.jpg")

            imageRef.putFile(allServiceImage!!)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        val newItem = AllMenu(
                            allServiceName = allServiceName,
                            allServicePrice = allServicePrice,
                            allServiceDescription = allServiceDescription,
                            allServiceSuitable = allServiceSuitable,
                            allServiceImage = downloadUri.toString()
                        )

                        menuRef.child(newItemKey).setValue(newItem)
                            .addOnSuccessListener {
                                Toast.makeText(this, "New service added successfully", Toast.LENGTH_SHORT).show()
                                // Hide progress bar after successful upload
                                binding.progressBar.visibility = View.GONE
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Data upload failed", Toast.LENGTH_SHORT).show()
                                // Hide progress bar in case of error
                                binding.progressBar.visibility = View.GONE
                            }
                    }.addOnFailureListener {
                        Toast.makeText(this, "Failed to retrieve image URL", Toast.LENGTH_SHORT).show()
                        // Hide progress bar in case of error
                        binding.progressBar.visibility = View.GONE
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                    // Hide progress bar in case of error
                    binding.progressBar.visibility = View.GONE
                }
        }
    }

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                allServiceImage = uri
                binding.selectedImage.setImageURI(uri)
            }
        }
}



