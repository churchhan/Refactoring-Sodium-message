package com.example.churc.messager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by churc on 6/28/2016.
 */
public class DBHandler extends SQLiteOpenHelper{
    // Database Version
    private static final int DATABASE_VERSION = 4;
    // Database Name
    private static final String DATABASE_NAME = "messager";
    // Contacts table name
    private static final String TABLE_MESSAGE_LIST = "message_list";
    // Message_list Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_SENDER_NAME = "sender_name";
    private static final String KEY_SUBJECT = "subject";
    private static final String KEY_CONTENT = "message_content";
    private static final String KEY_BORN = "born";
    private static final String KEY_TTL = "ttl";


    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_MESSAGE_LIST + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_SENDER_NAME + " TEXT,"
                + KEY_SUBJECT + " TEXT," + KEY_CONTENT + " TEXT," + KEY_BORN + " LONG,"+KEY_TTL + " LONG" +")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
// Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGE_LIST);
// Creating tables again
        onCreate(db);
    }

    // Adding new shop
    public void addmessage(message message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_SENDER_NAME, message.getsender());
        values.put(KEY_SUBJECT, message.getsubject());
        values.put(KEY_CONTENT, message.getcontent());
        values.put(KEY_BORN, message.getborn());
        values.put(KEY_TTL, message.getttl());
// Inserting Row
        db.insert(TABLE_MESSAGE_LIST, null, values);
        db.close(); // Closing database connection
    }

    // Getting one message
    public message getmessage(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        //Cursor cursor = db.query(TABLE_MESSAGE_LIST, new String[] { KEY_ID,
        //                KEY_SENDER_NAME, KEY_SUBJECT, KEY_CONTENT,KEY_TTL }, KEY_ID + "=?",
        //        new String[] { String.valueOf(id) }, null, null, null, null);
        Cursor cursor = db.rawQuery( "SELECT * FROM " + TABLE_MESSAGE_LIST + " LIMIT 1 OFFSET" + "?", new String[] { Integer.toString(id) } );
        if (cursor != null)
            cursor.moveToFirst();
        message mess_one = new message(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2),cursor.getString(3),Long.parseLong(cursor.getString(4)),Long.parseLong(cursor.getString(5)));
        //Cursor mess_one = db.rawQuery( "SELECT * FROM " + TABLE_MESSAGE_LIST + " LIMIT 1 OFFSET" + "?", new String[] { Integer.toString(id-1) } );
        cursor.close();
        db.close();
        return mess_one;
    }

    public message getmessagebyid(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MESSAGE_LIST, new String[] { KEY_ID,
                        KEY_SENDER_NAME, KEY_SUBJECT, KEY_CONTENT,KEY_BORN,KEY_TTL }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        //Cursor cursor = db.rawQuery( "SELECT * FROM " + TABLE_MESSAGE_LIST + " LIMIT 1 OFFSET" + "?", new String[] { Integer.toString(id) } );
        if (cursor != null)
            cursor.moveToFirst();
        message mess_one = new message(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2),cursor.getString(3),Long.parseLong(cursor.getString(4)),Long.parseLong(cursor.getString(5)));
        //Cursor mess_one = db.rawQuery( "SELECT * FROM " + TABLE_MESSAGE_LIST + " LIMIT 1 OFFSET" + "?", new String[] { Integer.toString(id-1) } );
        cursor.close();
        db.close();
        return mess_one;
    }

    // Getting All Shops
    public List<message> getAllmessage() {
        List<message> messageList = new ArrayList<message>();
// Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_MESSAGE_LIST;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
// looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                message message = new message(Integer.parseInt(cursor.getString(0)),
                        cursor.getString(1), cursor.getString(2),cursor.getString(3),Long.parseLong(cursor.getString(4)),Long.parseLong(cursor.getString(5)));
                message.setId(Integer.parseInt(cursor.getString(0)));
                message.setName(cursor.getString(1));
                message.setsubject(cursor.getString(2));
                message.setcontent(cursor.getString(3));
                message.setborn(Long.parseLong(cursor.getString(4)));
                message.setttl(Long.parseLong(cursor.getString(5)));
// Adding contact to list
                messageList.add(message);
            } while (cursor.moveToNext());
        }
// return contact list
        cursor.close();
        db.close();
        return messageList;
    }


    // Getting shops Count
    public int getmessagelistCount() {
        String countQuery = "SELECT * FROM " + TABLE_MESSAGE_LIST;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
// return count
        db.close();
        int result = cursor.getCount();
        cursor.close();
        return result;
    }

    public void deleteall() {
        String countQuery = "DELETE FROM " + TABLE_MESSAGE_LIST;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
// return count
        db.close();
        cursor.close();
    }


    public int updatemessagelist(message message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_SENDER_NAME, message.getsender());
        values.put(KEY_SUBJECT, message.getsubject());
        values.put(KEY_CONTENT, message.getcontent());
        values.put(KEY_BORN,message.getborn());
        values.put(KEY_TTL, message.getttl());
// updating row
        return db.update(TABLE_MESSAGE_LIST, values, KEY_ID + " = ?",
                new String[]{String.valueOf(message.getId())});
    }


    public void deletemessage(message message) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MESSAGE_LIST, KEY_ID + " = ?",
                new String[] { String.valueOf(message.getId()) });
        db.close();
    }

    public void deletemessagebyttl(long ttl) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MESSAGE_LIST, KEY_TTL + " = ?",
                new String[] { String.valueOf(ttl) });
        db.close();
    }

    public void deletemessagebyid(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MESSAGE_LIST, KEY_ID + " = ?",
                new String[] { String.valueOf(id) });
        db.close();
    }

    @Override
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }



}
