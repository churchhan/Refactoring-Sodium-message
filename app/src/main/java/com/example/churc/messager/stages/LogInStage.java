package com.example.churc.messager.stages;

import org.json.JSONObject;

import rx.Observable;
import rx.functions.Func1;
import com.example.churc.messager.Notification;
import com.example.churc.messager.WebHelper;
/**
 * Created by churc on 8/10/2016.
 */
public class LogInStage implements Func1<String, Observable<String>> {

    final String server;
    final String username;


    public LogInStage(String server, String username){
        this.server = server;
        this.username = username;
    }

    @Override
    public Observable<String> call(String challenge_response)  {
        try {
            JSONObject userDetails = new JSONObject();
            userDetails.put("username",username);
            userDetails.put("response",challenge_response);
            JSONObject response = WebHelper.JSONPut(server+"/login",userDetails);
            return Observable.just(response.getString("status"));
        } catch (Exception e) {
            e.printStackTrace();
            return Observable.error(e);
        }
    }
}
