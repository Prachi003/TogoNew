package com.togocourier.responceBean

import java.io.Serializable

class UserInfoFCM :Serializable {

    var notificationStatus :String = ""
    var uid :String = ""
    var email:String = ""
    var userType:String = ""

    var name :String = ""
    var firebaseToken:String = ""
    var profilePic:String = ""

  init {
      this.notificationStatus
      this.uid
      this.userType
      this.email
      this.name
      this.firebaseToken
      this.profilePic
  }


}
