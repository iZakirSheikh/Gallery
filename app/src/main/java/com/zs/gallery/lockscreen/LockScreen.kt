/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by 2024 on 12-09-2024.
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

package com.zs.gallery.lockscreen

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.size
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.primex.core.findActivity
import com.primex.core.textResource
import com.primex.material2.Button
import com.zs.foundation.AppTheme
import com.zs.foundation.LocalWindowSize
import com.zs.foundation.Range
import com.zs.gallery.R
import com.zs.gallery.common.LocalSystemFacade
import com.zs.gallery.common.Route

object RouteLockScreen: Route

@SuppressLint("NewApi")
@Composable
fun LockScreen() {
    // If the permissions are not granted, show the permission screen.
    val facade = LocalSystemFacade.current

    // Prevent user form removing this veil.
    val context = LocalContext.current
    BackHandler { context.findActivity().moveTaskToBack(true) }

    com.zs.gallery.common.Placeholder(
        iconResId = R.raw.lt_app_lock,
        title = stringResource(R.string.lock_scr_title),
        message = textResource(R.string.lock_scr_desc),
        vertical = LocalWindowSize.current.widthRange == Range.Compact,
    ) {
        Button(
            onClick = facade::unlock,
            modifier = Modifier.size(width = 200.dp, height = 46.dp),
            elevation = null,
            label = "Authenticate",
            border = ButtonDefaults.outlinedBorder,
            shape = AppTheme.shapes.medium,
        )
    }
}