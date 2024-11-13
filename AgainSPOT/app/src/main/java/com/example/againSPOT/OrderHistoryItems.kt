package com.example.againSPOT

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.againSPOT.adapter.RecentBuyAdapter
import com.example.againSPOT.databinding.ActivityOrderHistoryItemsBinding
import com.example.againSPOT.model.OrderDetails

class OrderHistoryItems : AppCompatActivity() {

    private val binding: ActivityOrderHistoryItemsBinding by lazy {
        ActivityOrderHistoryItemsBinding.inflate(layoutInflater)
    }

    private lateinit var allServiceNames: ArrayList<String>
    private lateinit var allServiceImages: ArrayList<String>
    private lateinit var allServicePrices: ArrayList<String>
    private lateinit var allServiceQuantities: ArrayList<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener {
            finish()
        }

        val orderDetails = intent.getSerializableExtra("OrderDetails") as? OrderDetails
        orderDetails?.let {
            allServiceNames = ArrayList(it.serviceNames ?: listOf())
            allServiceImages = ArrayList(it.serviceImages ?: listOf())
            allServicePrices = ArrayList(it.servicePrices ?: listOf())
            allServiceQuantities = ArrayList(it.serviceQuantities ?: listOf())
        }

        setAdapter()
    }

    private fun setAdapter() {
        val rv = binding.recyclerViewOrderHistory
        rv.layoutManager = LinearLayoutManager(this)
        val adapter = RecentBuyAdapter(this, allServiceNames, allServiceImages, allServicePrices, allServiceQuantities)
        rv.adapter = adapter
    }
}