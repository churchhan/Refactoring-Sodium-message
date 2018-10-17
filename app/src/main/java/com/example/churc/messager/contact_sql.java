package com.example.churc.messager;

/**
 * Created by churc on 6/29/2016.
 */
public class contact_sql {
    private int id;
    private String name;
    private String image;
    private String key;
    private int status;
    public contact_sql(int id, String name, String image, String key,int status)
    {
        this.id=id;
        this.name=name;
        this.image=image;
        this.key=key;
        this.status=status;
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setimage(String image) {
        this.image = image;
    }
    public void setkey(String key) {
        this.key = key;
    }
    public void setstatus(int status) {
        this.status = status;
    }
    public int getId() {
        return id;
    }
    public String getname() {
        return name;
    }
    public String getiamge() {
        return image;
    }
    public String getkey() {
        return key;
    }
    public int getstatus() {
        return status;
    }
}
