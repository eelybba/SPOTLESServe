// HomeFragment.kt
package com.example.againSPOT.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.againSPOT.R
import com.example.againSPOT.adapter.ServiceAdapter
import com.example.againSPOT.databinding.FragmentHomeBinding
import com.example.againSPOT.model.ServiceItem
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var database: DatabaseReference
    private lateinit var popularItems: MutableList<ServiceItem>
    private val orderFrequencyMap = mutableMapOf<String, Int>() // Map to track order frequency

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Set up the "View All" button to show the FAQ BottomSheet
        binding.viewAllMenu.setOnClickListener {
            val bottomSheetFragment = faqBottomSheetFragment()
            bottomSheetFragment.show(parentFragmentManager, "faqBottomSheetFragment")
        }

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().reference

        // Set up the slideshow
        setupSlideshow()

        // Retrieve and rank popular items based on order frequency
        fetchAndRankPopularItems()

        return binding.root
    }

    private fun setupSlideshow() {
        val imageList = arrayListOf(
            SlideModel(R.drawable.banner1, ScaleTypes.FIT),
            SlideModel(R.drawable.banner2, ScaleTypes.FIT),
            SlideModel(R.drawable.banner3, ScaleTypes.FIT)
        )
        binding.imageSlider.setImageList(imageList, ScaleTypes.FIT)
    }

    private fun fetchAndRankPopularItems() {
        // Fetch completed orders to calculate order frequency
        val ordersRef = database.child("CompletedOrders")
        ordersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Calculate order frequency
                for (orderSnapshot in snapshot.children) {
                    val serviceId = orderSnapshot.child("serviceId").getValue(String::class.java)
                    if (serviceId != null) {
                        orderFrequencyMap[serviceId] = orderFrequencyMap.getOrDefault(serviceId, 0) + 1
                    }
                }
                // Fetch service items to display
                fetchServiceItems()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load orders", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchServiceItems() {
        // Fetch all services from the menu
        val serviceRef = database.child("menu")
        serviceRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allItems = mutableListOf<ServiceItem>()

                for (serviceSnapshot in snapshot.children) {
                    val serviceItem = serviceSnapshot.getValue(ServiceItem::class.java)
                    serviceItem?.let { allItems.add(it) }
                }

                // Rank the services based on order frequency
                rankAndDisplayTopItems(allItems)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load services", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun rankAndDisplayTopItems(allItems: List<ServiceItem>) {
        // Check if there is any frequency data available
        val hasFrequencyData = orderFrequencyMap.values.any { it > 0 }

        // If there is frequency data, sort items based on order frequency in descending order;
        // if frequency is 0, sort by price in ascending order
        val rankedItems = if (hasFrequencyData) {
            allItems.sortedWith(compareByDescending<ServiceItem> {
                orderFrequencyMap[it.allServiceID] ?: 0 // Use order frequency; default to 0 if no orders found
            }.thenBy {
                it.allServicePrice?.toDoubleOrNull() ?: Double.MAX_VALUE // Convert price to Double, or use MAX_VALUE if null
            })
        } else {
            allItems.sortedBy { it.allServicePrice?.toDoubleOrNull() ?: Double.MAX_VALUE } // Sort by price if no frequency data is found
        }

        // Select the top 3 items
        val topItems = rankedItems.take(3)

        setPopularItemsAdapter(topItems)
    }


    private fun setPopularItemsAdapter(topItems: List<ServiceItem>) {
        val adapter = ServiceAdapter(topItems, requireContext())
        binding.popularRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.popularRecyclerView.adapter = adapter
    }
}
