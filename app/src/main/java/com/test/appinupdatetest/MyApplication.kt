package com.test.appinupdatetest

import android.app.Application

/**
 * Author: Lenovo
 * Date: 2022/11/23 11:53
 * Desc:
 */
class MyApplication : Application() {

    companion object {
        @JvmStatic
        lateinit var INSTANCE: Application
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
    }
}
