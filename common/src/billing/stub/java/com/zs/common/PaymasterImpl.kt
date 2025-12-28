/*
 * Copyright (c)  2025 Zakir Sheikh
 *
 * Created by sheik on 28 of Dec 2025
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Last Modified by sheik on 28 of Dec 2025
 *
 */

package com.zs.common

import android.app.Activity
import com.zs.common.billing.Paymaster
import com.zs.common.billing.Product
import com.zs.common.billing.Purchase
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
            BuildConfig.FLAVOR_PREMIUM -> Purchase(id, Paymaster.STATE_ACKNOWLEDGED)
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