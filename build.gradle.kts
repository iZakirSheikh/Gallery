// Top-level build file where you can add configuration options common to all sub-projects/modules.
// TODO: Remove once KTIJ-19369 is fixed
@Suppress("DSL_SCOPE_VIOLATION") plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.firebase) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.crashanlytics) apply false
}
// Needed to make the Suppress annotation work for the plugins block
true