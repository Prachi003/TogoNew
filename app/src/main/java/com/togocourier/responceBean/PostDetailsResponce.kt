package com.togocourier.responceBean

class PostDetailsResponce {
    /**
     * status : success
     * message :
     * postData : {"postId":"13","id":"13","userId":"25","title":"gitar","description":"Hhh juts the ft huuuuu hjjjjf fed ttttt hjjj tuuu jjjkkk hjjjjjjy jjjkk jujukujkukfg ghjjjk","otherDetails":"Htyhjyj hyyujyhj hh ceded fewfrew4 r gg hhhhh hyjyhj hj jjjjjj Yupik ffgghhh fff ggggg fffff. Gghghhhh","pickupAdrs":"Baba Kharak Singh Rd, Block 1, Kali Bari Lane, Type III, President's Estate, New Delhi, Delhi 110001, India","pickupLat":"28.627764","pickupLong":"77.208527","deliveryAdrs":"Pipliyahana, Indore, Madhya Pradesh, India","deliverLat":"22.709352","deliverLong":"75.901436","collectiveDate":"02/01/2018","collectiveTime":"11:35:00","deliveryDate":"02/01/2018","deliveryTime":"17:35:00","quantity":"23","price":"233","itemImage":"http://mindiii.com/togocourier/uploads/courierpost/thumb/3dd7fe394c12de867f3209e0a631c219.png","receiptImage":"","orderNo":"","postStatus":"new","status":"1","crd":"2018-01-02 06:06:15","upd":"0000-00-00 00:00:00","requestCount":1,"requestData":{"id":"25","cInfoId":"13","postUserId":"25","applyUserId":"5","bidPrice":"400","requestStatus":"pending","deliveryStatus":"pending","paymentStatus":"0","payBy":"0","status":"1","crd":"2018-01-02 09:03:33","upd":"0000-00-00 00:00:00"}}
     */

    private var status: String? = null
    private var message: String? = null
    private var postData: PostDataBean? = null

    fun getStatus(): String? {
        return status
    }

    fun setStatus(status: String) {
        this.status = status
    }

    fun getMessage(): String? {
        return message
    }

    fun setMessage(message: String) {
        this.message = message
    }

    fun getPostData(): PostDataBean? {
        return postData
    }

    fun setPostData(postData: PostDataBean) {
        this.postData = postData
    }

    class PostDataBean {
        /**
         * postId : 13
         * id : 13
         * userId : 25
         * title : gitar
         * description : Hhh juts the ft huuuuu hjjjjf fed ttttt hjjj tuuu jjjkkk hjjjjjjy jjjkk jujukujkukfg ghjjjk
         * otherDetails : Htyhjyj hyyujyhj hh ceded fewfrew4 r gg hhhhh hyjyhj hj jjjjjj Yupik ffgghhh fff ggggg fffff. Gghghhhh
         * pickupAdrs : Baba Kharak Singh Rd, Block 1, Kali Bari Lane, Type III, President's Estate, New Delhi, Delhi 110001, India
         * pickupLat : 28.627764
         * pickupLong : 77.208527
         * deliveryAdrs : Pipliyahana, Indore, Madhya Pradesh, India
         * deliverLat : 22.709352
         * deliverLong : 75.901436
         * collectiveDate : 02/01/2018
         * collectiveTime : 11:35:00
         * deliveryDate : 02/01/2018
         * deliveryTime : 17:35:00
         * quantity : 23
         * price : 233
         * itemImage : http://mindiii.com/togocourier/uploads/courierpost/thumb/3dd7fe394c12de867f3209e0a631c219.png
         * receiptImage :
         * orderNo :
         * postStatus : new
         * status : 1
         * crd : 2018-01-02 06:06:15
         * upd : 0000-00-00 00:00:00
         * requestCount : 1
         * requestData : {"id":"25","cInfoId":"13","postUserId":"25","applyUserId":"5","bidPrice":"400","requestStatus":"pending","deliveryStatus":"pending","paymentStatus":"0","payBy":"0","status":"1","crd":"2018-01-02 09:03:33","upd":"0000-00-00 00:00:00"}
         */

        var postId: String? = null
        var id: String? = null
        var userId: String? = null
        var title: String? = null
        var description: String? = null
        var otherDetails: String? = null
        var pickupAdrs: String? = null
        var pickupLat: String? = null
        var pickupLong: String? = null
        var deliveryAdrs: String? = null
        var deliverLat: String? = null
        var deliverLong: String? = null
        var collectiveDate: String? = null
        var collectiveTime: String? = null
        var deliveryDate: String? = null
        var deliveryTime: String? = null

        var pickUpDate: String? = null
        var pickUpTime: String? = null
        var outOfDeliveryDate: String? = null
        var outOfDeliveryTime: String? = null
        var itemDeliveryDate: String? = null
        var itemDeliveryTime: String? = null

        var quantity: String? = null
        var price: String? = null
        var itemImage: String? = null
        var receiptImage: String? = null
        var orderNo: String? = null
        var postStatus: String? = null
        var signatureStatus: String? = null
        var signatureImage: String? = null
        var status: String? = null
        var crd: String? = null
        var upd: String? = null
        var addBank: String? = null
        var ratingStatus: String? = null

        var senderName: String? = null
        var senderContactNo: String? = null
        var receiverName: String? = null
        var rcvCountryCode: String? = null
        var receiverContact: String? = null

        var requestCount: Int = 0
        var requestData: RequestDataBean? = null
        var collectiveToTime: String? = null
        var deliveryToTime: String? = null


        class RequestDataBean {
            /**
             * id : 25
             * cInfoId : 13
             * postUserId : 25
             * applyUserId : 5
             * bidPrice : 400
             * requestStatus : pending
             * deliveryStatus : pending
             * paymentStatus : 0
             * payBy : 0
             * status : 1
             * crd : 2018-01-02 09:03:33
             * upd : 0000-00-00 00:00:00
             */

            var id: String? = null
            var cInfoId: String? = null
            var postUserId: String? = null
            var applyUserId: String? = null
            var bidPrice: String? = null
            var requestStatus: String? = null
            var deliveryStatus: String? = null
            var paymentStatus: String? = null
            var payBy: String? = null
            var status: String? = null
            var crd: String? = null
            var upd: String? = null

            var drop_off_person: String? = null
            var drop_off_ccode: String? = null
            var drop_off_contact: String? = null
            var courierProfileImage: String? = null
            var customerProfileImage: String? = null
            var customerName: String? = null
            var ratingDate: String? = null
            var ratingTime: String? = null
            var tip: String? = null
            var avgRating: String? = null
            var commision: String? = null
        }
    }
}