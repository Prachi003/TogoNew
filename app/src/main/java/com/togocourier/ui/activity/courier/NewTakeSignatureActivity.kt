package com.togocourier.ui.activity.courier

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.togocourier.R
import com.togocourier.ui.activity.customer.model.newcustomer.GetMyPost
import com.togocourier.util.Constant
import kotlinx.android.synthetic.main.new_activity_take_signature.*


class NewTakeSignatureActivity : AppCompatActivity(), View.OnClickListener {
    // variable to track event time
    private var mLastClickTime: Long = 0
    private var postId = ""
    private var applyUserId = ""
    private var from = ""
    private var requestId = ""
    private var myPostResponce= GetMyPost.DataBean.ItemBean()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_activity_take_signature)

        val bundle = intent.extras
        postId = bundle!!.getString("POSTID")
        applyUserId = bundle!!.getString("applyUserId")
        from = bundle.getString("FROM")
        myPostResponce = bundle.getParcelable("itembean")
        requestId = bundle.getString("REQUESTID")

        iv_back_sign.setOnClickListener(this)
        clearBtn.setOnClickListener(this)
        submitSignBtn.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        // Preventing multiple clicks, using threshold of 1/2 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()

        when (view.id) {
            R.id.iv_back_sign -> {
                onBackPressed()
            }

            R.id.clearBtn -> {
                signature_pad.clear()
            }

            R.id.submitSignBtn -> {
                if (signature_pad.isEmpty) {
                    Toast.makeText(this, "Please enter signature", Toast.LENGTH_SHORT).show()
                } else {
                    val signatureBitmap = signature_pad.signatureBitmap
                    val signature_uri = signature_pad.getFileDataFromBitmap(this, signatureBitmap)

                    startActivity(Intent(this, NewCourierPostDetailsActivity::class.java)
                            .putExtra("POSTID", postId)
                            .putExtra("REQUESTID", requestId)
                            .putExtra("itembean",myPostResponce)
                            .putExtra("applyUserId",applyUserId)
                            .putExtra("FROM", Constant.newPost).
                            putExtra("SIGN_URI", signature_uri))
                    finish()
                }

            }
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, NewCourierPostDetailsActivity::class.java).putExtra("POSTID", postId)
                .putExtra("REQUESTID", requestId)
                .putExtra("applyUserId",applyUserId)

                .putExtra("itembean",myPostResponce)
                .putExtra("FROM", Constant.newPost))
        finish()
    }
}