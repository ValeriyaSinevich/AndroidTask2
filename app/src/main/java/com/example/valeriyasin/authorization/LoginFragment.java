package com.example.valeriyasin.authorization;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.net.InetAddress;

/**
 * Created by valeriyasin on 11/15/16.
 */
public class LoginFragment extends Fragment {
    private static final int REQUEST_CODE = 100;

    Utils utils;


    private static final String USERNAME = "USER_NAME";

    private String accessToken;
    private String date;

    private Boolean loginButtonPressed = false;

    public static final String RESULT_STRING = "RESULT_STRING";
    public static final String RESULT_STATUS = "RESULT_STATUS";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        utils = new Utils(this.getContext());
//        loginButtonPressed = getArguments().getBoolean("Proceed");

        View v = inflater.inflate(R.layout.login_fragment, container, false);

        Button login = (Button) v.findViewById(R.id.login_button);

        LoginClickListener loginClickListener = new LoginClickListener();
        login.setOnClickListener(loginClickListener);

//        login.performClick();

        return v;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == utils.REQUEST_CODE_GET) {
            if (resultCode == utils.RESULT_OK) {
                String token = data.getStringExtra(utils.ACCESS_TOKEN);
                String expDate = data.getStringExtra(utils.ACCESS_EXPIRATION_DATE);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Your token ").append(token).append("\n").append("Expiration date ").append(expDate);
//                Toast.makeText(getContext(), stringBuilder.toString(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), ActivityAuthorized.class);
                intent.putExtra(utils.RESULT_STRING, stringBuilder.toString());
                intent.putExtra(utils.RESULT_STATUS,utils.RESULT_OK);
                startActivity(intent);
//                accessToken = token;
            }
            if (resultCode == utils.RESULT_CANCELED) {
                if (data != null && data.hasExtra(utils.AUTH_ERROR)) {
                    String error = data.getStringExtra(utils.AUTH_ERROR);
                    if (error.equals(String.valueOf(utils.NO_INTERNET_CONNECTION))) {
                        Intent intent = new Intent(getActivity(), ActivityAuthorized.class);
                        intent.putExtra(utils.RESULT_STRING, "Internet connection was lost :(");
                        intent.putExtra(utils.RESULT_STATUS, utils.NO_INTERNET_CONNECTION);
                        startActivity(intent);
                    }
                    else {
//                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Ooops... Something is wrong with your token. Please try again.");
                        Intent intent = new Intent(getActivity(), ActivityAuthorized.class);
                        intent.putExtra(LoginFragment.RESULT_STATUS, Activity.RESULT_CANCELED);
                        startActivity(intent);
                    }
                }
            }
        }
    }

    private class LoginClickListener implements View.OnClickListener {
        public void onClick(View v) {
            Resources resources = v.getResources();
            String clientId = resources.getString(R.string.client_id);
            String secret = resources.getString(R.string.client_secret);
            Intent intent = AuthorizationActivity.createAuthActivityIntent(v.getContext(), clientId, secret);
            startActivityForResult(intent, utils.REQUEST_CODE_GET);
        }
    }

}

