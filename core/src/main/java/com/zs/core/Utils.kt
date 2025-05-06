/*
 * Copyright 2025 sheik
 *
 * Created by sheik on 05-05-2025.
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

package com.zs.core

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager

fun PackageManager.findActivity(value: Intent, pkg: String): Intent? {
    val info = queryIntentActivities(value, 0).find {
        it.activityInfo.packageName.contains(pkg)
    }
    if (info == null)
        return null
   return Intent(value).apply {
        component = ComponentName(
            info.activityInfo.packageName,
            info.activityInfo.name
        )
    }


}

inline fun Intent(action: String, block: Intent.() -> Unit) = Intent(action).apply(block)