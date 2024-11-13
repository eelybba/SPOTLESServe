// CartItems.kt
package com.example.againSPOT.model

data class CartItems(
    val allServiceName: String? = null,
    val allServicePrice: String? = null,
    val allServiceDescription: String? = null,
    val allServiceSuitable: String? = null,
    val allServiceImage: String? = null,
    val allServiceID: String? = null,
    val allServiceQuantity: Int = 1 // Default to 1
)
