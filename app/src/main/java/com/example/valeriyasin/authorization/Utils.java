package com.example.valeriyasin.authorization;

import android.content.Context;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by valeriyasin on 11/15/16.
 */

public class Utils {
    Context context;

    public static int STATUS_OK = 200;
    public static final int NO_INTERNET_CONNECTION = 2;

    String SAVED_TOKEN;
    String SAVED_EXPIRATION_DATE;


    String ACCESS_TOKEN;
    String ACCESS_EXPIRATION_DATE;

    String DEFAULT_TOKEN;
    String DEFAULT_EXPIRATION_DATE;

    String RESULT_STRING;
    String RESULT_STATUS;

    String AUTH_ERROR;

    int REQUEST_CODE_GET = 11;
    int REQUEST_CODE_CHECK = 12;

    int RESULT_OK = 0;
    int RESULT_CANCELED = 1;


    Utils(Context context) {
        this.context = context;
        SAVED_TOKEN = context.getResources().getString(R.string.SAVED_TOKEN);
        SAVED_EXPIRATION_DATE = context.getResources().getString(R.string.SAVED_EXPIRATION_DATE);

        DEFAULT_TOKEN = context.getResources().getString(R.string.DEFAULT_TOKEN);
        DEFAULT_EXPIRATION_DATE = context.getResources().getString(R.string.DEFAULT_EXPIRATION_DATE);

        RESULT_STRING = context.getResources().getString(R.string.RESULT_STRING);
        RESULT_STATUS = context.getResources().getString(R.string.RESULT_STATUS);
        AUTH_ERROR = context.getResources().getString(R.string.AUTH_ERROR);

        ACCESS_TOKEN = context.getResources().getString(R.string.ACCESS_TOKEN);
        ACCESS_EXPIRATION_DATE = context.getResources().getString(R.string.ACCESS_EXPIRATION_DATE);
    }


    public static String getResponseString(InputStream stream) throws IOException {
        StringBuilder sb = new StringBuilder();
        try {
            String line;
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            bufferedReader.close();
        } finally {
            stream.close();
        }
        return sb.toString();
    }
}
