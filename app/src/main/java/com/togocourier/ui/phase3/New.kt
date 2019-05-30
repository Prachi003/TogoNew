package com.togocourier.ui.phase3

class New {

    /**
     * status : success
     * message : OK
     * data : [{"postId":"3","userId":"15","postTitle":"Gaugle","itemCount":"1","totalPrice":"3","adminCommission":"30","postStatus":"complete","isActive":"1","crd":"2019-03-06 05:56:53","upd":"2019-03-06 06:20:19","deliveredItem":"1","applyUserId":null,"fullName":null,"email":null,"countryCode":null,"contactNo":null,"profileImage":"https://dev.togocouriers.com/backend_assets/images/images.png","rating":"0.0","ago":"12 mins ago"}]
     * paginatoin : {"si":0,"pages":1,"curr_page":"1"}
     */

    var status: String? = null
    var message: String? = null
    var paginatoin: PaginatoinBean? = null
    var data: List<DataBean>? = null

    class PaginatoinBean {
        /**
         * si : 0
         * pages : 1
         * curr_page : 1
         */

        var si: Int = 0
        var pages: Int = 0
        var curr_page: String? = null
    }

    class DataBean {
        /**
         * postId : 3
         * userId : 15
         * postTitle : Gaugle
         * itemCount : 1
         * totalPrice : 3
         * adminCommission : 30
         * postStatus : complete
         * isActive : 1
         * crd : 2019-03-06 05:56:53
         * upd : 2019-03-06 06:20:19
         * deliveredItem : 1
         * applyUserId : null
         * fullName : null
         * email : null
         * countryCode : null
         * contactNo : null
         * profileImage : https://dev.togocouriers.com/backend_assets/images/images.png
         * rating : 0.0
         * ago : 12 mins ago
         */

        var postId: String? = null
        var userId: String? = null
        var postTitle: String? = null
        var itemCount: String? = null
        var totalPrice: String? = null
        var adminCommission: String? = null
        var postStatus: String? = null
        var isActive: String? = null
        var crd: String? = null
        var upd: String? = null
        var deliveredItem: String? = null
        var applyUserId: Any? = null
        var fullName: Any? = null
        var email: Any? = null
        var countryCode: Any? = null
        var contactNo: Any? = null
        var profileImage: String? = null
        var rating: String? = null
        var ago: String? = null
    }
}
