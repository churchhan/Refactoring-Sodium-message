package com.example.churc.messager.stages;

import android.content.Context;

import com.example.churc.messager.DBHandler_C;
import com.example.churc.messager.Notification;
import com.example.churc.messager.WebHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by churc on 8/10/2016.
 */
public class GetUserinfoStage implements Func1<String, Observable<JSONObject>> {

    final String server;
    final String username;


    public GetUserinfoStage(String server, String username){
        this.server = server;
        this.username = username;
    }

    @Override
    public Observable<JSONObject> call(String string)  {


        try {


                JSONObject response = new JSONObject(WebHelper.StringGet(server+"/get-contact-info/"+username));

            return Observable.just(response);

        } catch (Exception e) {
            e.printStackTrace();
            return Observable.error(e);
        }


    }
}
