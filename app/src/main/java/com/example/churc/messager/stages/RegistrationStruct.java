package com.example.churc.messager.stages;

/**
 * Created by churc on 8/10/2016.
 */
public class RegistrationStruct {
    final public String server;
    final public String username;
    final public String base64Image;
    final public String keyString;

    public RegistrationStruct(String server,
                              String username,
                              String base64Image,
                              String keyString){
        this.server = server;
        this.username = username;
        this.base64Image = base64Image;
        this.keyString = keyString;
    }
}
