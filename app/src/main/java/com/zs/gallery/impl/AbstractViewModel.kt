/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 12-07-2024.
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

package com.zs.gallery.impl

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModel
import com.primex.core.getText2
import com.zs.compose_ktx.toast.Duration
import com.zs.compose_ktx.toast.ToastHostState
import com.zs.compose_ktx.toast.Result
import com.zs.compose_ktx.toast.Toast
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

open class AbstractViewModel() : ViewModel(), KoinComponent {

    private val resources: Resources by inject()
    private val relay: ToastHostState by inject()

    suspend fun showToast(
        message: CharSequence,
        action: CharSequence? = null,
        icon: ImageVector? = null,
        accent: Color = Color.Unspecified,
        @Duration duration: Int = if (action == null) Toast.DURATION_SHORT else Toast.DURATION_INDEFINITE
    ): @Result Int = relay.showToast(message, action, icon, accent, duration)

    suspend fun showToast(
        @StringRes message: Int,
        @StringRes action: Int = ResourcesCompat.ID_NULL,
        icon: ImageVector? = null,
        accent: Color = Color.Unspecified,
        @Duration duration: Int = if (action == ResourcesCompat.ID_NULL) Toast.DURATION_SHORT else Toast.DURATION_INDEFINITE
    ): @Result Int = showToast(
        message = resources.getText2(message),
        action = if (action == ResourcesCompat.ID_NULL) null else resources.getText2(action),
        icon = icon,
        accent = accent,
        duration = duration
    )
}