package com.togocourier.responceBean

class PaymentResponce {
    var status: String? = null
    var message: String? = null
    var data: DataBean? = null

    class DataBean {
        var id: String? = null
        var userId: String? = null
        var cardId: String? = null
        var cardDigit: String? = null
        var cardCustomerId: String? = null
        var bankCustomerId: String? = null
        var bankId: String? = null
        var bankAccId: String? = null
        var cardStatus: String? = null
        var bankStatus: String? = null
        var crd: String? = null
        var upd: String? = null

    }
}