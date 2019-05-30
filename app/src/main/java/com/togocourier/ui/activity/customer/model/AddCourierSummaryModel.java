package com.togocourier.ui.activity.customer.model;

import android.os.Parcel;
import android.os.Parcelable;

public class AddCourierSummaryModel implements Parcelable {

    public String title="";
    public String description="";
    public String pickupAdrs="";
    public String deliveryAdrs="";
    public String pickupLong="";
    public String deliverLat="";
    public String deliveryLng="";
    public String pickupLat="";
    public String collectiveDate="";
    public String collectiveTime="";
    public String deliveryTime="";
    public String quantity="";
    public String price="";
    public String otherDetails="";
    public String orderNo="";
    public String receiptImage="";
    public String senderContactNo="";
    public String receiverName="";
    private String itemId="";
    public String itemImage="";
    public String receiverContact="";
    public String senderName="";
    public String rcvCountryCode="";
    public String collectiveToTime="";
    public String deliveryToTime="";
    public String signatureStatus="";
    public String deliveryDate="";

    public AddCourierSummaryModel(){

    }

    private AddCourierSummaryModel(Parcel in) {
        title = in.readString();
        description = in.readString();
        pickupAdrs = in.readString();
        deliveryAdrs = in.readString();
        pickupLong = in.readString();
        deliverLat = in.readString();
        deliveryLng = in.readString();
        pickupLat = in.readString();
        collectiveDate = in.readString();
        collectiveTime = in.readString();
        deliveryTime = in.readString();
        quantity = in.readString();
        price = in.readString();
        otherDetails = in.readString();
        orderNo = in.readString();
        receiptImage = in.readString();
        senderContactNo = in.readString();
        receiverName = in.readString();
        itemId = in.readString();
        itemImage = in.readString();
        receiverContact = in.readString();
        senderName = in.readString();
        rcvCountryCode = in.readString();
        collectiveToTime = in.readString();
        deliveryToTime = in.readString();
        signatureStatus = in.readString();
        deliveryDate = in.readString();
    }

    public static final Creator<AddCourierSummaryModel> CREATOR = new Creator<AddCourierSummaryModel>() {
        @Override
        public AddCourierSummaryModel createFromParcel(Parcel in) {
            return new AddCourierSummaryModel(in);
        }

        @Override
        public AddCourierSummaryModel[] newArray(int size) {
            return new AddCourierSummaryModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeString(pickupAdrs);
        parcel.writeString(deliveryAdrs);
        parcel.writeString(pickupLong);
        parcel.writeString(deliverLat);
        parcel.writeString(deliveryLng);
        parcel.writeString(pickupLat);
        parcel.writeString(collectiveDate);
        parcel.writeString(collectiveTime);
        parcel.writeString(deliveryTime);
        parcel.writeString(quantity);
        parcel.writeString(price);
        parcel.writeString(otherDetails);
        parcel.writeString(orderNo);
        parcel.writeString(receiptImage);
        parcel.writeString(senderContactNo);
        parcel.writeString(receiverName);
        parcel.writeString(itemId);
        parcel.writeString(itemImage);
        parcel.writeString(receiverContact);
        parcel.writeString(senderName);
        parcel.writeString(rcvCountryCode);
        parcel.writeString(collectiveToTime);
        parcel.writeString(deliveryToTime);
        parcel.writeString(signatureStatus);
        parcel.writeString(deliveryDate);
    }


    /* params.put("title", tv_item_title.text.toString())

             if (tv_item_description.text == getString(R.string.na_txt)) {
        params.put("description", "")
    } else {
        params.put("description", tv_item_description.text.toString())
    }

                    params.put("pickupAdrs", tv_sel_pickup_address.text.toString())
                            params.put("pickupLat", pickUpLat)
            params.put("pickupLong", pickUpLng)
            params.put("deliveryAdrs", tv_sel_drop_off_address.text.toString())
            params.put("deliverLat", deliveryLat)
            params.put("deliverLong", deliveryLng)
            params.put("collectiveDate", pickUpDate)
            params.put("deliveryDate", deliveryDate)
            params.put("collectiveTime", collectiveTime)
            params.put("deliveryTime", deliveryTime)
            params.put("quantity", tv_item_quantity.text.toString())
            params.put("price", tv_delivery_amount.text.toString())
            params.put("otherDetails", "")
            params.put("orderNo", "")
            params.put("receiptImage", "")

            if (tv_pickup_person_contact.text.toString() == resources.getString(R.string.enter_contact_number)) {
        params.put("senderContactNo", "")
    } else {
        params.put("senderContactNo", tv_pickup_person_contact.text.toString())
    }

                    if (tv_delivery_person_contact.text.toString() == resources.getString(R.string.enter_contact_number)) {
        params.put("receiverContact", "")
    } else {
        params.put("receiverContact", tv_delivery_person_contact.text.toString())
    }

                    params.put("receiverName", receiverCustomerName)
            params.put("senderName", senderCustomerName)

            params.put("rcvCountryCode", "+1")

            params.put("collectiveToTime", "")
            params.put("deliveryToTime", "")
            params.put("orderNo", "")
            params.put("signatureStatus", signatureStatus.toString())

            if (addPicBitmap == null) {
        params.put("itemImage", "")
    }
                    return params
}

    override val byteData: Map<String, DataPart>?
@Throws(IOException::class)
                    get() {
                            val params = HashMap<String, DataPart>()
        if (addPicBitmap != null) {
        params.put("itemImage", DataPart("profileImage.jpg", AppHelper.getFileDataFromDrawable(addPicBitmap!!), "image/jpg"))
        }
        return params
        }*/
}
