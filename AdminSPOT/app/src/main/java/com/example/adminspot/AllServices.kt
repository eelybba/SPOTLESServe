package com.example.adminspot

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminspot.adapter.MenuServiceAdapter
import com.example.adminspot.databinding.ActivityAllServicesBinding
import com.example.adminspot.model.AllMenu
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AllServices : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private var menuItems: ArrayList<AllMenu> = ArrayList()
    private val binding: ActivityAllServicesBinding by lazy {
        ActivityAllServicesBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Initializing database reference
        database = FirebaseDatabase.getInstance()
        databaseReference = database.reference

        // Retrieve menu items from the database
        retrieveMenuItem()

        // Set up the back button functionality
        binding.backMainBtn.setOnClickListener {
            finish() // Close the activity and return to the previous screen
        }
    }

    private fun retrieveMenuItem() {
        // Show the progress bar before starting the database query
        binding.progressBar.visibility = View.VISIBLE

        val serviceRef: DatabaseReference = database.reference.child("menu")

        serviceRef.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear existing data before populating
                menuItems.clear()

                // Loop through each service item and populate the list
                for (serviceSnapshot in snapshot.children) {
                    val menuItem = serviceSnapshot.getValue(AllMenu::class.java)
                    menuItem?.let {
                        menuItems.add(it)
                    }
                }

                // Set up the adapter with the retrieved data
                setAdapter()

                // Hide the progress bar after loading the data
                binding.progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                // Log the error
                Log.d("DatabaseError", "Error: ${error.message}")

                // Hide the progress bar if an error occurs
                binding.progressBar.visibility = View.GONE
            }
        })
    }

    private fun setAdapter() {
        val adapter = MenuServiceAdapter(this@AllServices, menuItems, databaseReference)
        binding.MenuRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.MenuRecyclerView.adapter = adapter
    }
}
