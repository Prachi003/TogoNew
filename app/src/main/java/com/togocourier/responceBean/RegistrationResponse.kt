package com.togocourier.responceBean

class RegistrationResponse {


    /**
     * status : success
     * message : Logged in successfully
     * userData : {"id":"6","email":"ujohn@gmail.com","socialId":"","socialType":"","profileImage":"https://dev.togocouriers.com/uploads/profileImage/","fullName":"ujohn","countryCode":"+1","contactNo":"564566464","address":"Brooklyn, NY, USA","latitude":"40.678177","longitude":"-73.944160","userType":"1","authToken":"41ef68bf126872c04a9e","chatId":"","deviceToken":"cJLTeqJ4Zkw:APA91bE-LRg35UkRsnv4O2EnpHvU687VHuAvhvhTxFizcOygdsXV0mstcDopm7CKG1gjZdA_-p-F6urZW1othC5FkmLu3JxiO6apq7Z2xCBJ1zdZzvLeBPI0JDbq2Qgmy0-nWCcZJTG-","notificationStatus":"ON","uploadIdCard":"","status":"1","addBank":"NO","rating":0}
     */

    var status: String? = null
    var message: String? = null
    var userData: UserDataBean? = null

    class UserDataBean {
        /**
         * id : 6
         * email : ujohn@gmail.com
         * socialId :
         * socialType :
         * profileImage : https://dev.togocouriers.com/uploads/profileImage/
         * fullName : ujohn
         * countryCode : +1
         * contactNo : 564566464
         * address : Brooklyn, NY, USA
         * latitude : 40.678177
         * longitude : -73.944160
         * userType : 1
         * authToken : 41ef68bf126872c04a9e
         * chatId :
         * deviceToken : cJLTeqJ4Zkw:APA91bE-LRg35UkRsnv4O2EnpHvU687VHuAvhvhTxFizcOygdsXV0mstcDopm7CKG1gjZdA_-p-F6urZW1othC5FkmLu3JxiO6apq7Z2xCBJ1zdZzvLeBPI0JDbq2Qgmy0-nWCcZJTG-
         * notificationStatus : ON
         * uploadIdCard :
         * status : 1
         * addBank : NO
         * rating : 0
         */

        var id: String? = null
        var email: String? = null
        var socialId: String? = null
        var socialType: String? = null
        var profileImage: String? = null
        var fullName: String? = null
        var countryCode: String? = null
        var contactNo: String? = null
        var address: String? = null
        var latitude: String? = null
        var longitude: String? = null
        var userType: String? = null
        var authToken: String? = null
        var chatId: String? = null
        var deviceToken: String? = null
        var notificationStatus: String? = null
        var uploadIdCard: String? = null
        var status: String? = null
        var addBank: String? = null
        var rating: Int = 0
    }
}
