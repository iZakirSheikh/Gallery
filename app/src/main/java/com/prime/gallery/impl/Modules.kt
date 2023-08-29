package com.prime.gallery.impl

import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import com.prime.gallery.core.compose.snackbar.SnackbarHostState2
import com.primex.preferences.Preferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SingletonModule {
    /**
     * Provides the Singleton Implementation of Preferences DataStore.
     */
    @Provides
    @Singleton
    fun preferences(@ApplicationContext context: Context) =
        Preferences(context, "Shared_Preferences")

    @Singleton
    @Provides
    fun resolver(@ApplicationContext context: Context): ContentResolver =
        context.contentResolver

    @Singleton
    @Provides
    fun resources(@ApplicationContext context: Context): Resources =
        context.resources
}

@Module
@InstallIn(ActivityRetainedComponent::class)
object ActivityModule {
    @ActivityRetainedScoped
    @Provides
    fun channel() = SnackbarHostState2()

    @ActivityRetainedScoped
    @Provides
    fun systemDelegate(@ApplicationContext ctx: Context, channel: SnackbarHostState2): SystemDelegate =
        SystemDelegate(ctx, channel)
}