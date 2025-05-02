/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 17-07-2024.
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

import android.app.Application
import android.content.res.Resources
import android.text.format.Formatter
import android.util.Log
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.core.content.res.ResourcesCompat
import com.zs.compose.foundation.OrientRed
import com.zs.compose.foundation.getText2
import com.zs.compose.theme.snackbar.SnackbarDuration
import com.zs.compose.theme.snackbar.SnackbarHostState
import com.zs.compose.theme.snackbar.SnackbarResult
import com.zs.core.common.showPlatformToast
import com.zs.gallery.R
import com.zs.preferences.Preferences
import org.koin.androidx.scope.ScopeViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.component.inject

private const val TAG = "KoinViewModel"

@OptIn(KoinExperimentalAPI::class)
abstract class KoinViewModel : ScopeViewModel() {
    private val resources: Resources by inject()
    private val toastHostState: SnackbarHostState by inject()
    val preferences: Preferences by inject()
    private val context: Application by inject()

    fun ImageVector(@DrawableRes id: Int): ImageVector =
        ImageVector.vectorResource(context.theme, res = resources, id)

    fun showPlatformToast(
        @StringRes message: Int,
        length: Int = Toast.LENGTH_SHORT,
    ) = context.showPlatformToast(message, length)

    fun showPlatformToast(
        message: String,
        length: Int = Toast.LENGTH_SHORT,
    ) = context.showPlatformToast(message, length)

    suspend fun showSnackbar(
        message: CharSequence,
        action: CharSequence? = null,
        icon: ImageVector? = null,
        accent: Color = Color.Unspecified,
        duration: SnackbarDuration = if (action == null) SnackbarDuration.Short else SnackbarDuration.Long,
    ): SnackbarResult = toastHostState.showSnackbar(message, action, icon, accent, duration)

    suspend fun showSnackbar(
        @StringRes message: Int,
        @StringRes action: Int = ResourcesCompat.ID_NULL,
        icon: ImageVector? = null,
        accent: Color = Color.Unspecified,
        duration: SnackbarDuration = if (action == ResourcesCompat.ID_NULL) SnackbarDuration.Short else SnackbarDuration.Long,
    ): SnackbarResult = showSnackbar(
        message = resources.getText2(message),
        action = if (action == ResourcesCompat.ID_NULL) null else resources.getText2(action),
        icon = icon,
        accent = accent,
        duration = duration
    )

    fun getText(@StringRes id: Int): CharSequence = resources.getText2(id)
    fun getText(@StringRes id: Int, vararg args: Any) = resources.getText2(id, *args)
    fun formatFileSize(sizeBytes: Long): String = Formatter.formatFileSize(context, sizeBytes)

    /**
     * Reports an error message to the user.
     */
    suspend fun report(message: CharSequence) = showSnackbar(
        message = buildAnnotatedString {
            appendLine(getText(R.string.error))
            withStyle(SpanStyle(color = Color.Gray)) {
                append(message)
            }
        },
        action = getText(R.string.report),
        icon = Icons.Outlined.ErrorOutline,
        accent = Color.OrientRed,
        duration = SnackbarDuration.Indefinite
    )

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared: ${this::class.simpleName}")
    }
}