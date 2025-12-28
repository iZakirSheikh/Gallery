/*
 * Copyright (c)  2025 Zakir Sheikh
 *
 * Created by sheik on 27 of Dec 2025
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
 * Last Modified by sheik on 27 of Dec 2025
 *
 */

package com.zs.common.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.zs.common.BuildConfig
import com.zs.common.billing.Paymaster.Companion.STATE_ACKNOWLEDGED
import com.zs.common.billing.Paymaster.Companion.STATE_PENDING
import com.zs.common.billing.Paymaster.Companion.STATE_PURCHASED
import com.zs.common.billing.Paymaster.Companion.STATE_UNSPECIFIED
import kotlinx.coroutines.flow.Flow
import java.io.Closeable

/**
 * Central interface for managing product purchases and paywall synchronization.
 *
 * Represents the purchase states.
 * - [STATE_UNSPECIFIED] → No purchase or unknown state.
 * - [STATE_PURCHASED] → Item purchased but not yet acknowledged.
 * - [STATE_ACKNOWLEDGED] → Item purchased and acknowledged.
 * - [STATE_PENDING] → Purchase initiated but not completed.
 * Implementations of this interface coordinate product details, purchase flows,
 * and acknowledgement handling. It provides reactive signals for UI updates and
 * explicit methods for initiating and releasing billing operations.
 */
interface Paymaster: Closeable {

    companion object {
        /**
         * Creates a new instance of [Paymaster].
         *
         * @param context Application context used to initialize billing components.
         * @param products Array of product identifiers to be managed by this instance.
         * @return A configured [Paymaster] implementation.
         */
        operator fun invoke(context: Context, products: Array<String>): Paymaster =
            PaymasterImpl(context, products)

        // Represents different
        const val STATE_UNSPECIFIED = 0   // No purchase or unknown state
        const val STATE_PURCHASED = 1    // Item purchased, not yet acknowledged
        const val STATE_ACKNOWLEDGED = 2 // Item purchased and acknowledged
        const val STATE_PENDING = 3      // Purchase initiated but not completed

        init {
            Log.d("Paymaster", " : ${BuildConfig.FLAVOR}")
        }
    }

    /**
     * A flow that emits whenever [sync] completes and new data is available.
     *
     * - Emits `Unit` as a signal (no payload).
     * - Consumers can collect this flow to react to paywall updates.
     * - Useful for triggering UI refreshes or cache invalidation.
     */
    val signal: Flow<Unit>

    /**
     * Forces synchronization of payment data.
     *
     * Ensures the paywall has the latest information about products and purchases.
     * This may involve:
     * - Fetching updated product details.
     * - Retrieving the latest purchase statuses.
     * - Synchronizing data with a backend server.
     */
    fun sync()

    /**
     * Initiates the purchase flow for a specific product.
     *
     * @param activity The [Activity] used to launch the purchase flow.
     * @param productId The ID of the product to purchase.
     * @return `true` if the purchase flow was launched successfully, `false` otherwise.
     */
    fun beginTransition(activity: Activity, productId: String): Boolean

    /**
     * Retrieves a purchase record by its identifier.
     *
     * @param id The purchase identifier.
     * @return A [Purchase] instance if found, or `null` otherwise.
     */
    suspend fun getPurchase(id: String): Purchase?

    /**
     * Retrieves product metadata by its identifier.
     *
     * @param id The product identifier.
     * @return A [Product] instance if found, or `null` otherwise.
     */
    suspend fun getProduct(id: String): Product?
}