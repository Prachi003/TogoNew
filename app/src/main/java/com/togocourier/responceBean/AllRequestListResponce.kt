package com.togocourier.responceBean

class AllRequestListResponce {
    /**
     * status : success
     * message :
     * appliedReqData : [{"requestId":"14","postId":"10","applyUserId":"5","postUserId":"4","bidPrice":"25.5","requestStatus":"pending","deliveryStatus":"pending","applyUserImage":"","applyUserName":"pranjal","applyUserAddress":"Toyohashi, Aichi Prefecture 440-0034, Japan"}]
     */

    private var status: String? = null
    private var message: String? = null
    private var appliedReqData: List<AppliedReqDataBean>? = null

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

    fun getAppliedReqData(): List<AppliedReqDataBean>? {
        return appliedReqData
    }

    fun setAppliedReqData(appliedReqData: List<AppliedReqDataBean>) {
        this.appliedReqData = appliedReqData
    }

    class AppliedReqDataBean {
        /**
         * requestId : 14
         * postId : 10
         * applyUserId : 5
         * postUserId : 4
         * bidPrice : 25.5
         * requestStatus : pending
         * deliveryStatus : pending
         * applyUserImage :
         * applyUserName : pranjal
         * applyUserAddress : Toyohashi, Aichi Prefecture 440-0034, Japan
         */

        var requestId: String? = null
        var postId: String? = null
        var applyUserId: String? = null
        var postUserId: String? = null
        var bidPrice: String? = null
        var requestStatus: String? = null
        var deliveryStatus: String? = null
        var applyUserImage: String? = null
        var applyUserName: String? = null
        var applyUserAddress: String? = null
        var title: String? = null
        var applyUserCountry: String? = null
        var applyUserContact: String? = null
        var rating: String? = null
    }
}