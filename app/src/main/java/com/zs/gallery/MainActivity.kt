package com.zs.gallery

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.lifecycleScope
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
        val xyz = mutableIntStateOf()
        lifecycleScope.launch {
            val x = Product(null)
            Log.d(TAG, "onCreate: ${x.isSpecified}")
        }
    }
}