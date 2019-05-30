package com.togocourier.util

import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.IBinder
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.firebase.jobdispatcher.*
import com.google.firebase.database.FirebaseDatabase
import com.togocourier.Interface.MyClickListner
import com.togocourier.R
import com.togocourier.locationService.AlarmReceiver
import com.togocourier.locationService.DeviceBootReceiver
import com.togocourier.service.BackgroundService
import com.togocourier.service.MyJobLocationService
import com.togocourier.service.MyLocationService
import com.togocourier.ui.activity.UserSelectionActivity
import kotlinx.android.synthetic.main.new_dialog_change_password.*

class HelperClass(var mContext: Context, var myActivity: Activity) {

    fun changePasswordDialog(myClickListner: MyClickListner) {
        val openDialog = Dialog(mContext)
        openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        openDialog.setCancelable(false)
        openDialog.setContentView(R.layout.new_dialog_change_password)
        openDialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val lWindowParams = WindowManager.LayoutParams()
        lWindowParams.copyFrom(openDialog.window!!.attributes)
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        openDialog.window!!.attributes = lWindowParams

        openDialog.tv_ok.setOnClickListener({
            val oldPassword = openDialog.ed_old_password.text.toString()
            val newPassword = openDialog.new_password.text.toString()

            if (isValid(openDialog)) {
                myClickListner.getPassword(oldPassword, newPassword, openDialog)
            }

        })
        openDialog.rl_cancel.setOnClickListener({
            openDialog.dismiss()

        })
        openDialog.show()
    }

    private fun isValid(openDialog: Dialog): Boolean {
        val v = Validation()

        if (!v.isNullValue(openDialog.ed_old_password)) {
            Toast.makeText(mContext, "Enter old password", Toast.LENGTH_SHORT).show()
            openDialog.ed_old_password.requestFocus()
            return false

        } else if (!v.isNullValue(openDialog.new_password)) {
            Toast.makeText(mContext, "Enter new password", Toast.LENGTH_SHORT).show()
            openDialog.new_password.requestFocus()
            return false

        } else if (!v.isNullValue(openDialog.confirm_password)) {
            Toast.makeText(mContext, "Enter confirm password", Toast.LENGTH_SHORT).show()
            openDialog.new_password.requestFocus()
            return false

        } else if (!v.isPassword_Valid(openDialog.new_password)) {
            Toast.makeText(mContext, "Password atleast 6 character long", Toast.LENGTH_SHORT).show()
            openDialog.new_password.requestFocus()
            return false
        } else if (!openDialog.new_password.text.toString().trim().equals(openDialog.confirm_password.text.toString().trim())) {
            Toast.makeText(mContext, "New password and confirm password does not match", Toast.LENGTH_SHORT).show()
            openDialog.new_password.requestFocus()
            return false
        }
        return true
    }

    fun sessionExpairDialog() {
        val alertDialog = AlertDialog.Builder(mContext)
        alertDialog.setTitle("Alert")
        alertDialog.setCancelable(false)
        alertDialog.setMessage("Your current session has expired, please login again")
        alertDialog.setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which ->
            PreferenceConnector.clear(mContext)
            var intent = Intent(myActivity, UserSelectionActivity::class.java)
            myActivity.startActivity(intent)
            myActivity.finish()

        })

        alertDialog.show()

    }


    fun inActiveByAdmin(msg: String, isCallUserSelectionScreen: Boolean) {
        val alertDialog = AlertDialog.Builder(mContext)
        alertDialog.setTitle("Alert")
        alertDialog.setCancelable(false)
        alertDialog.setMessage(msg)
        alertDialog.setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which ->
            if (isCallUserSelectionScreen) {
                PreferenceConnector.clear(mContext)
                var intent = Intent(myActivity, UserSelectionActivity::class.java)
                myActivity.startActivity(intent)
                myActivity.finish()
            } else {

                PreferenceConnector.clear(mContext)

            }

            alertDialog.setCancelable(true)

        })

        alertDialog.show()

    }

    class GenericTextWatcher(var view: View, var mContext: Activity) : TextWatcher {
        var isOpen: Boolean = true
        fun textLimiteFull(msg: String) {
            val alertDialog = android.app.AlertDialog.Builder(mContext)
            alertDialog.setTitle("Alert")
            alertDialog.setCancelable(false)
            alertDialog.setMessage(msg)
            alertDialog.setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which ->
                alertDialog.setCancelable(true)

            })
            alertDialog.show()

        }

        override fun afterTextChanged(p0: Editable?) {
            var text: String = p0.toString()
            if (text.length == 100) {
                if (isOpen) {
                    textLimiteFull(mContext.getString(R.string.review_limite))
                    isOpen = false
                } else {
                    isOpen = true
                }
                return
            }
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}


    }

    /*.........start Alarm service......*/
    /* fun startAlarmService(context: Context) {
         val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
         val interval = 30 * 1000
         val alarmIntent = Intent(context, AlarmReceiver::class.java)
         val pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0)
         manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval.toLong(), pendingIntent)
         // Toast.makeText(context, "Alarm Set", Toast.LENGTH_SHORT).show();

     }

     fun stopAlarmService(context: Context) {
         val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
         val alarmIntent = Intent(context, AlarmReceiver::class.java)
         val pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0)

         pendingIntent.cancel()
         manager.cancel(pendingIntent)

         val mNotifyMgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
         if (mNotifyMgr != null) {
             mNotifyMgr.cancel(101)
         }
         // Toast.makeText(context, "Alarm Canceled", Toast.LENGTH_SHORT).show();

         val receiver = ComponentName(context, DeviceBootReceiver::class.java)
         val pm = context.packageManager
         pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                 PackageManager.DONT_KILL_APP)

         var f_id = PreferenceConnector.readString(context, PreferenceConnector.USERID, "")

         val database = FirebaseDatabase.getInstance().reference
         database.child(Constant.LOCATION).child(f_id).setValue(null)
     }*/

    companion object {
        var gpsService: BackgroundService? = null

        fun startBackgroundService(context: Context) {
            val intent = Intent(context, BackgroundService::class.java)
            context.startService(intent)
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

            gpsService?.startTracking()
        }

        private val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                val name = className.className
                if (name.endsWith("BackgroundService")) {
                    gpsService = (service as BackgroundService.LocationServiceBinder).service
                }
            }

            override fun onServiceDisconnected(className: ComponentName) {
                if (className.className == "BackgroundService") {
                    gpsService = null
                }
            }
        }

        fun stopBackgroundService(context: Context) {
            context.stopService(Intent(context, BackgroundService::class.java))
            gpsService?.stopTracking()
        }

         fun startJobDispatcher(context: Context) {
             if (isNetAvailable(context)) {
                 if (isGpsEnable(context)) {
                     val jobDispatcher = FirebaseJobDispatcher(GooglePlayDriver(context))
                     val job: Job
                         job = jobDispatcher.newJobBuilder()
                                 .setService(MyJobLocationService::class.java)
                                 .setLifetime(Lifetime.FOREVER)
                                 .setTag("my_job")
                                 // .setRecurring(true)
                                 //.setTrigger(Trigger.executionWindow(10,15))
                                 .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                                 .setReplaceCurrent(true)
                                 .setConstraints(Constraint.ON_ANY_NETWORK)
                                 .build()

                     jobDispatcher.mustSchedule(job)
                 }
             } else {
                 Toast.makeText(context, context.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
             }
         }

         fun stopJobDispatcher(context: Context) {
             val mNotifyMgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
             mNotifyMgr?.cancel(101)

             val jobDispatcher = FirebaseJobDispatcher(GooglePlayDriver(context))
             jobDispatcher.cancelAll()
         }

        fun isNetAvailable(context: Context): Boolean {
            val connec = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            // Check for network connections
            if (connec.getNetworkInfo(0).state == android.net.NetworkInfo.State.CONNECTED ||
                    connec.getNetworkInfo(0).state == android.net.NetworkInfo.State.CONNECTING ||
                    connec.getNetworkInfo(1).state == android.net.NetworkInfo.State.CONNECTING ||
                    connec.getNetworkInfo(1).state == android.net.NetworkInfo.State.CONNECTED) {

                // if connected with internet
                return true
            } else if (connec.getNetworkInfo(0).state == android.net.NetworkInfo.State.DISCONNECTED || connec.getNetworkInfo(1).state == android.net.NetworkInfo.State.DISCONNECTED) {

                return false
            }
            return false
        }

        private fun isGpsEnable(context: Context): Boolean {
            val lmgr = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            var isGPSEnable = lmgr.isProviderEnabled(LocationManager.GPS_PROVIDER)
            if (!isGPSEnable) {
                val ab = android.support.v7.app.AlertDialog.Builder(context)
                ab.setTitle(R.string.gps_not_enable)
                ab.setMessage(R.string.do_you_want_to_enable)
                ab.setCancelable(false)
                ab.setPositiveButton(R.string.settings, { dialog, which ->
                    isGPSEnable = true
                    val `in` = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    context.startActivity(`in`)
                })
                ab.show()
            }
            return isGPSEnable
        }
    }

}