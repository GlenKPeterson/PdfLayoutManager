package com.planbase.pdf.layoutmanager;

import java.awt.Color;

/**
 * Holds utility functions.
 */
final class Utils {
    private Utils() { throw new UnsupportedOperationException("No instances!"); }

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

    public static boolean equals(Object o1, Object o2) {
        return (o1 == o2) ||
                ((o1 != null) && o1.equals(o2));
    }

}
