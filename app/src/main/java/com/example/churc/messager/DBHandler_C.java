package com.example.churc.messager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by churc on 6/29/2016.
 */
public class DBHandler_C extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 2;
    // Database Name
    private static final String DATABASE_NAME = "contact";

    private static final String TABLE_CONTACT_LIST = "contact_list";

    // Contact_list Table Columns names
    private static final String KEY_ID_C = "id";
    private static final String KEY_NAME_C = "name";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_KEY = "key";
    private static final String KEY_STATUS = "status";


    public DBHandler_C(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_CONTACTS_TABLE_C = "CREATE TABLE " + TABLE_CONTACT_LIST + "("
                + KEY_ID_C + " INTEGER PRIMARY KEY," + KEY_NAME_C + " TEXT,"
                + KEY_IMAGE + " TEXT," + KEY_KEY + " TEXT," +KEY_STATUS + " INTEGER"+")";
        db.execSQL(CREATE_CONTACTS_TABLE_C);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
// Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACT_LIST);
// Creating tables again
        onCreate(db);
    }


    public void addcontact(contact_sql contact_sql) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME_C, contact_sql.getname());
        values.put(KEY_IMAGE, contact_sql.getiamge());
        values.put(KEY_KEY, contact_sql.getkey());
        values.put(KEY_STATUS, contact_sql.getstatus());
// Inserting Row
        db.insert(TABLE_CONTACT_LIST, null, values);
        db.close(); // Closing database connection
    }

    // Getting one message
    public contact_sql getcontact(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        //Cursor cursor = db.query(TABLE_MESSAGE_LIST, new String[] { KEY_ID,
        //                KEY_SENDER_NAME, KEY_SUBJECT, KEY_CONTENT,KEY_TTL }, KEY_ID + "=?",
        //        new String[] { String.valueOf(id) }, null, null, null, null);
        Cursor cursor = db.rawQuery( "SELECT * FROM " + TABLE_CONTACT_LIST + " LIMIT 1 OFFSET" + "?", new String[] { Integer.toString(id) } );
        if (cursor != null)
            cursor.moveToFirst();
        contact_sql c_one = new contact_sql(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2),cursor.getString(3),Integer.parseInt(cursor.getString(4)));
        //Cursor mess_one = db.rawQuery( "SELECT * FROM " + TABLE_MESSAGE_LIST + " LIMIT 1 OFFSET" + "?", new String[] { Integer.toString(id-1) } );
        db.close();
        cursor.close();
        return c_one;
    }

    public contact_sql getcontactbyid(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CONTACT_LIST, new String[] { KEY_ID_C,
                        KEY_NAME_C, KEY_IMAGE, KEY_KEY, KEY_STATUS }, KEY_ID_C + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        //Cursor cursor = db.rawQuery( "SELECT * FROM " + TABLE_MESSAGE_LIST + " LIMIT 1 OFFSET" + "?", new String[] { Integer.toString(id) } );
        if (cursor != null)
            cursor.moveToFirst();
        contact_sql c_one = new contact_sql(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2),cursor.getString(3),Integer.parseInt(cursor.getString(4)));
        //Cursor mess_one = db.rawQuery( "SELECT * FROM " + TABLE_MESSAGE_LIST + " LIMIT 1 OFFSET" + "?", new String[] { Integer.toString(id-1) } );
        db.close();
        cursor.close();
        return c_one;
    }

    public contact_sql getcontactbyname(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CONTACT_LIST, new String[] { KEY_ID_C,
                        KEY_NAME_C, KEY_IMAGE, KEY_KEY, KEY_STATUS }, KEY_NAME_C + "=?",
                new String[] {name}, null, null, null, null);
        //Cursor cursor = db.rawQuery( "SELECT * FROM " + TABLE_MESSAGE_LIST + " LIMIT 1 OFFSET" + "?", new String[] { Integer.toString(id) } );
        if (cursor != null)
            cursor.moveToFirst();
        contact_sql c_one = new contact_sql(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2),cursor.getString(3),Integer.parseInt(cursor.getString(4)));
        //Cursor mess_one = db.rawQuery( "SELECT * FROM " + TABLE_MESSAGE_LIST + " LIMIT 1 OFFSET" + "?", new String[] { Integer.toString(id-1) } );
        db.close();
        cursor.close();
        return c_one;
    }

    // Getting All Shops
    public List<contact_sql> getAllcontact() {
        List<contact_sql> contactList = new ArrayList<contact_sql>();
// Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_CONTACT_LIST;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
// looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                contact_sql contact_sql = new contact_sql(Integer.parseInt(cursor.getString(0)),
                        cursor.getString(1), cursor.getString(2),cursor.getString(3),Integer.parseInt(cursor.getString(4)));
                contact_sql.setId(Integer.parseInt(cursor.getString(0)));
                contact_sql.setName(cursor.getString(1));
                contact_sql.setimage(cursor.getString(2));
                contact_sql.setkey(cursor.getString(3));
// Adding contact to list
                contactList.add(contact_sql);
            } while (cursor.moveToNext());
        }
// return contact list
        db.close();
        cursor.close();
        return contactList;
    }



    public int updatecontactlistbyname(String name, String image, String key) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME_C, name);
        values.put(KEY_IMAGE, image);
        values.put(KEY_KEY, key);
// updating row
        return db.update(TABLE_CONTACT_LIST, values, KEY_NAME_C + " = ?",
                new String[]{name});
    }

    public int updatecontactstatus(String name, Integer status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_STATUS, status);
// updating row
        int result = db.update(TABLE_CONTACT_LIST, values, KEY_NAME_C + " = ?",
                new String[]{name});
        db.close();
        return  result;
    }


    // Deleting a message
    public void deletecontact(contact_sql contact_sql) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACT_LIST, KEY_ID_C + " = ?",
                new String[] { String.valueOf(contact_sql.getId()) });
        db.close();
    }


    @Override
    public void finalize() throws Throwable {
        this.close();
        super.finalize();
    }
}
