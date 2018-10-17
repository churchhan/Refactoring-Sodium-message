package com.example.churc.messager.stages;

import android.content.Context;
import android.util.Base64;

import com.example.churc.messager.Crypto;
import com.example.churc.messager.WebHelper;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by churc on 8/12/2016.
 */
public class SendmessageStage implements Func1<String, Observable<JSONObject>> {

    final String server;
    final String username;
    final JSONObject json;
    final Crypto crypto;
    final Context context;
    final PublicKey key;



    public SendmessageStage(String server, String username, JSONObject json, Crypto crypto, Context context, PublicKey key){
        this.server = server;
        this.username = username;
        this.json = json;
        this.crypto = crypto;
        this.context = context;
        this.key = key;

    }

    @Override
    public Observable<JSONObject> call(String string)  {
        SecretKey aesKey = Crypto.createAESKey();
        byte[] aesKeyBytes = aesKey.getEncoded();
        String base64encryptedAESKey =
                Base64.encodeToString(Crypto.encryptRSA(aesKeyBytes,key),
                        Base64.NO_WRAP);
        Long curent = System.currentTimeMillis();

        try {

            JSONObject userDetails = new JSONObject();
            userDetails.put("aes-key", base64encryptedAESKey);
            userDetails.put("sender",  base64AESEncrypted(username, aesKey));
            userDetails.put("recipient",  base64AESEncrypted(json.getString("contact"), aesKey));
            userDetails.put("subject-line",  base64AESEncrypted(json.getString("title"), aesKey));
            userDetails.put("body",  base64AESEncrypted(json.getString("message"), aesKey));
            userDetails.put("born-on-date",  base64AESEncrypted(curent.toString(), aesKey));
            userDetails.put("time-to-live",  base64AESEncrypted(json.getString("ttl_value"), aesKey));
            JSONObject response = WebHelper.JSONPut(server+"/send-message/"+json.getString("contact"),userDetails);
            return Observable.just(response);
        } catch (Exception e) {
            e.printStackTrace();
            return Observable.error(e);
        }



    }

    public String base64AESEncrypted(String clearText, SecretKey aesKey){
        try {
            return Base64.encodeToString(Crypto.encryptAES(clearText.getBytes("UTF-8"),aesKey), Base64.NO_WRAP);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }
}
