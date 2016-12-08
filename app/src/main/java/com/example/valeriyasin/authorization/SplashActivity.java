package com.example.valeriyasin.authorization;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

import com.turbomanage.httpclient.BasicHttpClient;
import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.ParameterMap;

import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;

import static com.example.valeriyasin.authorization.Utils.STATUS_OK;

/**
 * Created by valeriyasin on 11/24/16.
 */

public class SplashActivity extends AppCompatActivity {
    String accessToken;
    Utils utils;

    Boolean testTockenStarted = false;
    String TEST_TOCKEN_STARTED = "TEST_TOCKEN_STARTED";

    public void saveToken(TokenObject tokenObject) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(utils.SAVED_TOKEN, tokenObject.getToken());
        editor.putString(utils.SAVED_EXPIRATION_DATE, tokenObject.getExpirationDate());
        editor.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spash_activity);

        utils = new Utils(this);
        accessToken = getIntent().getStringExtra(utils.ACCESS_TOKEN);

        if (savedInstanceState == null || !testTockenStarted) {
            AccessTokenChecker tokenChecker = new AccessTokenChecker(accessToken, this);
            tokenChecker.execute();
            testTockenStarted = true;
        }
    }

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(null);
//        setContentView(R.layout.auth_acitivity);
////        clientId = getIntent().getStringExtra(EXTRA_CLIENT_ID);
////        clientSecret = getIntent().getStringExtra(EXTRA_CLIENT_SECRET);
////
//////        authUrlTemplate = getString(R.string.auth_url);
////        System.out.println(authUrlTemplate);
//////        tokenUrlTemplate = getString(R.string.token_url);
//////        redirectUrl = getString(R.string.callback_url);
//    }
//
//    @Override
//    public void onAuthStarted() {
//
//    }
//

    public void onComplete(Boolean result) {
        Intent intent = new Intent();
        if (result)
            setResult(Activity.RESULT_OK, intent);
        else
            setResult(Activity.RESULT_CANCELED, intent);
        finish();
    }

    private class AccessTokenChecker extends AsyncTask<String, Void, Boolean> {
        String accessToken;
        SplashActivity splashActivity;
//
        AccessTokenChecker(String accessToken, SplashActivity splashActivity) {
            this.accessToken = accessToken;
            this.splashActivity = splashActivity;
        }

        public boolean isInternetAvailable() {
            try {
                InetAddress ipAddr = InetAddress.getByName("google.com"); //You can replace it with your name
                return !ipAddr.equals("");

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

        }

        @Override
        protected Boolean doInBackground(String... params) {
            if (!isInternetAvailable()) {
                return false;
            }
            return testToken(accessToken);
        }
//
        @Override
        protected void onPostExecute(Boolean result) {
            splashActivity.onComplete(result);
        }

        private boolean testToken(String accessToken) {
            String baseURL = "http://192.168.1.78";
            BasicHttpClient httpClient = new BasicHttpClient(baseURL);
            String nBaseurl = "/api/v1/post/";
            String auth = "Bearer " + accessToken;
            httpClient.addHeader("Authorization", auth);
            HttpResponse response1 = httpClient.get(nBaseurl, httpClient.newParams());
            JSONObject jsonResponse;
            try {
                jsonResponse = new JSONObject(response1.getBodyAsString());
                jsonResponse.getString("count");
                return true;
            } catch (Exception ex) {
                return false;
            }
//            System.out.println(response1.getBodyAsString());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(TEST_TOCKEN_STARTED, testTockenStarted);
//        savedInstanceState.putInt(AUTHORIZED_ACTIVITY_STARTED, authorizedActivityStarted);

        super.onSaveInstanceState(savedInstanceState);
    }

}
