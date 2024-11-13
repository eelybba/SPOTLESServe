package com.example.againSPOT.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.againSPOT.databinding.FaqItemBinding
import com.example.againSPOT.model.FaqItem

class FaqAdapter(
    private val faqItems:List<FaqItem>,
    private val requireContext: Context
) : RecyclerView.Adapter<FaqAdapter.FaqViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        val binding = FaqItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FaqViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = faqItems.size

    inner class FaqViewHolder(private val binding: FaqItemBinding) : RecyclerView.ViewHolder(binding.root) {

        //set data into recyclerview item
        fun bind(position: Int) {
            val faqItem = faqItems[position]
            binding.apply {
                faqQuestion.text = faqItem.allFaqQuestion
                faqAnswer.text = faqItem.allFaqAnswer
                val uri = Uri.parse(faqItem.allFaqImage)
                Glide.with(requireContext).load(uri).into(faqImage)
            }
        }
    }
}

