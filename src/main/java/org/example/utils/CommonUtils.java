package org.example.utils;

import org.example.exception.BaseException;

public class CommonUtils {
    @FunctionalInterface
    public interface Checker {
        boolean check(); //true false
    }

    public static void throwIf(Checker checker, BaseException ex) {
        if (checker.check()) {
            throw ex;
        }
    }
    //ternary operatorunun daha təkmilləşdirilmiş forması
    //true gəlsə exception atılır
    //false gəlsə davam edirik
}