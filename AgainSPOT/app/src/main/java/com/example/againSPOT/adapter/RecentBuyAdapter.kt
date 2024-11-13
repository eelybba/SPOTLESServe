package com.example.againSPOT.adapter

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.againSPOT.databinding.RecentBuyItemBinding

class RecentBuyAdapter(
    private var context: Context,
    private var serviceNameList: List<String>,
    private var serviceImageList: List<String>,
    private var servicePriceList: List<String>, // Assume this contains individual item prices as strings like "5" for RM5
    private var serviceQuantityList: List<Int>
) : RecyclerView.Adapter<RecentBuyAdapter.RecentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentViewHolder {
        val binding = RecentBuyItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecentViewHolder(binding)
    }

    override fun getItemCount(): Int = serviceNameList.size

    override fun onBindViewHolder(holder: RecentViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class RecentViewHolder(private val binding: RecentBuyItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                serviceNameBuy.text = serviceNameList[position]

                // Log data to check values
                Log.d("Adapter", "Service Name: ${serviceNameList[position]}")
                Log.d("Adapter", "Service Image: ${serviceImageList[position]}")
                Log.d("Adapter", "Service Price: ${servicePriceList[position]}")
                Log.d("Adapter", "Service Quantity: ${serviceQuantityList[position]}")

                // Set price and quantity
                val pricePerItem = servicePriceList[position].toDoubleOrNull() ?: 0.0
                val quantity = serviceQuantityList[position]
                val totalPrice = pricePerItem * quantity
                servicePriceBuy.text = "RM%.2f".format(totalPrice)
                serviceQuantityBuy.text = quantity.toString()

                // Load image with Glide
                val uri = Uri.parse(serviceImageList[position])
                Glide.with(context).load(uri).into(serviceImageBuy)
            }
        }
    }

}

