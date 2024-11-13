package com.example.adminspot

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminspot.adapter.PendingOrderAdapter
import com.example.adminspot.databinding.ActivityPendingOrderBinding
import com.example.adminspot.model.OrderDetails
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PendingOrder : AppCompatActivity(), PendingOrderAdapter.OnItemClicked {

    private lateinit var binding: ActivityPendingOrderBinding
    private var listOfOrderItem: MutableList<OrderDetails> = mutableListOf()
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseOrderDetails: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPendingOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase database
        database = FirebaseDatabase.getInstance()
        databaseOrderDetails = database.reference.child("OrderDetails")

        // Fetch order details from Firebase
        getPendingOrderDetails()

        // Handle back button click
        binding.backHomeBtnPending.setOnClickListener {
            finish()
        }
    }

    private fun getPendingOrderDetails() {
        // Show progress bar while data is loading
        binding.progressBar.visibility = View.VISIBLE

        // Query for pending orders (orderAccepted = false)
        val pendingOrdersQuery = databaseOrderDetails.orderByChild("orderAccepted").equalTo(false)

        pendingOrdersQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listOfOrderItem.clear()
                for (orderSnapshot in snapshot.children) {
                    val orderDetails = orderSnapshot.getValue(OrderDetails::class.java)
                    if (orderDetails != null) {
                        orderDetails.itemPushKey = orderSnapshot.key
                        listOfOrderItem.add(orderDetails)
                    }
                }

                binding.progressBar.visibility = View.GONE

                if (listOfOrderItem.isEmpty()) {
                    Toast.makeText(this@PendingOrder, "No pending orders found", Toast.LENGTH_SHORT).show()
                }

                setAdapter()
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@PendingOrder, "Error loading data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setAdapter() {
        binding.pendingOrderRecyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = PendingOrderAdapter(this, listOfOrderItem, this)
        binding.pendingOrderRecyclerView.adapter = adapter
    }

    override fun onItemClickListener(position: Int) {
        // Handle item click to show details
        val intent = Intent(this, OrderDetailActivity::class.java)
        val userOrderDetails = listOfOrderItem[position]
        intent.putExtra("UserOrderDetails", userOrderDetails)
        startActivity(intent)
    }

    override fun onItemAcceptClickListener(position: Int) {
        // Handle item acceptance and update database
        val order = listOfOrderItem[position]
        val orderReference = databaseOrderDetails.child(order.itemPushKey ?: return)
        val completedOrderReference = database.reference.child("CompletedOrder").child(order.itemPushKey ?: return)

        binding.progressBar.visibility = View.VISIBLE

        orderReference.child("orderAccepted").setValue(true).addOnSuccessListener {
            completedOrderReference.setValue(order).addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Order accepted and moved to CompletedOrder", Toast.LENGTH_SHORT).show()
                listOfOrderItem.removeAt(position)
                setAdapter()
            }.addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Failed to move order to CompletedOrder", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, "Failed to accept order", Toast.LENGTH_SHORT).show()
        }
    }
}