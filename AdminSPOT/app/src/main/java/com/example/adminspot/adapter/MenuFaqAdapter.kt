package com.example.adminspot.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.adminspot.databinding.ItemFaqBinding
import com.example.adminspot.model.AllFaq
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class MenuFaqAdapter(
    private val context: Context,
    private val faqList: ArrayList<AllFaq>,
    private var databaseReference: DatabaseReference

) : RecyclerView.Adapter<MenuFaqAdapter.AddFAQViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddFAQViewHolder {
        val binding = ItemFaqBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        databaseReference = FirebaseDatabase.getInstance().reference
        return AddFAQViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddFAQViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = faqList.size

    inner class AddFAQViewHolder(private val binding: ItemFaqBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val faqItem = faqList[position]
            binding.apply {
                faqQuestion.text = faqItem.allFaqQuestion
                faqAnswer.text = faqItem.allFaqAnswer
                Glide.with(context).load(Uri.parse(faqItem.allFaqImage)).into(faqImage)
            }
        }
    }
}
