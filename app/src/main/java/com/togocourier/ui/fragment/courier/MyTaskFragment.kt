package com.togocourier.ui.fragment.courier

import android.os.Bundle
import android.os.SystemClock
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.togocourier.R
import com.togocourier.util.Constant
import kotlinx.android.synthetic.main.fragment_my_task.*

class MyTaskFragment : Fragment(), View.OnClickListener {
    lateinit var coordinateLay: CoordinatorLayout
    private var fm: FragmentManager? = null
    private var mParam1: String? = null
    private var mParam2: String? = null
    private var clickedId = 0

    // variable to track event time
    private var mLastClickTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments?.getString(ARG_PARAM1)
            mParam2 = arguments?.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_task, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
    }

    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"
        fun newInstance(param1: String, param2: String): MyTaskFragment {
            val fragment = MyTaskFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

    private fun initializeView() {
        coordinateLay = activity?.findViewById<View>(R.id.coordinateLay) as CoordinatorLayout
        completeBtn.setOnClickListener(this@MyTaskFragment)
        pendingBtn.setOnClickListener(this@MyTaskFragment)

        clickedId = R.id.pendingBtn
        replaceFragment(PendingTaskFragment(), false, R.id.childFragmentPlace)
        pendingBtn.setBackgroundResource(R.drawable.blue_left_round)
        pendingBtn.setTextColor(ContextCompat.getColor(context!!, R.color.colorWhite))
        Constant.setTypeface(pendingBtn, context!!, R.font.montserrat_medium)

        completeBtn.setBackgroundResource(R.drawable.white_right_round)
        completeBtn.setTextColor(ContextCompat.getColor(context!!, R.color.new_post_border_color))
        Constant.setTypeface(completeBtn, context!!, R.font.montserrat_light)
    }

    private fun replaceFragment(fragment: Fragment, addToBackStack: Boolean, containerId: Int) {
        val backStackName = fragment.javaClass.name
        val fragmentPopped = childFragmentManager.popBackStackImmediate(backStackName, 0)
        var i = fm?.backStackEntryCount
        if (i != null) {
            while (i > 0) {
                fm?.popBackStackImmediate()
                i--
            }
        }
        if (!fragmentPopped) {
            val transaction = childFragmentManager.beginTransaction()
            transaction.replace(containerId, fragment, backStackName).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            if (addToBackStack)
                transaction.addToBackStack(backStackName)
            transaction.commit()
        }
    }

    override fun onClick(view: View) {
        // Preventing multiple clicks, using threshold of 1/2 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()

        when (view.id) {
            R.id.pendingBtn -> {
                if (clickedId != R.id.pendingBtn) {
                    clickedId = R.id.pendingBtn
                    replaceFragment(PendingTaskFragment(), false, R.id.childFragmentPlace)
                    pendingBtn.setBackgroundResource(R.drawable.blue_left_round)
                    pendingBtn.setTextColor(ContextCompat.getColor(context!!, R.color.colorWhite))
                    Constant.setTypeface(pendingBtn, context!!, R.font.montserrat_medium)

                    completeBtn.setBackgroundResource(R.drawable.white_right_round)
                    completeBtn.setTextColor(ContextCompat.getColor(context!!, R.color.new_post_border_color))
                    Constant.setTypeface(completeBtn, context!!, R.font.montserrat_light)
                }
            }
            R.id.completeBtn -> {
                if (clickedId != R.id.completeBtn) {
                    clickedId = R.id.completeBtn
                    replaceFragment(CompleteTaskFragment(), false, R.id.childFragmentPlace)
                    completeBtn.setBackgroundResource(R.drawable.blue_right_round)
                    completeBtn.setTextColor(ContextCompat.getColor(context!!, R.color.colorWhite))
                    Constant.setTypeface(completeBtn, context!!, R.font.montserrat_medium)

                    pendingBtn.setBackgroundResource(R.drawable.white_left_round)
                    pendingBtn.setTextColor(ContextCompat.getColor(context!!, R.color.new_post_border_color))
                    Constant.setTypeface(pendingBtn, context!!, R.font.montserrat_light)
                }
            }

        }
    }
}
