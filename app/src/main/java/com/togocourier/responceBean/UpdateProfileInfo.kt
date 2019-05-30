package com.togocourier.responceBean

class UpdateProfileInfo {

    var status: String? = null
    var message: String? = null
    var data: DataBean? = null

    class DataBean {

        var fullName: String? = null
        var email: String? = null
        var address: String? = null
        var latitude: String? = null
        var longitude: String? = null
        var profileImage: String? = null
        var countryCode: String? = null
        var contactNo: String? = null
        var profileImageThumb: String? = null

    }
}