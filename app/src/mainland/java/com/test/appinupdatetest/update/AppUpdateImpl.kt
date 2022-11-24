package com.test.appinupdatetest.update

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.test.appinupdatetest.update.IAppUpgrade.Companion.TAG

/**
 * Author: Lenovo
 * Date: 2022/11/23 11:51
 * Desc: Google play 应用内更新
 */
class AppUpdateImpl : IAppUpgrade, LifecycleEventObserver {

    private var activity: AppCompatActivity? = null

    /**
     * 检查更新
     */
    override fun checkUpgrade(activity: AppCompatActivity?) {
        Log.i(TAG, "checkUpgrade: 国内市场，App更新逻辑")
    }

    private fun onResume(event: Event) {
        Log.i(TAG, "onResume: event=$event")
    }

    private fun onDestroy(event: Event) {
        Log.i(TAG, "onDestroy: event=$event")
    }

    override fun onStateChanged(
        source: LifecycleOwner,
        event: Event
    ) {
        when (event) {
            Event.ON_RESUME -> onResume(event)
            Event.ON_DESTROY -> onDestroy(event)
            else -> {
            }
        }
    }
}
