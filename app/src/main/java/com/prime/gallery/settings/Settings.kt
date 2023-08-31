@file:OptIn(ExperimentalTextApi::class, ExperimentalTextApi::class)

package com.prime.gallery.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.prime.gallery.BuildConfig
import com.prime.gallery.Material
import com.prime.gallery.R
import com.prime.gallery.core.ContentPadding
import com.prime.gallery.core.NightMode
import com.prime.gallery.core.billing.Banner
import com.prime.gallery.core.billing.purchased
import com.prime.gallery.core.compose.LocalNavController
import com.prime.gallery.core.compose.LocalSystemFacade
import com.prime.gallery.core.compose.LocalWindowSizeClass
import com.prime.gallery.core.compose.purchase
import com.primex.core.rememberState
import com.primex.core.rotate
import com.primex.core.stringResource2
import com.primex.core.withStyle
import com.primex.material3.Button
import com.primex.material3.DropDownPreference
import com.primex.material3.IconButton
import com.primex.material3.Preference
import com.primex.material3.SwitchPreference

private const val TAG = "Settings"

// Url to pages
// FixMe: In future replace donate me with in app purchases.
private const val WEB_PAGE_URL = "https://github.com/prime-zs/toolz2"
private const val DONATE_ME_URL = "https://www.buymeacoffee.com/sheikhzaki3"

private val SourceLauncherIntent
    get() = Intent(Intent.ACTION_VIEW, Uri.parse(WEB_PAGE_URL)).apply {
        this.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    };
private val DonateIntent
    get() = Intent(Intent.ACTION_VIEW, Uri.parse(DONATE_ME_URL)).apply {
        this.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    };

private val TopCurvedShape = RoundedCornerShape(24.dp, 24.dp, 0.dp, 0.dp)
private val Rectangular = RectangleShape
private val BottomCurved = RoundedCornerShape(0.dp, 0.dp, 24.dp, 24.dp)
private val CurvedShape = RoundedCornerShape(24.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@NonRestartableComposable
private fun Toolbar(
    modifier: Modifier = Modifier,
    behavior: TopAppBarScrollBehavior? = null
) {
    val controller = LocalNavController.current
    LargeTopAppBar(
        title = { Text(text = "Settings") },
        scrollBehavior = behavior,
        modifier = modifier,
        navigationIcon = {
            IconButton(icon = Icons.Default.ArrowBack,
                contentDescription = null,
                onClick = { controller.navigateUp() })
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Material.colorScheme.background),
        actions = {
            // Show if not purchased.
            val provider = LocalSystemFacade.current
            val purchased by purchase(id = BuildConfig.IAP_NO_ADS)
            if (purchased.purchased)
                IconButton(
                    icon = Icons.Outlined.ShoppingCart,
                    contentDescription = "buy full version",
                    onClick = { provider.launchBillingFlow(BuildConfig.IAP_NO_ADS) },
                )
        },
    )
}

@Composable
@NonRestartableComposable
private fun SideBar(
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier,
        containerColor = Material.colorScheme.surfaceColorAtElevation(2.dp)
    ) {
        val controller = LocalNavController.current
        IconButton(
            icon = Icons.Default.ArrowBack,
            contentDescription = null,
            onClick = { controller.navigateUp() },
        )

        Text(
            text = stringResource(id = R.string.settings),
            modifier = Modifier
                .rotate(false)
                .weight(1f),
            style = Material.typography.labelLarge
        )

        // Show if not purchased.
        val provider = LocalSystemFacade.current
        val purchased by purchase(id = BuildConfig.IAP_NO_ADS)
        if (purchased.purchased)
            IconButton(
                icon = Icons.Outlined.ShoppingCart,
                contentDescription = "buy full version",
                onClick = { provider.launchBillingFlow(BuildConfig.IAP_NO_ADS) },
            )
    }
}

@Composable
private inline fun GroupHeader(
    text: CharSequence,
    modifier: Modifier = Modifier
) {
    com.primex.material3.Text(
        text = text,
        modifier = Modifier
            .padding(ContentPadding.normal)
            .padding(ContentPadding.normal)
            .then(modifier),
        color = Material.colorScheme.primary,
        style = Material.typography.titleMedium
    )
}

@Composable
private fun Banner(
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        // Title + Developer + App version
        // App name etc.
        Text(
            maxLines = 2,
            modifier = Modifier.fillMaxWidth(),
            text = buildAnnotatedString {
                val appName = stringResource(id = R.string.app_name)
                withStyle(Material.typography.headlineSmall) {
                    append(appName)
                }
                // The app version and check for updates.
                val version = BuildConfig.VERSION_NAME
                withStyle(Material.typography.labelSmall) {
                    append("v$version")
                }
                withStyle(Material.typography.labelMedium) {
                    append("\nby Zakir Sheikh")
                }
            },
        )

        // Donate + Source Code Row
        Row(
            modifier = Modifier.padding(top = ContentPadding.xLarge)
        ) {
            // Donate
            val context = LocalContext.current
            Button(
                label = stringResource(R.string.donate),
                icon = rememberVectorPainter(image = Icons.Default.Euro),
                onClick = { context.startActivity(DonateIntent) },
                shape = Material.shapes.small,
                colors = ButtonDefaults.buttonColors(containerColor = Material.colorScheme.primary),
                modifier = Modifier
                    .padding(end = ContentPadding.medium)
                    .weight(1f)
                    .heightIn(52.dp),
            )

            // Source code
            Button(
                label = stringResource(R.string.github),
                icon = rememberVectorPainter(image = Icons.Default.DataObject),
                onClick = { context.startActivity(SourceLauncherIntent) },
                shape = Material.shapes.small,
                colors = ButtonDefaults.buttonColors(containerColor = Material.colorScheme.secondary),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(52.dp),
            )
        }
    }
}

private val NIGHT_MODE_ENTRIES = listOf(
    "Dark" to NightMode.YES,
    "Light" to NightMode.NO,
    "Sync with System" to NightMode.FOLLOW_SYSTEM
)

context(ColumnScope)
@Composable
private inline fun Appearance(
    state: Settings
) {
    val facade = LocalSystemFacade.current
    // Dark UI MOde
    val darkUiStrategy = state.darkModeStrategy
    DropDownPreference(
        title = darkUiStrategy.title,
        defaultValue = darkUiStrategy.value,
        onRequestChange = { state.set(Settings.NIGHT_MODE, it); facade.showAd(force = true) },
        entries = NIGHT_MODE_ENTRIES,
        shape = TopCurvedShape,
        modifier = Modifier.padding(horizontal = ContentPadding.normal),
        icon = darkUiStrategy.vector
    )

    // Color status bar
    val colorSystemBars = state.colorSystemBars
    SwitchPreference(
        checked = colorSystemBars.value,
        title = colorSystemBars.title,
        summery = colorSystemBars.summery,
        onCheckedChange = { should: Boolean ->
            state.set(Settings.COLOR_SYSTEM_BARS, should)
            facade.showAd(force = true)
        },
        icon = colorSystemBars.vector,
        //shape = TopCurvedShape,
        modifier = Modifier
            .clip(RectangleShape)
            .padding(horizontal = ContentPadding.normal)
    )

    // Enable/Disable dynamic colors
    val dynamicColors = state.dynamicColors
    SwitchPreference(
        checked = dynamicColors.value,
        title = dynamicColors.title,
        summery = dynamicColors.summery,
        onCheckedChange = { should: Boolean ->
            state.set(Settings.DYNAMIC_COLORS, should)
        },
        icon = dynamicColors.vector,
        // shape = TopCurvedShape,
        modifier = Modifier
            .clip(RectangleShape)
            .padding(horizontal = ContentPadding.normal)
    )
    //Hide StatusBar
    val hideStatusBar = state.hideSystemBars
    SwitchPreference(
        checked = hideStatusBar.value,
        title = hideStatusBar.title,
        summery = hideStatusBar.summery,
        onCheckedChange = { should: Boolean ->
            state.set(Settings.HIDE_SYSTEM_BARS, should)
            facade.showAd(true)
        },
        icon = hideStatusBar.vector,
        //shape = TopCurvedShape,
        modifier = Modifier
            .padding(horizontal = ContentPadding.normal)
            .clip(BottomCurved)
    )
}

context(ColumnScope)
@Composable
private inline fun General(
    state: Settings
) {
    val trashcan = state.isTrashCanEnabled
    SwitchPreference(
        title = trashcan.title,
        checked = trashcan.value,
        summery = trashcan.summery,
        onCheckedChange = {
            state.set(Settings.TRASH_CAN_ENABLED, it)
        },
        icon = trashcan.vector,
        // shape = TopCurvedShape,
        modifier = Modifier.padding(horizontal = ContentPadding.normal)
    )
    val list = state.excludedFiles
    var showBlackListDialog by rememberState(initial = false)
    // The Blacklist Dialog.
    BlacklistDialog(
        showBlackListDialog,
        state = state,
        onDismissRequest = { showBlackListDialog = false },
    )

    Preference(
        title = list.title,
        summery = list.summery,
        icon = list.vector,
        shape = BottomCurved,
        modifier = Modifier
            .padding(horizontal = ContentPadding.normal)
            .clickable {
                showBlackListDialog = true
            },
    )
}

context(ColumnScope)
@Composable
private fun Feedback(
) {
    val facade = LocalSystemFacade.current
    Preference(
        title = stringResource(R.string.feedback),
        summery = stringResource2(id = R.string.pref_feedback_summery),
        icon = Icons.Outlined.Feedback,
        modifier = Modifier
            .padding(horizontal = ContentPadding.normal)
            .clickable { facade.launchAppStore() },
        shape = TopCurvedShape
    )

    Preference(
        title = stringResource2(id = R.string.pref_rate_us),
        summery = stringResource2(id = R.string.pref_rate_us_summery),
        icon = Icons.Outlined.Star,
        modifier = Modifier
            .padding(horizontal = ContentPadding.normal)
            .clickable { facade.launchAppStore() },
        shape = RectangleShape
    )

    Preference(
        title = stringResource2(R.string.pref_spread_the_word),
        summery = stringResource2(R.string.pref_spread_the_word_summery),
        icon = Icons.Outlined.Share,
        modifier = Modifier
            .padding(horizontal = ContentPadding.normal)
            .clickable { facade.shareApp() },
        shape = BottomCurved
    )
}

context(ColumnScope)
@Composable
private fun AboutUs(
) {
    val provider = LocalSystemFacade.current
    Preference(
        title = stringResource(R.string.pref_header_about_us),
        summery = stringResource2(R.string.pref_about_us_summery),
        shape = TopCurvedShape,
        modifier = Modifier.padding(horizontal = ContentPadding.normal),
        icon = Icons.Outlined.Info
    )

    // The app version and check for updates.
    val version = BuildConfig.VERSION_NAME
    Preference(
        title = stringResource2(R.string.pref_app_version),
        summery = stringResource2(id = R.string.pref_app_version_summery),
        icon = Icons.Outlined.TouchApp,
        modifier = Modifier
            .padding(horizontal = ContentPadding.normal)
            .clickable { provider.launchUpdateFlow(true) },
        shape = BottomCurved
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Compact(
    state: Settings,
    modifier: Modifier = Modifier
) {
    val behavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        topBar = { Toolbar(behavior = behavior) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.nestedScroll(behavior.nestedScrollConnection)
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .then(
                    modifier
                        .padding(it)
                        .navigationBarsPadding()
                )
        ) {
            // Banner on Card
            Surface(
                modifier = Modifier.padding(ContentPadding.normal),
                color = Material.colorScheme.surfaceColorAtElevation(2.dp),
                shape = CurvedShape
            ) {
                Banner(Modifier.padding(ContentPadding.normal))
            }

            // Ad Banner
            val purchase by purchase(id = BuildConfig.IAP_NO_ADS)
            if (!purchase.purchased)
                Banner(
                    placementID = BuildConfig.IAP_NO_ADS,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            // Apearence
            GroupHeader(text = stringResource2(id = R.string.pref_header_appearance))
            Appearance(state = state)

            // General
            GroupHeader(text = stringResource2(id = R.string.pref_header_general))
            General(state = state)

            // Feedback
            GroupHeader(text = stringResource2(id = R.string.pref_header_feedback))
            Feedback()

            // About Us
            GroupHeader(text = stringResource2(id = R.string.pref_header_about_us))
            AboutUs()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Medium(
    state: Settings,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxSize()) {
        //SideBar
        SideBar()
        // Main
        Spacer(modifier = Modifier.weight(1f))
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .widthIn(max = 600.dp)
                .align(Alignment.CenterVertically)
                .fillMaxHeight()
                .systemBarsPadding()
        ) {
            // Banner on Card
            Surface(
                modifier = Modifier.padding(ContentPadding.normal),
                color = Material.colorScheme.surfaceColorAtElevation(2.dp),
                shape = CurvedShape
            ) {
                Banner(Modifier.padding(ContentPadding.normal))
            }

            // Ad Banner
            val purchase by purchase(id = BuildConfig.IAP_NO_ADS)
            if (!purchase.purchased)
                Banner(
                    placementID = BuildConfig.IAP_NO_ADS,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            // Apearence
            GroupHeader(text = stringResource2(id = R.string.pref_header_appearance))
            Appearance(state = state)

            // General
            GroupHeader(text = stringResource2(id = R.string.pref_header_general))
            General(state = state)

            // Feedback
            GroupHeader(text = stringResource2(id = R.string.pref_header_feedback))
            Feedback()

            // About Us
            GroupHeader(text = stringResource2(id = R.string.pref_header_about_us))
            AboutUs()
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private inline fun Expanded(
    state: Settings,
    modifier: Modifier = Modifier
) {
    Medium(state = state, modifier)
}


@Composable
@NonRestartableComposable
fun Settings(state: Settings) {
    when (LocalWindowSizeClass.current.widthSizeClass) {
        WindowWidthSizeClass.Compact -> Compact(state = state)
        WindowWidthSizeClass.Medium -> Medium(state = state)
        WindowWidthSizeClass.Expanded -> Expanded(state = state)
    }
}