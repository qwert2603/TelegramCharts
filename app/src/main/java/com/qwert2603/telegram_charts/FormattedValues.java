package com.qwert2603.telegram_charts;

public class FormattedValues {

    public static final int[] STEPS = {
            1, 2, 5,
            10, 20, 50,
            100, 200, 500,
            1000, 2000, 5000,
            10000, 20000, 50000,
            100000, 200000, 500000,
            1000000, 2000000, 5000000,
            10000000, 20000000, 50000000,
            100000000, 200000000, 500000000,
    };

    public static final String[][] FORMATTED_VALUES;

    private static final String ZERO = "0";

    static {
        FORMATTED_VALUES = new String[STEPS.length][ 3];
        for (int i = 0; i < STEPS.length; i++) {
            FORMATTED_VALUES[i][0] = ZERO;
            for (int j = 1; j < 3; j++) {
                int value = STEPS[i] * j;
                String formatted;
                if (value < 1000) {
                    formatted = Integer.toString(value);
                } else if (value < 1000000) {
                    formatted = Integer.toString(value / 1000) + "K";
                } else {
                    formatted = Integer.toString(value / 1000000) + "M";
                }
                FORMATTED_VALUES[i][j] = formatted;
            }
        }
    }

}
