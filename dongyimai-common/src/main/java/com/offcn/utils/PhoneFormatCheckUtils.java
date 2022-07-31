package com.offcn.utils;

public class PhoneFormatCheckUtils {
    public static boolean isPhoneLegal(String phone) {
        String regex = "[1][3-9]\\d{9}";
        return phone.matches(regex);
    }
}