package com.example.valeriyasin.authorization;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ButtonBarLayout;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by valeriyasin on 11/15/16.
 */
public class ActivityAuthorized extends AppCompatActivity {

    Utils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.autorized_activity);

        utils = new Utils(this);
        String s = getIntent().getStringExtra(utils.RESULT_STRING);
        System.out.println(s);
        int status = getIntent().getIntExtra(utils.RESULT_STATUS, 0);
        TextView tv = (TextView) findViewById(R.id.textView);
        tv.setText(s);
        tv.setVisibility(View.VISIBLE);

        if (status == utils.RESULT_OK) {
            Button loginButton = (Button) findViewById(R.id.login_again_button);
            loginButton.setEnabled(false);
            loginButton.setVisibility(View.INVISIBLE);

            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

        }
        else if (status == utils.NO_INTERNET_CONNECTION || status == utils.RESULT_CANCELED) {
            Button loginButton = (Button) findViewById(R.id.login_again_button);
            loginButton.setText("Back");
            loginButton.setEnabled(true);
            loginButton.setVisibility(View.VISIBLE);
            tv.setText(s);
            tv.setVisibility(View.VISIBLE);

            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
//            loginButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Fragment fragment = new LoginFragment();
//                    Bundle bundle = new Bundle();
//                    bundle.putBoolean("Proceed", true);
//                    fragment.setArguments(bundle);
//                    getSupportFragmentManager().beginTransaction()
//                            .replace(R.id.container, fragment).commit();
//                }
//            });

        }
    }
}
