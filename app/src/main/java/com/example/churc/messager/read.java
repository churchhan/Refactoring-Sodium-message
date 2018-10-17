package com.example.churc.messager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class read extends AppCompatActivity {
    Context context = this;
    int message_row;
    message message_content;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        //SharedPreferences sharedPref = context.getSharedPreferences(Main.CONTENT_KEY, Context.MODE_PRIVATE);
        //String content = sharedPref.getString(Main.CONTENT_KEY, "");
        Intent intent = getIntent();
        //String content = intent.getStringExtra(Main.EXTRA_MESSAGE);

        message_row = intent.getIntExtra("message_row", 0);
        message_content = (message) intent.getParcelableExtra("message_content");

        TextView sender_view = (TextView)findViewById(R.id.sender);
        sender_view.setText(message_content.getsender());

        TextView content_view = (TextView)findViewById(R.id.content);
        content_view.setText(message_content.getcontent());

        TextView subject_view = (TextView)findViewById(R.id.title);
        subject_view.setText(message_content.getsubject());

    }

    public void delete_read(View view) {
        Main.delete_Event.send(message_content);
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
    }

    public void reply(View view) {
        Intent intent = new Intent(this, compose.class);
        Bundle bundle= new Bundle();
        intent.putExtra("message_id", 1);
        bundle.putParcelable("message_content_from_read",message_content);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
