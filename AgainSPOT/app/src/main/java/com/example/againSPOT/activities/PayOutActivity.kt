package com.example.againSPOT

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.againSPOT.databinding.ActivityPayOutBinding
import com.example.againSPOT.model.CartItems
import com.example.againSPOT.model.OrderDetails
import com.example.againSPOT.utils.HuaweiIapHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class PayOutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPayOutBinding
    private lateinit var iapHelper: HuaweiIapHelper
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var userId: String
    private var totalAmount: Double = 0.0
    private var allServiceID: String = ""
    private lateinit var name: String
    private lateinit var address: String
    private lateinit var phone: String

    // Add the missing list declarations here
    private val serviceItemNames = mutableListOf<String>()
    private val serviceItemPrices = mutableListOf<String>()
    private val serviceItemImages = mutableListOf<String>()
    private val serviceItemQuantities = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPayOutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Huawei IAP Helper
        auth = FirebaseAuth.getInstance()
        iapHelper = HuaweiIapHelper(this)
        database = FirebaseDatabase.getInstance().reference
        userId = auth.currentUser?.uid ?: ""

        // Set user data
        setUserData()

        // Fetch cart items to calculate total amount and get allServiceID
        fetchCartItems()

        // Confirm IAP readiness
        iapHelper.queryIsReady { isReady ->
            if (!isReady) {
                Toast.makeText(this, "IAP environment not ready", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up purchase button click listener
        binding.placeOrderBtn.setOnClickListener {
            if (allServiceID.isNotEmpty()) {
                name = binding.name.text.toString().trim()
                address = binding.address.text.toString().trim()
                phone = binding.phone.text.toString().trim()

                if (name.isBlank() || address.isBlank() || phone.isBlank()) {
                    Toast.makeText(this, "Please enter all the details", Toast.LENGTH_SHORT).show()
                } else {
                    iapHelper.buy(allServiceID)
                    placeOrder() // Place order after successful purchase initiation
                }
            } else {
                Toast.makeText(this, "Service ID not available", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle back button action
        binding.backHomeBtn.setOnClickListener {
            finish()
        }
    }

    private fun fetchCartItems() {
        val cartReference = database.child("user").child(userId).child("CartItems")
        cartReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                totalAmount = 0.0
                allServiceID = ""

                serviceItemNames.clear()
                serviceItemPrices.clear()
                serviceItemImages.clear()
                serviceItemQuantities.clear()

                for (cartItemSnapshot in snapshot.children) {
                    val cartItem = cartItemSnapshot.getValue(CartItems::class.java)
                    cartItem?.let {
                        val price = it.allServicePrice?.toDoubleOrNull() ?: 0.0
                        totalAmount += price * it.allServiceQuantity

                        // Populate the lists with real data
                        serviceItemNames.add(it.allServiceName ?: "")
                        serviceItemPrices.add(it.allServicePrice ?: "0.0")
                        serviceItemImages.add(it.allServiceImage ?: "")
                        serviceItemQuantities.add(it.allServiceQuantity)

                        // Set the first item ID as allServiceID for purchase (you might change this logic if needed)
                        if (allServiceID.isEmpty()) {
                            allServiceID = it.allServiceID ?: ""
                        }
                    }
                }
                // Update UI with total amount
                binding.totalAmount.setText("RM %.2f".format(totalAmount))
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PayOutActivity, "Failed to load cart items", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun placeOrder() {
        val time = System.currentTimeMillis()
        val itemPushKey = database.child("OrderDetails").push().key

        // Create an orderDetails object with actual data from the cart
        val orderDetails = OrderDetails(
            userId, name, serviceItemNames, serviceItemPrices, serviceItemImages,
            serviceItemQuantities, address, "RM %.2f".format(totalAmount), phone, time, itemPushKey,
            orderAccepted = false,
            paymentReceived = false
        )

        // Save order details to Firebase
        val orderReference = database.child("OrderDetails").child(itemPushKey!!)
        orderReference.setValue(orderDetails).addOnSuccessListener {
            val bottomSheetDialog = CongratsBottomSheet()
            bottomSheetDialog.show(supportFragmentManager, "Test")
            removeItemFromCart()
            addOrderToHistory(orderDetails)
        }.addOnFailureListener {
            Toast.makeText(this, "Failed To Order", Toast.LENGTH_SHORT).show()
        }
    }


    private fun addOrderToHistory(orderDetails: OrderDetails) {
        database.child("user").child(userId).child("BuyHistory")
            .child(orderDetails.itemPushKey!!)
            .setValue(orderDetails)
    }

    private fun removeItemFromCart() {
        val cartItemsReference = database.child("user").child(userId).child("CartItems")
        cartItemsReference.removeValue()
    }

    private fun setUserData() {
        val user = auth.currentUser
        user?.let {
            val userReference = database.child("user").child(it.uid)
            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        name = snapshot.child("name").getValue(String::class.java) ?: ""
                        address = snapshot.child("address").getValue(String::class.java) ?: ""
                        phone = snapshot.child("phone").getValue(String::class.java) ?: ""

                        // Populate UI fields
                        binding.apply {
                            name.setText(this@PayOutActivity.name)
                            address.setText(this@PayOutActivity.address)
                            phone.setText(this@PayOutActivity.phone)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@PayOutActivity, "Failed to load user data", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        iapHelper.handlePurchaseResult(requestCode, resultCode, data)
    }
}
