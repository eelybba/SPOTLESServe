package com.example.adminspot.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.adminspot.databinding.ItemServiceBinding
import com.example.adminspot.model.AllMenu
import com.google.firebase.database.DatabaseReference

class MenuServiceAdapter(
    private val context: Context,
    private val menuList: ArrayList<AllMenu>,
    databaseReference: DatabaseReference

) : RecyclerView.Adapter<MenuServiceAdapter.AddServiceViewHolder>() {

    private val allServiceQuantities = IntArray(menuList.size) { 1 }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddServiceViewHolder {
        val binding = ItemServiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AddServiceViewHolder(binding)

    }

    override fun onBindViewHolder(holder: AddServiceViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = menuList.size

    inner class AddServiceViewHolder(private val binding: ItemServiceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                val quantity = allServiceQuantities[position]
                val menuItem = menuList[position]
                val uriString = menuItem.allServiceImage
                val uri = Uri.parse(uriString)
                serviceName.text = menuItem.allServiceName
                servicePrice.text = menuItem.allServicePrice
                Glide.with(context).load(uri).into(serviceImage)

                deleteBtn.setOnClickListener {
                    deleteQuantity(position)
                }
            }
        }
    }

    private fun deleteQuantity(position: Int) {
        menuList.removeAt(position)
        menuList.removeAt(position)
        menuList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, menuList.size)
    }

    private fun increaseQuantity(position: Int) {
        if (allServiceQuantities[position] < 10) {
            allServiceQuantities[position]++
            notifyItemChanged(position)
        }
    }

    private fun decreaseQuantity(position: Int) {
        if (allServiceQuantities[position] > 1) {
            allServiceQuantities[position]--
            notifyItemChanged(position)
        }

    }
}
