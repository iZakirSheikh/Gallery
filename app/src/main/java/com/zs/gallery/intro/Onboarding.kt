@file:OptIn(ExperimentalPermissionsApi::class)

package com.zs.gallery.intro

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.zs.compose.foundation.textResource
import com.zs.compose.theme.LocalWindowSize
import com.zs.compose.theme.OutlinedButton
import com.zs.compose.theme.WindowSize
import com.zs.compose.theme.WindowSize.Category
import com.zs.gallery.R
import com.zs.gallery.common.Gallery as G
import com.zs.gallery.common.LocalNavigator
import com.zs.gallery.common.Route
import com.zs.gallery.common.compose.Placeholder
import com.google.accompanist.permissions.rememberMultiplePermissionsState as Permissions


@Composable
@NonRestartableComposable
fun Onboarding() {
    val navigator = LocalNavigator.current
    // Compose the permission state.
    // Once granted set different route like folders as start.
    // Navigate from here to there.
    val permission = Permissions(permissions = G.REQUIRED_PERMISSIONS) {
        if (!it.all { (_, state) -> state }) return@Permissions
        navigator.rebase(Route.Timeline)
    }
    Placeholder(
        iconResId = R.raw.lt_permission,
        title = stringResource(R.string.scr_permission_title),
        message = textResource(R.string.scr_permission_desc),
        vertical = LocalWindowSize.current.width == Category.Small,
        action = {
            OutlinedButton(
                onClick = permission::launchMultiplePermissionRequest,
                modifier = Modifier.size(width = 200.dp, height = 46.dp),
                text = stringResource(R.string.allow),
                shape = CircleShape
            )
        }
    )
}