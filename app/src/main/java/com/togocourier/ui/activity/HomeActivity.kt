package com.togocourier.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.FrameLayout
import com.togocourier.R
import com.togocourier.ui.activity.courier.NewCourierPostDetailsActivity
import com.togocourier.ui.activity.customer.NewCustomerPostDetailsActivity
import com.togocourier.ui.activity.customer.model.newcustomer.GetMyPost
import com.togocourier.ui.fragment.ChatFragment
import com.togocourier.ui.fragment.NotificationFragment
import com.togocourier.ui.fragment.ProfileFragment
import com.togocourier.ui.fragment.courier.MyTaskFragment
import com.togocourier.ui.fragment.courier.NewPostFragment
import com.togocourier.ui.fragment.customer.MyPostFragment
import com.togocourier.ui.fragment.customer.NewAddCourierFragment
import com.togocourier.ui.fragment.customer.model.GetPostId
import com.togocourier.ui.phase3.activity.CostumerNewPostDetailActivity
import com.togocourier.ui.phase3.activity.PendingCostumerDetailActivity
import com.togocourier.util.Constant
import com.togocourier.util.PreferenceConnector
import kotlinx.android.synthetic.main.new_activity_home.*
import kotlinx.android.synthetic.main.new_title_bar.*

@Suppress("DEPRECATION")
class HomeActivity : AppCompatActivity(), View.OnClickListener {
    private var userType = ""
    private var clickedId = 0
    private var fm: FragmentManager? = null
    private var doubleBackToExitPressedOnce = false
    private var runnable: Runnable? = null
    private var arrayList: ArrayList<GetMyPost.DataBean.ItemBean>? = ArrayList()

    // variable to track event time
    private var mLastClickTime: Long = 0
    var id: Int = 0

    private lateinit var frameLayout: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.new_activity_home)
        initializeView()
        val type = intent.getStringExtra("type")
        val postId = intent.getStringExtra("reference_id")
        val requestId = intent.getStringExtra("requestId")

        if (type != null) {
            if (type == "sendrequest") {
                if (userType == Constant.CUSTOMER) {
                    val intent = Intent(this, PendingCostumerDetailActivity::class.java)
                    intent.putExtra("FROM", Constant.pendingPost)
                    intent.putExtra("userId", "")
                    intent.putExtra("POSTID", postId)
                    startActivity(intent)


                } else if (userType == Constant.COURIOR) {
                    val intent = Intent(this, NewCourierPostDetailsActivity::class.java)
                    intent.putExtra("POSTID", postId)
                    intent.putExtra("FROM", Constant.newPost)
                    intent.putExtra("REQUESTID", "")
                    startActivity(intent)
                }

            } else if (type == "deliverScreen") {
                if (userType == Constant.CUSTOMER) {
                    val intent = Intent(this, PendingCostumerDetailActivity::class.java)
                    intent.putExtra("FROM", Constant.pendingPost)
                    intent.putExtra("userId", "")
                    intent.putExtra("POSTID", postId)
                    startActivity(intent)


                } else if (userType == Constant.COURIOR) {
                    val intent = Intent(this, NewCourierPostDetailsActivity::class.java)
                    intent.putExtra("POSTID", postId)
                    intent.putExtra("FROM", Constant.pendingPost)
                    intent.putExtra("REQUESTID", requestId)
                    startActivity(intent)
                }

            } else if (type == "updateScreen") {
                if (userType == Constant.CUSTOMER) {
                    val intent = Intent(this, NewCustomerPostDetailsActivity::class.java)
                    intent.putExtra("POSTID", postId)
                    intent.putExtra("FROM", Constant.pendingTask)
                    intent.putExtra("REQUESTID", requestId)
                    startActivity(intent)

                } else if (userType == Constant.COURIOR) {
                    val intent = Intent(this, NewCourierPostDetailsActivity::class.java)
                    intent.putExtra("POSTID", postId)
                    intent.putExtra("FROM", Constant.pendingTask)
                    intent.putExtra("REQUESTID", requestId)
                    startActivity(intent)
                }

            } else if (type == "addpost") {
                val intent = Intent(this, CostumerNewPostDetailActivity::class.java)
                intent.putExtra("POSTID", postId)
                intent.putExtra("userId", "")
                intent.putExtra("FROM", "courierlist")
                startActivity(intent)

            } else if (type == "review") {
                if (userType == Constant.CUSTOMER) {
                    val intent = Intent(this, PendingCostumerDetailActivity::class.java)
                    intent.putExtra("POSTID", id)
                    intent.putExtra("userId", "")
                    intent.putExtra("REQUESTID", requestId)
                    intent.putExtra("FROM", "couriercompleted")
                    startActivity(intent)


                } else if (userType == Constant.COURIOR) {
                    val intent = Intent(this, PendingCostumerDetailActivity::class.java)
                    intent.putExtra("POSTID", postId)
                    intent.putExtra("userId", "")

                    intent.putExtra("FROM", "Courier")
                    intent.putExtra("REQUESTID", requestId)
                    startActivity(intent)
                }

            } else if (type == "tip") {
                if (userType == Constant.CUSTOMER) {
                    val intent = Intent(this, PendingCostumerDetailActivity::class.java)
                    intent.putExtra("POSTID", postId)
                    intent.putExtra("userId", "")

                    intent.putExtra("FROM", "Costumer")
                    intent.putExtra("REQUESTID", requestId)
                    startActivity(intent)
                    startActivity(intent)

                } else if (userType == Constant.COURIOR) {
                    val intent = Intent(this, PendingCostumerDetailActivity::class.java)
                    intent.putExtra("POSTID", postId)
                    intent.putExtra("itembean", "")
                    intent.putExtra("userId", "")
                    intent.putExtra("FROM", "Courier")
                    intent.putExtra("REQUESTID", requestId)
                    startActivity(intent)
                }
            } else if (type == "accept") {
                if (userType == Constant.COURIOR) {
                    val intent = Intent(this, PendingCostumerDetailActivity::class.java)
                    intent.putExtra("POSTID", postId)
                    intent.putExtra("itembean", "")
                    intent.putExtra("userId", "")
                    intent.putExtra("FROM", "Courier")
                    intent.putExtra("REQUESTID", requestId)
                    startActivity(intent)
                }
            }

        }
    }

    private fun initializeView() {
        fm = supportFragmentManager
        arrayList = intent.getParcelableArrayListExtra<GetMyPost.DataBean.ItemBean>("param1")

        frameLayout = findViewById(R.id.fragmentPlace)
        id = frameLayout.id
        userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "")
        clickedId = firstTabLay.id
        if (userType == Constant.CUSTOMER) {
            titleTxt.text = getString(R.string.new_post)

            secondTabImage.setImageResource(R.drawable.new_inactive_chat_ico)
            thirdTabImage.setImageResource(R.drawable.new_inactive_add_ico)
            fourthTabImage.setImageResource(R.drawable.new_inactive_notifications_ico)
            fifthTabImage.setImageResource(R.drawable.new_inactive_user_ico)
            thirdCourierTabImage.visibility = View.GONE


            if (arrayList != null) {
                if (arrayList!!.size > 1) {
                    manageFirstTabClick()

                } else {
                    headerLay.visibility = View.GONE
                    manageThirdTabClick()
                }
            } else {
                headerLay.visibility = View.GONE

                manageThirdTabClick()
            }


        } else {
            titleTxt.text = getString(R.string.new_post)

            secondTabImage.setImageResource(R.drawable.new_inactive_my_task_ico)
            thirdTabImage.visibility = View.GONE
            fourthTabImage.setImageResource(R.drawable.new_inactive_notifications_ico)
            fifthTabImage.setImageResource(R.drawable.new_inactive_user_ico)
            thirdCourierTabImage.setImageResource(R.drawable.new_inactive_chat_ico)
            thirdCourierTabImage.visibility = View.VISIBLE

            replaceFragment(NewPostFragment(), false, R.id.fragmentPlace)
        }

        //checkContactInfo()

        firstTabLay.setOnClickListener(this)
        secondTabLay.setOnClickListener(this)
        thirdTabLay.setOnClickListener(this)
        fourthTabLay.setOnClickListener(this)
        fifthTabLay.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        // Preventing multiple clicks, using threshold of 1/2 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()

        when (view.id) {
            R.id.firstTabLay -> {
                if (clickedId != R.id.firstTabLay) {
                    manageFirstTabClick()
                    headerLay.visibility = View.VISIBLE
                }
            }

            R.id.secondTabLay -> {
                if (clickedId != R.id.secondTabLay) {
                    manageSecondTabClick()
                    headerLay.visibility = View.VISIBLE
                }
            }
            R.id.thirdTabLay -> {
                if (clickedId != R.id.thirdTabLay) {
                    headerLay.visibility = View.GONE

                    manageThirdTabClick()
                }
            }
            R.id.fourthTabLay -> {
                if (clickedId != R.id.fourthTabLay) {
                    manageForthTabClick()
                    headerLay.visibility = View.VISIBLE
                    replaceFragment(NotificationFragment(), false, R.id.fragmentPlace)
                }
            }
            R.id.fifthTabLay -> {
                if (clickedId != R.id.fifthTabLay) {
                    profileFragmentClick()
                    /*manageFifthTabClick()
                    headerLay.visibility = View.GONE
                    replaceFragment(ProfileFragment(), false, R.id.fragmentPlace)*/
                }
            }

        }
    }

    private fun manageFirstTabClick() {
        clickedId = R.id.firstTabLay
        headerLay.visibility = View.VISIBLE

        if (userType == Constant.CUSTOMER) {
            GetPostId.instance.postID = ""

            titleTxt.text = getString(R.string.new_post)
            setCustomerTab()
            firstTabImage.setImageResource(R.drawable.new_active_post_ico)
            replaceFragment(MyPostFragment(), false, R.id.fragmentPlace)

        } else {
            titleTxt.text = getString(R.string.new_post)
            setCourierTab()
            firstTabImage.setImageResource(R.drawable.new_active_post_ico)
            replaceFragment(NewPostFragment(), false, R.id.fragmentPlace)
        }

        firstTabView.visibility = View.VISIBLE
        secondTabView.visibility = View.GONE
        thirdTabView.visibility = View.GONE
        fourthTabView.visibility = View.GONE
        fifthTabView.visibility = View.GONE
    }

    private fun manageSecondTabClick() {
        clickedId = R.id.secondTabLay
        if (userType == Constant.CUSTOMER) {
            GetPostId.instance.postID = ""

            titleTxt.text = getString(R.string.messages)
            setCustomerTab()
            secondTabImage.setImageResource(R.drawable.new_active_chat_ico)
            replaceFragment(ChatFragment(), false, R.id.fragmentPlace)

        } else {
            titleTxt.text = getString(R.string.my_task)
            setCourierTab()
            secondTabImage.setImageResource(R.drawable.new_active_my_task_ico)
            replaceFragment(MyTaskFragment(), false, R.id.fragmentPlace)
        }

        firstTabView.visibility = View.GONE
        secondTabView.visibility = View.VISIBLE
        thirdTabView.visibility = View.GONE
        fourthTabView.visibility = View.GONE
        fifthTabView.visibility = View.GONE
    }

    private fun manageThirdTabClick() {
        clickedId = R.id.thirdTabLay
        if (userType == Constant.CUSTOMER) {
            GetPostId.instance.postID = ""

            titleTxt.text = getString(R.string.add_post)
            setCustomerTab()
            headerLay.visibility = View.GONE
            thirdTabImage.setImageResource(R.drawable.new_active_add_ico)
            thirdTabView.visibility = View.GONE
            // replaceFragment(AddCourierItemFragment(), false, R.id.fragmentPlace)

            replaceFragment(NewAddCourierFragment(), false, R.id.fragmentPlace)

        } else {
            titleTxt.text = getString(R.string.messages)
            setCourierTab()
            headerLay.visibility = View.VISIBLE
            thirdCourierTabImage.setImageResource(R.drawable.new_active_chat_ico)
            thirdTabView.visibility = View.VISIBLE
            replaceFragment(ChatFragment(), false, R.id.fragmentPlace)
        }

        firstTabView.visibility = View.GONE
        secondTabView.visibility = View.GONE
        fourthTabView.visibility = View.GONE
        fifthTabView.visibility = View.GONE
    }

    private fun manageForthTabClick() {
        clickedId = R.id.fourthTabLay
        if (userType == Constant.CUSTOMER) {
            GetPostId.instance.postID = ""

            titleTxt.text = getString(R.string.notification)
            setCustomerTab()
            fourthTabImage.setImageResource(R.drawable.new_active_notifications_icon)

        } else {
            titleTxt.text = getString(R.string.notification)
            setCourierTab()
            fourthTabImage.setImageResource(R.drawable.new_active_notifications_icon)
        }

        firstTabView.visibility = View.GONE
        secondTabView.visibility = View.GONE
        thirdTabView.visibility = View.GONE
        fourthTabView.visibility = View.VISIBLE
        fifthTabView.visibility = View.GONE
    }

    private fun manageFifthTabClick() {
        clickedId = R.id.fifthTabLay
        if (userType == Constant.CUSTOMER) {
            GetPostId.instance.postID = ""

            titleTxt.text = getString(R.string.profile)
            setCustomerTab()
            fifthTabImage.setImageResource(R.drawable.new_active_user_ico)

        } else {
            titleTxt.text = getString(R.string.profile)
            setCourierTab()
            fifthTabImage.setImageResource(R.drawable.new_active_user_ico)
        }

        firstTabView.visibility = View.GONE
        secondTabView.visibility = View.GONE
        thirdTabView.visibility = View.GONE
        fourthTabView.visibility = View.GONE
        fifthTabView.visibility = View.VISIBLE
    }

    private fun profileFragmentClick() {
        manageFifthTabClick()
        headerLay.visibility = View.GONE
        replaceFragment(ProfileFragment(), false, R.id.fragmentPlace)
    }

    private fun setCustomerTab() {
        firstTabImage.setImageResource(R.drawable.new_inactive_post_ico)
        secondTabImage.setImageResource(R.drawable.new_inactive_chat_ico)
        thirdTabImage.setImageResource(R.drawable.new_inactive_add_ico)
        thirdTabImage.visibility = View.VISIBLE
        fourthTabImage.setImageResource(R.drawable.new_inactive_notifications_ico)
        fifthTabImage.setImageResource(R.drawable.new_inactive_user_ico)
        thirdCourierTabImage.visibility = View.GONE
    }

    private fun setCourierTab() {

        firstTabImage.setImageResource(R.drawable.new_inactive_post_ico)
        secondTabImage.setImageResource(R.drawable.new_inactive_my_task_ico)
        thirdTabImage.visibility = View.GONE
        fourthTabImage.setImageResource(R.drawable.new_inactive_notifications_ico)
        fifthTabImage.setImageResource(R.drawable.new_inactive_user_ico)
        thirdCourierTabImage.visibility = View.VISIBLE
        thirdCourierTabImage.setImageResource(R.drawable.new_inactive_chat_ico)
    }

    fun replaceFragment(fragment: Fragment, addToBackStack: Boolean, containerId: Int) {
        val backStackName = fragment.javaClass.name
        val fragmentPopped = fragmentManager.popBackStackImmediate(backStackName, 0)
        var i = fm?.backStackEntryCount
        if (i != null) {
            while (i > 0) {
                fm?.popBackStackImmediate()
                i--
            }
        }
        if (!fragmentPopped) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(containerId, fragment, backStackName).setTransition(FragmentTransaction.TRANSIT_UNSET)
            if (addToBackStack)
                transaction.addToBackStack(backStackName)
            transaction.commit()
        }

    }

    override fun onBackPressed() {
        try {
            if (fm!!.backStackEntryCount > 0) {
                val backStackEntryCount = fm!!.backStackEntryCount
                val fragment = fm!!.fragments[backStackEntryCount - 1]
                fragment?.onResume()
                fm!!.popBackStackImmediate()
            } else {
                val handler = Handler()
                if (!doubleBackToExitPressedOnce) {
                    this.doubleBackToExitPressedOnce = true
                    Constant.snackbar(coordinateLay, "Click again to exit")
                    runnable = Runnable { doubleBackToExitPressedOnce = false }
                    handler.postDelayed(runnable, 2000)
                } else {
                    handler.removeCallbacks(runnable)
                    finishAffinity()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
