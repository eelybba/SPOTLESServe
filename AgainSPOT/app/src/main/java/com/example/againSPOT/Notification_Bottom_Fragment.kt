package com.example.againSPOT

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.againSPOT.adapter.NotificationAdapter
import com.example.againSPOT.databinding.FragmentNotificationBottomBinding
import com.example.againSPOT.model.OrderDetails
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Notification_Bottom_Fragment : BottomSheetDialogFragment() {

    private var _binding: FragmentNotificationBottomBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: FirebaseDatabase
    private lateinit var notificationAdapter: NotificationAdapter
    private val notifications: MutableList<String> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBottomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notificationAdapter = NotificationAdapter(notifications)
        binding.notificationRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.notificationRecyclerView.adapter = notificationAdapter

        // Retrieve notifications from Firebase
        listenToOrderStatusUpdates()
    }

    private fun listenToOrderStatusUpdates() {
        database = FirebaseDatabase.getInstance()
        val orderReference = database.reference.child("CompletedOrder")

        // Get current user's UID
        val userUid = FirebaseAuth.getInstance().currentUser?.uid
        if (userUid == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        orderReference.orderByChild("userUid").equalTo(userUid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    notifications.clear()

                    for (orderSnapshot in snapshot.children) {
                        val order = orderSnapshot.getValue(OrderDetails::class.java)
                        order?.let {
                            val message = when (it.status) {
                                "Order Placed" -> "Your Order Placed!!"
                                "Picked up" -> "Your Clothes Picked Up!!"
                                "Washing in process" -> "Your Clothes Washing in process!!"
                                "Ready" -> "Your order is ready!!"
                                "Delivered" -> "Your order is delivered!!!"
                                else -> "Order ${it.status}"
                            }
                            notifications.add(message)

                            Log.d("NotificationFragment", "Notification added: $message")
                        }
                    }

                    if (notifications.isEmpty()) {
                        Toast.makeText(requireContext(), "No notifications available", Toast.LENGTH_SHORT).show()
                    }

                    notificationAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to load notifications", Toast.LENGTH_SHORT).show()
                    Log.e("NotificationFragment", "Database error: ${error.message}")
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
