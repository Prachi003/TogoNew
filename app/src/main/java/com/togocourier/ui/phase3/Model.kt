package com.togocourier.ui.phase3

class Model {


    /**
     * status : success
     * message : OK
     * data : [{"cardId":"2","userId":"15","cardNumber":"4242","cardHolderName":"","cardCvv":"123","costomerToken":"cus_Ec0FoncSvCqaNj","cardToken":"tok_1E8mEvBLRxRkS6S8pvn23UCT","cardType":"Visa","crd":"2019-02-28 10:35:46","upd":"2019-02-28 10:35:46"},{"cardId":"3","userId":"15","cardNumber":"4242","cardHolderName":"","cardCvv":"123","costomerToken":"cus_Ec0MIVcqRc4aiX","cardToken":"tok_1E8mMABLRxRkS6S8X8MhyXQt","cardType":"Visa","crd":"2019-02-28 10:43:15","upd":"2019-02-28 10:43:15"},{"cardId":"4","userId":"15","cardNumber":"4242","cardHolderName":"","cardCvv":"231","costomerToken":"cus_Ec0WVxkVq4dXSv","cardToken":"tok_1E8mVDBLRxRkS6S8r0TaeHOz","cardType":"Visa","crd":"2019-02-28 10:52:36","upd":"2019-02-28 10:52:36"},{"cardId":"5","userId":"15","cardNumber":"4242","cardHolderName":"","cardCvv":"123","costomerToken":"cus_Ec0a5yaGcho8AL","cardToken":"tok_1E8mZTBLRxRkS6S86QnGiLOk","cardType":"Visa","crd":"2019-02-28 10:57:00","upd":"2019-02-28 10:57:00"},{"cardId":"7","userId":"15","cardNumber":"4242","cardHolderName":"","cardCvv":"123","costomerToken":"cus_Edn4ZMg8BYlTT9","cardToken":"tok_1EAVU7BLRxRkS6S8sjVxlmrg","cardType":"Visa","crd":"2019-03-05 05:06:36","upd":"2019-03-05 05:06:36"},{"cardId":"8","userId":"15","cardNumber":"4242","cardHolderName":"","cardCvv":"123","costomerToken":"cus_EdnOnDvaa79smj","cardToken":"tok_1EAVo4BLRxRkS6S8e2dunJym","cardType":"Visa","crd":"2019-03-05 05:27:13","upd":"2019-03-05 05:27:13"},{"cardId":"9","userId":"15","cardNumber":"4242","cardHolderName":"","cardCvv":"123","costomerToken":"cus_Edo1PeTYFxsBxU","cardToken":"tok_1EAWPnBLRxRkS6S8tHFhTSW8","cardType":"Visa","crd":"2019-03-05 06:06:12","upd":"2019-03-05 06:06:12"}]
     */

    var status: String? = null
    var message: String? = null
    var data: List<DataBean>? = null

    class DataBean {
        /**
         * cardId : 2
         * userId : 15
         * cardNumber : 4242
         * cardHolderName :
         * cardCvv : 123
         * costomerToken : cus_Ec0FoncSvCqaNj
         * cardToken : tok_1E8mEvBLRxRkS6S8pvn23UCT
         * cardType : Visa
         * crd : 2019-02-28 10:35:46
         * upd : 2019-02-28 10:35:46
         */

        var cardId: String? = null
        var userId: String? = null
        var cardNumber: String? = null
        var cardHolderName: String? = null
        var cardCvv: String? = null
        var costomerToken: String? = null
        var cardToken: String? = null
        var cardType: String? = null
        var crd: String? = null
        var upd: String? = null
    }
}
