package com.example.adminspot

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminspot.adapter.OrderDetailsAdapter
import com.example.adminspot.databinding.ActivityOrderDetailBinding
import com.example.adminspot.model.OrderDetails


class OrderDetailActivity : AppCompatActivity() {
    private val binding: ActivityOrderDetailBinding by lazy {
        ActivityOrderDetailBinding.inflate(layoutInflater)
    }

    var userName: String? = null
    private var address: String? = null
    private var phoneNumber: String? = null
    private var totalPrice: String? = null
    private var serviceNames: ArrayList<String> = arrayListOf()
    private var serviceImages: ArrayList<String> = arrayListOf()
    private var serviceQuantities: ArrayList<Int> = arrayListOf()
    private var servicePrices: ArrayList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.backHomeBtn.setOnClickListener {
            finish()
        }
        getDataFromIntent()
    }

    private fun getDataFromIntent() {
        val receivedOrderDetails = intent.getSerializableExtra("UserOrderDetails") as OrderDetails
        receivedOrderDetails?.let { orderDetails ->
            userName = receivedOrderDetails.userName
            serviceNames = receivedOrderDetails.serviceNames as ArrayList<String>
            serviceImages = receivedOrderDetails.serviceImages as ArrayList<String>
            serviceQuantities = receivedOrderDetails.serviceQuantities as ArrayList<Int>
            address = receivedOrderDetails.address
            phoneNumber = receivedOrderDetails.phoneNumber
            servicePrices = receivedOrderDetails.servicePrices as ArrayList<String>
            totalPrice = receivedOrderDetails.totalPrice

            setUserDetail()
            setAdapter()
        }
    }

    private fun setUserDetail() {
        binding.name.text = userName
        binding.address.text = address
        binding.phone.text = phoneNumber
        binding.totalPay.text = totalPrice
    }

    private fun setAdapter() {
        binding.orderDetailsRecyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = OrderDetailsAdapter(
            this,
            serviceNames,
            serviceImages,
            serviceQuantities,
            servicePrices
        )
        binding.orderDetailsRecyclerView.adapter = adapter
    }
}