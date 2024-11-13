// ServiceAdapter.kt
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
import com.example.againSPOT.DetailsActivity
import com.example.againSPOT.databinding.ServiceItemBinding
import com.example.againSPOT.model.ServiceItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class ServiceAdapter(
    private var serviceItems: List<ServiceItem>,
    private val context: Context
) : RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val userId: String = auth.currentUser?.uid ?: ""
    private val cartItemsReference: DatabaseReference =
        database.reference.child("user").child(userId).child("CartItems")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val binding = ServiceItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return ServiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        holder.bind(serviceItems[position])
    }

    override fun getItemCount(): Int = serviceItems.size

    fun updateServiceItems(filteredServiceItems: List<ServiceItem>) {
        this.serviceItems = filteredServiceItems
        notifyDataSetChanged()
    }

    inner class ServiceViewHolder(private val binding: ServiceItemBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    openDetailsActivity(serviceItems[position])
                }
            }
            binding.addToCartPopular.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    addToCart(serviceItems[position])
                }
            }
        }

        private fun openDetailsActivity(serviceItem: ServiceItem) {
            val intent = Intent(context, DetailsActivity::class.java).apply {
                putExtra("serviceItemsName", serviceItem.allServiceName)
                putExtra("serviceItemsPrice", serviceItem.allServicePrice)
                putExtra("serviceItemsDescription", serviceItem.allServiceDescription)
                putExtra("serviceItemsImage", serviceItem.allServiceImage)
                putExtra("serviceItemsSuitable", serviceItem.allServiceSuitable)
            }
            context.startActivity(intent)
        }

        private fun addToCart(serviceItem: ServiceItem) {
            val serviceName = serviceItem.allServiceName ?: ""
            val servicePrice = serviceItem.allServicePrice ?: "0.00"
            val serviceImage = serviceItem.allServiceImage ?: ""
            val allServiceID = serviceItem.allServiceID ?: ""

            cartItemsReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var itemExists = false
                    var existingItemKey: String? = null
                    var existingQuantity = 0

                    for (cartItemSnapshot in snapshot.children) {
                        val existingID = cartItemSnapshot.child("allServiceID").getValue(String::class.java) ?: ""
                        if (existingID == allServiceID) {
                            itemExists = true
                            existingItemKey = cartItemSnapshot.key
                            existingQuantity = cartItemSnapshot.child("allServiceQuantity").getValue(Int::class.java) ?: 0
                            break
                        }
                    }

                    if (itemExists && existingItemKey != null) {
                        val newQuantity = existingQuantity + 1
                        cartItemsReference.child(existingItemKey).child("allServiceQuantity").setValue(newQuantity)
                            .addOnSuccessListener {
                                Toast.makeText(context, "$serviceName quantity updated", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        val newCartItem = mapOf(
                            "allServiceName" to serviceName,
                            "allServicePrice" to servicePrice,
                            "allServiceQuantity" to 1,
                            "allServiceImage" to serviceImage,
                            "allServiceID" to allServiceID
                        )
                        val newCartItemReference = cartItemsReference.push()
                        newCartItemReference.setValue(newCartItem).addOnSuccessListener {
                            Toast.makeText(context, "$serviceName added to cart", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener { exception ->
                            Log.e("ServiceAdapter", "Failed to add item to cart: ${exception.message}")
                            Toast.makeText(context, "Failed to add item to cart", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ServiceAdapter", "Failed to check if item exists in cart: ${error.message}")
                    Toast.makeText(context, "Failed to check cart items", Toast.LENGTH_SHORT).show()
                }
            })
        }

        fun bind(serviceItem: ServiceItem) {
            binding.apply {
                serviceNamePopular.text = serviceItem.allServiceName?.take(20) + if (serviceItem.allServiceName?.length ?: 0 > 20) "..." else ""
                servicePricePopular.text = "RM ${"%.2f".format(serviceItem.allServicePrice?.toDoubleOrNull() ?: 0.0)}"
                Glide.with(context).load(Uri.parse(serviceItem.allServiceImage)).into(serviceImagePopular)
            }
        }
    }
}
