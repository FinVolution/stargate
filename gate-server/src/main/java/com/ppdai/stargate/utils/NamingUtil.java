package com.ppdai.stargate.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NamingUtil {

    public static final String NOT_READY_FLOW = "NOT_READY_FLOW";

    public static String getServiceFromGroup(String group) {
        String service =  group.replaceAll("\\.", "--");
        if (Character.isDigit(group.charAt(0))) {
            return "s" + service;
        }

        return service;
    }

    public static String getServiceFromAppName(String appName) {
        String service = appName.replaceAll("\\.", "-");
        if (Character.isDigit(appName.charAt(0))) {
            return "s" + service;
        }

        return service;
    }

    public static String formatNamespace(String department) {
        return department.toLowerCase();
    }
}
