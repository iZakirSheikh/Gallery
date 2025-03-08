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

@file:Suppress("NOTHING_TO_INLINE")

package com.zs.gallery.settings

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.AutoAwesomeMotion
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Textsms
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.primex.core.fadeEdge
import com.primex.core.plus
import com.primex.core.textArrayResource
import com.primex.core.thenIf
import com.primex.material2.Button
import com.primex.material2.DropDownPreference
import com.primex.material2.IconButton
import com.primex.material2.Label
import com.primex.material2.ListTile
import com.primex.material2.Preference
import com.primex.material2.SliderPreference
import com.primex.material2.SwitchPreference
import com.primex.material2.Text
import com.primex.material2.TextButton
import com.primex.material2.appbar.LargeTopAppBar
import com.primex.material2.appbar.TopAppBarDefaults
import com.primex.material2.appbar.TopAppBarScrollBehavior
import com.zs.foundation.AppTheme
import com.zs.foundation.Colors
import com.zs.foundation.Header
import com.zs.foundation.LocalWindowSize
import com.zs.foundation.NightMode
import com.zs.foundation.None
import com.zs.foundation.Range
import com.zs.foundation.adaptive.HorizontalTwoPaneStrategy
import com.zs.foundation.adaptive.TwoPane
import com.zs.foundation.adaptive.VerticalTwoPaneStrategy
import com.zs.foundation.adaptive.contentInsets
import com.zs.gallery.BuildConfig
import com.zs.gallery.R
import com.zs.gallery.common.LocalNavController
import com.zs.gallery.common.LocalSystemFacade
import com.zs.gallery.common.preference
import androidx.compose.foundation.layout.PaddingValues as Padding
import androidx.compose.ui.graphics.RectangleShape as Rectangle
import com.primex.core.rememberVectorPainter as painter
import com.primex.core.textResource as stringResource
import com.zs.foundation.ContentPadding as CP

private const val TAG = "Settings"

// The max width of the secondary pane
private val sPaneMaxWidth = 280.dp

// Used to style individual items within a preference section.
private val TopTileShape = RoundedCornerShape(24.dp, 24.dp, 0.dp, 0.dp)
private val CentreTileShape = Rectangle
private val BottomTileShape = RoundedCornerShape(0.dp, 0.dp, 24.dp, 24.dp)
private val SingleTileShape = RoundedCornerShape(24.dp)

private val Colors.tileBackgroundColor
    @ReadOnlyComposable @Composable inline get() = background(elevation = 1.dp)

// when topBar doesn't fill the screen; this is for that case.
private val RoundedTopBarShape = RoundedCornerShape(15)

/**
 * Represents a Top app bar for this screen.
 *
 * Handles padding/margins based on shape to ensure proper layout.
 *
 * @param modifier [Modifier] to apply to this top app bar.
 * @param shape [Shape] of the top app bar. Defaults to `null`.
 * @param behaviour [TopAppBarScrollBehavior] for scroll behavior.
 */
@Composable
@NonRestartableComposable
private fun TopAppBar(
    modifier: Modifier = Modifier,
    insets: WindowInsets = WindowInsets.None,
    shape: Shape? = null,
    behaviour: TopAppBarScrollBehavior? = null
) {
    LargeTopAppBar(
        modifier = modifier.thenIf(shape != null) {
            windowInsetsPadding(insets)
                .padding(horizontal = CP.medium)
                .clip(shape!!)
        },
        title = { Label(text = stringResource(id = R.string.settings)) },
        navigationIcon = {
            val navController = LocalNavController.current
            IconButton(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                onClick = navController::navigateUp
            )
        },
        scrollBehavior = behaviour,
        windowInsets = if (shape == null) insets else WindowInsets.None,
        style = TopAppBarDefaults.largeAppBarStyle(
            scrolledContainerColor = AppTheme.colors.background(2.dp),
            scrolledContentColor = AppTheme.colors.onBackground,
            containerColor = AppTheme.colors.background,
            contentColor = AppTheme.colors.onBackground
        ),
        actions = {
            val facade = LocalSystemFacade.current
            // Feedback
            IconButton(
                imageVector = Icons.Outlined.AlternateEmail,
                onClick = { facade.launch(Settings.FeedbackIntent) },
            )
            // Star on Github
            IconButton(
                imageVector = Icons.Outlined.DataObject,
                onClick = { facade.launch(Settings.GithubIntent) },
            )
            // Report Bugs on Github.
            IconButton(
                imageVector = Icons.Outlined.BugReport,
                onClick = { facade.launch(Settings.GitHubIssuesPage) },
            )
            // Join our telegram channel
            IconButton(
                imageVector = Icons.Outlined.Textsms,
                onClick = { facade.launch(Settings.TelegramIntent) },
            )
        }
    )
}

private val HeaderPadding = PaddingValues(horizontal = CP.large, vertical = CP.xLarge)

/**
 * Represents the group header of [Preference]s
 */
@Composable
private inline fun GroupHeader(
    text: CharSequence,
    modifier: Modifier = Modifier,
    paddingValues: Padding = HeaderPadding,
) {
    Text(
        text = text,
        modifier = Modifier
            .padding(paddingValues)
            .then(modifier),
        color = AppTheme.colors.accent,
        style = AppTheme.typography.titleSmall
    )
}

private val APP_LOCK_VALUES = arrayOf(-1, 0, 1, 30)

private const val CONTENT_TYPE_HEADER = "header"
private const val CONTENT_TYPE_ITEM = "item"

/**
 * Represents the settings of General
 */
@SuppressLint("NewApi")
private inline fun LazyListScope.General(
    viewState: SettingsViewState
) {
    // Live Gallery
    item(contentType = CONTENT_TYPE_ITEM) {
        val prefLiveGallery by preference(Settings.KEY_DYNAMIC_GALLERY)
        SwitchPreference(
            text = stringResource(R.string.pref_live_gallery),
            checked = prefLiveGallery,
            icon = Icons.Outlined.AutoAwesomeMotion,
            onCheckedChange = { viewState.set(Settings.KEY_DYNAMIC_GALLERY, it) },
            modifier = Modifier.background(AppTheme.colors.tileBackgroundColor, TopTileShape)
        )
    }

    // AppLock
    item(contentType = CONTENT_TYPE_ITEM) {
        val facade = LocalSystemFacade.current
        //
        val value by preference(Settings.KEY_APP_LOCK_TIME_OUT)
        val entries = textArrayResource(R.array.pref_app_lock_options)
        DropDownPreference(
            text = stringResource(
                R.string.pref_app_lock_s,
                entries[APP_LOCK_VALUES.indexOf(value)]
            ),
            value = value,
            icon = Icons.Default.LightMode,
            entries = entries,
            onRequestChange = { value ->
                // User wishes to enable app lock
                if (!facade.canAuthenticate()) {
                    // If the user cannot authenticate, prompt them to enroll in biometric authentication
                    return@DropDownPreference facade.enroll()
                }
                // Securely make sure that app_lock is set.
                facade.authenticate((facade as Activity).getString(R.string.auth_confirm_biometric)) {
                    viewState.set(Settings.KEY_APP_LOCK_TIME_OUT, value)
                }
            },
            values = APP_LOCK_VALUES,
            modifier = Modifier.background(AppTheme.colors.tileBackgroundColor, CentreTileShape),
        )
    }

    // Recycle Bin
    item(contentType = CONTENT_TYPE_ITEM) {
        val value by preference(Settings.KEY_TRASH_CAN_ENABLED)
        SwitchPreference(
            text = stringResource(R.string.pref_enable_trash_can),
            checked = value,
            icon = Icons.Default.Recycling,
            onCheckedChange = { viewState.set(Settings.KEY_TRASH_CAN_ENABLED, it) },
            modifier = Modifier
                .background(AppTheme.colors.tileBackgroundColor, CentreTileShape)
        )
    }

    // Secure Mode
    item(contentType = CONTENT_TYPE_ITEM) {
        val value by preference(Settings.KEY_SECURE_MODE)
        SwitchPreference(
            text = stringResource(R.string.pref_secure_mode),
            checked = value,
            icon = Icons.Default.Security,
            onCheckedChange = { viewState.set(Settings.KEY_SECURE_MODE, it) },
            modifier = Modifier.background(AppTheme.colors.tileBackgroundColor, BottomTileShape)
        )
    }
}

/**
 * Represents items that are related to appearence of the App.
 */
private inline fun LazyListScope.Appearence(
    viewState: SettingsViewState
) {
    // Night Mode
    item(contentType = CONTENT_TYPE_ITEM) {
        // Night Mode Strategy
        // The strategy to use for night mode.
        val value by preference(Settings.KEY_NIGHT_MODE)
        val entries = textArrayResource(R.array.pref_night_mode_entries)
        DropDownPreference(
            text = stringResource(R.string.pref_app_theme_s, entries[value.ordinal]),
            value = value,
            icon = Icons.Default.LightMode,
            entries = entries,
            onRequestChange = {
                viewState.set(Settings.KEY_NIGHT_MODE, it)
            },
            values = NightMode.values(),
            modifier = Modifier.background(AppTheme.colors.tileBackgroundColor, TopTileShape)
        )
    }

    // Dynamic Colors
    item(contentType = CONTENT_TYPE_ITEM) {
        // Translucent System Bars
        // Whether System Bars are rendered as translucent or Transparent.
        val value by preference(Settings.KEY_DYNAMIC_COLORS)
        SwitchPreference(
            checked = value,
            text = stringResource(R.string.pref_dynamic_colors),
            onCheckedChange = { should: Boolean ->
                viewState.set(Settings.KEY_DYNAMIC_COLORS, should)
            },
            modifier = Modifier
                .background(AppTheme.colors.tileBackgroundColor, CentreTileShape)
        )
    }
    // Font Scale
    item(contentType = CONTENT_TYPE_ITEM) {
        // The font scale to use for the app if -1 is used, the system font scale is used.
        val scale by preference(Settings.KEY_FONT_SCALE)
        SliderPreference(
            value = scale,
            text = stringResource(R.string.pref_font_scale),
            valueRange = 0.7f..2f,
            steps = 13,   // (2.0 - 0.7) / 0.1 = 13 steps
            icon = Icons.Outlined.FormatSize,
            preview = {
                Label(
                    text = when {
                        it < 0.76f -> stringResource(R.string.system)
                        else -> stringResource(R.string.postfix_x_f, it)
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            onRequestChange = { value: Float ->
                val newValue = if (value < 0.76f) -1f else value
                viewState.set(Settings.KEY_FONT_SCALE, newValue)
            },
            modifier = Modifier
                .background(AppTheme.colors.tileBackgroundColor, CentreTileShape)
        )
    }

    // Trasnsparent System Bars
    item(contentType = CONTENT_TYPE_ITEM) {
        // Translucent System Bars
        // Whether System Bars are rendered as translucent or Transparent.
        val value by preference(Settings.KEY_TRANSPARENT_SYSTEM_BARS)
        SwitchPreference(
            checked = value,
            text = stringResource(R.string.pref_transparent_system_bars),
            onCheckedChange = { should: Boolean ->
                viewState.set(Settings.KEY_TRANSPARENT_SYSTEM_BARS, should)
            },
            modifier = Modifier
                .background(AppTheme.colors.tileBackgroundColor, CentreTileShape)
        )
    }

    // Use Accent in NavBar
    item(contentType = CONTENT_TYPE_ITEM) {
        val useAccent by preference(Settings.KEY_USE_ACCENT_IN_NAV_BAR)
        SwitchPreference(
            stringResource(R.string.pref_color_nav_bar),
            checked = useAccent,
            onCheckedChange = { viewState.set(Settings.KEY_USE_ACCENT_IN_NAV_BAR, it) },
            modifier = Modifier
                .background(AppTheme.colors.tileBackgroundColor, CentreTileShape),
        )
    }

    // Grid Size Multiplier

    item(contentType = CONTENT_TYPE_ITEM) {
        // Grid Item Multiplier
        // The multiplier increases/decreases the size of the grid item from 0.6 to 2f
        val gridItemSizeMultiplier by preference(Settings.KEY_GRID_ITEM_SIZE_MULTIPLIER)
        SliderPreference(
            value = gridItemSizeMultiplier,
            text = stringResource(R.string.pref_grid_item_size_multiplier),
            valueRange = 0.6f..2f,
            steps = 14, // (2.0 - 0.7) / 0.1 = 13 steps
            icon = Icons.Outlined.Dashboard,
            preview = {
                Label(
                    text = stringResource(R.string.postfix_x_f, it),
                    fontWeight = FontWeight.Bold
                )
            },
            onRequestChange = { value: Float ->
                viewState.set(Settings.KEY_GRID_ITEM_SIZE_MULTIPLIER, value)
            },
            modifier = Modifier.background(AppTheme.colors.tileBackgroundColor, CentreTileShape)
        )
    }

    // Hide/Show SystemBars for Immersive View
    item(contentType = CONTENT_TYPE_ITEM) {
        // Whether System Bars are hidden for immersive view or not.
        val immersiveView by preference(Settings.KEY_IMMERSIVE_VIEW)
        SwitchPreference(
            checked = immersiveView,
            text = stringResource(R.string.pref_immersive_view),
            onCheckedChange = { should: Boolean ->
                viewState.set(Settings.KEY_IMMERSIVE_VIEW, should)
            },
            modifier = Modifier
                .background(AppTheme.colors.tileBackgroundColor, BottomTileShape)
        )
    }
}

@Composable
private inline fun ColumnScope.AboutUs() {
    // The app version and check for updates.
    val facade = LocalSystemFacade.current
    ListTile(
        headline = { Label(stringResource(R.string.version), fontWeight = FontWeight.Bold) },
        subtitle = {
            Label(
                stringResource(R.string.version_info_s, BuildConfig.VERSION_NAME)
            )
        },
        footer = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CP.medium)
            ) {
                TextButton(
                    stringResource(R.string.update_gallery),
                    onClick = { facade.launchUpdateFlow(true) })
                TextButton(
                    stringResource(R.string.join_the_beta),
                    onClick = { facade.launch(Settings.JoinBetaIntent) },
                    enabled = false
                )
            }
        },
        leading = { Icon(imageVector = Icons.Outlined.NewReleases, contentDescription = null) },
    )

    // Privacy Policy
    Preference(
        text = stringResource(R.string.pref_privacy_policy),
        icon = Icons.Outlined.PrivacyTip,
        modifier = Modifier
            .clip(AppTheme.shapes.medium)
            .clickable { facade.launch(Settings.PrivacyPolicyIntent) },
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CP.medium)
    ) {
        Button(
            label = stringResource(R.string.rate_us),
            icon = painter(Icons.Outlined.Star),
            onClick = facade::launchAppStore,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = AppTheme.colors.background(2.dp),
                contentColor = AppTheme.colors.accent
            ),
            elevation = null,
            shape = AppTheme.shapes.small
        )

        Button(
            label = stringResource(R.string.share_app_label),
            icon = painter(Icons.Outlined.Share),
            onClick = { facade.launch(Settings.ShareAppIntent) },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = AppTheme.colors.background(2.dp),
                contentColor = AppTheme.colors.accent
            ),
            elevation = null,
            shape = AppTheme.shapes.small
        )
    }
}

/**
 * Represents the Settings screen.
 */
@Composable
fun Settings(viewState: SettingsViewState) {
    // Retrieve the current window size
    val (width, _) = LocalWindowSize.current
    // Determine the two-pane strategy based on window width range
    // when in mobile portrait; we don't show second pane;
    val strategy = when {
        // TODO  -Replace with OnePane Strategy when updating TwoPane Layout.
        width < Range.Medium -> VerticalTwoPaneStrategy(0.5f) // Use stacked layout with bias to centre for small screens
        else -> HorizontalTwoPaneStrategy(0.5f) // Use horizontal layout with 50% split for large screens
    }
    // Layout Modes:
    // When the width exceeds the "Compact" threshold, the layout is no longer immersive.
    // This is because a navigation rail is likely displayed, requiring content to be
    // indented rather than filling the entire screen width.
    //
    // The threshold helps to dynamically adjust the UI for different device form factors
    // and orientations, ensuring appropriate use of space. In non-compact layouts,
    // elements like the navigation rail or side panels prevent an immersive, full-width
    // layout, making the design more suitable for larger screens.
    val immersive = width < Range.Medium
    // Define the scroll behavior for the top app bar
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    // obtain the padding of BottomNavBar/NavRail
    val navBarPadding = WindowInsets.contentInsets
    // Place the content
    // FIXME: Width < 650dp then screen is single pane what if navigationBars are at end.
    TwoPane(
        spacing = CP.normal,
        strategy = strategy,
        topBar = {
            TopAppBar(
                behaviour = topAppBarScrollBehavior,
                insets = WindowInsets.statusBars,
                shape = if (immersive) null else RoundedTopBarShape,
            )
        },
        details = {
            // this will not be called when in single pane mode
            // this is just for decoration
            if (strategy is VerticalTwoPaneStrategy) return@TwoPane
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(top = CP.medium)
                    .widthIn(max = sPaneMaxWidth)
                    .systemBarsPadding()
                    .padding(navBarPadding),
                content = {
                    Header(
                        stringResource(R.string.about_us),
                        color = AppTheme.colors.accent,
                        // drawDivider = true,
                        style = AppTheme.typography.titleSmall,
                        contentPadding = PaddingValues(vertical = CP.normal, horizontal = CP.medium)
                    )
                    AboutUs()
                }
            )
        },
        content = {
            val state = rememberLazyListState()
            LazyColumn(
                state = state,
                // In immersive mode, add horizontal padding to prevent settings from touching the screen edges.
                // Immersive layouts typically have a bottom app bar, so extra padding improves aesthetics.
                // Non-immersive layouts only need vertical padding.
                contentPadding = Padding(if (immersive) CP.large else CP.medium, vertical = CP.normal) + navBarPadding + WindowInsets.contentInsets,
                modifier = Modifier
                    .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
                    .fadeEdge(AppTheme.colors.background(2.dp), state, false)
                    .thenIf(immersive) { navigationBarsPadding() },
            ) {
                // General
                item(contentType = CONTENT_TYPE_HEADER) {
                    GroupHeader(
                        text = stringResource(id = R.string.general),
                        paddingValues = Padding(CP.normal, CP.small, CP.normal, CP.xLarge)
                    )
                }
                General(viewState)

                // Appearence
                item(CONTENT_TYPE_HEADER) { GroupHeader(text = stringResource(id = R.string.appearance)) }
                Appearence(viewState = viewState)

                // AboutUs
                // Load AboutUs here if this is mobile port
                if (strategy !is VerticalTwoPaneStrategy)
                    return@LazyColumn

                item(contentType = CONTENT_TYPE_HEADER) {
                    Header(
                        stringResource(R.string.about_us),
                        color = AppTheme.colors.accent,
                        //drawDivider = true,
                        style = AppTheme.typography.titleSmall,
                        contentPadding = Padding(vertical = CP.normal, horizontal = CP.medium)
                    )
                }

                item(contentType = "about_us") {
                    Column { AboutUs() }
                }
            }
        }
    )
}
