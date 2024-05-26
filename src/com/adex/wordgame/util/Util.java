package com.adex.wordgame.util;

public class Util {

    public static byte[] convertByteArray(Byte[] old) {
        byte[] b = new byte[old.length];
        for (int i = 0; i < old.length; i++) {
            b[i] = old[i];
        }
        return b;
    }

}
