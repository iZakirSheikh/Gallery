package com.prime.gallery.core.compose

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.prime.gallery.Material
import com.prime.gallery.R
import com.prime.gallery.core.ContentPadding
import com.primex.core.drawHorizontalDivider
import com.primex.material3.Label

private const val TAG = "LazyDsl"

private val fullLineSpan: (LazyGridItemSpanScope.() -> GridItemSpan) = { GridItemSpan(maxLineSpan) }

/**
 * Item header.
 * //TODO: Handle padding in parent composable.
 */
@Composable
private fun Header(
    value: CharSequence,
    modifier: Modifier = Modifier
) {
    if (value.isBlank())
        return Spacer(modifier = modifier)

    val color = LocalContentColor.current
    Crossfade(
        targetState = value.length == 1,
        modifier = Modifier
            .padding(bottom = ContentPadding.normal)
            .drawHorizontalDivider(color = color)
            .fillMaxWidth()
            .then(modifier),
        label = "value"
    ) { single ->
        when (single) {
            // draw a single char/line header
            // in case the length of the title string is 1
            true -> Label(
                text = value,
                style = Material.typography.displayLarge,
                fontWeight = FontWeight.Normal,
                color = color,
                modifier = Modifier
                    .padding(top = ContentPadding.normal)
                    .padding(horizontal = ContentPadding.xLarge),
            )
            // draw a multiline line header
            // in case the length of the title string is 1
            else -> Label(
                text = value,
                color = color,
                maxLines = 2,
                fontWeight = FontWeight.Normal,
                style = Material.typography.headlineSmall,
                modifier = Modifier
                    // don't fill whole line.
                    .fillMaxWidth(0.7f)
                    .padding(top = ContentPadding.xLarge, bottom = ContentPadding.medium)
                    .padding(horizontal = ContentPadding.normal)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun <T> LazyGridScope.items(
    values: Map<String, List<T>>?,
    key: ((item: T) -> Any)? = null,
    itemContent: @Composable LazyGridItemScope.(item: T) -> Unit
) {
    // show placeholders as required.

    if (values == null)
        return item(span = fullLineSpan) {
            Placeholder(
                title = stringResource(R.string.loading),
                iconResId = R.raw.lt_loading_dots_blue,
                modifier = Modifier.fillMaxSize()
            )
        }

    if (values.isEmpty())
        return item(span = fullLineSpan) {
            Placeholder(
                title = "Oops Empty!!",
                iconResId = R.raw.lt_empty_box,
                modifier = Modifier.fillMaxSize()
            )
        }

    // actual list
    values.forEach { (header, list) ->
        //emit  list header
        item(
            key = header,
            contentType = "CONTENT_TYPE_HEADER",
            span = fullLineSpan,
            content = {
                Header(
                    value = header,
                    modifier = Modifier.animateItemPlacement()
                )
            }
        )

        // emit list of items.
        items(
            list,
            key = key,
            contentType = { "CONTENT_TYPE_LIST_ITEM" },
            itemContent = itemContent
        )
    }
}

