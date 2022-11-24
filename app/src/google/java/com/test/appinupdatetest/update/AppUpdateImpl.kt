package com.test.appinupdatetest.update

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.test.appinupdatetest.MyApplication
import com.test.appinupdatetest.update.IAppUpgrade.Companion.TAG
import com.test.appinupdatetest.update.IAppUpgrade.Companion.UPDATE_REQUEST_CODE

/**
 * Author: Lenovo
 * Date: 2022/11/23 11:51
 * Desc: Google play 应用内更新
 */
class AppUpdateImpl : IAppUpgrade, LifecycleEventObserver, InstallStateUpdatedListener {

    private val appUpdateManager: AppUpdateManager by lazy {
        AppUpdateManagerFactory.create(MyApplication.INSTANCE).apply {
            // 注册更新监听事件
            registerListener(this@AppUpdateImpl)
        }
    }

    private var forceUpdate: Boolean = false
    private var currUpdateType: Int = -1

    private var activity: AppCompatActivity? = null

    /**
     * 检查更新
     */
    override fun checkUpgrade(activity: AppCompatActivity?) {
        this.activity = activity
        if (activity == null) {
            Log.i(TAG, "checkUpdate: activity is null")
            return
        }
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            // 有更新
            val updateAvailability = appUpdateInfo.updateAvailability()
            Log.i(TAG, "checkUpgrade: updateAvailability=$updateAvailability")
            Log.i(TAG, "checkUpgrade: 是否有更新=${updateAvailability == UpdateAvailability.UPDATE_AVAILABLE}")
            // 应用返回到前台时，您应确认更新未在 UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS 状态下停止。如果更新在此状态下停止，请继续更新：
            if (updateAvailability == UpdateAvailability.UPDATE_AVAILABLE || updateAvailability == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                if (appUpdateInfo.updatePriority() >= 4) { // 更新优先级 4 和 5 ，则使用强制更新
                    forceUpdate = true // 需要强制更新
                    startUpdate(appUpdateInfo, AppUpdateType.IMMEDIATE, activity)
                } else {
                    // 灵活更新
                    startUpdate(appUpdateInfo, AppUpdateType.FLEXIBLE, activity)
                }
            }
        }
    }

    private fun startUpdate(
        appUpdateInfo: AppUpdateInfo,
        appUpdateType: Int,
        activity: Activity
    ) {
        Log.i(TAG, "startUpdate: appUpdateType=$appUpdateType")
        if (appUpdateInfo.isUpdateTypeAllowed(appUpdateType)) {
            Log.i(TAG, "startUpdate: 启动更新，开始下载")
            currUpdateType = appUpdateType
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                appUpdateType,
                activity,
                UPDATE_REQUEST_CODE
            )
        } else {
            Log.i(TAG, "startUpdate: 启动更新失败，appUpdateType=$appUpdateType")
        }
    }

    /**
     * 更新结果，会回调该方法
     */
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (requestCode == UPDATE_REQUEST_CODE) {
            // 如果强制更新被取消，则再次请求更新，即要求用户必须更新，否则无法使用app
            if (resultCode != RESULT_OK && currUpdateType == AppUpdateType.IMMEDIATE) {
                Log.e(TAG, "Update flow failed! Result code: $resultCode")
                // If the update is cancelled or fails,
                // you can request to start the update again.
                currUpdateType = -1
                checkUpgrade(activity)
            }
        }
    }

    private fun onResume(event: Event) {
        Log.i(TAG, "onResume: event=$event")
        checkUpdateResult()
    }

    /**
     * 检查更新的结果
     */
    private fun checkUpdateResult() {
        if (currUpdateType == -1) {
            Log.i(TAG, "checkUpdateResult: 没有执行更新操作，不用检查结果")
            return
        }
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            when (currUpdateType) {
                AppUpdateType.FLEXIBLE -> {
                    // 每当用户将您的应用调入前台时，请检查您的应用是否有等待安装的更新。如果您的应用有处于 DOWNLOADED 状态的更新，
                    // 请提示用户安装该更新。否则，更新数据会继续占用用户的设备存储空间。
                    if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                        currUpdateType = -1
                        popupSnackbarForCompleteUpdate()
                    }
                }
                AppUpdateType.IMMEDIATE -> { // 强制更新结果
                    // 应用返回到前台时，您应确认更新未在 UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS 状态下停止。如果更新在此状态下停止，请继续更新：
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                        // If an in-app update is already running, resume the update.
                        currUpdateType = -1
                        startUpdate(appUpdateInfo, AppUpdateType.IMMEDIATE, activity!!)
                    }
                }
                else -> Log.i(TAG, "checkUpdateResult: currUpdateType=$currUpdateType")
            }
        }
    }

    override fun onStateUpdate(state: InstallState) {
        // 灵活更新：下载完成，完成更新,app在前台的时候，会收到下载完成的通知
        if (state.installStatus() == InstallStatus.DOWNLOADED && currUpdateType == AppUpdateType.FLEXIBLE) {
            popupSnackbarForCompleteUpdate()
        }
    }

    private fun popupSnackbarForCompleteUpdate() {
        appUpdateManager.completeUpdate()
    }

    private fun onDestroy(event: Event) {
        Log.i(TAG, "onDestroy: event=$event")
        appUpdateManager.unregisterListener(this)
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
