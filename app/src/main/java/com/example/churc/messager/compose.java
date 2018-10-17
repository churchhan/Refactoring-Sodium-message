package com.example.churc.messager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.churc.messager.stages.SendmessageStage;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import rx.Observer;
import rx.schedulers.Schedulers;

public class compose extends AppCompatActivity {
    public final static String PREF_KEY_ADDR = "com.churc.messager.pref.key.addr";
    public final static String PREF_KEY_NAME = "com.churc.messager.pref.key.name";
    Context context=this;
    SecretKey AES_Key;
    ServerAPI serverAPI;
    HashMap<String,ServerAPI.UserInfo> myUserMap = new HashMap<String, ServerAPI.UserInfo>();
    String server_name;

    Crypto myCrypto;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);
        Intent intent = getIntent();
        int message_id = intent.getIntExtra("message_id", -1);
        int contact_row = intent.getIntExtra("contact_row",-1);

        DBHandler_C db_C = new DBHandler_C(this);
        contact_sql contact_sql= db_C.getcontact(contact_row);

        SharedPreferences sharedPref = context.getSharedPreferences(contact.PUBLIC_KEY, Context.MODE_PRIVATE);
        String key_string = sharedPref.getString(contact.PUBLIC_KEY, "");


        String key_contact = contact_sql.getkey();
        byte[] decodedKey = Base64.decode(key_contact, Base64.DEFAULT);
        AES_Key = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");


        if(message_id!=-1){
            DBHandler db = new DBHandler(this);
            message message_one = db.getmessagebyid(message_id);
            TextView sender_view = (TextView)findViewById(R.id.contact);
            sender_view.setText(message_one.getsender());
        }
        else if (contact_row!=-1){

            TextView sender_view = (TextView)findViewById(R.id.contact);
            sender_view.setText(contact_sql.getname());
        }
    }

    public String encryptToBase64(String clearText){
        try {

            Cipher aesCipher = Cipher.getInstance("AES");
            aesCipher.init(Cipher.ENCRYPT_MODE, AES_Key);
            byte[] bytes = aesCipher.doFinal(clearText.getBytes());

            return Base64.encodeToString(bytes,Base64.DEFAULT);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }  catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void sendmessage(View view) {
        SharedPreferences sharedPref1 = context.getSharedPreferences(PREF_KEY_ADDR, Context.MODE_PRIVATE);
        String address = sharedPref1.getString(PREF_KEY_ADDR, "129.115.27.54");
        SharedPreferences sharedPref2 = context.getSharedPreferences(PREF_KEY_NAME, Context.MODE_PRIVATE);
        String name = sharedPref2.getString(PREF_KEY_NAME, "Samuelhan");


        EditText editText = (EditText) findViewById(R.id.content);
        String message = editText.getText().toString();
        EditText editText2 = (EditText) findViewById(R.id.contact);
        String contact_name = editText2.getText().toString();
        EditText editText4 = (EditText) findViewById(R.id.title);
        String title = editText4.getText().toString();
        TextView editText3 = (TextView) findViewById(R.id.ttl_value);
        String ttl_value = editText3.getText().toString();
        DBHandler_C db_C = new DBHandler_C(this);
        contact_sql temp = db_C.getcontactbyname(contact_name);
        myCrypto = new Crypto(getPreferences(Context.MODE_PRIVATE));
        myCrypto.saveKeys(getPreferences(Context.MODE_PRIVATE));
        serverAPI = ServerAPI.getInstance(this.getApplicationContext(),
                myCrypto);
        serverAPI.setServerName(address);
        serverAPI.setServerPort("25666");
        ServerAPI.UserInfo contact = serverAPI.new UserInfo(temp.getname(),temp.getiamge(),temp.getkey());
        myUserMap.put(contact.username,contact);


        myCrypto = new Crypto(getPreferences(Context.MODE_PRIVATE));
        server_name= "http://"+address+":25666";
        JSONObject json = new JSONObject();
        try {
            json.put("message", message);
            json.put("contact",contact_name);
            json.put("title",title);
            json.put("ttl_value",ttl_value);
        }
        catch (Exception e) {
            e.printStackTrace();
        }


        rx.Observable.just("")
                .observeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.newThread())
                .flatMap(new SendmessageStage(server_name,name,json,myCrypto,context,myUserMap.get(contact_name).publicKey))
                .subscribe(new Observer<JSONObject>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(JSONObject json) {
                        final JSONObject response = json;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (response.getString("status").equals("ok")) {
                                        Toast.makeText(compose.this,"Message was sent successfully!", Toast.LENGTH_SHORT).show();

                                        new Handler().postDelayed(new Runnable() {

                                            @Override
                                            public void run() {
                                                Intent intent = new Intent(compose.this, contact.class);
                                                startActivity(intent);
                                            }
                                        }, 1000);
                                    } else {
                                        Toast.makeText(compose.this,"Sending failed, please try again!", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                }

                            }
                        });

                    }
                });

        //Intent intent = new Intent(this, Main.class);
        //startActivity(intent);
    }

    public void delete(View view) {
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
    }

    public void ttl(View view) {
        CharSequence times[] = new CharSequence[] {"5 s", "15 s", "30 s"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a TTL Period");
        builder.setItems(times, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // the user clicked on times[which]
                if (which==0){TextView edit_1 = (TextView)findViewById(R.id.ttl_value);
                    edit_1.setText(Integer.toString(5000));}
                else if (which==1){TextView edit_1 = (TextView)findViewById(R.id.ttl_value);
                    edit_1.setText(Integer.toString(15000));}
                else if (which==2){TextView edit_1 = (TextView)findViewById(R.id.ttl_value);
                    edit_1.setText(Integer.toString(30000));}
            }
        });
        builder.show();
    }
}
