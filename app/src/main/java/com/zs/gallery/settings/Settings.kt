/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 23-07-2024.
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

package com.zs.gallery.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.primex.core.textResource
import com.primex.material2.DropDownPreference
import com.primex.material2.IconButton
import com.primex.material2.Label
import com.primex.material2.Preference
import com.primex.material2.SliderPreference
import com.primex.material2.SwitchPreference
import com.primex.material2.appbar.LargeTopAppBar
import com.primex.material2.appbar.TopAppBarDefaults
import com.primex.material2.appbar.TopAppBarScrollBehavior
import com.zs.foundation.AppTheme
import com.zs.foundation.Colors
import com.zs.foundation.None
import com.zs.foundation.adaptive.contentInsets
import com.zs.gallery.BuildConfig
import com.zs.gallery.R
import com.zs.gallery.common.LocalNavController
import com.zs.gallery.common.LocalSystemFacade
import com.zs.gallery.common.NightMode
import kotlin.math.roundToInt

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Constants
// Region: Preference Item Shapes - Used to style individual items within a preference section.
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
private val TopTileShape = RoundedCornerShape(24.dp, 24.dp, 0.dp, 0.dp)
private val CentreTileShape = RectangleShape
private val BottomTileShape = RoundedCornerShape(0.dp, 0.dp, 24.dp, 24.dp)
private val SingleTileShape = RoundedCornerShape(24.dp)

private val Colors.tileBackgroundColor
    @ReadOnlyComposable @Composable get() =
        background(elevation = 1.dp)

@Composable
@NonRestartableComposable
private fun Toolbar(
    modifier: Modifier = Modifier,
    behavior: TopAppBarScrollBehavior? = null
) {
    LargeTopAppBar(
        title = { Label(text = textResource(id = R.string.settings)) },
        scrollBehavior = behavior,
        modifier = modifier,
        navigationIcon = {
            val navController = LocalNavController.current
            IconButton(
                imageVector = Icons.Filled.ArrowBack,
                onClick = navController::navigateUp
            )
        },
        style = TopAppBarDefaults.largeAppBarStyle(
            scrolledContainerColor = AppTheme.colors.background(elevation = 1.dp),
            containerColor = AppTheme.colors.background,
            scrolledContentColor = AppTheme.colors.onBackground,
            contentColor = AppTheme.colors.onBackground
        )
    )
}

@Suppress("NOTHING_TO_INLINE")
@Composable
private inline fun GroupHeader(
    text: CharSequence,
    modifier: Modifier = Modifier
) {
    com.primex.material2.Text(
        text = text,
        modifier = Modifier
            .padding(AppTheme.padding.xLarge)
            .then(modifier),
        color = AppTheme.colors.accent,
        style = AppTheme.typography.titleSmall
    )
}

context(ColumnScope)
@SuppressLint("NewApi")
@Composable
private inline fun General(
    viewState: SettingsViewState
) {


    val prefLiveGallery = viewState.liveGallery
    SwitchPreference(
        title = prefLiveGallery.title,
        checked = prefLiveGallery.value,
        summery = prefLiveGallery.summery,
        icon = prefLiveGallery.vector,
        onCheckedChange = { viewState.set(Settings.KEY_DYNAMIC_GALLERY, it) },
        modifier = Modifier
            .background(AppTheme.colors.tileBackgroundColor, TopTileShape)
    )

    val prefAppLock = viewState.applock
    val facade = LocalSystemFacade.current
    DropDownPreference(
        title = prefAppLock.title,
        defaultValue = prefAppLock.value,
        icon = prefAppLock.vector,
        entries = listOf(
            "App Lock Disabled" to -1,
            "Lock Immediately" to 0,
            "Lock After 1 Minute" to 1,
            "Lock After 30 Minutes" to 30
        ),
        modifier = Modifier
            .background(AppTheme.colors.tileBackgroundColor, CentreTileShape),
        onRequestChange = {value ->
            // User wishes to enable app lock
            if (!facade.canAuthenticate()) {
                // If the user cannot authenticate, prompt them to enroll in biometric authentication
                return@DropDownPreference facade.enroll()
            }
            // Securely make sure that app_lock is set.
            facade.authenticate("Confirm Biometric") {
                viewState.set(Settings.KEY_APP_LOCK_TIME_OUT, value)
            }
        },
    )

    val prefTrashCan = viewState.trashCanEnabled
    SwitchPreference(
        title = prefTrashCan.title,
        checked = prefTrashCan.value,
        summery = prefTrashCan.summery,
        icon = prefTrashCan.vector,
        onCheckedChange = { viewState.set(Settings.KEY_TRASH_CAN_ENABLED, it) },
        modifier = Modifier
            .background(AppTheme.colors.tileBackgroundColor, CentreTileShape)
    )

    val prefGridSizeMultiplier = viewState.gridItemSizeMultiplier
    SliderPreference(
        title = prefGridSizeMultiplier.title,
        summery = prefGridSizeMultiplier.summery,
        icon = prefGridSizeMultiplier.vector,
        valueRange = 0.5f..1.5f,
        steps = 9,
        defaultValue = prefGridSizeMultiplier.value,
        onValueChange = { viewState.set(Settings.KEY_GRID_ITEM_SIZE_MULTIPLIER, it) },
        modifier = Modifier.background(AppTheme.colors.tileBackgroundColor, CentreTileShape),
        preview = {
            Label(text = stringResource(R.string.times_factor_x_f, prefGridSizeMultiplier.value))
        }
    )

    val prefSecureMode = viewState.secureMode
    SwitchPreference(
        title = prefSecureMode.title,
        checked = prefSecureMode.value,
        summery = prefSecureMode.summery,
        icon = prefSecureMode.vector,
        onCheckedChange = { viewState.set(Settings.KEY_SECURE_MODE, it) },
        modifier = Modifier
            .background(AppTheme.colors.tileBackgroundColor, BottomTileShape)
    )
}

context(ColumnScope)
@Composable
private inline fun Appearance(
    viewState: SettingsViewState
) {
    val prefNightMode = viewState.nightMode
    DropDownPreference(
        title = prefNightMode.title,
        defaultValue = prefNightMode.value,
        icon = prefNightMode.vector,
        onRequestChange = { viewState.set(Settings.KEY_NIGHT_MODE, it) },
        entries = listOf(
            stringResource(R.string.dark) to NightMode.YES,
            stringResource(R.string.light) to NightMode.NO,
            stringResource(R.string.sync_with_system) to NightMode.FOLLOW_SYSTEM
        ),
        modifier = Modifier
            .background(AppTheme.colors.tileBackgroundColor, TopTileShape)
    )

    val isSystemBarsTransparent = viewState.isSystemBarsTransparent
    SwitchPreference(
        title = isSystemBarsTransparent.title,
        checked = isSystemBarsTransparent.value,
        summery = isSystemBarsTransparent.summery,
        icon = isSystemBarsTransparent.vector,
        onCheckedChange = { viewState.set(Settings.KEY_TRANSPARENT_SYSTEM_BARS, it) },
        modifier = Modifier
            .background(AppTheme.colors.tileBackgroundColor, CentreTileShape)
    )

    val prefImmersiveView = viewState.immersiveView
    SwitchPreference(
        title = prefImmersiveView.title,
        checked = prefImmersiveView.value,
        summery = prefImmersiveView.summery,
        icon = prefImmersiveView.vector,
        onCheckedChange = { viewState.set(Settings.KEY_IMMERSIVE_VIEW, it) },
        modifier = Modifier
            .background(AppTheme.colors.tileBackgroundColor, CentreTileShape)
    )

    val prefFontScale = viewState.fontScale
    SliderPreference(
        title = prefFontScale.title,
        summery = prefFontScale.summery,
        icon = prefFontScale.vector,
        // (2.0 - 0.7) / 0.1 = 13 steps
        steps = 13,
        valueRange = 0.7f..2.0f,
        defaultValue = prefFontScale.value,
        onValueChange = { value ->
            val newValue = if (value < 0.76f) -1f else value
            viewState.set(Settings.KEY_FONT_SCALE, newValue)
        },
        modifier = Modifier
            .background(AppTheme.colors.tileBackgroundColor, BottomTileShape),
        preview = {
            Label(
                text = if (prefFontScale.value == -1f) "System" else stringResource(
                    R.string.times_factor_x_f,
                    prefFontScale.value
                ),
                fontWeight = FontWeight.Bold
            )
        }
    )
}

context(ColumnScope)
@Composable
private inline fun AboutUs(
    viewState: SettingsViewState
) {
    // The app version and check for updates.
    val facade = LocalSystemFacade.current
    Preference(
        title = textResource(id = R.string.pref_app_version),
        icon = Icons.Outlined.TouchApp,
        summery = textResource(id = R.string.pref_app_version_summery, BuildConfig.VERSION_NAME),
        modifier = Modifier
            .clickable { facade.launchUpdateFlow(true) }
            .background(AppTheme.colors.tileBackgroundColor, TopTileShape)
    )

    Preference(
        title = textResource(id = R.string.pref_privacy_policy),
        summery = textResource(id = R.string.pref_privacy_policy_summery),
        icon = Icons.Default.PrivacyTip,
        modifier = Modifier
            .background(AppTheme.colors.tileBackgroundColor, CentreTileShape)
    )

    Preference(
        title = textResource(id = R.string.pref_report_an_issue),
        summery = textResource(id = R.string.pref_report_an_issue_summery),
        icon = Icons.Default.ErrorOutline,
        modifier = Modifier
            .background(AppTheme.colors.tileBackgroundColor, BottomTileShape)
    )
}

@Composable
fun Settings(
    viewState: SettingsViewState
) {
    val behavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val insets = WindowInsets.contentInsets
    Scaffold(
        topBar = { Toolbar(behavior = behavior) },
        contentWindowInsets = WindowInsets.None,
        modifier = Modifier.nestedScroll(behavior.nestedScrollConnection),
        content = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(it)
                    .padding(WindowInsets.contentInsets)
                    .padding(
                        horizontal = AppTheme.padding.large,
                        vertical = AppTheme.padding.normal
                    ),
                content = {
                    GroupHeader(text = stringResource(id = R.string.general))
                    General(viewState = viewState)
                    GroupHeader(text = stringResource(id = R.string.appearance))
                    Appearance(viewState = viewState)
                    GroupHeader(text = stringResource(id = R.string.about_gallery))
                    AboutUs(viewState = viewState)
                }
            )
        }
    )
}

