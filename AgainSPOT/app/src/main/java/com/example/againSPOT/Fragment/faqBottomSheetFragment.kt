package com.example.againSPOT.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.againSPOT.adapter.FaqAdapter
import com.example.againSPOT.databinding.FragmentFaqBottomSheetBinding
import com.example.againSPOT.model.FaqItem
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class faqBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentFaqBottomSheetBinding? = null

    private lateinit var database: FirebaseDatabase
    private lateinit var faqItems: MutableList<FaqItem>

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFaqBottomSheetBinding.inflate(inflater, container, false)

        binding.backBtn.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        retrieveFaqItems() // Retrieve FAQ items when the view is created
    }

    private fun retrieveFaqItems() {
        database = FirebaseDatabase.getInstance()
        val faqRef: DatabaseReference = database.reference.child("faq")
        faqItems = mutableListOf()

        faqRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (faqSnapshot in snapshot.children) {
                    val faqItem = faqSnapshot.getValue(FaqItem::class.java)
                    faqItem?.let { faqItems.add(it) }
                }
                Log.d("ITEMS", "onDataChange: Data Received")
                setAdapter() // Set adapter once data is received
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ITEMS", "onCancelled: Error fetching data", error.toException())
            }
        })
    }

    private fun setAdapter() {
        if (faqItems.isNotEmpty()) {
            val adapter = FaqAdapter(faqItems, requireContext())
            binding.faqRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.faqRecyclerView.adapter = adapter
            Log.d("ITEMS", "setAdapter: Data set")
        } else {
            Log.d("ITEMS", "setAdapter: No data to display")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



