package com.togocourier.responceBean

class AddressBean {

    /**
     * status : success
     * message : City List
     * result : [{"cityId":"1","address":"Bronx, NY, USA","latitude":"40.8447819","longitude":"-73.8648268","state":"New York","country":"United States","cityName":"Bronx County","status":"1","crd":"2019-03-11 11:26:59"},{"cityId":"2","address":"Manhattan, New York, NY, USA","latitude":"40.7830603","longitude":"-73.97124880000001","state":"New York","country":"United States","cityName":"New York County","status":"1","crd":"2019-03-11 12:14:10"},{"cityId":"3","address":"Queens, NY, USA","latitude":"40.7282239","longitude":"-73.79485160000002","state":"New York","country":"United States","cityName":"Queens County","status":"1","crd":"2019-03-11 12:14:21"},{"cityId":"4","address":"Brooklyn, NY, USA","latitude":"40.6781784","longitude":"-73.9441579","state":"New York","country":"United States","cityName":"Kings County","status":"1","crd":"2019-03-11 12:14:28"},{"cityId":"5","address":"Staten Island, NY, USA","latitude":"40.5795317","longitude":"-74.15020070000003","state":"New York","country":"United States","cityName":"Richmond County","status":"1","crd":"2019-03-11 12:14:38"},{"cityId":"6","address":"Indore, Madhya Pradesh, India","latitude":"22.7195687","longitude":"75.85772580000003","state":"Madhya Pradesh","country":"India","cityName":"Indore","status":"1","crd":"2019-03-11 13:08:08"}]
     */

    var status: String? = null
    var message: String? = null
    var result: List<ResultBean>? = null

    class ResultBean {
        /**
         * cityId : 1
         * address : Bronx, NY, USA
         * latitude : 40.8447819
         * longitude : -73.8648268
         * state : New York
         * country : United States
         * cityName : Bronx County
         * status : 1
         * crd : 2019-03-11 11:26:59
         */

        var cityId: String? = null
        var address: String? = null
        var latitude: String? = null
        var longitude: String? = null
        var state: String? = null
        var country: String? = null
        var cityName: String? = null
        var status: String? = null
        var crd: String? = null
    }
}
