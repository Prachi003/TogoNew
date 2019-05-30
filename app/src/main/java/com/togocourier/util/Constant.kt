package com.togocourier.util

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Environment
import android.os.StatFs
import android.support.design.widget.Snackbar
import android.support.v4.content.res.ResourcesCompat
import android.util.Base64
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.google.gson.GsonBuilder
import com.togocourier.R.string.set
import com.togocourier.responceBean.Country
import com.togocourier.ui.activity.HomeActivity
import org.json.JSONArray
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import java.security.MessageDigest
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by ANIL on 28/11/17.
 */
object Constant {
    // key for run time permissions
    val MY_PERMISSIONS_REQUEST_CAMERA = 101
    val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 102
    val REQUEST_CAMERA = 0
    val SELECT_FILE = 1
    val ACCESS_FINE_LOCATION = 104

    val MY_PERMISSIONS_REQUEST_FILE = 107

    val REQUEST_FILE_GALLERY = 2
    val REQUEST_FILE_CAMERA = 22

    val CUSTOMER = "1"
    val COURIOR = "2"
    val ARG_USERS = "users"
    val ARG_CHAT_ROOMS = "chat_rooms"
    val ARG_HISTORY = "chat_history"
    val BlockUsers = "BlockUsers"
    val blockedBy = "blockedBy"

    val newPost = "NEWPOST"
    val MycompletedPost = "MYCOMPLETEDPOST"
    val pendingPost = "PENDINGPOST"
    val pendingTask = "PENDINGTASK"
    val completeTask = "COMPLETETASK"

    // Location table firebase
    val LOCATION = "location"

    // cites
    val Manhattan = "Manhattan"
    val Bronx = "Bronx"
    val Queens = "Queens"
    val Brooklyn = "Brooklyn"
    val StatenIsland = "Staten Island"

    val webviewURL = "http://togocouriers.com/uploads/pdffile/termcondition.pdf"
    val baseWebViewUrl = "https://dev.togocouriers.com/api_v2/user/getTermCondition"

    //val BASE_URL = "http://togocouriers.com/index.php/service/"// LIVE SERVER
    // val BASE_URL = "http://togocouriers.com/dev-toogo/index.php/service/" //DEV SERVER
    val BASE_URL="https://dev.togocouriers.com/api_v2/"
    //val Registration_Url = "registration/userRegistration"
    val Registration_Url="user/registration"
    //val Log_In_Url = "registration/userLogin"
    val Log_In_Url = "user/login"
    //val Add_Post_Url = "userpost/addPost"
    val Add_Post_Url = "post/createPost"
   // val Get_My_Post_Url = "userpost/getMyPost?postStatus="
     val Get_My_Post_Url = "post/getMyPost"


    val Get_Courier_AllPost_List_Url = "post/getMyRecevePost"
    val Get_Post_Details_Url = "userpost/getPostDataByPostId"
    val Send_Request_Url = "post/applyBit"
    val Get_All_Request_Url = "post/getMyRecevePost"
    val Accept_reject_Request_Url = "post/acceptRequest"
    val StripPay = "payment/paymentByCard"
    val zeroTip = "payment/zeroTipAction"
    val paymentTipByCard = "payment/tipByCard"
    val paymentByCustomerToken = "payment/paymentByCustomerToken"


    val CoriorBankAdd = "payment/addBankAccount"
    val DeliveryStatus = "post/managePostItemStatus"
    val saveReviewRating = "post/sendReviewRating" // admin active inactive popup not apply here
    val getReviewByPostId = "post/getReviewRating"// aadmin active inactive popup not apply here
    //val UpdateProfile = "userpost/updateUserProfile"
    val UpdateProfile = "profile/updateProfile"
    val UploadIdResponse= "user/uploadFbInfoIdCard"
    val getUserProfile = "profile/getUserDetail"
    val withdraw = "payment/withdrawnAmount"
    val getBankDetail = "payment/getMyBackDetail"

    val deletePost = "post/deletePostOrItem"
    val deleteBankAccount = "payment/deleteMyBankAccount"

    val getPending = "post/getMyRecevePost"
    val sharePost = "post/sharePost"
    //val getUserDetailById = "post/getMyRecevePost"
    val getPendingPost="post/getMyPost"
    val getNotificationListData = "profile/getMyNotification" // admin active inactive popup not apply here
    val changePassword = "profile/changePassword"
    val updateNotiStatus = "profile/notificationOnOff"
    val logOut = "userpost/logout" //admin active inactive popup not apply here
    val getPreviousPickupAddress = "post/getOldAddress"
    val getAddress = "post/getCityList"
    val getPreviousDeliveryAddress = "post/getOldAddress"
    val updateLatLong = "userpost/updateLatLong" // admin active inactive popup not apply here
    val getLatLongById = "userpost/getLatLongById" // admin active inactive popup not apply here
    val getAllReviewForCourier = "userpost/getAllReviewForCourier" //admin active inactive popup not apply here
    val getAllReviewForCustomer = "userpost/getAllReviewForCustomer"
    val forgotPassword = "registration/forgotPassword" //admin active inactive popup not apply here
    val getAboutusContent = "user/getAboutusContent" //
    val deletePostById = "userpost/deletePostById"
    val getBankInfo = "stripes/getBankInfo"
    val deleteBankInfoById = "stripes/deleteBankInfoById"
    val uploadFbInfoIdCard = "userpost/uploadFbInfoIdCard"
    val userCardPaymentList = "payment/getMyAddedCard"
    val deleteCardById = "payment/deleteCardDetial"
    val paymentTip = "payment/tipByCustomerToken"

    // dev
    /*  val itemImageUrl = "http://togocouriers.com/dev-toogo/uploads/courierpost/"
      val signatureUrl = "http://togocouriers.com/dev-toogo/uploads/signature/"*/

    //live
    val itemImageUrl = "http://togocouriers.com/uploads/courierpost/"
    val signatureUrl = "https://dev.togocouriers.com/frontend_assets/images/circle_logo.png"


    // http://mindiii.com/togo/index.php/service/userpost/updateRequestStatus
    fun getCurrentDate(): String {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val month1 = month + 1
        return year.toString() + "-" + month1 + "-" + day

    }

    fun returnAlertDialog(activity: Activity, message: String) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Alert")
        builder.setCancelable(false)
        builder.setMessage(message)
        builder.setPositiveButton("Ok") { dialogInterface, i ->
            dialogInterface.dismiss()
            activity.finish()
        }
        val alert = builder.create()
        alert.show()
    }

    fun returnAlertDialogToMainActivity(activity: Activity, message: String) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Alert")
        builder.setCancelable(false)
        builder.setMessage(message)
        builder.setPositiveButton("Ok") { dialogInterface, i ->
            dialogInterface.dismiss()
            activity.startActivity(Intent(activity, HomeActivity::class.java))
            activity.finish()
        }
        val alert = builder.create()
        alert.show()
    }


    fun getCurrentTime(): String {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val munite = c.get(Calendar.MINUTE)
        return hour.toString() + ":" + munite

    }

    fun delete_file(filePath: String) {
        val dir = Environment.getExternalStorageDirectory()
        val file = File(dir, filePath)

        if (file.exists())
        // check if file exist
        {
            file.delete()
        }
    }

     // Function to remove duplicates from an ArrayList
   /* fun static <T> List<T> removeDuplicates(List<T> list)
    {

        // Create a new LinkedHashSet

        // Add the elements to set
        Set<T> set = new LinkedHashSet<>(list);

        // Clear the list
        list.clear();

        // add the elements of set
        // with no duplicates to the list
        list.addAll(set);

        // return the list
        return list;
    }*/

    fun setTimeFormat(time: String): String {
        val TimeList = time.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val hourSt = TimeList[0]
        val minute = TimeList[1]
        var hour = Integer.parseInt(hourSt)

        var status: String
        if (hour > 12) {
            hour -= 12
            status = "PM"
        } else if (hour === 0) {
            hour += 12
            status = "AM"
        } else if (hour === 12) {
            status = "PM"
        } else {
            status = "AM"
        }
        var minutes = if (minute.toInt() < 10) {
            "0" + minute.toInt()
        } else {
            minute
        }

        var houre = if (hour.toInt() < 10) {
            "0" + hour.toInt()
        } else {
            hour
        }
        val time = houre.toString() + ":" + minutes + " " + status


        //formatedTime = "$hour:$minute $format"
        return time
    }


    fun loadCountries(context: Context): List<Country>? {
        try {
            val builder = GsonBuilder()
            val gson = builder.create()
            val array = JSONArray(loadJSONFromAsset(context, "country_code.json"))
            val countries = ArrayList<Country>()
            for (i in 0 until array.length()) {
                val country = gson.fromJson<Country>(array.getString(i), Country::class.java!!)
                countries.add(country)
            }
            return countries
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }


    private fun loadJSONFromAsset(context: Context, jsonFileName: String): String? {
        var json: String? = null
        var `is`: InputStream? = null
        try {
            val manager = context.assets
            `is` = manager.open(jsonFileName)
            val size = `is`!!.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            json = String(buffer, Charset.forName("UTF-8"))
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }

        return json
    }


    fun setTimeRange(dateTime: String): String {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.ENGLISH)
        try {
            cal.time = sdf.parse(dateTime)
            cal.add(Calendar.HOUR_OF_DAY, 0)
            return "" + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return ""
    }

    fun checkDeliveryDateTimeGrater(pickDateTime: String, delDateTime: String): Boolean {
        var isgrater = false
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")

        try {
            val startDate = simpleDateFormat.parse(pickDateTime)
            val endDate = simpleDateFormat.parse(delDateTime)

            //milliseconds
            var different = endDate.time - startDate.time

            val secondsInMilli: Long = 1000
            val minutesInMilli = secondsInMilli * 60
            val hoursInMilli = minutesInMilli * 60
            val daysInMilli = hoursInMilli * 24
            val elapsedDays = different / daysInMilli
            different = different % daysInMilli

            val elapsedHours = different / hoursInMilli
            different = different % hoursInMilli

            val elapsedMinutes = different / minutesInMilli
            different = different % minutesInMilli

            val elapsedSeconds = different / secondsInMilli

            if (elapsedDays > 0) {
                isgrater = true
            } else if (elapsedDays == 0L) {
                if (elapsedHours >= 0) {
                    isgrater = true
                } /*else if (elapsedHours == 0L) {
                    if (elapsedMinutes > 30) {
                        isgrater = true
                    }
                }*/
            }

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return isgrater
    }

    fun getHashKey(packageName: String, context: Activity): String {
        var info: PackageInfo
        var something = ""
        try {
            info = context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                var md: MessageDigest
                md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                something = String(Base64.encode(md.digest(), 0))

            }
        } catch (e1: Exception) {
            e1.printStackTrace()
        }

        return something
    }

    fun writeFile(fileUrl: String, filepath: String) {
        val count: Int
        var input: InputStream? = null
        var output: OutputStream? = null
        try {
            val url = URL(fileUrl)
            val connection = url.openConnection()
            connection.connect()

            input = BufferedInputStream(url.openStream(), 8192)
            output = null

            var dir = Environment.getExternalStorageDirectory()

            output = FileOutputStream(dir.toString() + filepath)

            var data = ByteArray(4096)
            count = input.read(data)
            while (count != -1) {
                output.write(data, 0, count)
            }

            output.flush()
            output.close()
            input.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun hideSoftKeyboard(activity: Activity) {
        try {
            val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun readFile(filename: String): StringBuilder {

        val offlinetext = StringBuilder()

        val dir = Environment.getExternalStorageDirectory()

        val filepath = File(dir, filename)

        if (filepath.exists()) {

            try {
                val br = BufferedReader(FileReader(filepath), 8192)
                val line: String
                line = br.readLine()
                while (line != null) {
                    offlinetext.append(line)
                }
                br.close()
            } catch (e: Exception) {
            }

        }

        return offlinetext

    }

    fun downloadFileFromServer(fileURL: String, fileName: String) {

        val stat_fs = StatFs(Environment.getExternalStorageDirectory().path)
        val avail_sd_space = stat_fs.availableBlocks.toDouble() * stat_fs.blockSize.toDouble()

        val MB_Available = avail_sd_space / 10485783

        try {

            val u = URL(fileURL)
            val c = u.openConnection() as HttpURLConnection
            c.requestMethod = "GET"
            c.doOutput = true
            c.connect()
            val fileSize = c.contentLength / 1048576

            if (MB_Available <= fileSize) {
                c.disconnect()
                return
            }

            try {
                val f = FileOutputStream(File(fileName))
                val inpSt = c.inputStream
                val buffer = ByteArray(1024)
                val len1 = inpSt.read(buffer)
                while (len1 > 0) {
                    f.write(buffer, 0, len1)
                }
                f.close()
            } catch (e: Exception) {

                e.printStackTrace()
            }

        } catch (e: Exception) {
            e.printStackTrace()

        }

    }

    fun isNetworkAvailable(context: Context, coordinatorLayout: View): Boolean {
        val connec = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // Check for network connections
        if (connec.getNetworkInfo(0).state == android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).state == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).state == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).state == android.net.NetworkInfo.State.CONNECTED) {

            // if connected with internet
            return true
        } else if (connec.getNetworkInfo(0).state == android.net.NetworkInfo.State.DISCONNECTED || connec.getNetworkInfo(1).state == android.net.NetworkInfo.State.DISCONNECTED) {

            /* val snackbar = Snackbar
                     .make(coordinatorLayout, "No internet connection!", Snackbar.LENGTH_SHORT)
                     .setAction("RETRY") { }
             snackbar.setActionTextColor(Color.RED)
             val sbView = snackbar.view
             val textView = sbView.findViewById<View>(android.support.design.R.id.snackbar_text) as TextView
             textView.setTextColor(Color.YELLOW)
             snackbar.show()*/
            return false
        }
        return false
    }

    fun snackbar(coordinatorLayout: View, message: String) {
        val snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG)
        val sbView = snackbar.view
        val textView = sbView.findViewById<View>(android.support.design.R.id.snackbar_text) as TextView
        textView.setTextColor(Color.WHITE)
        textView.gravity = Gravity.CENTER
        snackbar.setActionTextColor(Color.WHITE)
        snackbar.show()
    }

    fun isConnectingToInternet(context: Context): Boolean {
        val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivity != null) {
            val info = connectivity.allNetworkInfo
            if (info != null)
                for (i in info.indices)
                    if (info[i].state == NetworkInfo.State.CONNECTED) {
                        return true
                    }
        }
        return false
    }


    /////////// New Changes
    val terms_and_condition = "userpost/getTermCondition"
    val AddCardDetail = "payment/addMyAccount"

    fun setTypeface(tv: TextView, context: Context, fontres: Int) {
        tv.typeface = ResourcesCompat.getFont(context, fontres)
    }

    fun getTypeface(context: Context, fontres: Int): Typeface? {
        return ResourcesCompat.getFont(context, fontres)
    }

    var ChatOpponentId = ""

}