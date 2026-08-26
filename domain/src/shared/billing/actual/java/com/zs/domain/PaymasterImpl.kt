package com.zs.domain

import android.app.Activity
import com.zs.domain.billing.Paymaster
import com.zs.domain.billing.Product
import com.zs.domain.billing.Purchase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

internal class PaymasterImpl() : Paymaster {

    // No billing events in stub builds
    override val signal: Flow<Unit> = emptyFlow()

    override fun sync() {
        // no-op
    }

    override fun beginTransition(activity: Activity, productId: String): Boolean {
        // not supported in stub
        return false
    }

    override suspend fun getPurchase(id: String): Purchase? {
        // return dummy purchase depending on flavor
        return when (BuildConfig.FLAVOR) {
            BuildConfig.FLAVOR_COMMUNITY -> Purchase(id, Paymaster.STATE_UNSPECIFIED)
            BuildConfig.FLAVOR_GOLD -> Purchase(id, Paymaster.STATE_ACKNOWLEDGED)
            else -> null // no purchase in other flavors
        }
    }

    override suspend fun getProduct(id: String): Product? {
        // not available in stub
        return null
    }

    override fun close() {
        // no resources to release
    }
}