package com.zs.foundation.menu

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents a single item within a menu. The [label] represents the id of this item as well.
 *
 * @property label The text label displayed for the menu item.
 *                  Can include a subtitle on a new line, up to two lines maximum.
 * @property icon Optional icon associated with the menu item.
 * @property enabled Indicates whether the menu item is currently enabled and interactive.
 */
interface Action {
    @get:StringRes val label: Int
    val id: String
    val icon: ImageVector?
    val enabled: Boolean

    /**
     * Creates a copy of this [MenuItem] with the specified modifications.
     *
     * @param title The text label for the new menu item.
     * @param icon The icon for the new menu item.
     * @param enabled Whether the new menu item is enabled.
     * @return A new [MenuItem] instance with the applied modifications.
     */
    fun copy(
        enabled: Boolean = this.enabled,
    ): Action = ActionImpl(label, id, icon, enabled)

    companion object {

        /**
         * @see MenuItem
         */
        operator fun invoke(
            @StringRes label: Int,
            icon: ImageVector? = null,
            enabled: Boolean = true,
            id: String = label.toString()
        ): Action = ActionImpl(label, id, icon, enabled)
    }
}

/**
 * Default implementation of [MenuItem].
 */
private class ActionImpl(
    override val label: Int ,
    override val id: String,
    override val icon: ImageVector?,
    override val enabled: Boolean
) : Action {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ActionImpl

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "Action(Label = $label, id = $id, icon = $icon, enabled = $enabled)"
    }
}