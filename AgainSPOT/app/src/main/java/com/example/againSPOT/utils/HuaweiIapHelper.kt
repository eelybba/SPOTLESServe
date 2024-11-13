package com.example.againSPOT.utils

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import android.widget.Toast
import com.huawei.hms.iap.Iap
import com.huawei.hms.iap.IapClient
import com.huawei.hms.iap.entity.OrderStatusCode
import com.huawei.hms.iap.entity.PurchaseIntentReq
import com.huawei.hms.iap.entity.PurchaseIntentResult
import com.huawei.hms.support.api.client.Status
import com.huawei.hms.common.ApiException

class HuaweiIapHelper(private val activity: Activity) {

    fun queryIsReady(callback: (Boolean) -> Unit) {
        val task = Iap.getIapClient(activity).isEnvReady
        task.addOnSuccessListener {
            callback(true)
        }.addOnFailureListener { e ->
            if (e is ApiException) {
                val errorCode = e.statusCode
                Log.e("HuaweiIapHelper", "isEnvReady failed with error code: $errorCode, message: ${e.message}")
                Toast.makeText(activity, "IAP environment not ready. Error code: $errorCode", Toast.LENGTH_SHORT).show()
            } else {
                Log.e("HuaweiIapHelper", "isEnvReady failed: ${e.message}")
                Toast.makeText(activity, "Unexpected error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            callback(false)
        }
    }

    fun buy(productId: String) {
        val req = PurchaseIntentReq().apply {
            this.productId = productId
            this.priceType = IapClient.PriceType.IN_APP_CONSUMABLE
            this.developerPayload = "testPayload"
        }

        val task = Iap.getIapClient(activity).createPurchaseIntent(req)
        task.addOnSuccessListener { result: PurchaseIntentResult ->
            result.status?.let { status: Status ->
                if (status.hasResolution()) {
                    try {
                        status.startResolutionForResult(activity, REQUEST_CODE_BUY)
                    } catch (e: IntentSender.SendIntentException) {
                        e.printStackTrace()
                        Toast.makeText(activity, "Failed to initiate purchase.", Toast.LENGTH_SHORT).show()
                        Log.e("HuaweiIapHelper", "Failed to initiate purchase: ${e.message}")
                    }
                }
            }
        }.addOnFailureListener { e ->
            if (e is ApiException) {
                val errorCode = e.statusCode
                Log.e("HuaweiIapHelper", "createPurchaseIntent failed with error code: $errorCode, message: ${e.message}")
                Toast.makeText(activity, "Purchase initiation failed: $errorCode", Toast.LENGTH_SHORT).show()
            } else {
                Log.e("HuaweiIapHelper", "createPurchaseIntent failed: ${e.message}")
                Toast.makeText(activity, "Purchase initiation failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun handlePurchaseResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_BUY) {
            data?.let {
                val purchaseResultInfo = Iap.getIapClient(activity).parsePurchaseResultInfoFromIntent(data)
                when (purchaseResultInfo.returnCode) {
                    OrderStatusCode.ORDER_STATE_SUCCESS -> {
                        Toast.makeText(activity, "Purchase successful!", Toast.LENGTH_SHORT).show()
                    }
                    OrderStatusCode.ORDER_STATE_CANCEL -> {
                        Toast.makeText(activity, "Purchase canceled.", Toast.LENGTH_SHORT).show()
                    }
                    OrderStatusCode.ORDER_STATE_FAILED -> {
                        Toast.makeText(activity, "Purchase failed. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                    OrderStatusCode.ORDER_PRODUCT_OWNED -> {
                        Toast.makeText(activity, "You already own this product.", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Log.e("HuaweiIapHelper", "Unexpected result code: ${purchaseResultInfo.returnCode}")
                        Toast.makeText(activity, "Unexpected result: ${purchaseResultInfo.returnCode}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_BUY = 6666
    }
}
