package com.togocourier.ui.fragment.customer.model

class GetPendingPost {


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
         * postId : 4
         * userId : 15
         * postTitle : newpost
         * itemCount : 2
         * totalPrice : 48
         * adminCommission : 30
         * postStatus : pending
         * isActive : 1
         * crd : 2019-02-26 08:13:45
         * upd : 2019-02-26 08:56:48
         * deliveredItem : 0
         * applyUserId : 9
         * fullName : aishwary
         * email : Caish@gmail.com
         * countryCode : 91
         * contactNo : 8982077519
         * profileImage : https://dev.togocouriers.com/uploads/profileImage/BM8NeZGrDPqv601I.jpg
         * rating : 4.5
         * ago : 4 hrs ago
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
        var applyUserId: String? = null
        var fullName: String? = null
        var email: String? = null
        var countryCode: String? = null
        var contactNo: String? = null
        var profileImage: String? = null
        var rating: String? = null
        var ago: String? = null
    }
}
