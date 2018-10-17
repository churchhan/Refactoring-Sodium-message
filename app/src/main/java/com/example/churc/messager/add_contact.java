package com.example.churc.messager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.churc.messager.stages.GetUserinfoStage;
import com.example.churc.messager.stages.RegisterContactsStage;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import rx.Observer;
import rx.schedulers.Schedulers;

public class add_contact extends AppCompatActivity {
    public final static String PREF_KEY_ADDR = "com.churc.messager.pref.key.addr";
    public final static String PREF_KEY_NAME = "com.churc.messager.pref.key.name";
    Context context=this;
    String server_name;
    ArrayList<String> contacts = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
    }


    public void delete_contact(View view) {
        Intent intent = new Intent(this, contact.class);
        startActivity(intent);
    }

    public void save_contact(View view) {
        EditText name_edit = (EditText) findViewById(R.id.new_name);
        String name_string = name_edit.getText().toString();
        TextView key_edit = (TextView) findViewById(R.id.public_key);
        String key_string = key_edit.getText().toString();
        int status = 0;
        ImageView iv1 = (ImageView)findViewById(R.id.imageView);
        iv1.buildDrawingCache();
        Bitmap bitmap = iv1.getDrawingCache();

        ByteArrayOutputStream stream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
        byte[] image=stream.toByteArray();

        String img_str = Base64.encodeToString(image, 0);
        DBHandler_C db = new DBHandler_C(this);
        db.addcontact(new contact_sql(0,name_string, img_str, key_string, status));

        SharedPreferences sharedPref1 = context.getSharedPreferences(PREF_KEY_ADDR, Context.MODE_PRIVATE);
        String address = sharedPref1.getString(PREF_KEY_ADDR, "129.115.27.54");
        SharedPreferences sharedPref2 = context.getSharedPreferences(PREF_KEY_NAME, Context.MODE_PRIVATE);
        String username = sharedPref2.getString(PREF_KEY_ADDR, "Samuelhan");
        server_name= "http://"+address+":25666";

        List<contact_sql> contactList = db.getAllcontact();
        for (contact_sql contact_sql : contactList) {
            contacts.add(contact_sql.getname());
        }

        rx.Observable.just("")
                .observeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.newThread())
                .flatMap(new RegisterContactsStage(server_name,username,contacts,context))
                .subscribe(new Observer<Notification>() {
                    @Override
                    public void onCompleted() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(add_contact.this,"New Contact was added successfully!", Toast.LENGTH_SHORT).show();

                                new Handler().postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(add_contact.this, contact.class);
                                        //intent.putExtra(EXTRA_MAP, myUserMap);
                                        startActivity(intent);
                                        // Main.this.startActivity(new Intent(Main.this, read.class));
                                    }
                                }, 1000);

                            }
                        });
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Notification notification) {

                    }
                });

    }

    public void search(View view){
        SharedPreferences sharedPref1 = context.getSharedPreferences(PREF_KEY_ADDR, Context.MODE_PRIVATE);
        String address = sharedPref1.getString(PREF_KEY_ADDR, "129.115.27.54");
        EditText name_search = (EditText)findViewById(R.id.new_name);
        String search_name = name_search.getText().toString();
        server_name= "http://"+address+":25666";

        rx.Observable.just(search_name)
                .observeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.newThread())
                .flatMap(new GetUserinfoStage(server_name,search_name))
                .subscribe(new Observer<JSONObject>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        try {
                            String status = jsonObject.getString("status");
                            if (status.equals("ok")){
                                String image = jsonObject.getString("image");
                                final String key = jsonObject.getString("key");
                                final Bitmap display_image = BitmapFactory.decodeByteArray(Base64.decode(image, Base64.DEFAULT), 0, Base64.decode(image, Base64.DEFAULT).length);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((ImageView)findViewById(R.id.imageView)).setImageBitmap(display_image);
                                        ((TextView)findViewById(R.id.public_key)).setText(key);
                                    }
                                });
                            }
                            else{
                                Toast.makeText(add_contact.this,"Could not find the User, please input another name!", Toast.LENGTH_SHORT).show();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }




                });
    }
}
