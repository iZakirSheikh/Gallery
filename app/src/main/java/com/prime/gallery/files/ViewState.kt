package com.prime.gallery.files

import com.google.android.datatransport.runtime.scheduling.jobscheduling.SchedulerConfig.Flag


interface Files {

    companion object {
        val route: String = "route_photos"

        private const val EXTRA_FLAG = "key_category"
        private const val FLAG_FOLDER = "flag_folder"
        private const val FLAG_TIMELINE = "flag_timeline"
        private const val EXTRA_URI = "_uri"

        fun direction(flag: String = FLAG_TIMELINE, uri: String? = null) = route

        private const val FLAG_IS_TIME_LINE  = "_flag_time_line"

    }
}