package com.example.adminspot.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.adminspot.ManageBookingDetail
import com.example.adminspot.databinding.DeliveryItemBinding
import com.example.adminspot.model.OrderDetails
import com.google.firebase.database.FirebaseDatabase

class DeliveryAdapter(
    private val context: Context,
    private val customerOrders: MutableList<OrderDetails>
) : RecyclerView.Adapter<DeliveryAdapter.DeliveryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeliveryViewHolder {
        val binding = DeliveryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeliveryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeliveryViewHolder, position: Int) {
        holder.bind(customerOrders[position])
    }

    override fun getItemCount(): Int = customerOrders.size

    inner class DeliveryViewHolder(private val binding: DeliveryItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(orderDetails: OrderDetails) {
            binding.apply {
                customerName.text = orderDetails.userName ?: "Unknown"
                moneyStatus.text = orderDetails.totalPrice ?: "RM8"

                // Set up Spinner for order status
                val statuses = listOf("Order Placed", "Picked up", "Washing in process", "Ready", "Delivered")
                val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, statuses)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                statusSpinner.adapter = adapter

                // Set Spinner to current status
                orderDetails.status?.let {
                    val statusIndex = statuses.indexOf(it)
                    if (statusIndex >= 0) {
                        statusSpinner.setSelection(statusIndex)
                    }
                }

                // Handle Spinner selection changes
                statusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val newStatus: String = statuses[position]
                        if (orderDetails.status != newStatus) {
                            orderDetails.status = newStatus
                            updateOrderStatus(orderDetails.itemPushKey ?: "", newStatus)
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // No action needed here
                    }
                }

                // Set up the item click listener to navigate to the ManageBookingDetail activity
                itemView.setOnClickListener {
                    val intent = Intent(context, ManageBookingDetail::class.java)
                    intent.putExtra("orderDetails", orderDetails)  // Pass the order details to the next activity
                    context.startActivity(intent)
                }
            }
        }

        private fun updateOrderStatus(orderId: String, newStatus: String) {
            if (orderId.isNotEmpty()) {
                val orderRef = FirebaseDatabase.getInstance().getReference("CompletedOrder").child(orderId)
                val updatedValues = hashMapOf<String, Any>(
                    "status" to newStatus,
                    "notificationTrigger" to System.currentTimeMillis()  // Trigger for notifying customer
                )
                orderRef.updateChildren(updatedValues)
            }
        }
    }
}
