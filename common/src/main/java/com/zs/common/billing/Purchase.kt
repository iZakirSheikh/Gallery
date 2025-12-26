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

import com.android.billingclient.api.Purchase as GP_Purchase

/**
 * Representing a purchase.
 *
 * This class wraps a [GP_Purchase] purchase object and provides convenient access to its properties.
 *
 * @property value The underlying [GP_Purchase] purchase object.
 * @property isAcknowledged Whether the purchase has been acknowledged.
 * @property state The state of the purchase. [com.android.billingclient.api.Purchase.PurchaseState]
 * @property quantity The quantity of the purchased items.
 * @property id The ID of the first product in the purchase, as assigned in Play Console Billing.
 * @property time The time of the purchase.
 */
@JvmInline
value class Purchase internal constructor(private val value: GP_Purchase) {
    val isAcknowledged get() = value.isAcknowledged
    val state get() = value.purchaseState
    val quantity get() = value.quantity
    val id get() = value.products.first()
    val time get() = value.purchaseTime

}
