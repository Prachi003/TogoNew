package com.togocourier.responceBean

class BankInfoBean {


    /**
     * status : success
     * message : OK
     * data : {"bankId":"5","userId":"7","accountNumber":"000123456789","bankHolderName":"prachi saini","firstName":"prachi","lastName":"saini","acctNumber":"acct_1EIrxAAhyuCdXhPj","crd":"2019-03-28 06:43:16","upd":"2019-03-28 06:43:16","country":"US","routingNumber":"110000000","currency":"usd","postalCode":"123","dob":"5-9-1985"}
     */

    var status: String? = null
    var message: String? = null
    var data: DataBean? = null

    class DataBean {
        /**
         * bankId : 5
         * userId : 7
         * accountNumber : 000123456789
         * bankHolderName : prachi saini
         * firstName : prachi
         * lastName : saini
         * acctNumber : acct_1EIrxAAhyuCdXhPj
         * crd : 2019-03-28 06:43:16
         * upd : 2019-03-28 06:43:16
         * country : US
         * routingNumber : 110000000
         * currency : usd
         * postalCode : 123
         * dob : 5-9-1985
         */

        var bankId: String? = null
        var userId: String? = null
        var accountNumber: String? = null
        var bankHolderName: String? = null
        var firstName: String? = null
        var lastName: String? = null
        var acctNumber: String? = null
        var crd: String? = null
        var upd: String? = null
        var country: String? = null
        var routingNumber: String? = null
        var currency: String? = null
        var postalCode: String? = null
        var dob: String? = null
    }
}
