package com.planbase.pdf.layoutmanager;

import java.awt.Color;

public class Utils {
    public static String toString(Color c) {
        if (c == null) { return "null"; }
        return new StringBuilder("#").append(twoDigitHex(c.getRed()))
                .append(twoDigitHex(c.getGreen()))
                .append(twoDigitHex(c.getBlue())).toString();
    }
    public static String twoDigitHex(int i) {
        String h = Integer.toHexString(i);
        return (h.length() < 2) ? "0" + h : h;
    }
//    public static void println(CharSequence cs) { System.out.println(cs); }
}
