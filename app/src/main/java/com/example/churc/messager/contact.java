package com.example.churc.messager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.StringWriter;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import nz.sodium.Cell;
import nz.sodium.CellLoop;
import nz.sodium.CellSink;
import nz.sodium.Lambda2;
import nz.sodium.Lambda3;
import nz.sodium.Stream;
import nz.sodium.StreamSink;
import nz.sodium.Transaction;
import nz.sodium.Unit;

public class contact extends AppCompatActivity {
    private static String DEBUG = "CryptoTestMain";

    public final static String CONTACT_NAME = "com.churc.message.contact.name";
    public final static String PUBLIC_KEY = "com.churc.message.public.key";

    public final static String LOGIN_STATUS = "com.churc.messager.pref.key.login.status";
    public final StringWriter writer = new StringWriter();
    Context context=this;

    CellLoop<Integer>            N;
    public static StreamSink<Integer>    logEvent = new StreamSink<>();

    List<contact_sql> contactList;
    ArrayList<String> list;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        SharedPreferences loginstatus = context.getSharedPreferences(LOGIN_STATUS, Context.MODE_PRIVATE);
        Integer result = loginstatus.getInt(LOGIN_STATUS, 0);


        if (result==1) {


        final DBHandler_C db = new DBHandler_C(this);

        contactList = db.getAllcontact();

        list = new ArrayList<String>();

        for (contact_sql contact_sql : contactList) {
            list.add(contact_sql.getname());
        }


        //instantiate custom adapter
        MyCustomAdapter adapter = new MyCustomAdapter(list, this);

        //handle listview and assign adapter
        final ListView lView = (ListView)findViewById(R.id.list_contact);
        lView.setAdapter(adapter);


        lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                ListView lView = (ListView) findViewById(R.id.list_contact);

                // ListView Clicked item index
                int itemPosition     = position;

                // ListView Clicked item value
                final String  itemValue    = (String) lView.getItemAtPosition(position);

                // Show Alert
                Toast.makeText(getApplicationContext(),
                        "Position :"+itemPosition+"  ListItem : " +itemValue , Toast.LENGTH_LONG)
                        .show();

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        Intent intent = new Intent(contact.this, compose.class);
                        intent.putExtra(CONTACT_NAME, itemValue);
                        startActivity(intent);

                        // Main.this.startActivity(new Intent(Main.this, read.class));
                    }
                }, 50);

            }

        });

            Transaction.runVoid(new Runnable() {
                @Override
                public void run() {
                    // define your reactive network here
                    N = new CellLoop<>();

                    Stream<Integer> logrecord = logEvent.snapshot(N, new Lambda2<Integer, Integer, Integer>() {
                        @Override
                        public Integer apply(Integer unit, Integer old_value) {
                            return old_value+1;
                        }
                    });
                    Cell<Integer> just_used = logrecord.hold(0);
                    N.loop(just_used);
                }
            });

            N.listen(new nz.sodium.Handler<Integer>() {
                @Override
                public void run(Integer value) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            List<contact_sql> temp_list = db.getAllcontact();
                            ArrayList<String> list = new ArrayList<String>();
                            for (contact_sql contact_sql : temp_list) {
                                list.add(contact_sql.getname());
                            }
                            MyCustomAdapter adapter = new MyCustomAdapter(list, context);
                            lView.setAdapter(adapter);
                        }
                    });

                }
            });


        }


        else{
            TextView textView = new TextView(this);
            textView.setTextSize(30);
            textView.setText("Please Login in First!!");

            LinearLayout layout = (LinearLayout) findViewById(R.id.content_contact);
            layout.addView(textView);
        }

        //Click end
    }


    public void add_contact(View view) {
        Intent intent = new Intent(this, add_contact.class);
        startActivity(intent);
    }


    public void main(View view) {
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
    }

    public void contacts(View view) {
        Intent intent = new Intent(this, contact.class);
        startActivity(intent);
    }

    public void composes(View view) {
        Intent intent = new Intent(this, compose.class);
        startActivity(intent);
    }
}
