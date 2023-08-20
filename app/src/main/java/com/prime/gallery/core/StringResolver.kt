package com.prime.gallery.core

import android.content.res.Resources
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.ui.text.ExperimentalTextApi
import com.primex.core.getQuantityText2
import com.primex.core.getText2

/**
 * An interface that provides methods to resolve strings from resources.
 */
interface StringResolver {
    /**
     * @see getText2
     */
    fun getText(@StringRes id: Int): CharSequence

    /**
     * @see getText2
     */
    fun getText(@StringRes id: Int, vararg args: Any): CharSequence

    /**
     * @see getQuantityText2
     */
    fun getQuantityText(@PluralsRes id: Int, quantity: Int, vararg args: Any): CharSequence

    /**
     * @see getQuantityText2
     */
    fun getQuantityText(@PluralsRes id: Int, quantity: Int): CharSequence
}

@OptIn(ExperimentalTextApi::class)
fun StringResolver(resources: Resources) =
    object : StringResolver {
        override fun getText(id: Int): CharSequence = resources.getText2(id)

        override fun getText(id: Int, vararg args: Any) =
            resources.getText2(id, *args)

        override fun getQuantityText(id: Int, quantity: Int, vararg args: Any) =
            resources.getQuantityText2(id, quantity, *args)

        override fun getQuantityText(id: Int, quantity: Int) =
            resources.getQuantityText2(id, quantity)
    }