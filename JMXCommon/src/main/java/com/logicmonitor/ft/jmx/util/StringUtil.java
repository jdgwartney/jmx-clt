package com.logicmonitor.ft.jmx.util;


import java.util.StringTokenizer;

public class StringUtil {
    public static final String WHITE_SPACES = " \r\n\t\u3000\u00A0\u2007\u202F";

    public static String[] split(String str, String delims) {
        return split(str, delims, false);
    }

    /**
     * Split "str" by run of delimiters and return first num of substr.
     */
    public static String[] split(String str, String delims, int num) {
        if (num <= 0) {
            return null;
        }
        StringTokenizer tokenizer = new StringTokenizer(str, delims);
        int n = tokenizer.countTokens();
        int retNum = num < n ? num : n;
        String[] list = new String[retNum];
        for (int i = 0; i < retNum; i++) {
            list[i] = tokenizer.nextToken();
        }
        return list;
    }

    /**
     * Split "str" into tokens by delimiters and optionally remove white spaces
     * from the splitted tokens.
     *
     * @param trimTokens if true, then trim the tokens
     */
    public static String[] split(String str, String delims, boolean trimTokens) {
        StringTokenizer tokenizer = new StringTokenizer(str, delims);
        int n = tokenizer.countTokens();
        String[] list = new String[n];
        for (int i = 0; i < n; i++) {
            if (trimTokens) {
                list[i] = tokenizer.nextToken().trim();
            }
            else {
                list[i] = tokenizer.nextToken();
            }
        }
        return list;
    }

    public static String matchGlobAndCapture(String glob, String text) {
        GlobExpr ge = new GlobExpr(glob);
        return ge.matchAndCapture(text);
    }

    /**
     * strip - strips both ways
     *
     * @param str what to strip
     * @return String the striped string
     */
    public static String strip(String str) {
        if (str == null) {
            return "";
        }

        return megastrip(str, true, true, WHITE_SPACES);
    }

    /**
     * This is a both way strip
     *
     * @param str   the string to strip
     * @param left  strip from left
     * @param right strip from right
     * @param what  character(s) to strip
     * @return the stripped string
     */
    public static String megastrip(String str,
                                   boolean left, boolean right,
                                   String what) {
        if (str == null) {
            return null;
        }

        int limitLeft = 0;
        int limitRight = str.length() - 1;

        while (left && limitLeft <= limitRight &&
                what.indexOf(str.charAt(limitLeft)) >= 0) {
            limitLeft++;
        }
        while (right && limitRight >= limitLeft &&
                what.indexOf(str.charAt(limitRight)) >= 0) {
            limitRight--;
        }

        return str.substring(limitLeft, limitRight + 1);
    }
}
