package com.example.churc.messager.stages;


import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.example.churc.messager.Crypto;
import com.example.churc.messager.DBHandler;
import com.example.churc.messager.DBHandler_C;
import com.example.churc.messager.Notification;
import com.example.churc.messager.WebHelper;
import com.example.churc.messager.message;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.SecretKey;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by churc on 8/10/2016.
 */
public class HandleNotice implements Func1<Long, Observable<Notification>> {

    final String server;
    final String username;
    final List<String> contacts;
    final Context context;
    final Crypto crypto;
    private static String LOG       = "LOG:";


    public HandleNotice(String server, String username, List<String> contacts, Context context, Crypto crypto){
        this.server = server;
        this.username = username;
        this.contacts = contacts;
        this.context = context;
        this.crypto = crypto;
    }

    @Override
    public Observable<Notification> call(Long unused)  {

        //Log.d("Message","Enter HandleNotice!");
        try {
            JSONObject json = new JSONObject();
            json.put("username",username);
            json.put("friends",new JSONArray(contacts));
            JSONObject response = new JSONObject(WebHelper.StringGet(server+"/wait-for-push/"+username));
            JSONArray notification = new JSONArray(response.getString("notifications"));
            ArrayList<Notification> notifications = new ArrayList<>();
            if (notification.length()==0){}
            else {
                for (int i =0; i<notification.length();i++){
                    JSONObject result = notification.getJSONObject(i);
                    String type = result.getString("type");

                    if (type.equals("login")) {
                        DBHandler_C db = new DBHandler_C(context);
                        db.updatecontactstatus(result.getString("username"),1);
                        notifications.add(new Notification.LogIn(result.getString("username")));
                    }
                    if (type.equals("logout")) {
                        DBHandler_C db = new DBHandler_C(context);
                        db.updatecontactstatus(result.getString("username"),0);
                        notifications.add(new Notification.LogOut(result.getString("username")));
                    }
                    if (type.equals("message")) {
                        JSONObject message= result.getJSONObject("content");

                        try{
                            SecretKey aesKey = Crypto.getAESSecretKeyFromBytes(crypto.decryptRSA(Base64.decode(message.getString("aes-key"),Base64.NO_WRAP)));
                            String sender = decryptAES64ToString(message.getString("sender"),aesKey);
                            String recipient = decryptAES64ToString(message.getString("recipient"),aesKey);
                            String body = decryptAES64ToString(message.getString("body"),aesKey);
                            String subject = decryptAES64ToString(message.getString("subject-line"),aesKey);
                            Long born = Long.parseLong(decryptAES64ToString(message.getString("born-on-date"),aesKey));
                            Long ttl = Long.parseLong(decryptAES64ToString(message.getString("time-to-live"),aesKey));

                            final DBHandler db = new DBHandler(context);
                            final message result_mess = new message(0,sender,subject,body,born,ttl);
                            db.addmessage(new message(0,sender,subject,body,born,ttl));
                            notifications.add(new Notification.Message(result_mess));
                            Log.d(LOG,sender+" says:");
                            Log.d(LOG,subject+":");
                            Log.d(LOG,body);
                            Log.d(LOG,"ttl: "+ttl);
                        } catch (Exception e) {
                            Log.d(LOG,"Failed to parse message",e);
                        }

                    }

                }
            }
            return Observable.from(notifications);

        } catch (Exception e) {
            e.printStackTrace();
            return Observable.error(e);
        }
    }

    private String decryptAES64ToString(String aes64, SecretKey aesKey) throws UnsupportedEncodingException {
        byte[] bytes = Base64.decode(aes64,Base64.NO_WRAP);
        if(bytes==null) return null;
        bytes = Crypto.decryptAES(bytes, aesKey);
        if(bytes==null) return null;
        return new String(bytes,"UTF-8");
    }
}
