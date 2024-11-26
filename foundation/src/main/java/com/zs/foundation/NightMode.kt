package com.zs.foundation

/**
 * Enum class representing different night modes.
 *
 * Night mode determines how the app's UI should appear during night time.
 * The mode can be set using AppCompatDelegate.setLocalNightMode(int).
 */
enum class NightMode {
    /**
     * Night mode which uses always uses a light mode, enabling {@code notnight} qualified
     * resources regardless of the time.
     *
     * @see #setLocalNightMode(int)
     */
    YES,

    /**
     * Night mode which uses always uses a dark mode, enabling {@code night} qualified
     * resources regardless of the time.
     *
     * @see #setLocalNightMode(int)
     */
    NO,

    /**
     * Mode which uses the system's night mode setting to determine if it is night or not.
     *
     * @see #setLocalNightMode(int)
     */
    FOLLOW_SYSTEM,
}