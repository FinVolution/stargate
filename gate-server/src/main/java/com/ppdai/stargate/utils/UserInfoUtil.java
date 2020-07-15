package com.ppdai.stargate.utils;

public class UserInfoUtil {
    static private ThreadLocal<String> usernameTL = new ThreadLocal<>();

    static public void setUsername(String username) {
        usernameTL.set(username);
    }

    static public String getUsername() {
        return usernameTL.get();
    }
}
