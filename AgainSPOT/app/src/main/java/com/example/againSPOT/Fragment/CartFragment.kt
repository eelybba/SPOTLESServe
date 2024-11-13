package com.example.againSPOT.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.againSPOT.PayOutActivity
import com.example.againSPOT.adapter.CartAdapter
import com.example.againSPOT.databinding.FragmentCartBinding
import com.example.againSPOT.model.CartItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CartFragment : Fragment() {

    private lateinit var binding: FragmentCartBinding
    private lateinit var cartAdapter: CartAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var userId: String

    private val serviceNames = mutableListOf<String>()
    private val servicePrices = mutableListOf<String>()
    private val serviceDescription = mutableListOf<String>()
    private val serviceSuitable = mutableListOf<String>()
    private val serviceImages = mutableListOf<String>()
    private val serviceQuantities = mutableListOf<Int>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCartBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()  
        userId = auth.currentUser?.uid ?: ""

        // Set up the Proceed button click listener
        binding.proceedBtn.setOnClickListener {
            getOrderItemsDetail()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        retrieveCartItems()
    }

    private fun getOrderItemsDetail() {
        val orderIdReference: DatabaseReference = database.reference.child("user").child(userId).child("CartItems")

        orderIdReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                serviceNames.clear()
                servicePrices.clear()
                serviceDescription.clear()
                serviceSuitable.clear()
                serviceImages.clear()
                serviceQuantities.clear()

                for (serviceSnapshot in snapshot.children) {
                    val orderItems = serviceSnapshot.getValue(CartItems::class.java)
                    orderItems?.let {
                        serviceNames.add(it.allServiceName ?: "")
                        servicePrices.add(it.allServicePrice ?: "")
                        serviceDescription.add(it.allServiceDescription ?: "")
                        serviceSuitable.add(it.allServiceSuitable ?: "")
                        serviceImages.add(it.allServiceImage ?: "")
                        serviceQuantities.add(it.allServiceQuantity ?: 1)
                    }
                }
                navigateToPayOut(serviceNames, servicePrices, serviceDescription, serviceSuitable, serviceImages, serviceQuantities)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Order Making Failed, Please Try Again", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun navigateToPayOut(
        serviceNames: MutableList<String>,
        servicePrices: MutableList<String>,
        serviceDescription: MutableList<String>,
        serviceSuitable: MutableList<String>,
        serviceImages: MutableList<String>,
        serviceQuantities: MutableList<Int>
    ) {
        if (isAdded && context != null) {
            val intent = Intent(requireContext(), PayOutActivity::class.java)
            intent.putStringArrayListExtra("ServiceItemName", ArrayList(serviceNames))
            intent.putStringArrayListExtra("ServiceItemPrice", ArrayList(servicePrices))
            intent.putStringArrayListExtra("ServiceItemDescription", ArrayList(serviceDescription))
            intent.putStringArrayListExtra("ServiceItemSuitable", ArrayList(serviceSuitable))
            intent.putStringArrayListExtra("ServiceItemImages", ArrayList(serviceImages))
            intent.putIntegerArrayListExtra("ServiceItemQuantities", ArrayList(serviceQuantities))
            startActivity(intent)
        }
    }

    private fun retrieveCartItems() {
        val serviceReference: DatabaseReference = database.reference.child("user").child(userId).child("CartItems")

        serviceReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                serviceNames.clear()
                servicePrices.clear()
                serviceDescription.clear()
                serviceSuitable.clear()
                serviceImages.clear()
                serviceQuantities.clear()

                for (serviceSnapshot in snapshot.children) {
                    val cartItem = serviceSnapshot.getValue(CartItems::class.java)

                    cartItem?.let {
                        serviceNames.add(it.allServiceName ?: "")
                        servicePrices.add(it.allServicePrice ?: "")
                        serviceDescription.add(it.allServiceDescription ?: "")
                        serviceSuitable.add(it.allServiceSuitable ?: "")
                        serviceImages.add(it.allServiceImage ?: "")
                        serviceQuantities.add(it.allServiceQuantity ?: 1)
                    }
                }

                setAdapter()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to fetch data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setAdapter() {
        cartAdapter = CartAdapter(requireContext(), serviceNames, servicePrices, serviceDescription, serviceSuitable, serviceImages, serviceQuantities)
        binding.cartRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.cartRecyclerView.adapter = cartAdapter
    }
}
