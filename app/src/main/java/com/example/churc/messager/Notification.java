package com.example.churc.messager;

/**
 * Created by churc on 8/10/2016.
 */
public class Notification {
    public static class LogIn extends Notification {
        public final String username;
        public LogIn(String username){this.username = username;}
    }
    public static class LogOut extends Notification {
        public final String username;
        public LogOut(String username){this.username = username;}
    }
    public static class Message extends Notification {
        public final message add;
        public Message(message add){this.add = add;}
    }
}
