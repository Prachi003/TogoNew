package com.togocourier.ui.activity.customer

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore
import android.support.design.widget.CoordinatorLayout
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.togocourier.R
import com.togocourier.image.picker.ImagePicker
import com.togocourier.ui.activity.customer.model.AddCourierSummaryModel
import com.togocourier.ui.fragment.customer.AddCourierItemFragment
import com.togocourier.util.Constant
import kotlinx.android.synthetic.main.new_add_courier_item_fragment.*

class AddCourierItemImageActivity : AppCompatActivity(),View.OnClickListener {


    lateinit var coordinateLay: CoordinatorLayout
    private var mContext: Context? = null
    private var courierItemBitmap: Bitmap? = null
    private var courierCroppedUri: Uri? = null
    private var mParam1: String? = null
    private var arrayList:ArrayList<AddCourierSummaryModel>?= null
    private var postTitle:String?=""
    private var deliveryAdrs:String?=""
    private var pickupCity=""
    private var deliveryCity=""
    private var pickUpLat=""
    private var picUpLng=""
    private var deliveryLat=""
    private var deliveryLng=""

    private var pickupAdrs:String?=""

    // variable to track event time
    private var mLastClickTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_add_courier_item_fragment)
        if (intent!=null){
            postTitle=intent.getStringExtra("postTitle")
            deliveryAdrs=intent.getStringExtra("deliveryAdrs")
            pickupAdrs=intent.getStringExtra("pickupAdrs")
            pickUpLat=intent.getStringExtra("pickUpLat")
            picUpLng=intent.getStringExtra("picUpLng")
            pickupCity=intent.getStringExtra("pickupCity")
            deliveryCity=intent.getStringExtra("deliveryCity")
            deliveryLat = intent.getStringExtra("deliveryLat")
            deliveryLng = intent.getStringExtra("deliveryLng")

        }

        initializeView()
    }

    private fun initializeView() {
        //coordinateLay = findViewById<View>(R.id.coordinateLay) as CoordinatorLayout

        rl_add_item_picture.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 23) {
                if (this@AddCourierItemImageActivity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(
                            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            Constant.MY_PERMISSIONS_REQUEST_CAMERA)
                } else {
                    ImagePicker.pickImage(this@AddCourierItemImageActivity)
                }
            } else {
                ImagePicker.pickImage(this@AddCourierItemImageActivity)
            }
        }

        add_courier_item_btn.setOnClickListener(this)
        ImgBack.setOnClickListener(this)
        tv_skip.setOnClickListener(this)

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {

            Constant.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.pickImage(this)
                } else {
                    Toast.makeText(this, "Your permission denied", Toast.LENGTH_LONG).show()
                }
            }

            Constant.MY_PERMISSIONS_REQUEST_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.pickImage(this)
                } else {
                    Toast.makeText(this, "Your permission denied", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == 234) {    // Image Picker
                val imageUri = ImagePicker.getImageURIFromResult(this, requestCode, resultCode, data)
                if (imageUri != null) {
                    /* // Calling Image Cropper
                     activity(imageUri).setCropShape(CropImageView.CropShape.RECTANGLE)
                             .setAspectRatio(4, 3)
                             .start(mContext!!, this@AddCourierItemFragment)*/
                    try {
                        courierCroppedUri = imageUri
                        courierItemBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)

                    }catch (e:OutOfMemoryError){
                        Toast.makeText(this, "This device doesn't have enough space , free up storage space", Toast.LENGTH_SHORT).show()

                        e.printStackTrace()
                    }

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
                    val intent = Intent(this, NewAddCourierItemActivity::class.java)
                    intent.putParcelableArrayListExtra(AddCourierItemFragment.ARG_PARAM1,arrayList)
                    intent.putExtra("courierItemUri", courierCroppedUri.toString())
                    intent.putExtra("deliveryAdrs",deliveryAdrs)
                    intent.putExtra("pickupAdrs",pickupAdrs)
                    intent.putExtra("pickUpLat",pickUpLat)
                    intent.putExtra("picUpLng",picUpLng)
                    intent.putExtra("deliveryLat",deliveryLat)
                    intent.putExtra("deliveryLng",deliveryLng)

                    intent.putExtra("pickupCity",pickupCity)
                    intent.putExtra("deliveryCity",deliveryCity)


                    intent.putExtra("postTitle", postTitle)
                    startActivity(intent)

                } else {
                    Constant.snackbar(main_layout, "Please upload your item picture")
                }
            }

            R.id.tv_skip -> {
                courierItemBitmap = null
                courierCroppedUri = null
                iv_item_picture.visibility = View.GONE
                val intent = Intent(this, NewAddCourierItemActivity::class.java)
                intent.putExtra("courierItemUri", "")
                intent.putExtra("deliveryAdrs",deliveryAdrs)
                intent.putExtra("pickupCity",pickupCity)
                intent.putExtra("deliveryCity",deliveryCity)
                intent.putExtra("pickUpLat",pickUpLat)
                intent.putExtra("picUpLng",picUpLng)
                intent.putExtra("deliveryLat",deliveryLat)
                intent.putExtra("deliveryLng",deliveryLng)

                intent.putExtra("pickupAdrs",pickupAdrs)

                intent.putExtra("postTitle", postTitle)
                intent.putParcelableArrayListExtra(AddCourierItemFragment.ARG_PARAM1,arrayList)
               startActivity(intent)
            }

            R.id.ImgBack->{
                onBackPressed()
            }
        }
    }


}
