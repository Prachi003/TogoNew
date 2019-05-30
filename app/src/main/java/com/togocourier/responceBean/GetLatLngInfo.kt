package com.togocourier.responceBean

import java.io.Serializable

/**
 * Created by mindiii on 7/2/18.
 */
class GetLatLngInfo :Serializable{

    var status: String? = null
    var message: String? = null
    var result: ResultBean? = null


    class ResultBean {

        var id: String? = null
        var latitude: String? = null
        var longitude: String? = null

        var pickupAdrs: String? = null
        var pickupLat: String? = null
        var pickupLong: String? = null

        var deliveryAdrs: String? = null
        var deliverLat: String? = null
        var deliverLong: String? = null
        var profileImage: String? = null


    }
}