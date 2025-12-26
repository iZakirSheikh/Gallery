/*
 * Copyright (c)  2025 Zakir Sheikh
 *
 * Created by sheik on 26 of Dec 2025
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
 * Last Modified by sheik on 26 of Dec 2025
 */

package com.zs.common.billing

import android.app.Activity
import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import com.android.billingclient.api.ProductDetails as RemoteProduct
import com.android.billingclient.api.Purchase as RemotePurchase


/**
 * Manages in-app purchases and provides access to product and purchase information.
 *
 * This interface defines methods for:
 *  * Initiating purchase flows.
 *  * Refreshing purchase data.
 *  * Releasing resources.
 *  * Accessing product and purchase details.
 *
 * @property purchases A [StateFlow] emitting a list of purchases the user has made.
 */
interface Paywall {

    val trigger: Flow<Unit>

    fun initiatePurchaseFlow(activity: Activity, productId: String): Boolean

    fun release()

    fun getPurchase(id: String): Purchase

    fun getProduct(id: String): Product
}