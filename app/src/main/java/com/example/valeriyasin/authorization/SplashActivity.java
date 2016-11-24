package com.example.valeriyasin.authorization;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.turbomanage.httpclient.BasicHttpClient;
import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.ParameterMap;

import java.io.IOException;

import static com.example.valeriyasin.authorization.Utils.STATUS_OK;

/**
 * Created by valeriyasin on 11/24/16.
 */

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spash_activity);
    }


    private class AccessTokenGetter extends AsyncTask<String, Void, AuthorizationActivity.TokenObject> {
        private AuthorizationListener listener;
//
        AccessTokenGetter(AuthorizationListener listener) {
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
//            if (tokenObject.getToken().contains("Error")) {
//                listener.onError(tokenObject.getToken());
//            } else {
//                listener.onComplete(tokenObject);
//            }
        }
    }
}
