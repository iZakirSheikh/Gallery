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

package com.zs.common.billing


import android.app.Activity
import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

internal class PaymasterImpl(context: Context, val products: Array<String>) : Paymaster {

    override val signal: Flow<Unit> = emptyFlow()

    override fun sync() {
        // Stub: no-op for now
        // TODO: Implement product/purchase synchronization
    }

    override fun beginTransition(
        activity: Activity,
        productId: String
    ): Boolean {
        // Stub: always returns false
        // TODO: Launch billing flow for productId
        return false
    }

    override fun close() {
        // Stub: no-op for now
        // TODO: Clean up listeners, connections, resources
    }

    override suspend fun getPurchase(id: String): Purchase? {
        // Stub: returns sentinel Purchase
        // TODO: Query billing client for purchase details
        return Purchase("", 0, 0, -1L,)
    }

    override suspend fun getProduct(id: String): Product? {
        // Stub: returns sentinel Product
        // TODO: Query billing client for product details
        return Product("", "", "", "")
    }
}
