package com.togocourier.responceBean

class RattingResponceBean {
    /**
     * status : success
     * message : Review And Rating
     * result : {"id":"7","postId":"7","requestId":"0","giverId":"15","receiverId":"19","rating":"3","ratingDate":"2019-03-06","ratingTime":"13:17:59","review":"thanks","status":"1","crd":"2019-03-06 13:17:59","upd":"2019-03-06 13:17:59"}
     */

    var status: String? = null
    var message: String? = null
    var result: ResultBean? = null

    class ResultBean {
        /**
         * id : 7
         * postId : 7
         * requestId : 0
         * giverId : 15
         * receiverId : 19
         * rating : 3
         * ratingDate : 2019-03-06
         * ratingTime : 13:17:59
         * review : thanks
         * status : 1
         * crd : 2019-03-06 13:17:59
         * upd : 2019-03-06 13:17:59
         */

        var id: String? = null
        var postId: String? = null
        var requestId: String? = null
        var giverId: String? = null
        var receiverId: String? = null
        var rating: String? = null
        var ratingDate: String? = null
        var ratingTime: String? = null
        var review: String? = null
        var status: String? = null
        var crd: String? = null
        var upd: String? = null
    }
}