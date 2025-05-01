/*
 * Copyright 2025 sheik
 *
 * Created by sheik on 01-05-2025.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zs.gallery.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.zs.core.billing.Paymaster
import com.zs.core.billing.Product

// Here extension methods to Paymaster are defined that are used within the app.

val Paymaster.Companion.IAP_BUY_ME_COFFEE get() = "buy_me_a_coffee"

private val _products = arrayOf(
    Paymaster.IAP_BUY_ME_COFFEE
)

/**
 * Represents a array of valid product ids for a valid set of products.
 *
 * @property products An [Array] of valid product IDs that can be purchased through Google Play Billing.
 *                   These IDs correspond to product listings configured in the Google Play Console.
 *                   These product Ids are used to query for [Product] and to initiate purchases.
 */
val Paymaster.Companion.products get() = _products

/**
 * Returns a formatted [AnnotatedString] representation of the product description.
 *
 * This property formats the product information by displaying the title in bold
 * followed by the description on a new line. It uses an [AnnotatedString] for
 * richer text representation.
 *
 * @return An [AnnotatedString] containing the formatted product description.
 */
val Product.richDesc
    get() = buildAnnotatedString {
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { appendLine(title.ellipsize(22)) }
        withStyle(SpanStyle(Color.Gray)) { append(description) }
    }