package com.togocourier.responceBean

class DeliverAddressInfo{

    var status: String? = null
    var message: String? = null
    var data: List<DataBean>? = null

    class DataBean {
        var postItemId: String? = null
        var deliveryAdrs: String? = null
        var deliverLat: String? = null
        var deliverLong: String? = null
        var isCheckedItem:Boolean = false
    }

  /*  var status: String? = null
    var message: String? = null
    var result: List<ResultBean>? = null

    class ResultBean {
        var deliveryAdrs: String? = null
        var deliverLat: String? = null
        var deliverLong: String? = null
        var isCheckedItem:Boolean = false
    }*/
}