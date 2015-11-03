package com.whale.xacab;

import android.content.Context;


import java.util.Formatter;
import java.util.Locale;


public class MusicUtils {

    private static StringBuilder sFormatBuilder = new StringBuilder();
    private static Formatter sFormatter = new Formatter(sFormatBuilder, Locale.getDefault());
    private static final Object[] sTimeArgs = new Object[5];
    
    // Taken from android music player
    public static String makeTimeString(Context context, long secs) {
        String durationFormat = context.getString(
                secs < 3600 ? R.string.duration_format : R.string.duration_format_long);

        /* Provide multiple arguments so the format can be changed easily
         * by modifying the xml.
         */
        sFormatBuilder.setLength(0);

        final Object[] timeArgs = sTimeArgs;
        timeArgs[0] = secs / 3600;
        timeArgs[1] = secs / 60;
        timeArgs[2] = (secs / 60) % 60;
        timeArgs[3] = secs;
        timeArgs[4] = secs % 60;

        return sFormatter.format(durationFormat, timeArgs).toString();
    }
}
