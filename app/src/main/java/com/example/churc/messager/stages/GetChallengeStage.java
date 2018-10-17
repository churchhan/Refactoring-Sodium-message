package com.example.churc.messager.stages;

import android.util.Base64;

import org.json.JSONObject;

import rx.Observable;
import rx.functions.Func1;

import com.example.churc.messager.Crypto;
import com.example.churc.messager.WebHelper;

import java.security.PublicKey;

/**
 * Created by churc on 8/10/2016.
 */
public class GetChallengeStage implements Func1<PublicKey, Observable<String>> {

    final String server;
    final String username;
    final Crypto crypto;

    public GetChallengeStage(String server, String username, Crypto crypto) {
        this.server = server;
        this.username = username;
        this.crypto = crypto;
    }

    @Override
    public Observable<String> call(PublicKey serverKey)  {
        try {
            String challenge = WebHelper.StringGet(this.server+"/get-challenge/"+this.username);
            byte[] decrypted = crypto.decryptRSA(Base64.decode(challenge,Base64.NO_WRAP));
            String response = Base64.encodeToString(Crypto.encryptRSA(decrypted, serverKey), Base64.NO_WRAP);
            return Observable.just(response);
        } catch (Exception e) {
            e.printStackTrace();
            return Observable.error(e);
        }
    }
}