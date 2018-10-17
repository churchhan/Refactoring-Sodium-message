package com.example.churc.messager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.churc.messager.stages.RegisterContactsStage;

import java.util.ArrayList;
import java.util.List;

import rx.Observer;
import rx.schedulers.Schedulers;

public class contact_detail extends AppCompatActivity {
    public final static String PREF_KEY_ADDR = "com.churc.messager.pref.key.addr";
    public final static String PREF_KEY_NAME = "com.churc.messager.pref.key.name";
    Context context=this;
    String server_name;
    ArrayList<String> contacts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);
        Intent intent = getIntent();
        int contact_row = intent.getIntExtra("contact_row", 0);
        DBHandler_C db = new DBHandler_C(this);
        contact_sql contact_sql= db.getcontact(contact_row);
        String image =  contact_sql.getiamge();
        Bitmap display_image = BitmapFactory.decodeByteArray(Base64.decode(image, Base64.DEFAULT), 0, Base64.decode(image, Base64.DEFAULT).length);
        ((ImageView)findViewById(R.id.imageView)).setImageBitmap(display_image);
        ((TextView)findViewById(R.id.contact_name)).setText(contact_sql.getname());
        ((TextView)findViewById(R.id.public_key)).setText(contact_sql.getkey());
    }

    public void delete_contact(View view) {
        Intent intent1 = getIntent();
        int contact_row = intent1.getIntExtra("contact_row", 0);
        DBHandler_C db = new DBHandler_C(this);
        contact_sql contact_one = db.getcontact(contact_row);
        db.deletecontact(contact_one);


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
                                Toast.makeText(contact_detail.this,"Contact was deleted successfully!", Toast.LENGTH_SHORT).show();

                                new Handler().postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(contact_detail.this, contact.class);
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
}
