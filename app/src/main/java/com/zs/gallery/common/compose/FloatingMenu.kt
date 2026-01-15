/*
 * Copyright (c)  2026 Zakir Sheikh
 *
 * Created by sheik on 8 of Jan 2026
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
 * Last Modified by sheik on 8 of Jan 2026
 *
 */

package com.zs.gallery.common.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import com.zs.compose.foundation.Background
import com.zs.compose.foundation.textResource
import com.zs.compose.foundation.thenIf
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.HorizontalDivider
import com.zs.compose.theme.Icon
import com.zs.compose.theme.IconButton
import com.zs.compose.theme.IconToggleButton
import com.zs.compose.theme.Surface
import com.zs.compose.theme.menu.DropDownMenu
import com.zs.compose.theme.menu.DropDownMenuItem
import com.zs.gallery.common.Res
import com.zs.gallery.common.Res.action
import com.zs.gallery.common.vectorResource

/**
 * A composable that renders a single action item.
 *
 * Depending on [compact], the action is displayed either as an icon-only button
 * or (in the future) with both icon and label.
 *
 * @param action The action definition containing icon and optional label resource.
 * @param onClick Callback invoked when the action is triggered.
 * @param compact If true, show only the icon; otherwise show icon + label (not yet implemented).
 * @param modifier Modifier for styling and layout adjustments.
 */
@NonRestartableComposable
@Composable
private fun Action(
    action: action,
    onClick: () -> Unit,
    compact: Boolean = true,
    modifier: Modifier = Modifier
) {
    // Resolve label text from resource ID; fallback to empty string if not defined
    val label = if (action.label == ResourcesCompat.ID_NULL) "" else textResource(action.label)
    when {
        compact -> IconButton(
            icon = vectorResource(action.icon), // Fallback for missing icons
            onClick = onClick,
            contentDescription = label.toString(),
        )

        else -> TODO("Action with Label not implemented yet!")  // Placeholder for future implementation of icon + label variant
    }
}

/**
 * Displays a horizontal row of quick actions with an overflow menu.
 *
 * @param actions Full list of available actions.
 * @param onMenuAction Callback invoked when an action is selected.
 * @param collapsed Number of actions shown directly in the row.
 * @param overflow Controls distribution of remaining actions in the overflow menu:
 *  - `Int.MIN_VALUE`: All remaining actions shown as menu entries.
 *  - `Positive`: That many actions shown as top menu entries; rest as bottom-row icons.
 *  - `Negative`: Absolute value shown as top-row icons; rest as menu entries below.
 * @param compact If true, show only icons; otherwise show icon and label stacked vertically.
 */
@Composable
@NonRestartableComposable
context(_: RowScope)
fun OverflowMenu(
    actions: List<action>,
    onMenuAction: (action: action) -> Unit,
    collapsed: Int = 3,
    overflow: Int = Int.MIN_VALUE,
    compact: Boolean = true,
) {
    val size = actions.size
    if (size == 0) return // Nothing to render if no actions exist

    // Show the first `collapsed` actions directly in the row as icon buttons
    repeat(minOf(size, collapsed)) { index ->
        val action = actions[index]
        Action(action, onClick = { onMenuAction(action) })
    }

    // If all actions are already shown in the row, skip rendering the overflow menu
    if (size <= collapsed) return

    // State holder for dropdown visibility (true = expanded, false = collapsed)
    val (show, onDismissRequest) = remember { mutableStateOf(false) }

    // "More" button to toggle dropdown visibility
    IconToggleButton(checked = show, onCheckedChange = { onDismissRequest(it) }) {
        // Icon for the overflow menu button (three dots)
        Icon(
            vectorResource(Res.drawable.ic_more_vert_filled),
            contentDescription = "More actions"
        )
        /// Dropdown menu showing remaining actions beyond the collapsed count
        DropDownMenu(
            expanded = show,
            onDismissRequest = { onDismissRequest(false) },
            modifier = Modifier.widthIn(min = 180.dp),
            content = {
                val remaining = size - collapsed
                when {

                    // Case 1: overflow == Int.MIN_VALUE → show all remaining items as dropdown entries
                    overflow == Int.MIN_VALUE -> {
                        repeat(remaining) { index ->
                            val item = actions[collapsed + index]
                            DropDownMenuItem(
                                title = stringResource(item.label),
                                onClick = {
                                    onMenuAction(item)
                                    onDismissRequest(false) // close menu after click
                                },
                                icon = vectorResource(item.icon)
                            )
                        }
                    }

                    // Case 2: overflow > 0 → show `overflow` items in dropdown, rest as row icons at bottom
                    overflow > 0 -> {
                        val dropdownCount = minOf(remaining, overflow)
                        val overflowCount = remaining - dropdownCount

                        // Top section: dropdown menu items
                        repeat(dropdownCount) { index ->
                            val item = actions[collapsed + index]
                            DropDownMenuItem(
                                title = stringResource(item.label),
                                onClick = {
                                    onMenuAction(item)
                                    onDismissRequest(false)
                                },
                                icon = vectorResource(item.icon)
                            )
                        }

                        // Divider between dropdown items and bottom row icons
                        if (overflowCount > 0) HorizontalDivider() else return@DropDownMenu

                        // Bottom section: row of compact icon buttons inside dropdown
                        Row {
                            repeat(overflowCount) { index ->
                                val item = actions[collapsed + dropdownCount + index]
                                Action(
                                    action = item,
                                    onClick = { onMenuAction(item); onDismissRequest(false) },
                                    compact = true
                                )
                            }
                        }
                    }

                    // Case 3: overflow < 0 → show abs(overflow) items as row icons first, rest as dropdown entries
                    else -> {
                        val dropdownCount = minOf(remaining, -overflow)
                        val overflowCount = remaining - dropdownCount

                        // Top section: row of icon buttons inside dropdown
                        if (overflowCount > 0) {
                            Row {
                                repeat(overflowCount) { index ->
                                    val item = actions[collapsed + dropdownCount + index]
                                    Action(
                                        action = item,
                                        onClick = { onMenuAction(item); onDismissRequest(false) },
                                        compact = true
                                    )
                                }
                            }
                            HorizontalDivider()
                        }

                        // Bottom section: dropdown menu items
                        repeat(dropdownCount) { index ->
                            val item = actions[collapsed + index]
                            DropDownMenuItem(
                                title = stringResource(item.label),
                                icon = vectorResource(item.icon),
                                onClick = {
                                    onMenuAction(item)
                                    onDismissRequest(false)
                                }
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
@NonRestartableComposable
fun FloatingActionMenu(
    visible: Boolean,
    background: Background,
    modifier: Modifier = Modifier,
    contentColor: Color = AppTheme.colors.onBackground,
    insets: PaddingValues? = null,
    border: BorderStroke? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val motion = AppTheme.motionScheme
    AnimatedVisibility(
        visible = visible,
        modifier = modifier.thenIf(insets != null) { padding(insets!!) },
        enter = fadeIn(motion.fastEffectsSpec()) + slideInVertically(motion.fastSpatialSpec()),
        exit = fadeOut(motion.fastEffectsSpec()) + slideOutVertically(motion.fastSpatialSpec()),
        content = {
            Surface(
                background = background,
                contentColor = contentColor,
                elevation = 12.dp,
                shape = AppTheme.shapes.medium,
                border = border,
                content = {
                    Row(
                        horizontalArrangement = Res.dimen.spacing_x_small,
                        verticalAlignment = Alignment.CenterVertically,
                        content = content,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp).animateContentSize(motion.fastSpatialSpec())
                    )
                }
            )
        }
    )
}