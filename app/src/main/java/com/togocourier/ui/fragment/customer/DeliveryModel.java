package com.togocourier.ui.fragment.customer;

public class DeliveryModel {
    private String deliverytitle;
    private static final DeliveryModel ourInstance = new DeliveryModel();

    public static DeliveryModel getInstance() {
        return ourInstance;
    }

    private DeliveryModel() {
    }

    public String getDeliverytitle() {
        return deliverytitle;
    }

    public void setDeliverytitle(String deliverytitle) {
        this.deliverytitle = deliverytitle;
    }
}
