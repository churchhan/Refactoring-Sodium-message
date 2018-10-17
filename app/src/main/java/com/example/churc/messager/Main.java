package com.example.churc.messager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nz.sodium.Cell;
import nz.sodium.CellLoop;
import nz.sodium.Lambda2;
import nz.sodium.Stream;
import nz.sodium.StreamSink;
import nz.sodium.Transaction;

public class Main extends AppCompatActivity {
    public final static String CONTENT_KEY = "com.churc.message.content.key";
    public final static String EXTRA_MESSAGE = "com.churc.message.extra.message";
    public final static String LOGIN_STATUS = "com.churc.messager.pref.key.login.status";
    public final static String EXTRA_MAP = "com.churc.messager.pref.key.login.map";
    public final static String PREF_KEY_ADDR = "com.churc.messager.pref.key.addr";
    public final static String PREF_KEY_NAME = "com.churc.messager.pref.key.name";
    private static String LOG       = "Message";
    Context context=this;


    CellLoop<List<message>> Mess_list_temp;
    public static StreamSink<message> mess_Event = new StreamSink<>();
    public static StreamSink<message> delete_Event = new StreamSink<>();
    List<message> messageList;


    public final DBHandler db = new DBHandler(context);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences loginstatus = context.getSharedPreferences(LOGIN_STATUS, Context.MODE_PRIVATE);
        Integer result = loginstatus.getInt(LOGIN_STATUS, 0);
        SharedPreferences sharedPref1 = context.getSharedPreferences(PREF_KEY_ADDR, Context.MODE_PRIVATE);
        final String address = sharedPref1.getString(PREF_KEY_ADDR, "129.115.27.54");
        SharedPreferences sharedPref2 = context.getSharedPreferences(PREF_KEY_NAME, Context.MODE_PRIVATE);
        final String name = sharedPref2.getString(PREF_KEY_NAME, "Samuelhan");
        final ListView listView = (ListView) findViewById(R.id.list_view);





        if (result==1) {
            //db.addmessage(new message(0,"alice","test","test",System.currentTimeMillis(),15000));

            List<message> temp_list = db.getAllmessage();
            long current_time = System.currentTimeMillis();
            long result1 = 0L;

            for (message message : temp_list) {
                result1=current_time-message.getborn();
                if(result1>message.getttl()){db.deletemessagebyid(message.getId());}
            }

            messageList = db.getAllmessage();

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1);
            for (message message : messageList) {
                adapter.add("Sender: " + message.getsender()+ " , TTL: " + (message.getttl()/1000) + "s");
            }
            listView.setAdapter(adapter);

            // ListView Item Click Listener
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    ListView listView = (ListView) findViewById(R.id.list_view);

                    // ListView Clicked item index
                    final int itemPosition     = position;

                    // ListView Clicked item value
                    final String  itemValue    = (String) listView.getItemAtPosition(position);

                    SharedPreferences sharedPref = context.getSharedPreferences(CONTENT_KEY, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt(CONTENT_KEY,itemPosition);
                    editor.commit();

                    // Show Alert
                    //Toast.makeText(getApplicationContext(),
                    //       "Position :"+itemPosition+"  ListItem : " +itemValue , Toast.LENGTH_LONG)
                    //        .show();

                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            Intent intent = new Intent(Main.this, read.class);
                            Bundle bundle = new Bundle();
                            //intent.putExtra("message_row", itemPosition);
                            bundle.putParcelable("message_content",messageList.get(itemPosition));
                            intent.putExtras(bundle);
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
                    Mess_list_temp = new CellLoop<>();

                    Stream<List<message>> list_after_received = mess_Event.snapshot(Mess_list_temp, new Lambda2<message, List<message>, List<message>>() {
                        @Override
                        public List<message> apply(message received, List<message> old_value) {

                            old_value.add(received);

                            return old_value;
                        }
                    });

                    Stream<List<message>> list_after_delete = delete_Event.snapshot(Mess_list_temp, new Lambda2<message, List<message>, List<message>>() {
                        @Override
                        public List<message> apply(message received, List<message> old_value) {
                            int position = -1;
                            message compared_one;
                            List<message> after_delete= new ArrayList<message>();
                            for(int l=0;l<old_value.size();l++){
                                compared_one=old_value.get(l);
                                if(compared_one.getborn()==received.getborn() && compared_one.getcontent().equals(received.getcontent()) && compared_one.getsender().equals(received.getsender())){
                                    old_value.remove(received);
                                    Log.d(LOG,"message position:"+l+" was deleted!");
                                }
                                else{
                                    after_delete.add(old_value.get(l));
                                }
                            }

                            return after_delete;
                        }
                    });

                    Stream<List<message>> list_merged = list_after_received.merge(list_after_delete,
                            new Lambda2<List<message>, List<message>, List<message>>() {
                                @Override
                                public List<message> apply(List<message> inc, List<message> dec) {
                                    return inc;
                                }
                            });

                    Cell<List<message>> just_used2 = list_merged.hold(messageList);

                    long current = System.currentTimeMillis();
                    Cell<Long> Current_time = new Cell<>(current);

                    Cell<List<message>> final_list= just_used2.lift(Current_time,new Lambda2<List<message>, Long, List<message>>() {
                        @Override
                        public List<message> apply(List<message> list, Long current) {
                            message temp_used;
                            if (list.size() == 0) {
                                return list;
                            }
                            else {
                                List<message> final_one = new ArrayList<message>();
                                long current1 = System.currentTimeMillis();
                                for (Integer k=0;k<list.size();k++) {
                                    temp_used = list.get(k);
                                    Long result_test=current1-temp_used.getborn();
                                    String temp10= String.valueOf(result_test);

                                    if(result_test<temp_used.getttl()){
                                        final_one.add(temp_used);}
                                        Log.d(LOG,"result:"+temp10+", ttl:"+ String.valueOf(temp_used.getttl())+", born:"+ String.valueOf(temp_used.getborn()));
                                }
                                return  final_one;
                            }
                        }
                    });

                    Mess_list_temp.loop(final_list);
                }
            });

            Mess_list_temp.listen(new nz.sodium.Handler<List<message>>() {
                @Override
                public void run(final List<message> value) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                                    android.R.layout.simple_list_item_1);

                            for (message message : value) {
                                adapter.add("Sender: " + message.getsender()+ " , TTL: " + (message.getttl()/1000) + "s");
                            }

                            listView.setAdapter(adapter);
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                                @Override
                                public void onItemClick(AdapterView<?> parent, View view,
                                                        int position, long id) {

                                    ListView listView = (ListView) findViewById(R.id.list_view);

                                    // ListView Clicked item index
                                    final int itemPosition     = position;


                                    // Show Alert
                                    //Toast.makeText(getApplicationContext(),
                                    //       "Position :"+itemPosition+"  ListItem : " +itemValue , Toast.LENGTH_LONG)
                                    //        .show();

                                    new Handler().postDelayed(new Runnable() {

                                        @Override
                                        public void run() {
                                            Intent intent = new Intent(Main.this, read.class);
                                            Bundle bundle = new Bundle();
                                            //intent.putExtra("message_row", itemPosition);
                                            bundle.putParcelable("message_content",value.get(itemPosition));
                                            intent.putExtras(bundle);
                                            startActivity(intent);

                                            // Main.this.startActivity(new Intent(Main.this, read.class));
                                        }
                                    }, 50);

                                }

                            });
                        }
                    });

                }
            });

        }

        else {
            TextView textView = new TextView(this);
            textView.setTextSize(30);
            textView.setText("Please Login in First!!");


            LinearLayout layout = (LinearLayout) findViewById(R.id.content_main);
            layout.addView(textView);
        }

    }



    public void settings(View view) {
        Intent intent = new Intent(this, setting.class);
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

    public void add_test_message(View view) {
        Intent refresh =new Intent(this, Main.class);
        startActivity(refresh);
    }

    public void refresh(View view) {
        Intent refresh =new Intent(this, Main.class);
        startActivity(refresh);
    }
}
