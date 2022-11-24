package com.test.appinupdatetest.update

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

/**
 * Author: Lenovo
 * Date: 2022/11/23 15:58
 * Desc:
 */

interface IAppUpgrade {
    companion object {
        const val TAG = "AppUpdateImpl"

        // 应用内更新，请求状态码
        const val UPDATE_REQUEST_CODE = 10

        const val KEY_VERSION_CODE = "forceUpdateVersionCode"
    }

    /**
     * 检查更新，并执行更新
     */
    fun checkUpgrade(activity: AppCompatActivity?)

    /**
     * 回传结果
     */
    fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
    }
}
