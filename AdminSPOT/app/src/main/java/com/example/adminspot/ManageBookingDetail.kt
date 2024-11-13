package com.example.adminspot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.adminspot.databinding.ActivityManageBookingDetailBinding
import com.example.adminspot.model.OrderDetails

class ManageBookingDetail : AppCompatActivity() {

    private lateinit var binding: ActivityManageBookingDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageBookingDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve the OrderDetails object passed from the previous activity
        val orderDetails = intent.getSerializableExtra("orderDetails") as? OrderDetails
        orderDetails?.let { setupUI(it) }

        // Back button listener to close the activity
        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    // Setup the RecyclerView and bind the data
    private fun setupUI(orderDetails: OrderDetails) {
        binding.recyclerViewRecentBuy.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewRecentBuy.adapter = BookingDetailsAdapter(orderDetails)
    }

    // Adapter for RecyclerView to display order details
    inner class BookingDetailsAdapter(private val orderDetails: OrderDetails) : RecyclerView.Adapter<BookingDetailsAdapter.BookingDetailViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingDetailViewHolder {
            val binding = com.example.adminspot.databinding.BookingDetailItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
            return BookingDetailViewHolder(binding)
        }

        override fun onBindViewHolder(holder: BookingDetailViewHolder, position: Int) {
            holder.bind(orderDetails, position)
        }

        override fun getItemCount(): Int = orderDetails.serviceNames?.size ?: 0

        // ViewHolder class to bind the data for each item
        inner class BookingDetailViewHolder(private val binding: com.example.adminspot.databinding.BookingDetailItemBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(orderDetails: OrderDetails, position: Int) {
                binding.apply {
                    serviceNameHistory.text = orderDetails.serviceNames?.get(position) ?: "N/A"
                    servicePriceHistory.text = orderDetails.servicePrices?.get(position) ?: "N/A"
                    serviceQuantityHistory.text = orderDetails.serviceQuantities?.get(position)?.toString() ?: "0"

                    // Load image from URL or show a placeholder
                    val uriString = orderDetails.serviceImages?.getOrNull(position) ?: ""
                    if (uriString.isNotEmpty()) {
                        Glide.with(binding.root.context).load(uriString).into(serviceImageHistory)
                    } else {
                        serviceImageHistory.setImageResource(R.drawable.placeholder_image) // Placeholder image
                    }
                }
            }
        }
    }
}
