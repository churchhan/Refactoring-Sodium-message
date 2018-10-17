package com.example.churc.messager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.churc.messager.stages.GetChallengeStage;
import com.example.churc.messager.stages.GetServerKeyStage;
import com.example.churc.messager.stages.HandleNotice;
import com.example.churc.messager.stages.LogInStage;
import com.example.churc.messager.stages.LogoutStage;
import com.example.churc.messager.stages.RegisterContactsStage;
import com.example.churc.messager.stages.RegistrationStage;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.schedulers.Schedulers;

public class setting extends AppCompatActivity {
    public final static String PREF_KEY_ADDR = "com.churc.messager.pref.key.addr";
    public final static String PREF_KEY_NAME = "com.churc.messager.pref.key.name";
    public final static String PREF_KEY_KEY = "com.churc.messager.pref.key.key";
    public final static String LOGIN_STATUS = "com.churc.messager.pref.key.login.status";
    public final static String EXTRA_MAP = "com.churc.messager.pref.key.login.map";

    ArrayList<String> contacts = new ArrayList<>();

    String name;
    String server_name;
    String username;
    int temp;

    ServerAPI serverAPI;

    Crypto myCrypto;

    HashMap<String,ServerAPI.UserInfo> myUserMap = new HashMap<>();

    Context context = this;

    private String getServerName(){
        return ((EditText)findViewById(R.id.address)).getText().toString();
    }

    private String getUserName(){
        return ((EditText)findViewById(R.id.name)).getText().toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        myCrypto = new Crypto(getPreferences(Context.MODE_PRIVATE));
        myCrypto.saveKeys(getPreferences(Context.MODE_PRIVATE));

        SharedPreferences sharedPref1 = context.getSharedPreferences(PREF_KEY_ADDR, Context.MODE_PRIVATE);
        String address = sharedPref1.getString(PREF_KEY_ADDR, "129.115.27.54");
        EditText editText1 = (EditText) findViewById(R.id.address);
        editText1.setText(address);

        SharedPreferences sharedPref2 = context.getSharedPreferences(PREF_KEY_NAME, Context.MODE_PRIVATE);
        String name = sharedPref2.getString(PREF_KEY_NAME, "Samuelhan");
        EditText editText2 = (EditText) findViewById(R.id.name);
        editText2.setText(name);


        String key = getPreferences(Context.MODE_PRIVATE).getString("RSAPublicKey","");
        EditText editText3 = (EditText) findViewById(R.id.public_key);
        editText3.setText(key);

    }

    protected void onPause(){
        super.onPause();
        EditText editText1 = (EditText) findViewById(R.id.address);
        String address = editText1.getText().toString();
        SharedPreferences sharedPref1 = context.getSharedPreferences(PREF_KEY_ADDR, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor1 = sharedPref1.edit();
        editor1.putString(PREF_KEY_ADDR, address);
        editor1.commit();

        EditText editText2 = (EditText) findViewById(R.id.name);
        String name = editText2.getText().toString();
        SharedPreferences sharedPref2 = context.getSharedPreferences(PREF_KEY_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor2 = sharedPref2.edit();
        editor2.putString(PREF_KEY_NAME, name);
        editor2.commit();
    }


    public void doLogin(View view) {

        EditText address = (EditText) findViewById(R.id.address);
        server_name= "http://"+address.getText().toString()+":25666";
        EditText user_name = (EditText) findViewById(R.id.name);
        username=user_name.getText().toString();

        DBHandler_C db = new DBHandler_C(context);
        List<contact_sql> contactList = db.getAllcontact();
        for (contact_sql contact_sql : contactList) {
            contacts.add(contact_sql.getname());
        }


        myCrypto = new Crypto(getPreferences(Context.MODE_PRIVATE));
        Observable.just(0)
                .observeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.newThread())
                .flatMap(new GetServerKeyStage(server_name))
                .flatMap(new RegistrationStage(server_name, username,
                        getBase64Image(), myCrypto.getPublicKeyString()))
                .flatMap(new GetChallengeStage(server_name,username,myCrypto))
                .flatMap(new LogInStage(server_name, username))
                .flatMap(new RegisterContactsStage(server_name, username, contacts, context))
                .subscribe(new Observer<Notification>() {
                    @Override
                    public void onCompleted() {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(setting.this,"Logged in!", Toast.LENGTH_SHORT).show();
                                SharedPreferences sharedPref1 = context.getSharedPreferences(LOGIN_STATUS, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor1 = sharedPref1.edit();
                                editor1.putInt(LOGIN_STATUS, 1);
                                editor1.commit();
                                new Handler().postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(setting.this, Main.class);
                                        //intent.putExtra(EXTRA_MAP, myUserMap);
                                        startActivity(intent);
                                        // Main.this.startActivity(new Intent(Main.this, read.class));
                                    }
                                }, 1000);

                            }
                        });

                        Observable.interval(0,1, TimeUnit.SECONDS, Schedulers.newThread())
                                .take(150) // would only poll five times
                                .observeOn(Schedulers.newThread())
                                .subscribeOn(Schedulers.newThread())
                                //   .takeWhile( <predicate> ) // could stop based on a flag variable
                                .flatMap(new HandleNotice(server_name,username,contacts,context, myCrypto))
                                .subscribe(new Observer<Notification>() {
                                    @Override
                                    public void onCompleted() {

                                    }

                                    @Override
                                    public void onError(Throwable e) {}

                                    @Override
                                    public void onNext(Notification notification) {

                                        if(notification instanceof Notification.LogIn) {
                                            Log.d("LOG","User "+((Notification.LogIn)notification).username+" is logged in");
                                            contact.logEvent.send(0);
                                        }
                                        if(notification instanceof Notification.LogOut) {
                                            Log.d("LOG","User "+((Notification.LogOut)notification).username+" is logged out");
                                            contact.logEvent.send(0);

                                        }

                                        if (notification instanceof Notification.Message) {
                                            Log.d("LOG","Message + 1");
                                            Main.mess_Event.send(((Notification.Message)notification).add);
                                            //Log.d("LOG","Message + "+((Notification.Message)notification).add);
                                            //Log.d("LOG",total + " message received!");
                                        }


                                    }


                                })
                        ;




                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Notification notification) {
                        Log.d("LOG","Next "+ notification);
                        if(notification instanceof Notification.LogIn) {
                            Log.d("LOG","User "+((Notification.LogIn)notification).username+" is logged in");

                        }
                        if(notification instanceof Notification.LogOut) {
                            Log.d("LOG","User "+((Notification.LogOut)notification).username+" is logged out");

                        }

                    }
                });




    }

    public void doLogout(View view) {
        EditText address = (EditText) findViewById(R.id.address);
        server_name= "http://"+address.getText().toString()+":25666";
        EditText user_name = (EditText) findViewById(R.id.name);
        username=user_name.getText().toString();
        myCrypto = new Crypto(getPreferences(Context.MODE_PRIVATE));

        Observable.just(0)
                .observeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.newThread())
                .flatMap(new GetServerKeyStage(server_name))
                .flatMap(new RegistrationStage(server_name, username,
                        getBase64Image(), myCrypto.getPublicKeyString()))
                .flatMap(new GetChallengeStage(server_name,username,myCrypto))
                .flatMap(new LogoutStage(server_name,username,myCrypto))
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(String s) {
                        if (s.equals("ok")){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(setting.this,"Logged out!", Toast.LENGTH_SHORT).show();
                                    SharedPreferences sharedPref1 = context.getSharedPreferences(LOGIN_STATUS, Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor1 = sharedPref1.edit();
                                    editor1.putInt(LOGIN_STATUS, 0);
                                    editor1.commit();
                                    new Handler().postDelayed(new Runnable() {

                                        @Override
                                        public void run() {
                                            Intent intent = new Intent(setting.this, Main.class);
                                            //intent.putExtra(EXTRA_MAP, myUserMap);
                                            startActivity(intent);
                                            // Main.this.startActivity(new Intent(Main.this, read.class));
                                        }
                                    }, 1000);

                                }
                            });
                        }
                        else{
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(setting.this,"Logout failed, please try again!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                    }
                });
    }

    String getBase64Image(){
        InputStream is;
        byte[] buffer = new byte[0];
        try {
            is = getAssets().open("images/head.png");
            buffer = new byte[is.available()];
            is.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Base64.encodeToString(buffer,Base64.DEFAULT).trim();
    }
}
