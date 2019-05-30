package com.togocourier.responceBean

import java.io.Serializable

/**
 * Created by mindiii on 23/10/18.
 */
class UserInfo:Serializable {
    var firebaseid: String? = null
    var name: String? = null
    var email: String? = null
    var profileImage: String? = ""
    var firebaseToken: String? = null
}