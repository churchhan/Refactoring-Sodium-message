package com.example.churc.messager.stages;

import com.example.churc.messager.Crypto;
import com.example.churc.messager.WebHelper;

import org.json.JSONObject;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by churc on 8/11/2016.
 */
public class LogoutStage implements Func1<String, Observable<String>> {

    final String server;
    final String username;
    final Crypto crypto;


public LogoutStage(String server, String username, Crypto crypto){
        this.server = server;
        this.username = username;
        this.crypto = crypto;
        }

@Override
public Observable<String> call(String challenge_response)  {
    try {
        JSONObject userDetails = new JSONObject();
        userDetails.put("username",username);
        userDetails.put("response",challenge_response);
        JSONObject response = WebHelper.JSONPut(server+"/logout",userDetails);
        return Observable.just(response.getString("status"));
    } catch (Exception e) {
        e.printStackTrace();
        return Observable.error(e);
    }
    }
}