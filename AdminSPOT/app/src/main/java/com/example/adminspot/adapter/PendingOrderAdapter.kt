package com.example.adminspot.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.adminspot.PendingOrderDetails
import com.example.adminspot.R
import com.example.adminspot.databinding.PendingOrderItemBinding
import com.example.adminspot.model.OrderDetails
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class PendingOrderAdapter(
    private val context: Context,
    private val orderList: MutableList<OrderDetails>,
    private val itemClicked: OnItemClicked
) : RecyclerView.Adapter<PendingOrderAdapter.PendingOrderViewHolder>() {

    interface OnItemClicked {
        fun onItemClickListener(position: Int)
        fun onItemAcceptClickListener(position: Int)
    }

    private val databaseOrderDetails: DatabaseReference = FirebaseDatabase.getInstance().reference.child("OrderDetails")
    private val completedOrderRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("CompletedOrder")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingOrderViewHolder {
        val binding = PendingOrderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PendingOrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PendingOrderViewHolder, position: Int) {
        holder.bind(orderList[position])
    }

    override fun getItemCount(): Int = orderList.size

    inner class PendingOrderViewHolder(private val binding: PendingOrderItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(orderDetails: OrderDetails) {
            binding.apply {
                // Display user name, with fallback to "Unknown"
                orderedCustomerName.text = orderDetails.userName?.takeIf { it.isNotEmpty() } ?: "Unknown"

                // Ensure total price is correctly set and displayed
                totalPrice.text = orderDetails.totalPrice?.let {
                    "$it"
                } ?: "N/A"

                // Load image if available, or use placeholder
                val uriString = orderDetails.serviceImages?.firstOrNull() ?: ""
                if (uriString.isNotEmpty()) {
                    val uri = Uri.parse(uriString)
                    Glide.with(binding.root.context).load(uri).into(orderedImage)
                } else {
                    orderedImage.setImageResource(R.drawable.placeholder_image)
                }

                // Set button text depending on order status
                orderedAcceptBtn.apply {
                    text = if (orderDetails.orderAccepted) {
                        "Received"
                    } else {
                        "Accept"
                    }
                    setOnClickListener {
                        if (!orderDetails.orderAccepted) {
                            // Mark the order as accepted and update Firebase
                            orderDetails.orderAccepted = true
                            updateOrderStatusInFirebase(orderDetails, adapterPosition)
                        }
                    }
                }

                // Open details on item click
                itemView.setOnClickListener {
                    val intent = Intent(context, PendingOrderDetails::class.java)
                    intent.putExtra("OrderDetails", orderDetails)
                    context.startActivity(intent)
                }
            }
        }

        private fun updateOrderStatusInFirebase(orderDetails: OrderDetails, position: Int) {
            val orderReference = databaseOrderDetails.child(orderDetails.itemPushKey ?: return)
            val completedOrderReference = completedOrderRef.child(orderDetails.itemPushKey ?: return)

            // Step 1: Set orderAccepted to true in "OrderDetails"
            orderReference.child("orderAccepted").setValue(true).addOnSuccessListener {
                // Step 2: Move the accepted order to "CompletedOrder" in Firebase
                completedOrderReference.setValue(orderDetails).addOnSuccessListener {
                    // Step 3: Remove the order from "OrderDetails"
                    orderReference.removeValue().addOnSuccessListener {
                        Toast.makeText(context, "Order accepted and moved to CompletedOrder", Toast.LENGTH_SHORT).show()
                        // Remove the order from the local list and update the RecyclerView
                        orderList.removeAt(position)
                        notifyDataSetChanged() // Refresh UI to remove the accepted order
                    }.addOnFailureListener {
                        Toast.makeText(context, "Failed to remove order from OrderDetails", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(context, "Failed to add order to CompletedOrder", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to accept order", Toast.LENGTH_SHORT).show()
            }
        }
    }
}