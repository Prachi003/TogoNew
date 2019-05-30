package com.togocourier.ui.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import com.togocourier.R
import com.togocourier.util.Constant
import com.togocourier.util.PreferenceConnector
import com.togocourier.util.RemPreferenceConnector
import kotlinx.android.synthetic.main.new_activity_user_selection.*

class UserSelectionActivity : AppCompatActivity(), View.OnClickListener {
    private var cllickId: Int = 0
    var userType = Constant.CUSTOMER

    // variable to track event time
    private var mLastClickTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_activity_user_selection)
        custmerLay.setOnClickListener(this)
        couriorLay.setOnClickListener(this)
        continueBtn.setOnClickListener(this)
        cllickId = R.id.custmerLay
    }

    override fun onClick(view: View) {
        // Preventing multiple clicks, using threshold of 1/2 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()

        when (view.id) {
            R.id.custmerLay -> {
                if (cllickId == R.id.custmerLay) {
                    Toast.makeText(this, "Already choose as a Customer", Toast.LENGTH_SHORT).show()
                } else {
                    cllickId = R.id.custmerLay
                    userType = Constant.CUSTOMER

                    val oa1 = ObjectAnimator.ofFloat(custmerBtn, "scaleY", 1f, 0f)
                    val oa2 = ObjectAnimator.ofFloat(custmerBtn, "scaleY", 0f, 1f)
                    oa1.duration = 300
                    oa2.duration = 300
                    oa1.interpolator = DecelerateInterpolator()
                    oa2.interpolator = AccelerateDecelerateInterpolator()
                    oa1.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            custmerBtn.setImageResource(R.drawable.new_active_customer_ico)
                            custmerTxt.setTextColor(ContextCompat.getColor(this@UserSelectionActivity, R.color.new_app_color))
                            couriorBtn.setImageResource(R.drawable.new_inactive_courier_ico)
                            couriorTxt.setTextColor(ContextCompat.getColor(this@UserSelectionActivity, R.color.new_light_gray_color))
                            oa2.start()
                        }
                    })
                    oa1.start()
                }
            }
            R.id.couriorLay -> {
                if (cllickId == R.id.couriorLay) {
                    Toast.makeText(this, "Already choose as a Courier", Toast.LENGTH_SHORT).show()
                } else {
                    cllickId = R.id.couriorLay
                    userType = Constant.COURIOR

                    val oa1 = ObjectAnimator.ofFloat(couriorBtn, "scaleY", 1f, 0f)
                    val oa2 = ObjectAnimator.ofFloat(couriorBtn, "scaleY", 0f, 1f)
                    oa1.duration = 300
                    oa2.duration = 300
                    oa1.interpolator = DecelerateInterpolator()
                    oa2.interpolator = AccelerateDecelerateInterpolator()
                    oa1.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            custmerBtn.setImageResource(R.drawable.new_inactive_coustomer_ico)
                            custmerTxt.setTextColor(ContextCompat.getColor(this@UserSelectionActivity, R.color.new_light_gray_color))
                            couriorBtn.setImageResource(R.drawable.new_active_courier_ico)
                            couriorTxt.setTextColor(ContextCompat.getColor(this@UserSelectionActivity, R.color.new_app_color))
                            oa2.start()
                        }
                    })
                    oa1.start()
                }
            }
            R.id.continueBtn -> {
               /* PreferenceConnector.writeString(this, PreferenceConnector.USERTYPE, userType)
                RemPreferenceConnector.writeString(this, RemPreferenceConnector.USERTYPE, userType)

                startActivity(Intent(this, SignInActivity::class.java))
                finish()*/

                PreferenceConnector.writeString(this,PreferenceConnector.USERTYPE,userType)
                RemPreferenceConnector.writeString(this, RemPreferenceConnector.USERTYPE, userType)

                val userType =  PreferenceConnector.readString(this,PreferenceConnector.USERTYPE,"")

                if (userType == Constant.CUSTOMER){
                    val welcomeScreenShow = RemPreferenceConnector.readString(this@UserSelectionActivity, RemPreferenceConnector.WELCOMESCREENSHOW_USER, "")

                    if(welcomeScreenShow != "save_cust"){
                        startActivity(Intent(this@UserSelectionActivity, WelcomeActivity::class.java))
                        //finish()
                    }else{
                        startActivity(Intent(this, SignInActivity::class.java))
                        //finish()
                    }

                }else if(userType == Constant.COURIOR){
                    val welcomeScreenShow = RemPreferenceConnector.readString(this@UserSelectionActivity, RemPreferenceConnector.WELCOMESCREENSHOW_COURIER, "")

                    if(welcomeScreenShow != "save_cust"){
                        startActivity(Intent(this@UserSelectionActivity, WelcomeActivityCourier::class.java))
                        //finish()
                    }else{
                        startActivity(Intent(this, SignInActivity::class.java))
                        //finish()
                    }
                }

            }
        }
    }

}
