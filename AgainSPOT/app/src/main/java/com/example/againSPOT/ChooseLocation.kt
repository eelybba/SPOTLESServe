package com.example.againSPOT

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.example.againSPOT.databinding.ActivityChooseLocationBinding
import com.example.againSPOT.databinding.ActivityRegisterBinding

class ChooseLocation : AppCompatActivity() {
    private val binding:ActivityChooseLocationBinding by lazy{
        ActivityChooseLocationBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val locationList = arrayOf("KK5-Block A","KK5-Block B","KK5-Block C","KK5-Block D","KK5-Block E","KK5-Block F","KK5-Block G","KK5-Block H","KK5-Block I")
        val adapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,locationList)
        val autoCompleteTextView = binding.listOfLocation
        autoCompleteTextView.setAdapter(adapter)
    }
}