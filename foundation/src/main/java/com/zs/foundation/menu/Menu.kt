package com.zs.foundation.menu

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Icon
import androidx.compose.material.IconToggleButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.primex.material2.DropDownMenuItem
import com.primex.material2.IconButton
import com.primex.material2.menu.DropDownMenu2


/**
 * A composable function that displays a row of menu items followed by a more button.
 *
 * @param items The list of menu items to display.
 * @param onItemClicked Callback invoked when a menu item is clicked.
 * @param moreIcon The icon to use for the "more" button when the menu is collapsed.
 * @param collapsed The number of items in collapsed state in this state only icon is displayed.
 */
@Composable
inline fun RowScope.Menu(
    items: List<Action>,
    noinline onItemClicked: (item: Action) -> Unit,
    moreIcon: ImageVector = Icons.Outlined.MoreVert,
    collapsed: Int = 4
) {
    val size = items.size
    // Early return if this has no items
    if (size == 0)
        return
    // Display the first 'collapsed' number of items as IconButtons
    repeat(minOf(size, collapsed)) { index ->
        val item = items[index]
        IconButton(
            imageVector = requireNotNull(item.icon) {
                "Collapsed Icon must not be null"
            }, // Icon is required for collapsed items
            onClick = { onItemClicked(item) },
            contentDescription = stringResource(item.label),
            enabled = item.enabled
        )
    }

    // If all items are already displayed, return
    if (size < collapsed)
        return

    // State to control the expanded state of the dropdown menu
    val (expanded, onDismissRequest) = remember { mutableStateOf(false) }

    // IconToggleButton to show/hide the dropdown menu
    IconToggleButton(expanded, onDismissRequest) {
        Icon(moreIcon, contentDescription = "more") // Icon for the "more" button

        // DropdownMenu to display the remaining items
        DropDownMenu2(expanded, onDismissRequest = { onDismissRequest(false) }, modifier = Modifier.widthIn(min = 180.dp)) {
            repeat(size - collapsed) { index ->
                val item = items[index + collapsed]
                DropDownMenuItem(
                    title = stringResource(item.label),
                    onClick = { onItemClicked(item); onDismissRequest(false) },
                    icon = item.icon?.let { rememberVectorPainter(it) }, // Icon is optional for dropdown items
                )
            }
        }
    }
}

