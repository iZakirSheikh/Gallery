package com.zs.gallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.lifecycle.lifecycleScope
import com.zs.common.analytics.Analytics
import com.zs.common.billing.Paymaster
import com.zs.common.billing.Product
import com.zs.common.db.SyncWorker
import com.zs.gallery.common.Navigator
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private val TAG = "MainActivity"

    lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SyncWorker.execute(applicationContext)
        val color = Color.Black
        //val master = Paymaster(this, emptyArray())
        val analytics = Analytics.getInstance()
        analytics.logEvent("test", Bundle.EMPTY)
        color.isSpecified
        lifecycleScope.launch {
        }
    }
}