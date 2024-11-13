package com.example.againSPOT.model

import java.io.Serializable

class OrderDetails() : Serializable {
    var status: String? = null
    var userUid: String? = null
    var userName: String? = null
    var serviceNames: MutableList<String>? = null
    var servicePrices: MutableList<String>? = null
    var serviceImages: MutableList<String>? = null
    var serviceQuantities: MutableList<Int>? = null
    var address: String? = null
    var totalPrice: String? = null
    var phoneNumber: String? = null
    var currentTime: Long = 0
    var itemPushKey: String? = null
    var orderAccepted: Boolean = false
    var paymentReceived: Boolean = false

    constructor(
        userUid: String?,
        userName: String?,
        serviceNames: MutableList<String>?,
        servicePrices: MutableList<String>?,
        serviceImages: MutableList<String>?,
        serviceQuantities: MutableList<Int>?,
        address: String?,
        totalPrice: String?,
        phoneNumber: String?,
        currentTime: Long,
        itemPushKey: String?,
        orderAccepted: Boolean,
        paymentReceived: Boolean
    ) : this() {
        this.userUid = userUid
        this.userName = userName
        this.serviceNames = serviceNames
        this.servicePrices = servicePrices
        this.serviceImages = serviceImages
        this.serviceQuantities = serviceQuantities
        this.address = address
        this.totalPrice = totalPrice
        this.phoneNumber = phoneNumber
        this.currentTime = currentTime
        this.itemPushKey = itemPushKey
        this.orderAccepted = orderAccepted
        this.paymentReceived = paymentReceived
    }
}