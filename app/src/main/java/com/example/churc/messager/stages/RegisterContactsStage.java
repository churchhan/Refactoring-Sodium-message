package com.example.churc.messager.stages;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;

import com.example.churc.messager.DBHandler_C;
import com.example.churc.messager.Notification;
import com.example.churc.messager.WebHelper;

/**
 * Created by churc on 8/10/2016.
 */
public class RegisterContactsStage implements Func1<String, Observable<Notification>> {

    final String server;
    final String username;
    final List<String> contacts;
    final Context context;


    public RegisterContactsStage(String server, String username, List<String> contacts, Context context){
        this.server = server;
        this.username = username;
        this.contacts = contacts;
        this.context = context;
    }

    @Override
    public Observable<Notification> call(String challenge_response)  {
        try {
            JSONObject json = new JSONObject();
            json.put("username",username);
            json.put("friends",new JSONArray(contacts));
            JSONObject response = WebHelper.JSONPut(server+"/register-friends",json);

            ArrayList<Notification> notifications = new ArrayList<>();
            JSONObject status = response.getJSONObject("friend-status-map");
            DBHandler_C db = new DBHandler_C(context);
            for(String contact : contacts){
                JSONObject response1 = new JSONObject(WebHelper.StringGet(server+"/get-contact-info/"+contact));
                String name = response1.getString("username");
                String image = response1.getString("image");
                String key = response1.getString("key");
                db.updatecontactlistbyname(name,image,key);

                if(status.getString(contact).equals("logged-in")){
                    notifications.add(new Notification.LogIn(contact));
                    //Log.d("Contact",contact + status.getString(contact));
                } else {
                    notifications.add(new Notification.LogOut(contact));
                    //Log.d("Contact",contact + status.getString(contact));
                }
            }


            return Observable.from(notifications);
        } catch (Exception e) {
            e.printStackTrace();
            return Observable.error(e);
        }
    }
}
