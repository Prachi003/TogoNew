package com.togocourier.util;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AddressLocationTask extends AsyncTask<Void, Void, String> {
    private Context mContext;
    private AddressLocationListner listner;
    private String result;
    private String url;

    public interface AddressLocationListner {
        void getLocation(String result);
    }

    public AddressLocationTask(Context mContext, String url, AddressLocationListner listner) {
        this.mContext = mContext;
        this.url = url;
        this.listner = listner;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            result = getAddressByURL(url);
            return result;
        } catch (Exception e) {
            return "error";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        if (listner != null) listner.getLocation(s);
    }

    private String getAddressByURL(String requestURL) {
        URL url;
        StringBuilder response = new StringBuilder();
        try {
            url = new URL(requestURL);
            Log.e("Geolocation Url", url.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            conn.setDoOutput(true);
            int responseCode = conn.getResponseCode();

            String line;
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = br.readLine()) != null) {
                response.append(line);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response.toString();
    }
}