package com.togocourier.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import com.togocourier.R
import com.togocourier.util.Constant
import com.togocourier.util.PreferenceConnector

class SplashActivity : AppCompatActivity() {
    val SPLASH_TIME_OUT = 1500
    val handler = Handler()
    var runnable: Runnable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_activity_splash)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        runnable = Runnable {
            val intent: Intent?
            val isLogin = PreferenceConnector.readString(this@SplashActivity, PreferenceConnector.ISLOGIN, "")

            intent = if (isLogin == "yes") {
                Intent(this@SplashActivity, HomeActivity::class.java)
            } else {
                Intent(this@SplashActivity, UserSelectionActivity::class.java)
            }
            startActivity(intent)
            finish()
        }
        handler.postDelayed(runnable, SPLASH_TIME_OUT.toLong())

       // Log.e("Key Hash", Constant.getHashKey("com.togocourier", this))

    }

    override fun onBackPressed() {
        super.onBackPressed()
        handler.removeCallbacks(runnable)
    }

}
