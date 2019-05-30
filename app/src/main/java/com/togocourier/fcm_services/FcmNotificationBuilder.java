package com.togocourier.fcm_services;

import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FcmNotificationBuilder {
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String TAG = "FcmNotificationBuilder";
    private static final String SERVER_API_KEY = "AAAAXrBSous:APA91bEk-AB6Hlr9pMa1ftsjKpiLu89EBmzFUN88CSGOHN0WRQnIHDS0b4EAfPsQf1_2f6vJDPvhGEU-4u4Y54A02UPTJb_RKJkv23UcyjdOltZUfdpsrWqAKMUhQctUJo6axPvnLVSK";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String AUTHORIZATION = "Authorization";
    //private static final String AUTH_KEY = "key=" + "AAAAXrBSous:APA91bEk-AB6Hlr9pMa1ftsjKpiLu89EBmzFUN88CSGOHN0WRQnIHDS0b4EAfPsQf1_2f6vJDPvhGEU-4u4Y54A02UPTJb_RKJkv23UcyjdOltZUfdpsrWqAKMUhQctUJo6axPvnLVSK";
    private static final String AUTH_KEY_DEV = "key=" + "AAAAyT8sl1Q:APA91bHPAm3A_26cEUwMlKLxF2oC7TipZOx0Ua10EyyLUMVElzE5S6ZDPCeaEcFAqo2sd_Tu1_nWLFlA_1nMDKD4qmKVf_xbH_YkE6A5zMrBK_gl9cyofcmPADTy8Beql69G9TFjQxvX";
    private static final String FCM_URL = "https://fcm.googleapis.com/fcm/send";
    // json related keys
    private static final String KEY_TO = "to";
    private static final String KEY_NOTIFICATION = "notification";
    private static final String KEY_TITLE = "title";
    private static final String KEY_TEXT = "text";
    private static final String KEY_DATA = "data";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_UID = "uid";
    private static final String KEY_FCM_TOKEN = "fcm_token";

    private String mTitle;
    private String mMessage;
    private String mUsername;
    private String mUid;
    private String mFirebaseToken;
    private String mReceiverFirebaseToken;

    private FcmNotificationBuilder() {

    }

    public static FcmNotificationBuilder initialize() {
        return new FcmNotificationBuilder();
    }

    public FcmNotificationBuilder title(String title) {
        mTitle = title;
        return this;
    }

    public FcmNotificationBuilder message(String message) {
        mMessage = message;
        return this;
    }

    public FcmNotificationBuilder username(String username) {
        mUsername = username;
        return this;
    }

    public FcmNotificationBuilder uid(String uid) {
        mUid = uid;
        return this;
    }

    public FcmNotificationBuilder firebaseToken(String firebaseToken) {
        mFirebaseToken = firebaseToken;
        return this;
    }

    public FcmNotificationBuilder receiverFirebaseToken(String receiverFirebaseToken) {
        mReceiverFirebaseToken = receiverFirebaseToken;
        return this;
    }

    public void send() {
        RequestBody requestBody = null;
        try {
            requestBody = RequestBody.create(MEDIA_TYPE_JSON, getValidJsonBody().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        assert requestBody != null;
        Request request = new Request.Builder()
                .addHeader(CONTENT_TYPE, APPLICATION_JSON)
                .addHeader(AUTHORIZATION, AUTH_KEY_DEV)
                .url(FCM_URL)
                .post(requestBody)
                .build();

        Call call = new OkHttpClient().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "onGetAllUsersFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.e(TAG, "onResponse: " + response.body().string());
            }
        });
    }

    private JSONObject getValidJsonBody() throws JSONException {
        JSONObject jsonObjectBody = new JSONObject();
        jsonObjectBody.put(KEY_TO, mReceiverFirebaseToken);
        JSONObject jsonObjectData = new JSONObject();
        jsonObjectData.put(KEY_TITLE, mUsername);
        jsonObjectData.put(KEY_TEXT, mMessage);
        jsonObjectData.put(KEY_USERNAME, mUsername);
        jsonObjectData.put(KEY_UID, mUid);
        jsonObjectData.put(KEY_FCM_TOKEN, mFirebaseToken);
        jsonObjectData.put("type", "chat");
        jsonObjectData.put("ChatTitle", mTitle);
        jsonObjectData.put("opponentChatId", mUid);
        jsonObjectBody.put(KEY_DATA, jsonObjectData);
        jsonObjectBody.put(KEY_NOTIFICATION, jsonObjectData);
        jsonObjectBody.put("priority","high");

        return jsonObjectBody;
    }
}
