package com.example.againSPOT.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.againSPOT.OrderHistoryItems
import com.example.againSPOT.databinding.BuyAgainItemBinding
import com.example.againSPOT.model.OrderDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class BuyAgainAdapter(
    private val orderList: MutableList<OrderDetails>,
    private var requiredContext: Context
) : RecyclerView.Adapter<BuyAgainAdapter.BuyAgainViewHolder>() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val userId: String = auth.currentUser?.uid ?: ""
    private val cartItemsReference: DatabaseReference =
        database.reference.child("user").child(userId).child("CartItems")

    override fun onBindViewHolder(holder: BuyAgainViewHolder, position: Int) {
        holder.bind(orderList[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuyAgainViewHolder {
        val binding = BuyAgainItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BuyAgainViewHolder(binding)
    }

    override fun getItemCount(): Int = orderList.size

    inner class BuyAgainViewHolder(private val binding: BuyAgainItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(orderDetails: OrderDetails) {
            binding.buyAgainServiceName.text = orderDetails.serviceNames?.firstOrNull() ?: ""

            // Calculate the total price for all items in the order
            val totalPrice = orderDetails.servicePrices?.zip(orderDetails.serviceQuantities ?: listOf()) { price, quantity ->
                price.toDoubleOrNull()?.times(quantity) ?: 0.0
            }?.sum() ?: 0.0

            binding.buyAgainServicePrice.text = "RM%.2f".format(totalPrice) // Display total price

            val uri = orderDetails.serviceImages?.firstOrNull()?.let { Uri.parse(it) }
            if (uri != null) {
                Glide.with(requiredContext).load(uri).into(binding.buyAgainServiceImage)
            }

            // Set click listener for "Order Again" button
            binding.buyAgainServiceButton.setOnClickListener {
                addToCart(orderDetails)
            }

            // Set click listener to view order details
            binding.root.setOnClickListener {
                val intent = Intent(requiredContext, OrderHistoryItems::class.java)
                intent.putExtra("OrderDetails", orderDetails)
                requiredContext.startActivity(intent)
            }
        }

        private fun addToCart(orderDetails: OrderDetails) {
            orderDetails.serviceNames?.forEachIndexed { index, serviceName ->
                val servicePrice = orderDetails.servicePrices?.getOrNull(index) ?: "0.00"
                val serviceQuantity = orderDetails.serviceQuantities?.getOrNull(index) ?: 1
                val serviceImage = orderDetails.serviceImages?.getOrNull(index) ?: ""

                // Check if the item already exists in the cart
                cartItemsReference.orderByChild("allServiceName").equalTo(serviceName)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                // If item exists, update its quantity
                                for (cartItemSnapshot in snapshot.children) {
                                    val existingQuantity =
                                        cartItemSnapshot.child("allServiceQuantity").getValue(Int::class.java) ?: 0
                                    val newQuantity = existingQuantity + serviceQuantity

                                    cartItemSnapshot.ref.child("allServiceQuantity").setValue(newQuantity)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                requiredContext,
                                                "$serviceName quantity updated to $newQuantity",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }.addOnFailureListener { exception ->
                                            Log.e("BuyAgainAdapter", "Failed to update item quantity: ${exception.message}")
                                            Toast.makeText(requiredContext, "Failed to update item quantity", Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                }
                            } else {
                                // If item does not exist, add it to the cart
                                val newCartItem = mapOf(
                                    "allServiceName" to serviceName,
                                    "allServicePrice" to servicePrice,
                                    "allServiceQuantity" to serviceQuantity,
                                    "allServiceImage" to serviceImage
                                )

                                val newCartItemReference = cartItemsReference.push()
                                newCartItemReference.setValue(newCartItem).addOnSuccessListener {
                                    Toast.makeText(requiredContext, "$serviceName added to cart", Toast.LENGTH_SHORT).show()
                                }.addOnFailureListener { exception ->
                                    Log.e("BuyAgainAdapter", "Failed to add item to cart: ${exception.message}")
                                    Toast.makeText(requiredContext, "Failed to add item to cart", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("BuyAgainAdapter", "Failed to check if item exists in cart: ${error.message}")
                        }
                    })
            }
        }
    }
}
