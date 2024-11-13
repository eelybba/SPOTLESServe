package com.example.adminspot.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.adminspot.databinding.OrderDetailItemBinding

class OrderDetailsAdapter(
    private val context: Context,
    private val serviceNames: ArrayList<String>,
    private val serviceImages: ArrayList<String>,
    private val serviceQuantities: ArrayList<Int>,
    private val servicePrices: ArrayList<String>
) : RecyclerView.Adapter<OrderDetailsAdapter.OrderDetailsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderDetailsViewHolder {
        val binding = OrderDetailItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderDetailsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderDetailsViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = serviceNames.size

    inner class OrderDetailsViewHolder(private val binding: OrderDetailItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                serviceName.text = serviceNames[position]
                serviceQuantity.text = serviceQuantities[position].toString()
                val uriString = serviceImages[position]
                val uri = Uri.parse(uriString)
                Glide.with(context).load(uri).into(serviceImage)

                // Convert servicePrice to a Double and multiply by serviceQuantity
                val pricePerUnit = servicePrices[position].toDoubleOrNull() ?: 0.0
                val quantity = serviceQuantities[position]
                val totalPrice = pricePerUnit * quantity

                // Format the totalPrice to 2 decimal places and display it
                servicePrice.text = String.format("RM %.2f", totalPrice)
            }
        }
    }
}
