package com.example.churc.messager;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by churc on 6/21/2016.
 */
public class MyCustomAdapter extends BaseAdapter implements ListAdapter {
    private ArrayList<String> list = new ArrayList<String>();
    private Context context;



    public MyCustomAdapter(ArrayList<String> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int pos) {
        return list.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return 0;
        //return list.get(pos).getId();
        //just return 0 if your list items do not have an Id variable.
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.contact_list, null);
        }

        //Handle TextView and display string from your list
        TextView listItemText = (TextView)view.findViewById(R.id.list_contact_string);
        listItemText.setText(list.get(position));
        listItemText.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, compose.class);
                intent.putExtra("contact_row", position);
                context.startActivity(intent);

                //context.startActivity(new Intent(context, compose.class));


            }
        });

        final DBHandler_C db = new DBHandler_C(context);
        contact_sql contact_sn= db.getcontactbyname(list.get(position));
        TextView contact_status = (TextView)view.findViewById(R.id.status);

        if (contact_sn.getstatus()== 1){contact_status.setText("IN");}
        else {contact_status.setText("OUT");}

        //Handle buttons and add onClickListeners
        ImageButton setting = (ImageButton)view.findViewById(R.id.setting);


        setting.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, contact_detail.class);
                intent.putExtra("contact_row", position);
                context.startActivity(intent);

                    //context.startActivity(new Intent(context, add_contact.class));


            }
        });



        return view;
    }
}

