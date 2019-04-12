package com.qwert2603.telegram_charts;

public class Utils {

    public static int commonEnd(String prev, String current) {
        int common = 0;
        while (common < current.length()) {
            if (current.charAt(current.length() - common - 1) == prev.charAt(prev.length() - common - 1)) {
                ++common;
            } else {
                break;
            }
        }

        if (common != current.length()) {
            while (common > 0 && current.charAt(current.length() - common - 1) != ' ') {
                --common;
            }
        }

        return common;
    }
}
