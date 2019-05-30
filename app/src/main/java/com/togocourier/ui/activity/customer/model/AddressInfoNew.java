package com.togocourier.ui.activity.customer.model;

import java.util.List;

public class AddressInfoNew {

    /**
     * status : success
     * message : OK
     * data : [{"postItemId":"4","pickupAdrs":"Brooklyn, NY, USA","pickupLat":"40.6781784","pickupLong":"-73.9441579"},{"postItemId":"3","pickupAdrs":"indore","pickupLat":"23.0123","pickupLong":"74.7966"}]
     */

    private String status;
    private String message;
    private List<DataBean> data;

    public boolean isCheckedItem() {
        return isCheckedItem;
    }

    public void setCheckedItem(boolean checkedItem) {
        isCheckedItem = checkedItem;
    }

    private boolean isCheckedItem = false;


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * postItemId : 4
         * pickupAdrs : Brooklyn, NY, USA
         * pickupLat : 40.6781784
         * pickupLong : -73.9441579
         */

        private String postItemId;
        private String pickupAdrs;
        private String pickupLat;
        private String pickupLong;

        public String getPostItemId() {
            return postItemId;
        }

        public void setPostItemId(String postItemId) {
            this.postItemId = postItemId;
        }

        public String getPickupAdrs() {
            return pickupAdrs;
        }

        public void setPickupAdrs(String pickupAdrs) {
            this.pickupAdrs = pickupAdrs;
        }

        public String getPickupLat() {
            return pickupLat;
        }

        public void setPickupLat(String pickupLat) {
            this.pickupLat = pickupLat;
        }

        public String getPickupLong() {
            return pickupLong;
        }

        public void setPickupLong(String pickupLong) {
            this.pickupLong = pickupLong;
        }
    }
}
