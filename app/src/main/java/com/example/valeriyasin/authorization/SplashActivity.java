package com.example.valeriyasin.authorization;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

import com.turbomanage.httpclient.BasicHttpClient;
import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.ParameterMap;

import java.io.IOException;
import java.net.URI;

import static com.example.valeriyasin.authorization.Utils.STATUS_OK;

/**
 * Created by valeriyasin on 11/24/16.
 */

public class SplashActivity extends AppCompatActivity implements AuthorizationListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spash_activity);
        AccessTokenChecker tokenChecker = new AccessTokenChecker();
        tokenChecker.execute();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        setContentView(R.layout.auth_acitivity);
        clientId = getIntent().getStringExtra(EXTRA_CLIENT_ID);
        clientSecret = getIntent().getStringExtra(EXTRA_CLIENT_SECRET);

        authUrlTemplate = getString(R.string.auth_url);
        System.out.println(authUrlTemplate);
        tokenUrlTemplate = getString(R.string.token_url);
        redirectUrl = getString(R.string.callback_url);
    }

    @Override
    public void onAuthStarted() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        onAuthStarted();
        String url = String.format(authUrlTemplate, clientId, "&", redirectUrl, "&");
        URI uri = URI.create(url);
    }

    @Override
    public void onComplete(AuthorizationActivity.TokenObject tokenObject) {
        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onError(String error) {
        Intent intent = new Intent();
        intent.putExtra(AuthorizationActivity.AUTH_ERROR, error);
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
    }


    private class AccessTokenChecker extends AsyncTask<String, Void, AuthorizationActivity.TokenObject> {
        private AuthorizationListener listener;
//
        AccessTokenChecker(AuthorizationListener listener) {
            this.listener = listener;
        }

        @Override
        protected AuthorizationActivity.TokenObject doInBackground(String... params) {
//            try {
//                    return new AuthorizationActivity.TokenObject(getAccessToken(response), "");
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            return null;
        }
//
        @Override
        protected void onPostExecute(AuthorizationActivity.TokenObject tokenObject) {
            if (tokenObject.getToken().contains("Error")) {
                listener.onError(tokenObject.getToken());
            } else {
                listener.onComplete(tokenObject);
            }
        }
    }
}
