package com.example.againSPOT.adapter

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.againSPOT.databinding.CartItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CartAdapter(
    private val context: Context,
    private val cartItems: MutableList<String>,
    private val cartItemPrices: MutableList<String>,
    private val cartDescription: MutableList<String>,
    private val cartSuitable: MutableList<String>,
    private val cartImages: MutableList<String>,
    private val cartQuantities: MutableList<Int>
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val auth = FirebaseAuth.getInstance()
    private lateinit var cartItemsReference: DatabaseReference
    private var itemQuantities: IntArray = cartQuantities.toIntArray()

    init {
        val database = FirebaseDatabase.getInstance()
        val userId = auth.currentUser?.uid ?: ""
        cartItemsReference = database.reference.child("user").child(userId).child("CartItems")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = CartItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        // Ensure proper data is bound to the view holder
        holder.bind(position)
    }

    override fun getItemCount(): Int = cartItems.size

    inner class CartViewHolder(private val binding: CartItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                // Check if the position is valid for all data arrays
                if (position < cartItems.size && position < cartItemPrices.size && position < cartImages.size) {
                    // Truncate service name if it’s too long
                    val serviceName = if (cartItems[position].length > 20) {
                        "${cartItems[position].substring(0, 17)}..."
                    } else {
                        cartItems[position]
                    }

                    // Bind the name, price, and other details
                    cartServiceName.text = serviceName
                    cartServicePrice.text = "RM " + cartItemPrices[position]

                    // Validate the image URI before loading with Glide
                    val uriString = cartImages[position]
                    if (uriString.isNotEmpty()) {
                        val uri = Uri.parse(uriString)
                        Glide.with(context).load(uri).into(cartImage)
                    }

                    // Set the quantity
                    cartItemsquantity.text = itemQuantities[position].toString()

                    // Handle button clicks for increasing, decreasing, and deleting items
                    plusBtn.setOnClickListener { increaseQuantity(position) }
                    minusBtn.setOnClickListener { decreaseQuantity(position) }
                    deleteBtn.setOnClickListener { deleteItem(position) }
                }
            }
        }

        private fun increaseQuantity(position: Int) {
            if (itemQuantities[position] < 10) {
                itemQuantities[position]++
                cartQuantities[position] = itemQuantities[position]
                binding.cartItemsquantity.text = itemQuantities[position].toString()
                updateQuantityInFirebase(position, itemQuantities[position])
            }
        }

        private fun decreaseQuantity(position: Int) {
            if (itemQuantities[position] > 1) {
                itemQuantities[position]--
                cartQuantities[position] = itemQuantities[position]
                binding.cartItemsquantity.text = itemQuantities[position].toString()
                updateQuantityInFirebase(position, itemQuantities[position])
            }
        }

        private fun updateQuantityInFirebase(position: Int, newQuantity: Int) {
            getUniqueKeyAtPosition(position) { uniqueKey ->
                uniqueKey?.let {
                    cartItemsReference.child(it).child("allServiceQuantity").setValue(newQuantity)
                }
            }
        }

        private fun deleteItem(position: Int) {
            getUniqueKeyAtPosition(position) { uniqueKey ->
                uniqueKey?.let {
                    removeItem(position, it)
                }
            }
        }

        private fun removeItem(position: Int, uniqueKey: String) {
            cartItemsReference.child(uniqueKey).removeValue().addOnSuccessListener {
                cartItems.removeAt(position)
                cartItemPrices.removeAt(position)
                cartDescription.removeAt(position)
                cartSuitable.removeAt(position)
                cartImages.removeAt(position)
                cartQuantities.removeAt(position)

                Toast.makeText(context, "Item deleted", Toast.LENGTH_SHORT).show()

                // Update itemQuantities array
                itemQuantities = itemQuantities.filterIndexed { index, _ -> index != position }.toIntArray()

                notifyItemRemoved(position)
                notifyItemRangeChanged(position, cartItems.size)
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to delete item", Toast.LENGTH_SHORT).show()
            }
        }

        private fun getUniqueKeyAtPosition(positionRetrieve: Int, onComplete: (String?) -> Unit) {
            cartItemsReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var uniqueKey: String? = null
                    snapshot.children.forEachIndexed { index, dataSnapshot ->
                        if (index == positionRetrieve) {
                            uniqueKey = dataSnapshot.key
                            return@forEachIndexed
                        }
                    }
                    onComplete(uniqueKey)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("Firebase", "Failed to retrieve unique key")
                }
            })
        }
    }
}









