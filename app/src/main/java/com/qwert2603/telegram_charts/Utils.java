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

    public static int floorToPowerOf2(int i) {
        int m = 0;
        while (i > 1) {
            i >>= 1;
            ++m;
        }
        for (int q = 0; q < m; q++) {
            i <<= 1;
        }
        return i;
    }

    public static String formatY(int y) {
        if (y < 1_000) {
            return formatYLessThousand(y);
        } else if (y < 1_000_000) {
            final int q = y / 100;
            if (FORMATTED_CACHE_K[q] != null) return FORMATTED_CACHE_K[q];
            final int div = y % 1_000 / 100;
            final String formatted = formatYLessThousand(y / 1_000) + "." + formatYLessThousand(div) + "K";
            FORMATTED_CACHE_K[q] = formatted;
            return formatted;
        } else {
            final int q = y / 100_000;
            if (FORMATTED_CACHE_M[q] != null) return FORMATTED_CACHE_M[q];
            final int div = y % 1_000_000 / 100_000;
            final String formatted = formatYLessThousand(y / 1_000_000) + "." + formatYLessThousand(div) + "M";
            FORMATTED_CACHE_M[q] = formatted;
            return formatted;
        }
    }

    private static String formatYLessThousand(int y) {
        if (FORMATTED_CACHE[y] != null) return FORMATTED_CACHE[y];
        final String formatted = Integer.toString(y);
        FORMATTED_CACHE[y] = formatted;
        return formatted;
    }

    private static final String[] FORMATTED_CACHE = new String[1000];
    private static final String[] FORMATTED_CACHE_K = new String[10000];
    private static final String[] FORMATTED_CACHE_M = new String[10000];

    public static String toStringWithSpaces(int y) {
        if (y == 0) return "0";

        StringBuilder stringBuilder = new StringBuilder();
        int q = 0;
        while (y > 0) {
            stringBuilder.append(y % 10);
            y /= 10;
            ++q;
            if (q == 3) {
                q = 0;
                stringBuilder.append(' ');
            }
        }
        return stringBuilder.reverse().toString();
    }
}
