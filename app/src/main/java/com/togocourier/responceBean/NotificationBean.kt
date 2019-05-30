package com.togocourier.responceBean

class NotificationBean {
    var status: String? = null
    var message: String? = null
    var data: List<DataBean>? = null

    class DataBean {
        var postId: String? = null
        var requestId: String? = null
        var type: String? = null
        var sentById: String? = null
        var receiveById: String? = null
        var notiMsg: String? = null
        var crd: String? = null
        var fullName: String? = null
        var profileImage: String? = null
        var showtime: String? = null
    }
}