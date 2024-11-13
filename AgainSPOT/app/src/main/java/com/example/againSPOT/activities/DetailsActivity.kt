package com.example.againSPOT

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.againSPOT.databinding.ActivityDetailsBinding
import com.example.againSPOT.model.CartItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class DetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailsBinding
    private var allServiceName: String? = null
    private var allServicePrice: String? = null
    private var allServiceDescription: String? = null
    private var allServiceImage: String? = null
    private var allServiceSuitable: String? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Retrieve data from the intent
        allServiceName = intent.getStringExtra("serviceItemsName")
        allServicePrice = intent.getStringExtra("serviceItemsPrice")
        allServiceDescription = intent.getStringExtra("serviceItemsDescription")
        allServiceSuitable = intent.getStringExtra("serviceItemsSuitable")
        allServiceImage = intent.getStringExtra("serviceItemsImage")

        // Populate UI with data
        with(binding) {
            detailServiceName.text = allServiceName
            detailServicePrice.text = allServicePrice
            detailDescription.text = allServiceDescription
            detailSuitable.text = allServiceSuitable
            Glide.with(this@DetailsActivity).load(allServiceImage).into(detailServiceImage)
        }

        // Back button functionality
        binding.imageButton2.setOnClickListener {
            finish() // Closes the activity
        }

        // Set addItemBtn to act as "Add to Cart" button
        binding.addItemBtn.text = "Add to Cart"
        binding.addItemBtn.setOnClickListener {
            addItemToCart() // Adds the item to the cart
        }
    }

    private fun addItemToCart() {
        val database = FirebaseDatabase.getInstance().reference
        val userId = auth.currentUser?.uid ?: ""

        // Check if the item already exists in the cart
        val cartItemsReference = database.child("user").child(userId).child("CartItems")

        cartItemsReference.orderByChild("allServiceName").equalTo(allServiceName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var itemExists = false

                    for (cartItemSnapshot in snapshot.children) {
                        val existingPrice = cartItemSnapshot.child("allServicePrice").getValue(String::class.java) ?: ""
                        if (existingPrice == allServicePrice) {
                            // If item with the same name and price exists, update its quantity
                            val existingQuantity = cartItemSnapshot.child("allServiceQuantity").getValue(Int::class.java) ?: 0
                            val newQuantity = existingQuantity + 1
                            cartItemSnapshot.ref.child("allServiceQuantity").setValue(newQuantity)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this@DetailsActivity,
                                        "$allServiceName quantity updated to $newQuantity",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }.addOnFailureListener {
                                    Toast.makeText(this@DetailsActivity, "Failed to update item quantity", Toast.LENGTH_SHORT).show()
                                }
                            itemExists = true
                            break
                        }
                    }

                    // If item does not exist, add it to the cart
                    if (!itemExists) {
                        val newCartItem = CartItems(
                            allServiceName = allServiceName ?: "",
                            allServicePrice = allServicePrice ?: "",
                            allServiceDescription = allServiceDescription ?: "",
                            allServiceSuitable = allServiceSuitable ?: "",
                            allServiceImage = allServiceImage ?: "",
                            allServiceQuantity = 1
                        )

                        val newCartItemReference = cartItemsReference.push()
                        newCartItemReference.setValue(newCartItem).addOnSuccessListener {
                            Toast.makeText(this@DetailsActivity, "$allServiceName added to cart", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener {
                            Toast.makeText(this@DetailsActivity, "Failed to add item to cart", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@DetailsActivity, "Failed to check cart items: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
