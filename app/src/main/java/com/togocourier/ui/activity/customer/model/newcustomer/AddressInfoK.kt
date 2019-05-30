package com.togocourier.ui.activity.customer.model.newcustomer

import android.os.Parcel
import android.os.Parcelable

class AddressInfoK {
    var status: String? = null
    var message: String? = null
    var data: List<DataBean>? = null

    class DataBean() :Parcelable {

        var postItemId: String? = null
        var pickupAdrs: String? = null
        var pickupLat: String? = null
        var pickupLong: String? = null
        var isCheckedItem:Boolean = false

        constructor(parcel: Parcel) : this() {
            postItemId = parcel.readString()
            pickupAdrs = parcel.readString()
            pickupLat = parcel.readString()
            pickupLong = parcel.readString()
            isCheckedItem = parcel.readByte() != 0.toByte()
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(postItemId)
            parcel.writeString(pickupAdrs)
            parcel.writeString(pickupLat)
            parcel.writeString(pickupLong)
            parcel.writeByte(if (isCheckedItem) 1 else 0)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<DataBean> {
            override fun createFromParcel(parcel: Parcel): DataBean {
                return DataBean(parcel)
            }

            override fun newArray(size: Int): Array<DataBean?> {
                return arrayOfNulls(size)
            }
        }


    }
}