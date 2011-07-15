package com.rhymestore.android.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class Utils
{
    private static String TAG = "RHYMESTORE";

    public static void AlertShort(final Context context, final String message)
    {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void AlertShort(final Context context, final int message)
    {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void Logger(final String message)
    {
        Log.d(TAG, message);
    }
}
