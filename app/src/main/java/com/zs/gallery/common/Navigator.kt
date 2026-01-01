package com.zs.gallery.common

import androidx.compose.runtime.mutableStateListOf

/**
 * A lightweight navigation stack manager.
 *
 * - Maintains a backstack of Routes starting from a root `start`.
 * - Supports navigation patterns like singleTop, popChildren, trimTo, and rebase.
 * - Guarantees that the root/start route is always preserved.
 */
class Navigator<T>(start: T) {

    /**
     * The root route of this navigator.
     * Always preserved as the base of the backstack.
     */
    var start: T = start
        private set

    /**
     * Internal backstack of routes.
     * Backed by Compose's [androidx.compose.runtime.mutableStateListOf] so UI can react to changes.
     */
    private val _backstack = mutableStateListOf(start)
    val backstack: List<T> get() = _backstack

    val active get() = _backstack.last()

    /**
     * Pop the backstack until [route] is at the top.
     *
     * @param route The target route to pop back to.
     * @param inclusive If true, also removes the target route itself.
     * @return false if the route is not found, true otherwise.
     *
     * Special case: if [route] == [start], the stack is reset to root.
     */
    fun trimTo(route: T, inclusive: Boolean = false): Boolean {
        val index = _backstack.indexOf(route)
        if (index == -1) return false

        // Special case: root must always remain.
        if (route == start) {
            _backstack.clear()
            _backstack.add(start)
            return true
        }

        // If inclusive, cut at the route itself; otherwise cut above it.
        val cutIndex = if (inclusive) index else index + 1
        if (cutIndex < _backstack.size) {
            _backstack.subList(cutIndex, _backstack.size).clear()
            return true
        }

        return false
    }

    /**
     * Reset the navigator to a new root route.
     *
     * Clears the backstack and sets [newStart] as the only route.
     */
    fun rebase(newStart: T) {
        start = newStart
        _backstack.clear()
        _backstack.add(newStart)
    }

    /**
     * Peek at the route just below the current top.
     *
     * @return The previous route, or null if only root exists.
     */
    fun peek(): T? =
        if (_backstack.size > 1) _backstack[_backstack.lastIndex - 1] else null

    /**
     * Attempts to navigate up in the navigation hierarchy.
     *
     * @return false if only root remains, true if a route was popped.
     */
    fun navigateUp(): Boolean {
        // Root must never be removed.
        if (_backstack.size <= 1) return false
        _backstack.removeLast()
        return true
    }

    /**
     * Navigate to a new [route].
     *
     * @param singleTop If true, ensures only one instance of the route exists.
     *                  Removes any existing instance before adding fresh.
     * @param collapseTo If true, clears everything above the existing route
     *                    (if found), effectively "bringing it to front".
     * @return false if the route is already at top, true otherwise.
     *
     * Special case: navigating to [start] resets the stack to root.
     */
    fun navigate(
        route: T,
        singleTop: Boolean = true,
        collapseTo: Boolean = false
    ): Boolean {
        val currentTop = _backstack.last()

        // If already at top, no-op.
        if (currentTop == route) return false

        // Special case: navigating to root resets stack.
        if (route == start) {
            _backstack.clear()
            _backstack.add(start)
            return true
        }

        val index = _backstack.indexOf(route)
        return when {
            // Pop everything above the existing route.
            collapseTo && index != -1 -> {
                _backstack.subList(index + 1, _backstack.size).clear()
                true
            }
            // SingleTop: remove old instance, add fresh one.
            singleTop -> {
                if (index != -1) _backstack.removeAt(index)
                _backstack.add(route)
                true
            }
            // Default: always push new instance (duplicates allowed).
            else -> {
                _backstack.add(route)
                true
            }
        }
    }


    fun popBackStack(): Boolean = navigateUp()
}