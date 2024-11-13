package com.example.adminspot

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.adminspot.databinding.ActivityPendingOrderDetailBinding
import com.example.adminspot.model.OrderDetails

class PendingOrderDetails : AppCompatActivity() {

    private lateinit var binding: ActivityPendingOrderDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPendingOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the OrderDetails passed from the adapter
        val orderDetails: OrderDetails? = intent.getSerializableExtra("OrderDetails") as? OrderDetails
        orderDetails?.let { setupUI(it) }

        // Back button click listener
        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun setupUI(orderDetails: OrderDetails) {
        // Set up the UI elements with the order details data
        binding.recyclerViewRecentBuy.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewRecentBuy.adapter = OrderDetailsAdapter(orderDetails)
    }

    inner class OrderDetailsAdapter(private val orderDetails: OrderDetails) : RecyclerView.Adapter<OrderDetailsAdapter.OrderDetailViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderDetailViewHolder {
            val binding = com.example.adminspot.databinding.PendingDetailItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
            return OrderDetailViewHolder(binding)
        }

        override fun onBindViewHolder(holder: OrderDetailViewHolder, position: Int) {
            holder.bind(orderDetails, position)
        }

        override fun getItemCount(): Int = orderDetails.serviceNames?.size ?: 0

        inner class OrderDetailViewHolder(private val binding: com.example.adminspot.databinding.PendingDetailItemBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(orderDetails: OrderDetails, position: Int) {
                binding.apply {
                    serviceNameHistory.text = orderDetails.serviceNames?.get(position) ?: "N/A"
                    servicePriceHistory.text = orderDetails.servicePrices?.get(position) ?: "N/A"
                    serviceQuantityHistory.text = orderDetails.serviceQuantities?.get(position)?.toString() ?: "0"

                    val uriString = orderDetails.serviceImages?.getOrNull(position) ?: ""
                    if (uriString.isNotEmpty()) {
                        val uri = Uri.parse(uriString)
                        Glide.with(binding.root.context).load(uri).into(serviceImageHistory)
                    } else {
                        serviceImageHistory.setImageResource(R.drawable.placeholder_image) // Set a placeholder image
                    }
                }
            }
        }
    }
}
