package com.zs.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import com.zs.gallery.common.NavController
import com.zs.compose.theme.snackbar.SnackbarHostState as SnackbarController


@Composable
@NonRestartableComposable
context(activity: MainActivity)
fun Gallery(
    navController: NavController,
    controller: SnackbarController
) {

}