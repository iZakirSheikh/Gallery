/*
 * Copyright (c)  2025 Zakir Sheikh
 *
 * Created by sheik on 26 of Dec 2025
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
 * Last Modified by sheik on 26 of Dec 2025
 */

package com.zs.gallery.common

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.ProduceStateScope
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import com.zs.compose.foundation.findActivity
import com.zs.gallery.MainActivity
import com.zs.preferences.Key
import com.zs.preferences.Key.Key1
import com.zs.preferences.Key.Key2
import com.zs.preferences.Preferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.CoroutineContext

/**
 * Defines the strategies for extracting a source color accent
 * to construct the application theme.
 */
enum class ColorPalette { MANUAL, DEFAULT, WALLPAPER }

/**
 * Represents the available options for applying dark theme
 * behavior within the application.
 */
enum class NightMode { YES, NO, FOLLOW_SYSTEM }

private const val ELLIPSIS_NORMAL = "\u2026"; // HORIZONTAL ELLIPSIS (…)

/**
 * Ellipsizes this CharSequence, adding a horizontal ellipsis (…) if it is longer than [after] characters.
 *
 * @param after The maximum length of the CharSequence before it is ellipsized.
 * @return The ellipsized CharSequence.
 */
fun CharSequence.ellipsize(after: Int): CharSequence =
    if (this.length > after) this.substring(0, after) + ELLIPSIS_NORMAL else this

private fun noLocalProvidedFor(name: String): Nothing {
    error("CompositionLocal $name not present")
}

/** Provides a [Navigator] that can be accessed within a Compose hierarchy. */
val LocalNavigator =
    staticCompositionLocalOf<Navigator<Route>> {
        noLocalProvidedFor("LocalNavigator")
    }

/**
 * Provides a [SystemFacade] for child views within a Compose hierarchy.
 *
 * The facade exposes system‑level behaviors (e.g. window, lifecycle, or input modes)
 * that can be implemented by a parent activity hosting a single view with children.
 * Child views can query this local to adapt their behavior based on the current mode.
 *
 * Throws an error if no [SystemFacade] is provided.
 */
val LocalSystemFacade = staticCompositionLocalOf<SystemFacade> {
    noLocalProvidedFor("LocalSystemFacade")
}

private class ProduceStateScopeImpl<T>(
    state: MutableState<T>,
    override val coroutineContext: CoroutineContext,
) : ProduceStateScope<T>, MutableState<T> by state {

    override suspend fun awaitDispose(onDispose: () -> Unit): Nothing {
        try {
            suspendCancellableCoroutine<Nothing> {}
        } finally {
            onDispose()
        }
    }
}

/**
 * Observes a [Preferences] key as a Compose [State].
 *
 * Bridges a Flow-based preference observation into Compose state,
 * allowing UI to reactively update when the preference changes.
 *
 * Behavior:
 * - Resolves the initial value synchronously using [runBlocking] and [Flow.first],
 *   ensuring the UI does not flicker with a null initial state.
 * - Collects subsequent values in a coroutine tied to the composition lifecycle,
 *   updating the returned [State] whenever the preference changes.
 *
 * @param key The preference key to observe. Supports [Key1] and [Key2].
 * @return A Compose [State] wrapping the observed preference value.
 */
@Composable
private inline fun <S, O> Preferences.observeAsState(key: Key<S, O>): State<O?> {
    // TODO - Maybe flow needs to be remembered.
    // Resolve the flow based on the key type.
    val flow = when (key) {
        is Key1 -> observe(key)
        is Key2 -> observe(key)
    }

    // TODO - ob
    // why runBlocking is required, because we don't want everytime initial value to be null that causes flickering.
    // it is guaranteed that this will return with value default or null
    // Initialize state with the first value synchronously.
    val result = remember {
        mutableStateOf(runBlocking { flow.first() })
    }

    // Launch a coroutine tied to the composition lifecycle.
    // Collects updates from the flow and updates the state accordingly.
    LaunchedEffect(Unit) {
        flow.collect {
            result.value = it
        }
    }

    // Return the Compose State that reflects the preference value.
    return result
}

/**
 * Returns a Compose [State] for the given preference [key].
 *
 * Resolves the current [MainActivity] from [LocalContext] and delegates
 * to [Preferences.observeAsState] for reactive observation.
 */
@Composable
@NonRestartableComposable
fun <S, O> preference(key: Key.Key1<S, O>): State<O?> {
    //TODO - find better way
    val activity = LocalContext.current.findActivity() as MainActivity
    return activity.preferences.observeAsState(key)
}

/**
 * @see preference
 */
@Composable
@NonRestartableComposable
fun <S, O> preference(key: Key.Key2<S, O>): State<O> {
    //TODO - find better way
    val activity = LocalContext.current.findActivity() as MainActivity
    return activity.preferences.observeAsState(key) as State<O>
}

@Composable
inline fun imageVectorOf(@DrawableRes id: Int) = ImageVector.vectorResource(id)