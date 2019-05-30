package com.togocourier.ui.fragment.customer

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.togocourier.R
import com.togocourier.image.picker.ImagePicker
import com.togocourier.ui.activity.customer.NewAddCourierItemActivity
import com.togocourier.ui.activity.customer.model.AddCourierSummaryModel
import com.togocourier.ui.fragment.courier.PendingTaskFragment
import com.togocourier.util.Constant
import com.togocourier.view.cropper.CropImage.*
import com.togocourier.view.cropper.CropImageView
import kotlinx.android.synthetic.main.new_add_courier_item_fragment.*

class AddCourierItemFragment : Fragment(), View.OnClickListener {
    lateinit var coordinateLay: CoordinatorLayout
    private var mContext: Context? = null
    private var courierItemBitmap: Bitmap? = null
    private var courierCroppedUri: Uri? = null
    private var mParam1: String? = null
    private var arrayList:ArrayList<AddCourierSummaryModel>?= null

    // variable to track event time
    private var mLastClickTime: Long = 0


    companion object {
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        fun newInstance(): AddCourierItemFragment {
            val fragment = AddCourierItemFragment()
            val args = Bundle()
          //  args.putParcelableArrayList(ARG_PARAM1,addCourierSummaryModelList)

            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*if (arguments != null) {
            arrayList = arguments?.getParcelableArrayList<AddCourierSummaryModel>(AddCourierItemFragment.ARG_PARAM1)
        }*/
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.new_add_courier_item_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
    }

    private fun initializeView() {
        coordinateLay = activity?.findViewById<View>(R.id.coordinateLay) as CoordinatorLayout

        rl_add_item_picture.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 23) {
                if (mContext!!.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(
                            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            Constant.MY_PERMISSIONS_REQUEST_CAMERA)
                } else {
                    ImagePicker.pickImage(this@AddCourierItemFragment)
                }
            } else {
                ImagePicker.pickImage(this@AddCourierItemFragment)
            }
        }

        add_courier_item_btn.setOnClickListener(this)
        tv_skip.setOnClickListener(this)

    }

    override fun onAttach(context: Context?) {
        mContext = context
        super.onAttach(context)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {

            Constant.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.pickImage(this@AddCourierItemFragment)
                } else {
                    Toast.makeText(context!!, "Your permission denied", Toast.LENGTH_LONG).show()
                }
            }

            Constant.MY_PERMISSIONS_REQUEST_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.pickImage(this@AddCourierItemFragment)
                } else {
                    Toast.makeText(context!!, "Your permission denied", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {

            if (requestCode == 234) {    // Image Picker
                val imageUri = ImagePicker.getImageURIFromResult(mContext, requestCode, resultCode, data)
                if (imageUri != null) {
                    /* // Calling Image Cropper
                     activity(imageUri).setCropShape(CropImageView.CropShape.RECTANGLE)
                             .setAspectRatio(4, 3)
                             .start(mContext!!, this@AddCourierItemFragment)*/

                    courierCroppedUri = imageUri
                    courierItemBitmap = MediaStore.Images.Media.getBitmap(mContext!!.contentResolver, imageUri)
                    iv_item_picture.setImageBitmap(courierItemBitmap)
                    iv_item_picture.visibility = View.VISIBLE

                } else {
                    Constant.snackbar(coordinateLay, resources.getString(R.string.something_went_wrong))
                }

            } /*else if (requestCode == CROP_IMAGE_ACTIVITY_REQUEST_CODE) {   // Image Cropper
                val result = getActivityResult(data)
                try {
                    if (result != null) {
                        courierCroppedUri = result.uri
                        courierItemBitmap = MediaStore.Images.Media.getBitmap(mContext!!.contentResolver, result.uri)
                        iv_item_picture.setImageBitmap(courierItemBitmap)
                        iv_item_picture.visibility = View.VISIBLE
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Constant.snackbar(coordinateLay, resources.getString(R.string.alertImageException))
                } catch (error: OutOfMemoryError) {
                    Constant.snackbar(coordinateLay, resources.getString(R.string.alertOutOfMemory))
                }

            }*/
        }
    }

    override fun onClick(view: View?) {
        // Preventing multiple clicks, using threshold of 1/2 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()

        when (view!!.id) {
            R.id.add_courier_item_btn -> {
                if (courierItemBitmap != null) {
                    val intent = Intent(context, NewAddCourierItemActivity::class.java)
                    intent.putParcelableArrayListExtra(ARG_PARAM1,arrayList)
                    intent.putExtra("courierItemUri", courierCroppedUri.toString())
                    mContext!!.startActivity(intent)

                } else {
                    Constant.snackbar(coordinateLay, "Please upload your item picture")
                }
            }

            R.id.tv_skip -> {
                courierItemBitmap = null
                courierCroppedUri = null
                iv_item_picture.visibility = View.GONE
                val intent = Intent(context, NewAddCourierItemActivity::class.java)
                intent.putExtra("courierItemUri", "")
                intent.putParcelableArrayListExtra(ARG_PARAM1,arrayList)
                mContext!!.startActivity(intent)
            }
        }
    }


}


