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

package com.zs.core

import android.app.Activity
import android.content.Context
import com.zs.core.billing.Paymaster
import com.zs.core.billing.Product
import com.zs.core.billing.Purchase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class PaymasterImpl(
    context: Context,
    securityKey: String,
    val products: Array<String>,
) : Paymaster {
    override val purchases: StateFlow<List<Purchase>> = MutableStateFlow(emptyList())
    override val details: StateFlow<List<Product>> = MutableStateFlow(emptyList())

    override fun sync() {

    }

    override fun initiatePurchaseFlow(activity: Activity, productId: String): Boolean {
       return false
    }

    override fun release() {
    }
}