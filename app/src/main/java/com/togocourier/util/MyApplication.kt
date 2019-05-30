package com.togocourier.util

import android.app.Activity
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import com.togocourier.ui.activity.HomeActivity
import android.R.attr.name



/**
 * Created by chiranjib on 28/11/17.
 */
class MyApplication : Application() {

    /*companion object {

    }*/
    private var activeActivity: Activity? = null
    private var deliverytitle=""
        private var postttilte=""



    override fun onCreate() {
        super.onCreate()
        setupActivityListener()
        // EmojiManager.install(GoogleEmojiProvider())

    }

    fun getName(): String {
        return postttilte
    }

    fun Setpostttilte(name: String) {
        this.postttilte = name
    }


    private fun setupActivityListener() {

        registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityPaused(p0: Activity?) {
                activeActivity = null
            }

            override fun onActivityResumed(p0: Activity?) {
                activeActivity = p0
            }

            override fun onActivityStarted(p0: Activity?) {
            }

            override fun onActivityDestroyed(p0: Activity?) {
                if (p0 is HomeActivity) {
                    // stopService(Intent(applicationContext, MyLocationService::class.java))
                    val mNotifyMgr = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
                    if (mNotifyMgr != null) {
                        mNotifyMgr.cancel(101)
                    }

                    // HelperClass.stopJobDispatcher(applicationContext)

                   // HelperClass.stopBackgroundService(applicationContext)
                    /* val helper = HelperClass(applicationContext, activeActivity!!)
                     helper.stopAlarmService(applicationContext)*/

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        HelperClass.stopBackgroundService(this@MyApplication)
                    } else {
                        HelperClass.stopJobDispatcher(this@MyApplication!!)
                    }
                }
            }

            override fun onActivitySaveInstanceState(p0: Activity?, p1: Bundle?) {
            }

            override fun onActivityStopped(p0: Activity?) {
            }

            override fun onActivityCreated(p0: Activity?, p1: Bundle?) {
            }
        })
    }

    fun getActiveActivity(): Activity? {
        return activeActivity
    }
}