package com.offcn.utils;

/**
 * @description: TODO
 * @author: LongChen
 * @date: 2022/7/18 17:06
 * @version: 1.0
 */

public class Test {

    public static void main(String[] args) {
        String phone="17356407847";
        if(!PhoneFormatCheckUtils.isPhoneLegal(phone)){
            System.out.println("手机号不正确");
        }else {
            System.out.println("手机号正确");
        }

    }
}
