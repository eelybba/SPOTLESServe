package com.example.againSPOT.Fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.againSPOT.adapter.BuyAgainAdapter
import com.example.againSPOT.databinding.FragmentHistoryBinding
import com.example.againSPOT.model.OrderDetails
import com.example.againSPOT.OrderHistoryItems
import com.example.againSPOT.RecentOrderItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding
    private lateinit var buyAgainAdapter: BuyAgainAdapter
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String
    private var listOfOrderItem: MutableList<OrderDetails> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHistoryBinding.inflate(layoutInflater, container, false)

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        database = FirebaseDatabase.getInstance()

        // Set up RecyclerView and retrieve user order history
        retrieveBuyHistory()

        // Recent buy Button click
        binding.recentbuyitem.setOnClickListener {
            seeItemsRecentBuy()
        }

        return binding.root
    }

    // Function to see items recently bought
    private fun seeItemsRecentBuy() {
        listOfOrderItem.firstOrNull()?.let {
            val intent = Intent(requireContext(), RecentOrderItems::class.java)
            intent.putExtra("RecentBuyOrderItem", ArrayList(listOfOrderItem))
            startActivity(intent)
        }
    }

    // Function to retrieve items buy history
    private fun retrieveBuyHistory() {
        binding.recentbuyitem.visibility = View.INVISIBLE
        userId = auth.currentUser?.uid ?: ""

        if (userId.isNotEmpty()) {
            val buyItemReference: DatabaseReference =
                databaseReference.child("user").child(userId).child("BuyHistory")
            val sortingQuery = buyItemReference.orderByChild("currentTime")

            sortingQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listOfOrderItem.clear()
                    for (buySnapshot in snapshot.children) {
                        val buyHistoryItem = buySnapshot.getValue(OrderDetails::class.java)
                        buyHistoryItem?.let {
                            listOfOrderItem.add(it)
                        }
                    }
                    // Sort by most recent
                    listOfOrderItem.sortByDescending { it.currentTime }
                    if (listOfOrderItem.isNotEmpty()) {
                        // Display the most recent order details
                        setDataInRecentBuyItem()
                        // Set up the recycler view with previous order details
                        setPreviousBuyItemsRecyclerView()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                }
            })
        }
    }

    // Function to display the most recent order details
    private fun setDataInRecentBuyItem() {
        val recentOrderItem = listOfOrderItem.firstOrNull()
        if (recentOrderItem != null) {
            binding.recentbuyitem.visibility = View.VISIBLE
            with(binding) {
                buyAgainServiceName.text = recentOrderItem.serviceNames?.firstOrNull() ?: ""
                buyAgainServicePrice.text = calculateTotalPrice(recentOrderItem)
                val image = recentOrderItem.serviceImages?.firstOrNull() ?: ""
                val uri = Uri.parse(image)
                Glide.with(requireContext()).load(uri).into(buyAgainServiceImage)

                // Listen for real-time changes to the orderAccepted field
                listenToOrderStatus(recentOrderItem)
            }
        } else {
            binding.recentbuyitem.visibility = View.INVISIBLE
        }
    }

    // Listen for real-time changes to the orderAccepted field in CompletedOrder
    private fun listenToOrderStatus(orderDetails: OrderDetails) {
        val completedOrderReference = databaseReference.child("CompletedOrder").child(orderDetails.itemPushKey ?: return)

        // Attach a listener to the orderAccepted field in CompletedOrder
        completedOrderReference.child("orderAccepted").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isOrderAccepted = snapshot.getValue(Boolean::class.java) ?: false
                Log.d("HistoryFragment", "Order status updated: $isOrderAccepted")

                // Update the button text based on the order status
                if (isOrderAccepted) {
                    binding.statusBtn.visibility = View.VISIBLE
                    binding.statusBtn.text = "Order Received"
                } else {
                    binding.statusBtn.visibility = View.VISIBLE
                    binding.statusBtn.text = "Still Pending"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HistoryFragment", "Error fetching order status: ${error.message}")
            }
        })
    }

    // Function to calculate the total price
    private fun calculateTotalPrice(orderDetails: OrderDetails): String {
        val totalPrice = orderDetails.servicePrices?.zip(orderDetails.serviceQuantities ?: listOf()) { price, quantity ->
            price.toDoubleOrNull()?.times(quantity) ?: 0.00
        }?.sum() ?: 0.00
        return "RM%.2f".format(totalPrice)
    }

    // Function to set up the recycler view with previous order details
    private fun setPreviousBuyItemsRecyclerView() {
        val rv = binding.BuyAgainRecyclerView
        rv.layoutManager = LinearLayoutManager(requireContext())
        buyAgainAdapter = BuyAgainAdapter(listOfOrderItem, requireContext())
        rv.adapter = buyAgainAdapter
    }
}
