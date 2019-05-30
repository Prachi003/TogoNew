package com.togocourier.util;

import android.content.Context;
import android.content.SharedPreferences;
import static android.content.Context.MODE_PRIVATE;


public class SessionManager {

    private SharedPreferences mypref ;
    private SharedPreferences.Editor editor;
    private static final String PREF_NAME = "LoyalMe";
    private static final String AUTHTOKEN = "AUTHTOKEN";
    Context _context;


    public SessionManager(Context context ){
        _context = context;
        mypref = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        editor = mypref.edit();
        editor.apply();}


    public void createSessionAuthToken(String authToken){
        editor.putString(AUTHTOKEN,authToken);
        editor.commit();
    }



    public String getAUTH_TOKEN(){
        return  mypref.getString(AUTHTOKEN, "");
    }










}
