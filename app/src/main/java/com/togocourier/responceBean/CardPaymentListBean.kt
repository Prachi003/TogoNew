package com.togocourier.responceBean

import android.os.Parcel
import android.os.Parcelable

class CardPaymentListBean {
    /**
     * status : success
     * message : OK
     * result : [{"cardId":"1","userId":"192","cardNumber":"4242424242424242","expMonth":"7","expYear":"30","cvc":"125","cardType":"Visa","crd":"2018-09-13 08:16:14","upd":"2018-09-13 09:55:46"}]
     */

    var status: String? = null
    var message: String? = null
    var data: List<DataBean>? = null

    class DataBean() :Parcelable {
        /**
         * cardId : 2
         * userId : 15
         * cardNumber : 4242
         * cardHolderName :
         * cardCvv : 123
         * costomerToken : cus_Ec0FoncSvCqaNj
         * cardToken : tok_1E8mEvBLRxRkS6S8pvn23UCT
         * cardType : Visa
         * crd : 2019-02-28 10:35:46
         * upd : 2019-02-28 10:35:46
         */

        var cardId: String? = null
        var userId: String? = null
        var cardNumber: String? = null
        var cardHolderName: String? = null
        var cardCvv: String? = null
        var costomerToken: String? = null
        var cardToken: String? = null
        var cardType: String? = null
        var crd: String? = null
        var upd: String? = null

        constructor(parcel: Parcel) : this() {
            cardId = parcel.readString()
            userId = parcel.readString()
            cardNumber = parcel.readString()
            cardHolderName = parcel.readString()
            cardCvv = parcel.readString()
            costomerToken = parcel.readString()
            cardToken = parcel.readString()
            cardType = parcel.readString()
            crd = parcel.readString()
            upd = parcel.readString()
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(cardId)
            parcel.writeString(userId)
            parcel.writeString(cardNumber)
            parcel.writeString(cardHolderName)
            parcel.writeString(cardCvv)
            parcel.writeString(costomerToken)
            parcel.writeString(cardToken)
            parcel.writeString(cardType)
            parcel.writeString(crd)
            parcel.writeString(upd)
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