package com.zs.gallery.common.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zs.compose.theme.appbar.AppBarDefaults
import com.zs.gallery.common.Res

@Composable
fun NavigationBar(
    content: @Composable RowScope.() -> Unit,
    handle: BackdropHandle,
    modifier: Modifier = Modifier,
    insets: WindowInsets = AppBarDefaults.bottomAppBarWindowInsets,
    elevation: Dp = 0.dp,
    shape: Shape = Res.shape.rectangle
) {
    Box(contentAlignment = Alignment.CenterEnd, modifier = modifier) {
        Row(
            modifier = modifier
                .windowInsetsPadding(insets)
                .padding(horizontal = Res.dimen.normal)
                //.acrylic(elevation= elevation, shape = shape,AppTheme.colors.background, AppTheme.colors.accent)
                .backropEffect(shape, elevation, handle = handle)
                .padding(horizontal = Res.dimen.normal, vertical = Res.dimen.small)
                .height(56.dp),
            content = content
        )
    }
}