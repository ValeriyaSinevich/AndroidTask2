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
    final String SAVED_TOKEN = "saved_token";
    final String SAVED_EXPIRATION_DATE = "saved_date";

    final String DEFAULT_TOKEN = "default_token";
    final String DEFAULT_EXPIRATION_DATE = "default_date";

    int REQUEST_CODE = 0;

    public static final int NO_INTERNET_CONNECTION = 2;

    public String getToken() {
        final SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String value = (mSharedPreference.getString(SAVED_TOKEN, DEFAULT_TOKEN));
        return value;
    }

    public String getExpDate() {
        final SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String value = (mSharedPreference.getString(SAVED_EXPIRATION_DATE, DEFAULT_EXPIRATION_DATE));
        return value;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                String token = data.getStringExtra(AuthorizationActivity.ACCESS_TOKEN);
                String expDate = data.getStringExtra(AuthorizationActivity.EXPIRATION_DATE);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Your token ").append(token).append("\n").append("Expiration date ").append(expDate);
//                Toast.makeText(getContext(), stringBuilder.toString(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, ActivityAuthorized.class);
                intent.putExtra(LoginFragment.RESULT_STRING, stringBuilder.toString());
                intent.putExtra(LoginFragment.RESULT_STATUS, Activity.RESULT_OK);
                startActivity(intent);
//                accessToken = token;
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                if (data != null && data.hasExtra(AuthorizationActivity.AUTH_ERROR)) {
                    String error = data.getStringExtra(AuthorizationActivity.AUTH_ERROR);
//                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Ooops... Something is wrong with your token. Please try again.");
                    Intent intent = new Intent(MainActivity.this, ActivityAuthorized.class);
                    intent.putExtra(LoginFragment.RESULT_STATUS, Activity.RESULT_CANCELED);
                    startActivity(intent);
                }
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (getToken() != DEFAULT_TOKEN) {
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
            Date now = new Date();
            Date expDate = new Date();
            try {
                expDate = format.parse(getExpDate());
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
            if (expDate.getTime() < now.getTime()) {
                Fragment fragment = new LoginFragment();
                Bundle bundle = new Bundle();
                bundle.putBoolean("Proceed", false);
                fragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, fragment).commit();
            }
            else {
                Intent intent = new Intent(MainActivity.this, SplashActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        }
        else {
            Fragment fragment = new LoginFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean("Proceed", false);
            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment).commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onRestoreInstanceState(savedInstanceState);
    }
}
