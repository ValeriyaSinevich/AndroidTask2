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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.autorized_activity);
        String s = getIntent().getStringExtra("RESULT_STRING");
        int status = getIntent().getIntExtra("RESULT_STATUS", 0);
        TextView tv = (TextView) findViewById(R.id.textView);
        tv.setText(s);

        if (status == Activity.RESULT_OK) {
            Button loginButton = (Button) findViewById(R.id.login_again_button);
            loginButton.setEnabled(true);

            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Fragment fragment = new LoginFragment();
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("Proceed", true);
                    fragment.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, fragment).commit();
                }
            });
        }

        if (status == MainActivity.NO_INTERNET_CONNECTION) {
            Button loginButton = (Button) findViewById(R.id.login_again_button);
            loginButton.setText("Back");
            loginButton.setEnabled(true);

            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }
}
