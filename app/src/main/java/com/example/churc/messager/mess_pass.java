package com.example.churc.messager;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by churc on 6/28/2016.
 */
public class mess_pass implements Parcelable {
    private int id;
    private String sender_name;
    private String subject;
    private String message_content;
    private long born;
    private long ttl;

    public mess_pass()
    {
    }

    public int describeContents(){
        return 0;
    }

    public void writeToParcel(Parcel pc, int flags) {
        pc.writeInt(id);
        pc.writeString(sender_name);
        pc.writeString(subject);
        pc.writeString(message_content);
        pc.writeLong(born);
        pc.writeLong(ttl);
    }

    public static final Parcelable.Creator<mess_pass> CREATOR = new Parcelable.Creator<mess_pass>() {
        public mess_pass createFromParcel(Parcel pc) {
            return new mess_pass(pc);
        }
        public mess_pass[] newArray(int size) {
            return new mess_pass[size];
        }
    };

    public mess_pass(Parcel pc){
        id         = pc.readInt();
        sender_name        =  pc.readString();
        subject        =  pc.readString();
        message_content = pc.readString();
        born = pc.readLong();
        ttl = pc.readLong();
    }


    public void setId(int id) {
        this.id = id;
    }
    public void setName(String sender_name) {
        this.sender_name = sender_name;
    }

    public void setsubject(String subject) {
        this.subject = subject;
    }
    public void setcontent(String message_content) {
        this.message_content = message_content;
    }
    public void setttl(long ttl) {
        this.ttl = ttl;
    }
    public void setborn(long born) {
        this.born = born;
    }
    public int getId() {
        return id;
    }
    public String getsender() {
        return sender_name;
    }
    public String getsubject() {
        return subject;
    }
    public String getcontent() {
        return message_content;
    }
    public long getttl() {
        return ttl;
    }
    public long getborn() {
        return born;
    }
}

