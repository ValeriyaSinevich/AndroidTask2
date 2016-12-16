package com.example.valeriyasin.authorization;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    Utils utils;

    Boolean splashActivityStarted = false;
    String SPLASH_ACTIVITY_STARTED = "SPLASH_ACTIVITY_STARTED";
//    Boolean authorizedActivityStarted = false;


    public String getToken() {
        final SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String value = (mSharedPreference.getString(utils.SAVED_TOKEN, utils.DEFAULT_TOKEN));
        return value;
    }

    public void saveToken(TokenObject tokenObject) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(utils.SAVED_TOKEN, tokenObject.getToken());
        editor.putString(utils.SAVED_EXPIRATION_DATE, tokenObject.getExpirationDate());
        editor.commit();
    }

    public String getExpDate() {
        final SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String value = (mSharedPreference.getString(utils.SAVED_EXPIRATION_DATE, utils.DEFAULT_EXPIRATION_DATE));
        return value;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == utils.REQUEST_CODE_GET) {
//            if (resultCode == Activity.RESULT_OK) {
//                String token = data.getStringExtra(utils.ACCESS_TOKEN);
//                String expDate = data.getStringExtra(utils.ACCESS_EXPIRATION_DATE);
//                StringBuilder stringBuilder = new StringBuilder();
//                stringBuilder.append("Your token ").append(token).append("\n").append("Expiration date ").append(expDate);
////                Toast.makeText(getContext(), stringBuilder.toString(), Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(MainActivity.this, ActivityAuthorized.class);
//                intent.putExtra(utils.RESULT_STRING, stringBuilder.toString());
//                intent.putExtra(utils.RESULT_STATUS,utils.RESULT_OK);
//                startActivity(intent);
////                accessToken = token;
//            }
//            if (resultCode == utils.RESULT_CANCELED) {
//                if (data != null && data.hasExtra(utils.AUTH_ERROR)) {
//                    String error = data.getStringExtra(utils.AUTH_ERROR);
//                    if (error.equals(String.valueOf(utils.NO_INTERNET_CONNECTION))) {
//                        Intent intent = new Intent(MainActivity.this, ActivityAuthorized.class);
//                        intent.putExtra(utils.RESULT_STATUS, utils.NO_INTERNET_CONNECTION);
//                        startActivity(intent);
//                    }
//                    else {
////                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
//                        StringBuilder stringBuilder = new StringBuilder();
//                        stringBuilder.append("Ooops... Something is wrong with your token. Please try again.");
//                        Intent intent = new Intent(MainActivity.this, ActivityAuthorized.class);
//                        intent.putExtra(LoginFragment.RESULT_STATUS, Activity.RESULT_CANCELED);
//                        startActivity(intent);
//                    }
//                }
//            }
//        }
        if (requestCode == utils.REQUEST_CODE_CHECK) {
            if (resultCode == Activity.RESULT_OK) {
                Intent intent = new Intent(this, ActivityAuthorized.class);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Your token ").append(getToken()).append("\n").append("Expiration date ").append(getExpDate());
                intent.putExtra(utils.RESULT_STRING, stringBuilder.toString());
                intent.putExtra(utils.RESULT_STATUS, utils.RESULT_OK);
                startActivity(intent);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                saveToken(new TokenObject(utils.DEFAULT_TOKEN, utils.DEFAULT_EXPIRATION_DATE));
                startActivity(getIntent());
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        utils = new Utils(this);
        String tag = "login_fragment";

        String accessToken = getToken();
//        String accessToken = "default_token";
        if (!accessToken.equals(utils.DEFAULT_TOKEN)) {
            SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
            Date now = new Date();
            Date expDate = new Date();
            try {
                expDate = format.parse(getExpDate());
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
            if (expDate.getTime() < now.getTime()) {
                if (getSupportFragmentManager().findFragmentByTag(tag) == null) {
                    Fragment fragment = new LoginFragment();
                    Bundle bundle = new Bundle();
//                bundle.putBoolean("Proceed", false);
                    fragment.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, fragment).commit();
                }
            }
            else {
                if (savedInstanceState == null || !savedInstanceState.getBoolean(SPLASH_ACTIVITY_STARTED)) {
                    Intent intent = new Intent(MainActivity.this, SplashActivity.class);
                    intent.putExtra(utils.ACCESS_TOKEN, accessToken);
                    startActivityForResult(intent, utils.REQUEST_CODE_CHECK);
                    splashActivityStarted = true;
                }
            }
        }
        else {
            if (getSupportFragmentManager().findFragmentByTag(tag) == null) {
                Fragment fragment = new LoginFragment();
                Bundle bundle = new Bundle();
//                bundle.putBoolean("Proceed", false);
                fragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, fragment, "login_fragment")
                        .addToBackStack("login_fragment").commit();
//                        .commit();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(SPLASH_ACTIVITY_STARTED, splashActivityStarted);
//        savedInstanceState.putInt(AUTHORIZED_ACTIVITY_STARTED, authorizedActivityStarted);

        super.onSaveInstanceState(savedInstanceState);
    }

//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        // TODO Auto-generated method stub
//        super.onRestoreInstanceState(savedInstanceState);
//    }
}
