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

package com.zs.core.billing

import com.zs.core.BuildConfig

/**
 * Represents a completed or pending purchase transaction.
 *
 * This class encapsulates the essential metadata returned by the billing system
 * when a user buys a product. It provides identifiers, state information, and
 * acknowledgement status required for entitlement management.
 *
 * @property id Unique identifier of the purchase transaction. Used to correlate
 *              with backend records and entitlement checks.
 *
 * @property state Integer code representing the current state of the purchase.
 *                 Indicates whether the transaction is pending, completed, or
 *                 cancelled.
 *
 * @property purchased Flag indicating whether the item has been successfully
 *                     purchased. True if the transaction is complete.
 *
 * @property isAcknowledged Internal flag indicating whether the purchase has
 *                          been acknowledged by the app. Required to comply
 *                          with billing system rules.
 *
 * @property quantity Number of units purchased in this transaction. Typically
 *                    1 for most products, but may vary for consumables.
 *
 * @property time Epoch timestamp (milliseconds) when the purchase was made.
 *                Useful for subscription validation and history display.
 */
class Purchase(
    val id: String,
    val acknowledged: Boolean,
    val state: Int,
    val quantity: Int = -1,
    val time: Long = -1,
)

/**
 * @return `true` if this [Purchase] object is `non-null`, has been `acknowledged`, and is in the
 * [GP_Purchase.PurchaseState.PURCHASED] state. Returns `false` otherwise.
 */
val Purchase?.purchased get() = if (this == null) false else acknowledged && state == Paymaster.STATE_PURCHASED
