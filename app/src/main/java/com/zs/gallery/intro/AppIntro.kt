/*
 * Copyright (c)  2026 Zakir Sheikh
 *
 * Created by sheik on 4 of Jan 2026
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
 * Last Modified by sheik on 4 of Jan 2026
 *
 */

@file:OptIn(ExperimentalPermissionsApi::class)

package com.zs.gallery.intro


import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.zs.common.db.album.MediaProvider
import com.zs.compose.foundation.textResource
import com.zs.compose.theme.LocalWindowSize
import com.zs.compose.theme.OutlinedButton
import com.zs.compose.theme.WindowSize.Category
import com.zs.gallery.common.LocalNavController
import com.zs.gallery.common.Res
import com.zs.gallery.common.Route
import com.zs.gallery.common.compose.Placeholder
import com.google.accompanist.permissions.rememberMultiplePermissionsState as Permissions

@Composable
@NonRestartableComposable
fun AppIntro() {
    // --- Navigation & Context Setup ---
    // Obtain the current navigation controller to handle route changes.
    // Get the current Android context for permission handling and sync operations.
    val navigator = LocalNavController.current
    val context = LocalContext.current

    // --- Permission Handling ---
    // Define the permission state using the app's manifest permissions.
    // Once all required permissions are granted:
    //   1. Trigger immediate media synchronization.
    //   2. Rebase navigation to the Timeline route (main screen).
    val permission = Permissions(permissions = Res.manifest.permissions) { results ->
        // Check if all permissions are granted; if not, exit early.
        if (!results.all { (_, state) -> state }) return@Permissions
        MediaProvider.runImmediateSync(context)  // Perform immediate sync of media content.
        navigator.rebase(Route.Timeline)   // Navigate to Timeline as the new starting route.
    }

    // --- UI Placeholder ---
    // Display a placeholder screen prompting the user to grant permissions.
    // Includes:
    //   - Icon (illustration for permissions)
    //   - Title and description text
    //   - Layout adapts based on window size (vertical for small screens)
    //   - Action button to request permissions
    Placeholder(
        iconResId = Res.raw.lt_permission,
        title = stringResource(Res.string.scr_permission_title),
        message = textResource(Res.string.scr_permission_desc),
        vertical = LocalWindowSize.current.width == Category.Small,
        action = {
            // Outlined button that launches the permission request dialog.
            OutlinedButton(
                onClick = permission::launchMultiplePermissionRequest,
                modifier = Modifier.size(width = 200.dp, height = 46.dp),
                text = stringResource(Res.string.allow),
                shape = Res.shape.circle
            )
        }
    )
}