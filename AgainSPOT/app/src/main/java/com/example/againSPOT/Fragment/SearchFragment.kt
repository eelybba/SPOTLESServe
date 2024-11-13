package com.example.againSPOT.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.againSPOT.adapter.ServiceAdapter
import com.example.againSPOT.databinding.FragmentSearchBinding
import com.example.againSPOT.model.ServiceItem
import com.google.firebase.database.*

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var adapter: ServiceAdapter
    private lateinit var database: FirebaseDatabase
    private val originalServiceItems = mutableListOf<ServiceItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        database = FirebaseDatabase.getInstance()

        // Set up RecyclerView
        binding.serviceRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Retrieve menu items from the database
        retrieveServiceItems()

        // Set up the SearchView
        setupSearchView()

        return binding.root
    }

    private fun retrieveServiceItems() {
        // Get reference to the "menu" node in the Firebase database
        val serviceReference: DatabaseReference = database.reference.child("menu")

        serviceReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                originalServiceItems.clear()

                for (serviceSnapshot in snapshot.children) {
                    val serviceItem = serviceSnapshot.getValue(ServiceItem::class.java)
                    serviceItem?.let {
                        originalServiceItems.add(it)
                    }
                }

                // Show all menu items initially
                showAllMenu()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle potential errors here
            }
        })
    }

    private fun showAllMenu() {
        // Show all menu items initially
        setAdapter(originalServiceItems)
    }

    private fun setAdapter(serviceItems: List<ServiceItem>) {
        // Initialize or update the RecyclerView adapter
        adapter = ServiceAdapter(ArrayList(serviceItems), requireContext())
        binding.serviceRecyclerView.adapter = adapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterSearchItems(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterSearchItems(newText)
                return true
            }
        })
    }

    private fun filterSearchItems(query: String?) {
        // Filter items based on the search query
        val filteredServiceItems = originalServiceItems.filter {
            it.allServiceName?.contains(query ?: "", ignoreCase = true) == true
        }

        // Update the RecyclerView with filtered results
        setAdapter(filteredServiceItems)
    }
}
