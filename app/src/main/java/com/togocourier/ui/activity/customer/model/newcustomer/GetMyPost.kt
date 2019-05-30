package com.togocourier.ui.activity.customer.model.newcustomer

import android.os.Parcel
import android.os.Parcelable

class GetMyPost {

    /**
     * status : success
     * message : OK
     * data : [{"postId":"26","userId":"39","postTitle":"hello","itemCount":"1","totalPrice":"3","adminCommission":"30","postStatus":"new","isActive":"1","crd":"2019-05-30 06:27:10","upd":"2019-05-30 06:29:07","deliveredItem":"0","applyUserId":"40","fullName":"seeta","email":"seeta@gmail.com","countryCode":"+1","contactNo":"89765566","profileImage":"https://dev.togocouriers.com/backend_assets/images/images.png","rating":"0.0","ratingVal":"0","reviewVal":"","senderName":null,"senderImage":"https://dev.togocouriers.com/backend_assets/images/images.png","receiverName":null,"receiverImage":"https://dev.togocouriers.com/backend_assets/images/images.png","ago":"4 hrs ago","item":[{"postItemId":"31","postId":"26","userId":"39","itemTitle":"gello","itemImage":"","itemQuantity":"23","description":"hiii","pickupCity":"Indore","pickupAdrs":"Indore, Madhya Pradesh, India","pickupLat":"22.719568699999996","pickupLong":"75.8577258","deliveryAdrs":"Indore, Madhya Pradesh, India","deliveryCity":"Indore","deliverLat":"22.719568699999996","deliverLong":"75.8577258","collectiveDate":"2019-05-30","collectiveTime":"11:56:00","collectiveToTime":"12:56:00","deliveryDate":"2019-05-30","deliveryTime":"12:56:00","deliveryToTime":"13:56:00","pickUpDate":"0000-00-00","pickUpTime":"00:00:00","itemDeliveryDate":"0000-00-00","itemDeliveryTime":"00:00:00","outOfDeliveryDate":"0000-00-00","outOfDeliveryTime":"00:00:00","senderName":"ram","senderContactNo":"6546787655","receiverName":"ram","receiverContactNo":"6546787655","signatureStatus":"0","signatureImage":"https://dev.togocouriers.com/frontend_assets/images/circle_logo.png","price":"3.0","tipPrice":"0","itemStatus":"pending","isActive":"1","crd":"2019-05-30 06:27:10","upd":"2019-05-30 06:27:10","itemImageUrl":"https://dev.togocouriers.com/frontend_assets/images/circle_logo.png","requestStatus":"pending"}],"requests":[{"bidId":"30","postId":"26","postUserId":"39","applyUserId":"40","bidPrice":"23","requestStatus":"pending","deliveryStatus":"pending","reqCustStatus":"0","paymentStatus":"0","payBy":"0","showRequest":"1","commision":"30","status":"1","crd":"2019-05-30 09:42:42","fullName":"seeta","email":"seeta@gmail.com","countryCode":"+1","contactNo":"89765566","profileImage":"https://dev.togocouriers.com/backend_assets/images/images.png","rating":"0.0"}]}]
     * paginatoin : {"si":0,"pages":1,"curr_page":"1"}
     */

    var status: String? = null
    var message: String? = null
    var paginatoin: PaginatoinBean? = null
    var data: List<DataBean>? = null

    class PaginatoinBean {
        /**
         * si : 0
         * pages : 1
         * curr_page : 1
         */

        var si: Int = 0
        var pages: Int = 0
        var curr_page: String? = null
    }

    class DataBean {
        /**
         * postId : 26
         * userId : 39
         * postTitle : hello
         * itemCount : 1
         * totalPrice : 3
         * adminCommission : 30
         * postStatus : new
         * isActive : 1
         * crd : 2019-05-30 06:27:10
         * upd : 2019-05-30 06:29:07
         * deliveredItem : 0
         * applyUserId : 40
         * fullName : seeta
         * email : seeta@gmail.com
         * countryCode : +1
         * contactNo : 89765566
         * profileImage : https://dev.togocouriers.com/backend_assets/images/images.png
         * rating : 0.0
         * ratingVal : 0
         * reviewVal :
         * senderName : null
         * senderImage : https://dev.togocouriers.com/backend_assets/images/images.png
         * receiverName : null
         * receiverImage : https://dev.togocouriers.com/backend_assets/images/images.png
         * ago : 4 hrs ago
         * item : [{"postItemId":"31","postId":"26","userId":"39","itemTitle":"gello","itemImage":"","itemQuantity":"23","description":"hiii","pickupCity":"Indore","pickupAdrs":"Indore, Madhya Pradesh, India","pickupLat":"22.719568699999996","pickupLong":"75.8577258","deliveryAdrs":"Indore, Madhya Pradesh, India","deliveryCity":"Indore","deliverLat":"22.719568699999996","deliverLong":"75.8577258","collectiveDate":"2019-05-30","collectiveTime":"11:56:00","collectiveToTime":"12:56:00","deliveryDate":"2019-05-30","deliveryTime":"12:56:00","deliveryToTime":"13:56:00","pickUpDate":"0000-00-00","pickUpTime":"00:00:00","itemDeliveryDate":"0000-00-00","itemDeliveryTime":"00:00:00","outOfDeliveryDate":"0000-00-00","outOfDeliveryTime":"00:00:00","senderName":"ram","senderContactNo":"6546787655","receiverName":"ram","receiverContactNo":"6546787655","signatureStatus":"0","signatureImage":"https://dev.togocouriers.com/frontend_assets/images/circle_logo.png","price":"3.0","tipPrice":"0","itemStatus":"pending","isActive":"1","crd":"2019-05-30 06:27:10","upd":"2019-05-30 06:27:10","itemImageUrl":"https://dev.togocouriers.com/frontend_assets/images/circle_logo.png","requestStatus":"pending"}]
         * requests : [{"bidId":"30","postId":"26","postUserId":"39","applyUserId":"40","bidPrice":"23","requestStatus":"pending","deliveryStatus":"pending","reqCustStatus":"0","paymentStatus":"0","payBy":"0","showRequest":"1","commision":"30","status":"1","crd":"2019-05-30 09:42:42","fullName":"seeta","email":"seeta@gmail.com","countryCode":"+1","contactNo":"89765566","profileImage":"https://dev.togocouriers.com/backend_assets/images/images.png","rating":"0.0"}]
         */

        var postId: String? = null
        var userId: String? = null
        var postTitle: String? = null
        var itemCount: String? = null
        var totalPrice: String? = null
        var adminCommission: String? = null
        var postStatus: String? = null
        var isActive: String? = null
        var crd: String? = null
        var upd: String? = null
        var deliveredItem: String? = null
        var applyUserId: String? = null
        var fullName: String? = null
        var email: String? = null
        var countryCode: String? = null
        var contactNo: String? = null
        var profileImage: String? = null
        var rating: String? = null
        var ratingVal: String? = null
        var reviewVal: String? = null
        var senderName: Any? = null
        var senderImage: String? = null
        var receiverName: Any? = null
        var receiverImage: String? = null
        var ago: String? = null
        var item: List<ItemBean>? = null
        var requests: List<RequestsBean>? = null

        class ItemBean() :Parcelable {
            /**
             * postItemId : 31
             * postId : 26
             * userId : 39
             * itemTitle : gello
             * itemImage :
             * itemQuantity : 23
             * description : hiii
             * pickupCity : Indore
             * pickupAdrs : Indore, Madhya Pradesh, India
             * pickupLat : 22.719568699999996
             * pickupLong : 75.8577258
             * deliveryAdrs : Indore, Madhya Pradesh, India
             * deliveryCity : Indore
             * deliverLat : 22.719568699999996
             * deliverLong : 75.8577258
             * collectiveDate : 2019-05-30
             * collectiveTime : 11:56:00
             * collectiveToTime : 12:56:00
             * deliveryDate : 2019-05-30
             * deliveryTime : 12:56:00
             * deliveryToTime : 13:56:00
             * pickUpDate : 0000-00-00
             * pickUpTime : 00:00:00
             * itemDeliveryDate : 0000-00-00
             * itemDeliveryTime : 00:00:00
             * outOfDeliveryDate : 0000-00-00
             * outOfDeliveryTime : 00:00:00
             * senderName : ram
             * senderContactNo : 6546787655
             * receiverName : ram
             * receiverContactNo : 6546787655
             * signatureStatus : 0
             * signatureImage : https://dev.togocouriers.com/frontend_assets/images/circle_logo.png
             * price : 3.0
             * tipPrice : 0
             * itemStatus : pending
             * isActive : 1
             * crd : 2019-05-30 06:27:10
             * upd : 2019-05-30 06:27:10
             * itemImageUrl : https://dev.togocouriers.com/frontend_assets/images/circle_logo.png
             * requestStatus : pending
             */

            var postItemId: String? = null
            var postId: String? = null
            var userId: String? = null
            var itemTitle: String? = null
            var itemImage: String? = null
            var itemQuantity: String? = null
            var description: String? = null
            var pickupCity: String? = null
            var pickupAdrs: String? = null
            var pickupLat: String? = null
            var pickupLong: String? = null
            var deliveryAdrs: String? = null
            var deliveryCity: String? = null
            var deliverLat: String? = null
            var deliverLong: String? = null
            var collectiveDate: String? = null
            var collectiveTime: String? = null
            var collectiveToTime: String? = null
            var deliveryDate: String? = null
            var deliveryTime: String? = null
            var deliveryToTime: String? = null
            var pickUpDate: String? = null
            var pickUpTime: String? = null
            var itemDeliveryDate: String? = null
            var itemDeliveryTime: String? = null
            var outOfDeliveryDate: String? = null
            var outOfDeliveryTime: String? = null
            var senderName: String? = null
            var senderContactNo: String? = null
            var receiverName: String? = null
            var receiverContactNo: String? = null
            var signatureStatus: String? = null
            var signatureImage: String? = null
            var price: String? = null
            var tipPrice: String? = null
            var itemStatus: String? = null
            var isActive: String? = null
            var crd: String? = null
            var upd: String? = null
            var itemImageUrl: String? = null
            var requestStatus: String? = null

            constructor(parcel: Parcel) : this() {
                postItemId = parcel.readString()
                postId = parcel.readString()
                userId = parcel.readString()
                itemTitle = parcel.readString()
                itemImage = parcel.readString()
                itemQuantity = parcel.readString()
                description = parcel.readString()
                pickupCity = parcel.readString()
                pickupAdrs = parcel.readString()
                pickupLat = parcel.readString()
                pickupLong = parcel.readString()
                deliveryAdrs = parcel.readString()
                deliveryCity = parcel.readString()
                deliverLat = parcel.readString()
                deliverLong = parcel.readString()
                collectiveDate = parcel.readString()
                collectiveTime = parcel.readString()
                collectiveToTime = parcel.readString()
                deliveryDate = parcel.readString()
                deliveryTime = parcel.readString()
                deliveryToTime = parcel.readString()
                pickUpDate = parcel.readString()
                pickUpTime = parcel.readString()
                itemDeliveryDate = parcel.readString()
                itemDeliveryTime = parcel.readString()
                outOfDeliveryDate = parcel.readString()
                outOfDeliveryTime = parcel.readString()
                senderName = parcel.readString()
                senderContactNo = parcel.readString()
                receiverName = parcel.readString()
                receiverContactNo = parcel.readString()
                signatureStatus = parcel.readString()
                signatureImage = parcel.readString()
                price = parcel.readString()
                tipPrice = parcel.readString()
                itemStatus = parcel.readString()
                isActive = parcel.readString()
                crd = parcel.readString()
                upd = parcel.readString()
                itemImageUrl = parcel.readString()
                requestStatus = parcel.readString()
            }

            override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeString(postItemId)
                parcel.writeString(postId)
                parcel.writeString(userId)
                parcel.writeString(itemTitle)
                parcel.writeString(itemImage)
                parcel.writeString(itemQuantity)
                parcel.writeString(description)
                parcel.writeString(pickupCity)
                parcel.writeString(pickupAdrs)
                parcel.writeString(pickupLat)
                parcel.writeString(pickupLong)
                parcel.writeString(deliveryAdrs)
                parcel.writeString(deliveryCity)
                parcel.writeString(deliverLat)
                parcel.writeString(deliverLong)
                parcel.writeString(collectiveDate)
                parcel.writeString(collectiveTime)
                parcel.writeString(collectiveToTime)
                parcel.writeString(deliveryDate)
                parcel.writeString(deliveryTime)
                parcel.writeString(deliveryToTime)
                parcel.writeString(pickUpDate)
                parcel.writeString(pickUpTime)
                parcel.writeString(itemDeliveryDate)
                parcel.writeString(itemDeliveryTime)
                parcel.writeString(outOfDeliveryDate)
                parcel.writeString(outOfDeliveryTime)
                parcel.writeString(senderName)
                parcel.writeString(senderContactNo)
                parcel.writeString(receiverName)
                parcel.writeString(receiverContactNo)
                parcel.writeString(signatureStatus)
                parcel.writeString(signatureImage)
                parcel.writeString(price)
                parcel.writeString(tipPrice)
                parcel.writeString(itemStatus)
                parcel.writeString(isActive)
                parcel.writeString(crd)
                parcel.writeString(upd)
                parcel.writeString(itemImageUrl)
                parcel.writeString(requestStatus)
            }

            override fun describeContents(): Int {
                return 0
            }

            companion object CREATOR : Parcelable.Creator<ItemBean> {
                override fun createFromParcel(parcel: Parcel): ItemBean {
                    return ItemBean(parcel)
                }

                override fun newArray(size: Int): Array<ItemBean?> {
                    return arrayOfNulls(size)
                }
            }
        }

        class RequestsBean {
            /**
             * bidId : 30
             * postId : 26
             * postUserId : 39
             * applyUserId : 40
             * bidPrice : 23
             * requestStatus : pending
             * deliveryStatus : pending
             * reqCustStatus : 0
             * paymentStatus : 0
             * payBy : 0
             * showRequest : 1
             * commision : 30
             * status : 1
             * crd : 2019-05-30 09:42:42
             * fullName : seeta
             * email : seeta@gmail.com
             * countryCode : +1
             * contactNo : 89765566
             * profileImage : https://dev.togocouriers.com/backend_assets/images/images.png
             * rating : 0.0
             */

            var bidId: String? = null
            var postId: String? = null
            var postUserId: String? = null
            var applyUserId: String? = null
            var bidPrice: String? = null
            var requestStatus: String? = null
            var deliveryStatus: String? = null
            var reqCustStatus: String? = null
            var paymentStatus: String? = null
            var payBy: String? = null
            var showRequest: String? = null
            var commision: String? = null
            var status: String? = null
            var crd: String? = null
            var fullName: String? = null
            var email: String? = null
            var countryCode: String? = null
            var contactNo: String? = null
            var profileImage: String? = null
            var rating: String? = null
        }
    }


}
