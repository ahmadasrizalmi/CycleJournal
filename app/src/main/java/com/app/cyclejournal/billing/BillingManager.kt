package com.app.cyclejournal.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enterprise Google Play Billing (v6+) controller for CycleJournal.
 * Manages the "lifetime_pro_access" non-consumable one-time purchase entitlement.
 */
@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val entitlementManager: UserEntitlementManager
) : PurchasesUpdatedListener, BillingClientStateListener {

    companion object {
        const val SKU_LIFETIME_PRO = UserEntitlementManager.SKU_LIFETIME_PRO
    }

    val isProUser: StateFlow<Boolean> = entitlementManager.isProUserFlow

    private var billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    private var productDetails: ProductDetails? = null

    init {
        initialize()
    }

    fun initialize() {
        if (!billingClient.isReady) {
            billingClient.startConnection(this)
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            queryProductDetails()
            queryExistingPurchases()
        }
    }

    override fun onBillingServiceDisconnected() {
        // Retry connection logic with exponential backoff if necessary
    }

    private fun queryProductDetails() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SKU_LIFETIME_PRO)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, queryProductDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                productDetails = queryProductDetailsList.firstOrNull { it.productId == SKU_LIFETIME_PRO }
            }
        }
    }

    fun queryExistingPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasPro = purchasesList.any { purchase ->
                    purchase.products.contains(SKU_LIFETIME_PRO) &&
                            purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                entitlementManager.setProUser(hasPro)
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity): Boolean {
        val details = productDetails ?: return false
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .build()

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        val result = billingClient.launchBillingFlow(activity, billingFlowParams)
        return result.responseCode == BillingClient.BillingResponseCode.OK
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.products.contains(SKU_LIFETIME_PRO)) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                entitlementManager.setProUser(true)

                // Acknowledge non-consumable purchase
                if (!purchase.isAcknowledged) {
                    val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()

                    CoroutineScope(Dispatchers.IO).launch {
                        billingClient.acknowledgePurchase(acknowledgePurchaseParams) { }
                    }
                }
            }
        }
    }
}
