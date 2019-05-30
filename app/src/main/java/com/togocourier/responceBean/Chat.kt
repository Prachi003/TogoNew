package com.togocourier.responceBean

import java.io.Serializable

class Chat : Serializable{

    var name: String = ""
    var firebaseId: String = ""
    var message: String = ""
    var timestamp: Any ?=null
    var deleteby: String = ""
    var firebaseToken: String = ""
    var title:String = ""
    var profilePic: String = ""
    var uid: String = ""
    var key: String = ""
    var messageCount: Int = 0

  init {

      this.uid
      this.name
      this.firebaseId
      this.message
      this.timestamp
      this.deleteby
      this.firebaseToken
      this.profilePic
      this.messageCount

  }
}