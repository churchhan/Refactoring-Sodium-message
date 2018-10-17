package com.example.churc.messager.stages;

import android.util.Log;

import java.security.PublicKey;

import rx.Observable;
import rx.functions.Func1;

import com.example.churc.messager.Crypto;
import com.example.churc.messager.WebHelper;

/**
 * Created by churc on 8/10/2016.
 */
public class GetServerKeyStage implements Func1<Integer, Observable<PublicKey>> {

    final String server;

    public GetServerKeyStage(String server) {
        this.server = server;
    }

    @Override
    public Observable<PublicKey> call(Integer unused)  {
        try {
            String response = WebHelper.StringGet(this.server+"/get-key");
            Log.d("GetServerKeyStage",response);
            return Observable.just(Crypto.getPublicKeyFromString(response));
        } catch (Exception e) {
            e.printStackTrace();
            return Observable.error(e);
        }
    }
}
