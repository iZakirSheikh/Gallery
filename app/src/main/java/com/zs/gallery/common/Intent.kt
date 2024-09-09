/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by 2024 on 09-09-2024.
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

@file:Suppress("FunctionName")

package com.zs.gallery.common

import android.content.Intent
import android.net.Uri


/**
 * Composes an Intent to open the app details page on the App Store for a given package.
 *
 * This Intent is designed to open the App Store without remaining in the task stack.
 *
 * @param pkg the package name of the app to open on the App Store.
 * @return the Intent to open the App Store.
 * @see FallbackAppStoreIntent
 */
fun AppStoreIntent(
    pkg: String
): Intent {
    return Intent(Intent.ACTION_VIEW).apply {
        setPackage(pkg)
        addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )
    }
}

/**
 * Composes an Intent to open the app details page on the App Store for a given package.
 * @see AppStoreIntent
 */
fun FallbackAppStoreIntent(pkg: String) =
    Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=$pkg"))