// ServiceItem.kt
package com.example.againSPOT.model

data class ServiceItem(
    val allServiceID: String? = null, // Added this field to identify the service uniquely
    val allServiceName: String? = null,
    val allServicePrice: String? = null,
    val allServiceDescription: String? = null,
    val allServiceSuitable: String? = null,
    val allServiceImage: String? = null
)
