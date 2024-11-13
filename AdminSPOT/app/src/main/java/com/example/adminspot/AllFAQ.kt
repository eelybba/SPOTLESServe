package com.example.adminspot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminspot.adapter.MenuFaqAdapter
import com.example.adminspot.adapter.MenuServiceAdapter
import com.example.adminspot.databinding.ActivityAllFaqBinding
import com.example.adminspot.databinding.ActivityAllServicesBinding
import com.example.adminspot.model.AllFaq
import com.example.adminspot.model.AllMenu
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AllFAQ : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private var faqItems: ArrayList<AllFaq> = ArrayList()
    private val binding: ActivityAllFaqBinding by lazy {
        ActivityAllFaqBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        databaseReference = FirebaseDatabase.getInstance().reference
        retrieveFaqItem()

        // Back button functionality
        binding.backHomeBtn.setOnClickListener {
            finish() // This will close the activity and return to the previous screen
        }
    }

    private fun retrieveFaqItem() {
        // Show progress bar before fetching data
        binding.progressBar.visibility = View.VISIBLE

        val serviceRef: DatabaseReference = databaseReference.child("faq")

        //fetch data from database
        serviceRef.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear existing data before populating
                faqItems.clear()

                // Loop through each service item
                for (serviceSnapshot in snapshot.children) {
                    val faqItem = serviceSnapshot.getValue(AllFaq::class.java)
                    faqItem?.let {
                        faqItems.add(it)
                    }
                }
                setAdapter()
                // Hide progress bar after data is fetched
                binding.progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                // Hide progress bar if there is an error
                binding.progressBar.visibility = View.GONE
                Log.d("DatabaseError", "Error: ${error.message}")
            }
        })
    }

    private fun setAdapter() {
        val adapter = MenuFaqAdapter(this, faqItems, databaseReference)
        binding.FAQRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.FAQRecyclerView.adapter = adapter
    }
}
