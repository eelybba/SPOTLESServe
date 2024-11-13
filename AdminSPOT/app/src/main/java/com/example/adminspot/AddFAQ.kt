package com.example.adminspot

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.adminspot.databinding.ActivityAddFaqBinding
import com.example.adminspot.model.AllFaq
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class AddFAQ : AppCompatActivity() {

    // Service Item Details
    private lateinit var allFaqQuestion: String
    private lateinit var allFaqAnswer: String
    private var allFaqImage: Uri? = null

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private val binding: ActivityAddFaqBinding by lazy {
        ActivityAddFaqBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        binding.addFaqBtn.setOnClickListener {
            allFaqQuestion = binding.enterFaqQuestion.text.toString().trim()
            allFaqAnswer = binding.enterFaqAnswer.text.toString().trim()

            if (allFaqQuestion.isNotBlank() && allFaqAnswer.isNotBlank()) {
                if (allFaqImage != null) {
                    uploadData()
                } else {
                    Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all the required information", Toast.LENGTH_SHORT).show()
            }
        }

        binding.selectFaqImage.setOnClickListener {
            pickImageFAQ.launch("image/*")
        }

        binding.backHomeBtn.setOnClickListener {
            finish()
        }
    }

    private fun uploadData() {
        // Show progress bar before starting the upload
        binding.progressBar.visibility = View.VISIBLE

        val faqRef = database.child("faq")
        val newItemKey: String? = faqRef.push().key

        if (allFaqImage != null && newItemKey != null) {
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("faq_images/$newItemKey.jpg")

            imageRef.putFile(allFaqImage!!)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        val newItem = AllFaq(
                            allFaqQuestion = allFaqQuestion,
                            allFaqAnswer = allFaqAnswer,
                            allFaqImage = downloadUri.toString()
                        )

                        faqRef.child(newItemKey).setValue(newItem)
                            .addOnSuccessListener {
                                Toast.makeText(this, "FAQ added successfully", Toast.LENGTH_SHORT).show()
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

    private val pickImageFAQ =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                allFaqImage = uri
                binding.selectedFAQImage.setImageURI(uri)
            }
        }
}
