package com.zs.foundation.menu

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents a single item within a menu.
 */
interface MenuItem {

    /**
     * Unique identifier for the menu item.
     */
    val id: String

    /**
     * The text label displayed for the menu item.
     * Can include a subtitle on a new line, up to two lines maximum.
     */
    @get:StringRes
    val label: Int

    /**
     * Optional icon associated with the menu item.
     */
    val icon: ImageVector?

    /**
     * Indicates whether the menu item is currently enabled and interactive.
     */
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
        @StringRes label: Int = this.label,
        icon: ImageVector? = this.icon,
    ): MenuItem = MenuItemImpl(id, label, icon, enabled)


    companion object {

        /**
         * Creates a [MenuItem] instance.
         *
         * @param id Unique identifier for the menu item.
         * @param label The text label displayed for the menu item.
         * Can include a subtitle on a new line, up to two lines maximum.
         * @param icon Optional icon associated with the menu item.
         * @param enabled Indicates whether the menu item is currently enabled.
         */
        operator fun invoke(
            id: String,
            @StringRes label: Int,
            icon: ImageVector? = null,
            enabled: Boolean = true
        ): MenuItem = MenuItemImpl(id, label, icon, enabled)
    }
}


/**
 * Default implementation of [MenuItem].
 */
private class MenuItemImpl(
    override val id: String,
    override val label: Int ,
    override val icon: ImageVector?,
    override val enabled: Boolean
) : MenuItem {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MenuItemImpl

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
